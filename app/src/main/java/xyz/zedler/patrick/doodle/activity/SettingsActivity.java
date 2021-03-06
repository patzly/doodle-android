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

import android.app.Activity;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnChangeListener;
import com.google.android.material.snackbar.Snackbar;
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
import xyz.zedler.patrick.doodle.util.HapticUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class SettingsActivity extends AppCompatActivity
    implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, OnChangeListener {

  private final static String TAG = SettingsActivity.class.getSimpleName();

  private ActivitySettingsBinding binding;
  private SharedPreferences sharedPrefs;
  private ActivityResultLauncher<Intent> wallpaperPickerLauncher;
  private ViewUtil viewUtil;
  private HapticUtil hapticUtil;
  private boolean settingsApplied;
  private boolean themeApplied;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    binding = ActivitySettingsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    sharedPrefs = new PrefsUtil(this).checkForMigrations().getSharedPrefs();

    viewUtil = new ViewUtil();
    hapticUtil = new HapticUtil(this);

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

    wallpaperPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> setActivateButtonEnabled(result.getResultCode() != Activity.RESULT_OK)
    );

    binding.cardGpu.setVisibility(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.GONE : View.VISIBLE
    );

    binding.switchNightMode.setChecked(sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE));

    binding.switchFollowSystem.setChecked(
        sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM)
    );
    binding.switchFollowSystem.setEnabled(binding.switchNightMode.isChecked());

    binding.linearFollowSystem.setEnabled(binding.switchNightMode.isChecked());
    binding.linearFollowSystemContainer.setAlpha(binding.switchNightMode.isChecked() ? 1 : 0.5f);

    binding.imageNightMode.setImageResource(
        sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE)
            ? R.drawable.ic_round_dark_mode_to_light_mode_anim
            : R.drawable.ic_round_light_mode_to_dark_mode_anim
    );

    binding.switchWhiteText.setChecked(
        sharedPrefs.getBoolean(PREF.USE_WHITE_TEXT, DEF.USE_WHITE_TEXT)
    );

    binding.sliderParallax.setValue(sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX));
    binding.sliderParallax.addOnChangeListener(this);
    binding.sliderParallax.setLabelFormatter(value -> {
      if (value == 0) {
        return getString(R.string.settings_parallax_none);
      } else if (value == DEF.PARALLAX) {
        return getString(R.string.settings_default);
      } else {
        return String.valueOf((int) value);
      }
    });

    binding.sliderSize.setValue(sharedPrefs.getFloat(PREF.SCALE, DEF.SCALE));
    binding.sliderSize.addOnChangeListener(this);
    binding.sliderSize.setLabelFormatter(value -> {
      if (value == 1) {
        return getString(R.string.settings_default);
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      binding.linearGpu.setVisibility(View.VISIBLE);
    } else {
      binding.linearGpu.setVisibility(View.GONE);
    }
    binding.switchGpu.setChecked(sharedPrefs.getBoolean(PREF.GPU, DEF.GPU));

    refreshSelectionTheme(sharedPrefs.getString(PREF.WALLPAPER, WALLPAPER.PIXEL), false);

    String[] wallpapers = new String[]{
        WALLPAPER.PIXEL, WALLPAPER.JOHANNA, WALLPAPER.REIKO, WALLPAPER.ANTHONY
    };
    for (String wallpaper : wallpapers) {
      refreshSelectionVariant(
          wallpaper,
          sharedPrefs.getString(Constants.VARIANT_PREFIX + wallpaper, wallpaper + "1"),
          false
      );
    }

    ViewUtil.setOnClickListeners(
        this,
        // Wallpapers
        binding.cardPixel, binding.cardJohanna, binding.cardReiko, binding.cardAnthony,
        // Variants
        binding.cardPixel1, binding.cardPixel2, binding.cardPixel3,
        binding.cardPixel4, binding.cardPixel5,
        binding.cardJohanna1,
        binding.cardReiko1, binding.cardReiko2,
        binding.cardAnthony1,
        // Other
        binding.frameClose,
        binding.buttonSet,
        binding.cardInfo,
        binding.linearNightMode,
        binding.linearFollowSystem,
        binding.linearWhiteText,
        binding.linearZoomLauncher,
        binding.linearZoomUnlock,
        binding.linearGpu,
        binding.linearReset,
        binding.linearChangelog,
        binding.linearFeedback,
        binding.linearDeveloper,
        binding.linearLicenseMaterialComponents,
        binding.linearLicenseMaterialIcons,
        binding.linearLicenseJost
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchNightMode,
        binding.switchFollowSystem,
        binding.switchWhiteText,
        binding.checkboxZoomLauncher,
        binding.checkboxZoomUnlock,
        binding.switchGpu
    );

    showChangelog(true);
    showFeedbackPopUp(true);
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

    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(this));

    binding.cardTouchWiz.setVisibility(isTouchWiz() ? View.VISIBLE : View.GONE);

    settingsApplied = sharedPrefs.getBoolean(PREF.SETTINGS_APPLIED, true);
    themeApplied = sharedPrefs.getBoolean(PREF.THEME_APPLIED, true);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.frame_close && viewUtil.isClickEnabled()) {
      performHapticClick();
      finish();
    } else if (id == R.id.button_set && viewUtil.isClickEnabled()) {
      wallpaperPickerLauncher.launch(
          new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
              WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
              new ComponentName(getPackageName(), LiveWallpaperService.class.getCanonicalName())
          )
      );
      performHapticHeavyClick();
    } else if (id == R.id.card_info) {
      showTextBottomSheet(R.raw.information, R.string.info_title, -1);
      performHapticClick();
    } else if (id == R.id.card_pixel) {
      refreshSelectionTheme(WALLPAPER.PIXEL, true);
      performHapticClick();
    } else if (id == R.id.card_johanna) {
      refreshSelectionTheme(WALLPAPER.JOHANNA, true);
      performHapticClick();
    } else if (id == R.id.card_reiko) {
      refreshSelectionTheme(WALLPAPER.REIKO, true);
      performHapticClick();
    } else if (id == R.id.card_anthony) {
      refreshSelectionTheme(WALLPAPER.ANTHONY, true);
      performHapticClick();
    } else if (id == R.id.card_pixel1) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL1, true);
      performHapticClick();
    } else if (id == R.id.card_pixel2) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL2, true);
      performHapticClick();
    } else if (id == R.id.card_pixel3) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL3, true);
      performHapticClick();
    } else if (id == R.id.card_pixel4) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL4, true);
      performHapticClick();
    } else if (id == R.id.card_pixel5) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL5, true);
      performHapticClick();
    } else if (id == R.id.card_johanna1) {
      refreshSelectionVariant(WALLPAPER.JOHANNA, VARIANT.JOHANNA1, true);
      performHapticClick();
    } else if (id == R.id.card_reiko1) {
      refreshSelectionVariant(WALLPAPER.REIKO, VARIANT.REIKO1, true);
      performHapticClick();
    } else if (id == R.id.card_reiko2) {
      refreshSelectionVariant(WALLPAPER.REIKO, VARIANT.REIKO2, true);
      performHapticClick();
    } else if (id == R.id.card_anthony1) {
      refreshSelectionVariant(WALLPAPER.ANTHONY, VARIANT.ANTHONY1, true);
      performHapticClick();
    } else if (id == R.id.linear_night_mode) {
      binding.switchNightMode.setChecked(!binding.switchNightMode.isChecked());
    } else if (id == R.id.linear_follow_system) {
      if (binding.switchNightMode.isChecked()) {
        binding.switchFollowSystem.setChecked(!binding.switchFollowSystem.isChecked());
      }
    } else if (id == R.id.linear_white_text) {
      binding.switchWhiteText.setChecked(!binding.switchWhiteText.isChecked());
    } else if (id == R.id.linear_zoom_launcher) {
      ViewUtil.startIcon(binding.imageZoom);
      binding.checkboxZoomLauncher.setChecked(!binding.checkboxZoomLauncher.isChecked());
    } else if (id == R.id.linear_zoom_unlock) {
      ViewUtil.startIcon(binding.imageZoom);
      binding.checkboxZoomUnlock.setChecked(!binding.checkboxZoomUnlock.isChecked());
    } else if (id == R.id.linear_gpu) {
      ViewUtil.startIcon(binding.imageGpu);
      binding.switchGpu.setChecked(!binding.switchGpu.isChecked());
    } else if (id == R.id.linear_reset) {
      ViewUtil.startIcon(binding.imageReset);
      Snackbar.make(
          binding.scroll,
          getString(R.string.msg_reset),
          Snackbar.LENGTH_LONG
      ).setActionTextColor(
          ContextCompat.getColor(this, R.color.retro_green_fg_invert)
      ).setAction(
          getString(R.string.action_reset),
          view -> {
            sharedPrefs.edit().clear().apply();
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            Runtime.getRuntime().exit(0);
          }
      ).show();
    } else if (id == R.id.linear_changelog && viewUtil.isClickEnabled()) {
      ViewUtil.startIcon(binding.imageChangelog);
      showChangelog(false);
      performHapticClick();
    } else if (id == R.id.linear_feedback && viewUtil.isClickEnabled()) {
      ViewUtil.startIcon(binding.imageFeedback);
      showFeedbackPopUp(false);
      performHapticClick();
    } else if (id == R.id.linear_developer && viewUtil.isClickEnabled()) {
      ViewUtil.startIcon(binding.imageDeveloper);
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> startActivity(
              new Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(getString(R.string.app_developer_play))
              )
          ), 300
      );
      performHapticClick();
    } else if (id == R.id.linear_license_material_components && viewUtil.isClickEnabled()) {
      ViewUtil.startIcon(binding.imageLicenseMaterialComponents);
      showTextBottomSheet(
          R.raw.license_apache,
          R.string.license_material_components,
          R.string.license_material_components_link
      );
      performHapticClick();
    } else if (id == R.id.linear_license_material_icons && viewUtil.isClickEnabled()) {
      ViewUtil.startIcon(binding.imageLicenseMaterialIcons);
      showTextBottomSheet(
          R.raw.license_apache,
          R.string.license_material_icons,
          R.string.license_material_icons_link
      );
      performHapticClick();
    } else if (id == R.id.linear_license_jost && viewUtil.isClickEnabled()) {
      ViewUtil.startIcon(binding.imageLicenseJost);
      showTextBottomSheet(
          R.raw.license_ofl,
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
      ViewUtil.startIcon(binding.imageNightMode);
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
      requestThemeRefresh();
    } else if (id == R.id.switch_follow_system) {
      ViewUtil.startIcon(binding.imageFollowSystem);
      sharedPrefs.edit().putBoolean(PREF.FOLLOW_SYSTEM, isChecked).apply();
      requestThemeRefresh();
    } else if (id == R.id.switch_white_text) {
      sharedPrefs.edit().putBoolean(PREF.USE_WHITE_TEXT, isChecked).apply();
      requestThemeRefresh();
    } else if (id == R.id.checkbox_zoom_launcher) {
      sharedPrefs.edit().putBoolean(PREF.ZOOM_LAUNCHER, isChecked).apply();
      requestSettingsRefresh();
    } else if (id == R.id.checkbox_zoom_unlock) {
      sharedPrefs.edit().putBoolean(PREF.ZOOM_UNLOCK, isChecked).apply();
      requestSettingsRefresh();
    } else if (id == R.id.switch_gpu) {
      sharedPrefs.edit().putBoolean(PREF.GPU, isChecked).apply();
      requestSettingsRefresh();
    }
    performHapticClick();
  }

  @Override
  public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
    if (!fromUser) {
      return;
    }
    int id = slider.getId();
    if (id == R.id.slider_parallax) {
      sharedPrefs.edit().putInt(PREF.PARALLAX, (int) value).apply();
      ViewUtil.startIcon(binding.imageParallax);
      requestSettingsRefresh();
    } else if (id == R.id.slider_size) {
      sharedPrefs.edit().putFloat(PREF.SCALE, value).apply();
      ViewUtil.startIcon(binding.imageSize);
      // When the size changes the drawables have to be reloaded
      // Without this and the new size is smaller, the big cached bitmaps are causing lags
      requestSettingsRefresh();
    } else if (id == R.id.slider_zoom) {
      sharedPrefs.edit().putInt(PREF.ZOOM, (int) value).apply();
      ViewUtil.startIcon(binding.imageZoom);
      requestSettingsRefresh();
    }
    performHapticClick();
  }

  private void refreshSelectionTheme(String selection, boolean animated) {
    binding.linearVariantPixel.setVisibility(View.GONE);
    binding.linearVariantJohanna.setVisibility(View.GONE);
    binding.linearVariantReiko.setVisibility(View.GONE);
    binding.linearVariantAnthony.setVisibility(View.GONE);

    MaterialCardView mcv1 = binding.cardPixel;
    MaterialCardView mcv2 = binding.cardJohanna;
    MaterialCardView mcv3 = binding.cardReiko;
    MaterialCardView mcv4 = binding.cardAnthony;
    MaterialCardView mcvSelected = null;

    switch (selection) {
      case WALLPAPER.PIXEL:
        mcvSelected = binding.cardPixel;
        binding.linearVariantPixel.setVisibility(View.VISIBLE);
        break;
      case WALLPAPER.JOHANNA:
        mcvSelected = binding.cardJohanna;
        binding.linearVariantJohanna.setVisibility(View.VISIBLE);
        break;
      case WALLPAPER.REIKO:
        mcvSelected = binding.cardReiko;
        binding.linearVariantReiko.setVisibility(View.VISIBLE);
        break;
      case WALLPAPER.ANTHONY:
        mcvSelected = binding.cardAnthony;
        binding.linearVariantAnthony.setVisibility(View.VISIBLE);
        break;
    }
    if (mcvSelected == null || mcvSelected.isChecked()) {
      return;
    }
    ViewUtil.setChecked(false, mcv1, mcv2, mcv3, mcv4);
    mcvSelected.setChecked(true);

    if (animated) {
      ViewUtil.startIcon(binding.imageWallpaper);
      ViewUtil.startIcon(mcvSelected.getCheckedIcon());
      sharedPrefs.edit().putString(PREF.WALLPAPER, selection).apply();
      requestThemeRefresh();
    }
  }

  private void refreshSelectionVariant(String wallpaper, String selection, boolean animated) {
    MaterialCardView mcv1, mcv2, mcv3, mcv4, mcv5, mcvSelected;
    mcvSelected = mcv1 = mcv2 = mcv3 = mcv4 = mcv5 = null;

    switch (wallpaper) {
      case WALLPAPER.PIXEL: {
        mcv1 = binding.cardPixel1;
        mcv2 = binding.cardPixel2;
        mcv3 = binding.cardPixel3;
        mcv4 = binding.cardPixel4;
        mcv5 = binding.cardPixel5;
        switch (selection) {
          case VARIANT.PIXEL1:
            mcvSelected = binding.cardPixel1;
            break;
          case VARIANT.PIXEL2:
            mcvSelected = binding.cardPixel2;
            break;
          case VARIANT.PIXEL3:
            mcvSelected = binding.cardPixel3;
            break;
          case VARIANT.PIXEL4:
            mcvSelected = binding.cardPixel4;
            break;
          case VARIANT.PIXEL5:
            mcvSelected = binding.cardPixel5;
            break;
        }
      }
      break;
      case WALLPAPER.JOHANNA: {
        mcv1 = binding.cardJohanna1;
        switch (selection) {
          case VARIANT.JOHANNA1:
            mcvSelected = binding.cardJohanna1;
            break;
        }
      }
      break;
      case WALLPAPER.REIKO: {
        mcv1 = binding.cardReiko1;
        mcv2 = binding.cardReiko2;
        switch (selection) {
          case VARIANT.REIKO1:
            mcvSelected = binding.cardReiko1;
            break;
          case VARIANT.REIKO2:
            mcvSelected = binding.cardReiko2;
            break;
        }
      }
      break;
      case WALLPAPER.ANTHONY: {
        mcv1 = binding.cardAnthony1;
        switch (selection) {
          case VARIANT.ANTHONY1:
            mcvSelected = binding.cardAnthony1;
            break;
        }
      }
      break;
    }
    if (mcvSelected == null || mcvSelected.isChecked()) {
      return;
    }
    ViewUtil.setChecked(false, mcv1, mcv2, mcv3, mcv4, mcv5);
    mcvSelected.setChecked(true);

    if (animated) {
      ViewUtil.startIcon(binding.imageVariant);
      ViewUtil.startIcon(mcvSelected.getCheckedIcon());
      sharedPrefs.edit().putString(Constants.VARIANT_PREFIX + wallpaper, selection).apply();
      requestThemeRefresh();
    }
  }

  private void requestSettingsRefresh() {
    if (settingsApplied) {
      sharedPrefs.edit().putBoolean(PREF.SETTINGS_APPLIED, false).apply();
      settingsApplied = false;
    }
  }

  private void requestThemeRefresh() {
    if (themeApplied) {
      sharedPrefs.edit().putBoolean(PREF.THEME_APPLIED, false).apply();
      themeApplied = false;
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

  private void showTextBottomSheet(@RawRes int file, @StringRes int title, @StringRes int link) {
    Bundle bundle = new Bundle();
    bundle.putString(EXTRA.TITLE, getString(title));
    bundle.putInt(EXTRA.FILE, file);
    if (link != -1) {
      bundle.putString(EXTRA.LINK, getString(link));
    }
    ViewUtil.showBottomSheet(this, new TextBottomSheetDialogFragment(), bundle);
  }

  private void showChangelog(boolean onlyIfUpdated) {
    if (!onlyIfUpdated) {
      ViewUtil.showBottomSheet(this, new ChangelogBottomSheetDialogFragment());
      return;
    }
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefs.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      ViewUtil.showBottomSheet(this, new ChangelogBottomSheetDialogFragment());
    }
  }

  private void showFeedbackPopUp(boolean onlyAfterSomeUsage) {
    if (!onlyAfterSomeUsage) {
      ViewUtil.showBottomSheet(this, new FeedbackBottomSheetDialogFragment());
      return;
    }
    int feedbackCount = sharedPrefs.getInt(Constants.PREF.FEEDBACK_POP_UP_COUNT, 1);
    if (feedbackCount > 0) {
      if (feedbackCount < 5) {
        sharedPrefs.edit().putInt(Constants.PREF.FEEDBACK_POP_UP_COUNT, feedbackCount + 1).apply();
      } else {
        ViewUtil.showBottomSheet(this, new FeedbackBottomSheetDialogFragment());
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
    hapticUtil.click();
  }

  private void performHapticHeavyClick() {
    hapticUtil.heavyClick();
  }
}
