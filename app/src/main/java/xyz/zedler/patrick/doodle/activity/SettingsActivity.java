/*
 * This file is part of Doodle Android.
 *
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2020-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.activity;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnChangeListener;
import java.util.Locale;
import xyz.zedler.patrick.doodle.BuildConfig;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.VARIANT;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.ActivitySettingsBinding;
import xyz.zedler.patrick.doodle.fragment.ChangelogBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.fragment.FeedbackBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.fragment.TextBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.ClickUtil;
import xyz.zedler.patrick.doodle.util.IconUtil;
import xyz.zedler.patrick.doodle.util.MigrationUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.SheetUtil;
import xyz.zedler.patrick.doodle.util.VibratorUtil;

public class SettingsActivity extends AppCompatActivity
    implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, OnChangeListener {

  private final static String TAG = SettingsActivity.class.getSimpleName();

  private ActivitySettingsBinding binding;
  private SharedPreferences sharedPrefs;
  private ClickUtil clickUtil;
  private VibratorUtil vibratorUtil;
  private SheetUtil sheetUtil;
  private boolean settingsChanged;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    binding = ActivitySettingsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    sharedPrefs = new PrefsUtil(this).getSharedPrefs();
    new MigrationUtil(sharedPrefs).checkForMigrations();

    clickUtil = new ClickUtil();
    vibratorUtil = new VibratorUtil(this);
    sheetUtil = new SheetUtil(getSupportFragmentManager());

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(this);
    systemBarBehavior.setAppBar(binding.appBar);
    systemBarBehavior.setScroll(binding.scroll, binding.linearContainer);
    systemBarBehavior.setUp();

    new ScrollBehavior(this).setUpScroll(binding.appBar, binding.scroll, true);

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

    binding.switchNightMode.setChecked(sharedPrefs.getBoolean(PREF.NIGHT_MODE, true));

    binding.switchFollowSystem.setChecked(sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, true));
    binding.switchFollowSystem.setEnabled(binding.switchNightMode.isChecked());

    boolean isDoodleThemeActive = sharedPrefs.getString(
        PREF.WALLPAPER, WALLPAPER.DOODLE
    ).equals(WALLPAPER.DOODLE);
    setVariantSelectionEnabled(isDoodleThemeActive, false);

    binding.linearFollowSystem.setEnabled(binding.switchNightMode.isChecked());
    binding.linearFollowSystemContainer.setAlpha(binding.switchNightMode.isChecked() ? 1 : 0.5f);

    binding.imageNightMode.setImageResource(
        sharedPrefs.getBoolean(PREF.NIGHT_MODE, true)
            ? R.drawable.ic_round_dark_mode_to_light_mode_anim
            : R.drawable.ic_round_light_mode_to_dark_mode_anim
    );

    binding.sliderParallax.setValue(sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX));
    binding.sliderParallax.addOnChangeListener(this);
    binding.sliderParallax.setLabelFormatter(value -> {
      if (value == 0) {
        return getString(R.string.setting_parallax_none);
      } else if (value == 200) {
        return getString(R.string.setting_parallax_much);
      } else {
        return getString(R.string.setting_parallax_little);
      }
    });

    binding.sliderSize.setValue(sharedPrefs.getFloat(PREF.SIZE, DEF.SIZE));
    binding.sliderSize.addOnChangeListener(this);
    binding.sliderSize.setLabelFormatter(value -> {
      if (value == 1) {
        return getString(R.string.setting_size_default);
      } else {
        return String.format(Locale.getDefault(), "×%.1f", value);
      }
    });

    binding.sliderZoom.setValue(sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM));
    binding.sliderZoom.addOnChangeListener(this);
    binding.sliderZoom.setLabelFormatter(
        value -> String.format(Locale.getDefault(), "-%.1f", value / 10)
    );

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      binding.linearZoomLauncher.setVisibility(View.VISIBLE);
    } else {
      binding.linearZoomLauncher.setVisibility(View.GONE);
    }
    binding.checkboxZoomLauncher.setChecked(
        sharedPrefs.getBoolean(PREF.ZOOM_LAUNCHER, DEF.ZOOM_LAUNCHER)
    );
    binding.checkboxZoomUnlock.setChecked(
        sharedPrefs.getBoolean(PREF.ZOOM_UNLOCK, DEF.ZOOM_UNLOCK)
    );

    refreshSelectionTheme(sharedPrefs.getString(PREF.WALLPAPER, WALLPAPER.DOODLE), false);
    refreshSelectionVariant(sharedPrefs.getString(PREF.VARIANT, VARIANT.BLACK), false);

    ClickUtil.setOnClickListeners(
        this,
        binding.frameClose,
        binding.buttonSet,
        binding.cardInfo,
        binding.cardDoodle, binding.cardNeon, binding.cardGeometric,
        binding.cardBlack, binding.cardWhite, binding.cardOrange,
        binding.linearNightMode,
        binding.linearFollowSystem,
        binding.linearZoomLauncher,
        binding.linearZoomUnlock,
        binding.linearGithub,
        binding.linearChangelog,
        binding.linearFeedback,
        binding.linearDeveloper,
        binding.linearLicenseMaterialComponents,
        binding.linearLicenseMaterialIcons,
        binding.linearLicenseJost
    );

    ClickUtil.setOnCheckedChangeListeners(
        this,
        binding.switchNightMode,
        binding.switchFollowSystem,
        binding.checkboxZoomLauncher,
        binding.checkboxZoomUnlock
    );

    showChangelog();
    showFeedbackPopUp();
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

    binding.cardTouchWiz.setVisibility(isTouchWiz() ? View.VISIBLE : View.GONE);

    boolean changesApplied = sharedPrefs.getBoolean(PREF.CHANGES_APPLIED, false);
    if (changesApplied) {
      settingsChanged = false;
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
    if (id == R.id.frame_close && clickUtil.isEnabled()) {
      performHapticClick();
      finish();
    } else if (id == R.id.button_set && clickUtil.isEnabled()) {
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
      performHapticHeavyClick();
    } else if (id == R.id.card_info) {
      showTextBottomSheet("info", R.string.info_title, -1);
      performHapticClick();
    } else if (id == R.id.card_doodle) {
      refreshSelectionTheme(WALLPAPER.DOODLE, true);
      performHapticClick();
    } else if (id == R.id.card_neon) {
      refreshSelectionTheme(WALLPAPER.NEON, true);
      performHapticClick();
    } else if (id == R.id.card_geometric) {
      refreshSelectionTheme(WALLPAPER.GEOMETRIC, true);
      performHapticClick();
    } else if (id == R.id.card_black) {
      refreshSelectionVariant(VARIANT.BLACK, true);
      performHapticClick();
    } else if (id == R.id.card_white) {
      refreshSelectionVariant(VARIANT.WHITE, true);
      performHapticClick();
    } else if (id == R.id.card_orange) {
      refreshSelectionVariant(VARIANT.ORANGE, true);
      performHapticClick();
    } else if (id == R.id.linear_night_mode) {
      binding.switchNightMode.setChecked(!binding.switchNightMode.isChecked());
    } else if (id == R.id.linear_follow_system) {
      IconUtil.start(binding.imageFollowSystem);
      if (binding.switchNightMode.isChecked()) {
        binding.switchFollowSystem.setChecked(!binding.switchFollowSystem.isChecked());
      }
    } else if (id == R.id.linear_zoom_launcher) {
      IconUtil.start(binding.imageZoom);
      binding.checkboxZoomLauncher.setChecked(!binding.checkboxZoomLauncher.isChecked());
    } else if (id == R.id.linear_zoom_unlock) {
      IconUtil.start(binding.imageZoom);
      binding.checkboxZoomUnlock.setChecked(!binding.checkboxZoomUnlock.isChecked());
    } else if (id == R.id.linear_github && clickUtil.isEnabled()) {
      startActivity(new Intent(
          Intent.ACTION_VIEW,
          Uri.parse(getString(R.string.app_github))
      ));
    } else if (id == R.id.linear_changelog && clickUtil.isEnabled()) {
      IconUtil.start(binding.imageChangelog);
      sheetUtil.show(new ChangelogBottomSheetDialogFragment());
      performHapticClick();
    } else if (id == R.id.linear_feedback && clickUtil.isEnabled()) {
      IconUtil.start(binding.imageFeedback);
      sheetUtil.show(new FeedbackBottomSheetDialogFragment());
      performHapticClick();
    } else if (id == R.id.linear_developer && clickUtil.isEnabled()) {
      IconUtil.start(binding.imageDeveloper);
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> startActivity(
              new Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(getString(R.string.app_developer_play))
              )
          ), 300
      );
      performHapticClick();
    } else if (id == R.id.linear_license_material_components && clickUtil.isEnabled()) {
      IconUtil.start(binding.imageLicenseMaterialComponents);
      showTextBottomSheet(
          "apache",
          R.string.license_material_components,
          R.string.license_material_components_link
      );
      performHapticClick();
    } else if (id == R.id.linear_license_material_icons && clickUtil.isEnabled()) {
      IconUtil.start(binding.imageLicenseMaterialIcons);
      showTextBottomSheet(
          "apache",
          R.string.license_material_icons,
          R.string.license_material_icons_link
      );
      performHapticClick();
    } else if (id == R.id.linear_license_jost && clickUtil.isEnabled()) {
      IconUtil.start(binding.imageLicenseJost);
      showTextBottomSheet(
          "ofl",
          R.string.license_jost,
          R.string.license_jost_link
      );
      performHapticClick();
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
      sharedPrefs.edit().putBoolean(PREF.NIGHT_MODE, isChecked).apply();
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> binding.imageNightMode.setImageResource(
              isChecked
                  ? R.drawable.ic_round_dark_mode_to_light_mode_anim
                  : R.drawable.ic_round_light_mode_to_dark_mode_anim
          ),
          300
      );
    } else if (id == R.id.switch_follow_system) {
      sharedPrefs.edit().putBoolean(PREF.FOLLOW_SYSTEM, isChecked).apply();
    } else if (id == R.id.checkbox_zoom_launcher) {
      sharedPrefs.edit().putBoolean(PREF.ZOOM_LAUNCHER, isChecked).apply();
    } else if (id == R.id.checkbox_zoom_unlock) {
      sharedPrefs.edit().putBoolean(PREF.ZOOM_UNLOCK, isChecked).apply();
    }
    performHapticClick();
    notifySettingsHaveChanged();
  }

  @Override
  public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
    if (!fromUser) {
      return;
    }
    int id = slider.getId();
    if (id == R.id.slider_parallax) {
      sharedPrefs.edit().putInt(PREF.PARALLAX, (int) value).apply();
      IconUtil.start(binding.imageParallax);
    } else if (id == R.id.slider_size) {
      sharedPrefs.edit().putFloat(PREF.SIZE, value).apply();
      IconUtil.start(binding.imageSize);
    } else if (id == R.id.slider_zoom) {
      sharedPrefs.edit().putInt(PREF.ZOOM, (int) value).apply();
      IconUtil.start(binding.imageZoom);
    }
    performHapticClick();
    notifySettingsHaveChanged();
  }

  private void refreshSelectionTheme(String selection, boolean animated) {
    if (animated) {
      IconUtil.start(binding.imageWallpaper);
    }
    MaterialCardView mcvSelected, mcv1, mcv2;
    switch (selection) {
      case WALLPAPER.NEON:
        mcvSelected = binding.cardNeon;
        mcv1 = binding.cardDoodle;
        mcv2 = binding.cardGeometric;
        break;
      case WALLPAPER.GEOMETRIC:
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
    setVariantSelectionEnabled(selection.equals(WALLPAPER.DOODLE), true);
    sharedPrefs.edit().putString(PREF.WALLPAPER, selection).apply();
    notifySettingsHaveChanged();
  }

  private void refreshSelectionVariant(String selection, boolean animated) {
    if (animated) {
      IconUtil.start(binding.imageVariant);
    }
    MaterialCardView mcvSelected, mcv1, mcv2;
    switch (selection) {
      case VARIANT.WHITE:
        mcvSelected = binding.cardWhite;
        mcv1 = binding.cardBlack;
        mcv2 = binding.cardOrange;
        break;
      case VARIANT.ORANGE:
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
    sharedPrefs.edit().putString(PREF.VARIANT, selection).apply();
    notifySettingsHaveChanged();
  }

  private void notifySettingsHaveChanged() {
    if (!settingsChanged) {
      settingsChanged = true;
      sharedPrefs.edit()
          .putBoolean(PREF.SETTINGS_CHANGED, true)
          .putBoolean(PREF.CHANGES_APPLIED, false)
          .apply();
    }
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
    bundle.putString(EXTRA.TITLE, getString(title));
    bundle.putString(EXTRA.FILE, file);
    if (link != -1) {
      bundle.putString(EXTRA.LINK, getString(link));
    }
    sheetUtil.show(new TextBottomSheetDialogFragment(), bundle);
  }

  private void showChangelog() {
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefs.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      sheetUtil.show(new ChangelogBottomSheetDialogFragment());
    }
  }

  private void showFeedbackPopUp() {
    int feedbackCount = sharedPrefs.getInt(Constants.PREF.FEEDBACK_POP_UP_COUNT, 1);
    if (feedbackCount > 0) {
      if (feedbackCount < 5) {
        sharedPrefs.edit().putInt(Constants.PREF.FEEDBACK_POP_UP_COUNT, feedbackCount + 1).apply();
      } else {
        sheetUtil.show(new FeedbackBottomSheetDialogFragment());
      }
    }
  }

  private boolean isTouchWiz() {
    PackageManager localPackageManager = getPackageManager();
    Intent intent = new Intent("android.intent.action.MAIN");
    intent.addCategory("android.intent.category.HOME");
    String launcher = localPackageManager.resolveActivity(
        intent, PackageManager.MATCH_DEFAULT_ONLY
    ).activityInfo.packageName;
    return launcher != null && launcher.equals("com.sec.android.app.launcher");
  }

  private void performHapticClick() {
    vibratorUtil.click();
  }

  private void performHapticHeavyClick() {
    vibratorUtil.heavyClick();
  }
}
