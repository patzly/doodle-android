/*
 * This file is part of Doodle Android.
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 * Copyright (c) 2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.activity;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.ActivitySettingsBinding;
import xyz.zedler.patrick.doodle.fragment.ChangelogBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.fragment.TextBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.ClickUtil;
import xyz.zedler.patrick.doodle.util.IconUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class SettingsActivity extends AppCompatActivity
    implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private final static String TAG = SettingsActivity.class.getSimpleName();

  private ActivitySettingsBinding binding;
  private SharedPreferences sharedPrefs;
  private ClickUtil clickUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    binding = ActivitySettingsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    sharedPrefs = new PrefsUtil(this).getSharedPrefs();;
    clickUtil = new ClickUtil();

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(this);
    systemBarBehavior.setAppBar(binding.appBar);
    systemBarBehavior.setScroll(binding.scroll, binding.linearContainer);
    systemBarBehavior.setUp();

    new ScrollBehavior(this).setUpScroll(binding.appBar, binding.scroll, true);

    binding.frameClose.setOnClickListener(v -> finish());

    binding.buttonSet.setEnabled(!isWallpaperServiceRunning());
    binding.buttonSet.setBackgroundColor(
        ContextCompat.getColor(
            this,
            isWallpaperServiceRunning() ? R.color.secondary_disabled : R.color.retro_green_bg_white
        )
    );
    binding.buttonSet.setTextColor(
        ContextCompat.getColor(
            this,
            isWallpaperServiceRunning() ? R.color.on_secondary_disabled : R.color.on_secondary
        )
    );

    binding.switchNightMode.setChecked(
        sharedPrefs.getBoolean(Constants.PREF.NIGHT_MODE, true)
    );

    binding.switchFollowSystem.setChecked(
        sharedPrefs.getBoolean(Constants.PREF.FOLLOW_SYSTEM, true)
    );
    binding.switchFollowSystem.setEnabled(binding.switchNightMode.isChecked());

    boolean isDoodleThemeActive = sharedPrefs.getString(
        Constants.PREF.THEME, Constants.THEME.DOODLE
    ).equals(Constants.THEME.DOODLE);
    setVariantSelectionEnabled(isDoodleThemeActive, false);

    binding.linearFollowSystem.setEnabled(binding.switchNightMode.isChecked());
    binding.linearFollowSystemContainer.setAlpha(binding.switchNightMode.isChecked() ? 1 : 0.5f);

    binding.imageNightMode.setImageResource(
        sharedPrefs.getBoolean(Constants.PREF.NIGHT_MODE, true)
            ? R.drawable.ic_round_dark_mode_to_light_mode_anim
            : R.drawable.ic_round_light_mode_to_dark_mode_anim
    );

    switch (sharedPrefs.getInt(Constants.PREF.PARALLAX, 100)) {
      case 0:
        binding.radioGroupParallax.check(R.id.radio_none);
        break;
      case 100:
        binding.radioGroupParallax.check(R.id.radio_little);
        break;
      case 200:
        binding.radioGroupParallax.check(R.id.radio_much);
        break;
    }
    binding.radioGroupParallax.jumpDrawablesToCurrentState();
    binding.radioGroupParallax.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
      IconUtil.start(binding.imageParallax);
      int parallax;
      if (checkedId == R.id.radio_little) {
        parallax = 100;
      } else if (checkedId == R.id.radio_much) {
        parallax = 200;
      } else {
        parallax = 0;
      }
      sharedPrefs.edit().putInt(Constants.PREF.PARALLAX, parallax).apply();
    });

    switch (sharedPrefs.getInt(Constants.PREF.SIZE, 0)) {
      case 0:
        binding.radioGroupSize.check(R.id.radio_default);
        break;
      case 1:
        binding.radioGroupSize.check(R.id.radio_medium);
        break;
      case 2:
        binding.radioGroupSize.check(R.id.radio_big);
        break;
    }
    binding.radioGroupSize.jumpDrawablesToCurrentState();
    binding.radioGroupSize.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
      IconUtil.start(binding.imageSize);
      int size;
      if (checkedId == R.id.radio_medium) {
        size = 1;
      } else if (checkedId == R.id.radio_big) {
        size = 2;
      } else {
        size = 0;
      }
      sharedPrefs.edit().putInt(PREF.SIZE, size).apply();
    });

    refreshSelectionTheme(
        sharedPrefs.getString(Constants.PREF.THEME, Constants.THEME.DOODLE),
        false
    );
    refreshSelectionVariant(
        sharedPrefs.getString(Constants.PREF.VARIANT, Constants.VARIANT.BLACK),
        false
    );

    ClickUtil.setOnClickListeners(
        this,
        binding.buttonSet,
        binding.cardInfo,
        binding.cardDoodle, binding.cardNeon, binding.cardGeometric,
        binding.cardBlack, binding.cardWhite, binding.cardOrange,
        binding.linearNightMode,
        binding.linearFollowSystem,
        binding.linearChangelog,
        binding.linearDeveloper,
        binding.linearLicenseMaterialComponents,
        binding.linearLicenseMaterialIcons,
        binding.linearLicenseJost
    );

    ClickUtil.setOnCheckedChangeListeners(
        this, binding.switchNightMode, binding.switchFollowSystem
    );
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (!isWallpaperServiceRunning()) {
      setActivateButtonEnabled(true);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Constants.REQUEST_CODE) {
      setActivateButtonEnabled(resultCode != RESULT_OK);
    }
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.button_set) {
      if (clickUtil.isDisabled()) {
        return;
      }
      startActivityForResult(
          new Intent()
              .setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
              .putExtra(
                  WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                  new ComponentName(
                      "xyz.zedler.patrick.doodle",
                      "xyz.zedler.patrick.doodle.service.LiveWallpaperService"
                  )
              ),
          Constants.REQUEST_CODE
      );
    } else if (id == R.id.card_info) {
      showTextBottomSheet("info", R.string.info_title, -1);
    } else if (id == R.id.card_doodle) {
      refreshSelectionTheme(Constants.THEME.DOODLE, true);
    } else if (id == R.id.card_neon) {
      refreshSelectionTheme(Constants.THEME.NEON, true);
    } else if (id == R.id.card_geometric) {
      refreshSelectionTheme(Constants.THEME.GEOMETRIC, true);
    } else if (id == R.id.card_black) {
      refreshSelectionVariant(Constants.VARIANT.BLACK, true);
    } else if (id == R.id.card_white) {
      refreshSelectionVariant(Constants.VARIANT.WHITE, true);
    } else if (id == R.id.card_orange) {
      refreshSelectionVariant(Constants.VARIANT.ORANGE, true);
    } else if (id == R.id.linear_night_mode) {
      binding.switchNightMode.setChecked(!binding.switchNightMode.isChecked());
    } else if (id == R.id.linear_follow_system) {
      IconUtil.start(binding.imageFollowSystem);
      if (binding.switchNightMode.isChecked()) {
        binding.switchFollowSystem.setChecked(!binding.switchFollowSystem.isChecked());
      }
    } else if (id == R.id.linear_changelog) {
      if (clickUtil.isDisabled()) {
        return;
      }
      IconUtil.start(binding.imageChangelog);
      BottomSheetDialogFragment fragment = new ChangelogBottomSheetDialogFragment();
      fragment.show(getSupportFragmentManager(), fragment.toString());
    } else if (id == R.id.linear_developer) {
      if (clickUtil.isDisabled()) {
        return;
      }
      IconUtil.start(binding.imageDeveloper);
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> startActivity(
              new Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse("http://play.google.com/store/apps/dev?id=8122479227040208191")
              )
          ), 300
      );
    } else if (id == R.id.linear_license_material_components) {
      if (clickUtil.isDisabled()) {
        return;
      }
      IconUtil.start(binding.imageLicenseMaterialComponents);
      showTextBottomSheet(
          "apache",
          R.string.license_material_components,
          R.string.license_material_components_link
      );
    } else if (id == R.id.linear_license_material_icons) {
      if (clickUtil.isDisabled()) {
        return;
      }
      IconUtil.start(binding.imageLicenseMaterialIcons);
      showTextBottomSheet(
          "apache",
          R.string.license_material_icons,
          R.string.license_material_icons_link
      );
    } else if (id == R.id.linear_license_jost) {
      if (clickUtil.isDisabled()) {
        return;
      }
      IconUtil.start(binding.imageLicenseJost);
      showTextBottomSheet(
          "open-font",
          R.string.license_jost,
          R.string.license_jost_link
      );
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_night_mode) {
      IconUtil.start(binding.imageNightMode);
      binding.switchFollowSystem.setEnabled(isChecked);
      binding.linearFollowSystem.setEnabled(isChecked);
      binding.linearFollowSystemContainer.animate()
          .alpha(isChecked ? 1 : 0.5f)
          .setDuration(200)
          .start();
      sharedPrefs.edit().putBoolean(Constants.PREF.NIGHT_MODE, isChecked).apply();
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> binding.imageNightMode.setImageResource(
              isChecked
                  ? R.drawable.ic_round_dark_mode_to_light_mode_anim
                  : R.drawable.ic_round_light_mode_to_dark_mode_anim
          ),
          300
      );
    } else if (id == R.id.switch_follow_system) {
      sharedPrefs.edit().putBoolean(Constants.PREF.FOLLOW_SYSTEM, isChecked).apply();
    }
  }

  private void refreshSelectionTheme(String selection, boolean animated) {
    if (animated) {
      IconUtil.start(binding.imageTheme);
    }
    MaterialCardView mcvSelected, mcv1, mcv2;
    switch (selection) {
      case Constants.THEME.NEON:
        mcvSelected = binding.cardNeon;
        mcv1 = binding.cardDoodle;
        mcv2 = binding.cardGeometric;
        break;
      case Constants.THEME.GEOMETRIC:
        mcvSelected = binding.cardGeometric;
        mcv1 = binding.cardDoodle;
        mcv2 = binding.cardNeon;
        break;
      default:
        mcvSelected = binding.cardDoodle;
        mcv1 = binding.cardGeometric;
        mcv2 = binding.cardNeon;
        break;
    }
    mcvSelected.setStrokeColor(ContextCompat.getColor(this, R.color.secondary));
    mcvSelected.setChecked(true);
    mcv1.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
    mcv1.setChecked(false);
    mcv2.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
    mcv2.setChecked(false);
    setVariantSelectionEnabled(selection.equals(Constants.THEME.DOODLE), true);
    sharedPrefs.edit().putString(Constants.PREF.THEME, selection).apply();
  }

  private void refreshSelectionVariant(String selection, boolean animated) {
    if (animated) {
      IconUtil.start(binding.imageVariant);
    }
    MaterialCardView mcvSelected, mcv1, mcv2;
    switch (selection) {
      case Constants.VARIANT.WHITE:
        mcvSelected = binding.cardWhite;
        mcv1 = binding.cardBlack;
        mcv2 = binding.cardOrange;
        break;
      case Constants.VARIANT.ORANGE:
        mcvSelected = binding.cardOrange;
        mcv1 = binding.cardBlack;
        mcv2 = binding.cardWhite;
        break;
      default:
        mcvSelected = binding.cardBlack;
        mcv1 = binding.cardWhite;
        mcv2 = binding.cardOrange;
        break;
    }
    mcvSelected.setStrokeColor(ContextCompat.getColor(this, R.color.secondary));
    mcvSelected.setChecked(true);
    mcv1.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
    mcv1.setChecked(false);
    mcv2.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
    mcv2.setChecked(false);
    sharedPrefs.edit().putString(Constants.PREF.VARIANT, selection).apply();
  }

  private void setActivateButtonEnabled(boolean enabled) {
    binding.buttonSet.setEnabled(enabled);
    binding.buttonSet.setBackgroundColor(
        ContextCompat.getColor(
            this, enabled ? R.color.retro_green_bg_white : R.color.secondary_disabled
        )
    );
    binding.buttonSet.setTextColor(
        ContextCompat.getColor(
            this, enabled ? R.color.on_secondary : R.color.on_secondary_disabled
        )
    );
  }

  private void setVariantSelectionEnabled(boolean enabled, boolean animated) {
    if (animated) {
      binding.linearVariant.animate().alpha(enabled ? 1 : 0.5f).setDuration(200).start();
    } else {
      binding.linearVariant.setAlpha(enabled ? 1 : 0.5f);
    }
    for (int i = 0; i < binding.linearVariant.getChildCount(); i++) {
      binding.linearVariant.getChildAt(i).setEnabled(enabled);
    }
  }

  private boolean isWallpaperServiceRunning() {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    if (manager != null) {
      for (ActivityManager.RunningServiceInfo service
          : manager.getRunningServices(Integer.MAX_VALUE)
      ) {
        if (LiveWallpaperService.class.getName().equals(service.service.getClassName())) {
          return true;
        }
      }
    }
    return false;
  }

  private void showTextBottomSheet(String file, @StringRes int title, @StringRes int link) {
    Bundle bundle = new Bundle();
    bundle.putString(Constants.EXTRA.TITLE, getString(title));
    bundle.putString(Constants.EXTRA.FILE, file);
    if (link != -1) {
      bundle.putString(Constants.EXTRA.LINK, getString(link));
    }
    DialogFragment fragment = new TextBottomSheetDialogFragment();
    fragment.setArguments(bundle);
    fragment.show(getSupportFragmentManager(), fragment.toString());
  }
}
