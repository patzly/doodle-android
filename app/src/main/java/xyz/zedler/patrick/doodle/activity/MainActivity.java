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
 * Copyright (c) 2019-2023 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.PackageInfoFlags;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;
import xyz.zedler.patrick.doodle.BuildConfig;
import xyz.zedler.patrick.doodle.Constants.ACTION;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.NavMainDirections;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.ActivityMainBinding;
import xyz.zedler.patrick.doodle.fragment.BaseFragment;
import xyz.zedler.patrick.doodle.fragment.dialog.ApplyBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.HapticUtil;
import xyz.zedler.patrick.doodle.util.LocaleUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.UiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ActivityMainBinding binding;
  private NavController navController;
  private NavHostFragment navHost;
  private SharedPreferences sharedPrefs;
  private ViewUtil viewUtil;
  private HapticUtil hapticUtil;
  private ActivityResultLauncher<Intent> wallpaperPickerLauncher;
  private Locale locale;
  private boolean isServiceRunning;
  private int fabTopEdgeDistance;
  private int bottomInset;
  private boolean runAsSuperClass;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    runAsSuperClass = savedInstanceState != null
        && savedInstanceState.getBoolean(EXTRA.RUN_AS_SUPER_CLASS, false);

    if (runAsSuperClass) {
      super.onCreate(savedInstanceState);
      return;
    }

    sharedPrefs = new PrefsUtil(this).checkForMigrations().getSharedPrefs();

    // DARK MODE

    int modeNight = sharedPrefs.getInt(PREF.MODE, DEF.MODE);
    int uiMode = getResources().getConfiguration().uiMode;
    switch (modeNight) {
      case AppCompatDelegate.MODE_NIGHT_NO:
        uiMode = Configuration.UI_MODE_NIGHT_NO;
        break;
      case AppCompatDelegate.MODE_NIGHT_YES:
        uiMode = Configuration.UI_MODE_NIGHT_YES;
        break;
    }
    AppCompatDelegate.setDefaultNightMode(modeNight);

    // APPLY CONFIG TO RESOURCES

    // base
    Resources resBase = getBaseContext().getResources();
    Configuration configBase = resBase.getConfiguration();
    configBase.uiMode = uiMode;
    resBase.updateConfiguration(configBase, resBase.getDisplayMetrics());
    // app
    Resources resApp = getApplicationContext().getResources();
    Configuration configApp = resApp.getConfiguration();
    // Don't set uiMode here, won't let FOLLOW_SYSTEM apply correctly
    resApp.updateConfiguration(configApp, getResources().getDisplayMetrics());

    switch (sharedPrefs.getString(PREF.THEME, DEF.THEME)) {
      case THEME.RED:
        setTheme(R.style.Theme_Doodle_Red);
        break;
      case THEME.YELLOW:
        setTheme(R.style.Theme_Doodle_Yellow);
        break;
      case THEME.LIME:
        setTheme(R.style.Theme_Doodle_Lime);
        break;
      case THEME.GREEN:
        setTheme(R.style.Theme_Doodle_Green);
        break;
      case THEME.TURQUOISE:
        setTheme(R.style.Theme_Doodle_Turquoise);
        break;
      case THEME.TEAL:
        setTheme(R.style.Theme_Doodle_Teal);
        break;
      case THEME.BLUE:
        setTheme(R.style.Theme_Doodle_Blue);
        break;
      case THEME.PURPLE:
        setTheme(R.style.Theme_Doodle_Purple);
        break;
      case THEME.AMOLED:
        setTheme(R.style.Theme_Doodle_Amoled);
        break;
      default:
        if (DynamicColors.isDynamicColorAvailable()) {
          DynamicColors.applyToActivityIfAvailable(
              this,
              new DynamicColorsOptions.Builder().setOnAppliedCallback(
                  activity -> HarmonizedColors.applyToContextIfAvailable(
                      this, HarmonizedColorsOptions.createMaterialDefaults()
                  )
              ).build()
          );
        } else {
          setTheme(R.style.Theme_Doodle_Yellow);
        }
        break;
    }

    Bundle bundleInstanceState = getIntent().getBundleExtra(EXTRA.INSTANCE_STATE);
    super.onCreate(bundleInstanceState != null ? bundleInstanceState : savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    viewUtil = new ViewUtil();
    hapticUtil = new HapticUtil(this);

    locale = LocaleUtil.getLocale();

    wallpaperPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> setFabVisibility(
            result.getResultCode() != Activity.RESULT_OK, true
        )
    );

    // Calculate FAB top edge distance to bottom (excluding bottom inset)
    int height = UiUtil.dpToPx(this, 32) + UiUtil.spToPx(
        this, 16
    );
    int margin = UiUtil.dpToPx(this, 40);
    fabTopEdgeDistance = height + margin;

    navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_main_nav_host
    );
    assert navHost != null;
    navController = navHost.getNavController();

    SystemBarBehavior.applyBottomInset(binding.fabMain);

    isServiceRunning = LiveWallpaperService.isMainEngineRunning();
    if (isServiceRunning) {
      binding.fabMain.setVisibility(View.INVISIBLE);
    }

    if (getIntent() != null) {
      if (getIntent().getBooleanExtra(EXTRA.SHOW_FORCE_STOP_REQUEST, false)) {
        new Handler(Looper.getMainLooper()).postDelayed(this::showForceStopRequest, 200);
      }
    }

    ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
      bottomInset = insets.getInsets(Type.systemBars()).bottom;
      setFabVisibility(!isServiceRunning, false);
      return insets;
    });

    binding.fabMain.setRippleColor(
        ColorStateList.valueOf(
            ResUtil.getColorAttr(this, R.attr.colorOnPrimaryContainer, 0.07f)
        )
    );

    binding.fabMain.setOnClickListener(v -> {
      if (viewUtil.isClickDisabled(v.getId())) {
        return;
      }
      performHapticClick();
      try {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            new ComponentName(getPackageName(), LiveWallpaperService.class.getCanonicalName())
        );
        intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true);
        wallpaperPickerLauncher.launch(intent);
      } catch (ActivityNotFoundException e) {
        showSnackbar(R.string.msg_preview_missing);
      }
    });

    if (savedInstanceState == null && bundleInstanceState == null) {
      new Handler(Looper.getMainLooper()).postDelayed(
          this::showInitialBottomSheets, Build.VERSION.SDK_INT >= 31 ? 950 : 0
      );
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (runAsSuperClass) {
      return;
    }

    checkIfServiceIsRunning();

    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(this));
  }

  @Override
  protected void attachBaseContext(Context base) {
    if (runAsSuperClass) {
      super.attachBaseContext(base);
    } else {
      SharedPreferences sharedPrefs = new PrefsUtil(base).checkForMigrations().getSharedPrefs();
      // Night mode
      int modeNight = sharedPrefs.getInt(PREF.MODE, DEF.MODE);
      int uiMode = base.getResources().getConfiguration().uiMode;
      switch (modeNight) {
        case AppCompatDelegate.MODE_NIGHT_NO:
          uiMode = Configuration.UI_MODE_NIGHT_NO;
          break;
        case AppCompatDelegate.MODE_NIGHT_YES:
          uiMode = Configuration.UI_MODE_NIGHT_YES;
          break;
      }
      AppCompatDelegate.setDefaultNightMode(modeNight);
      // Apply config to resources
      Resources resources = base.getResources();
      Configuration config = resources.getConfiguration();
      config.uiMode = uiMode;
      resources.updateConfiguration(config, resources.getDisplayMetrics());
      super.attachBaseContext(base.createConfigurationContext(config));
    }
  }

  @NonNull
  public BaseFragment getCurrentFragment() {
    return (BaseFragment) navHost.getChildFragmentManager().getFragments().get(0);
  }

  public void checkIfServiceIsRunning() {
    boolean isServiceRunningNew = LiveWallpaperService.isMainEngineRunning();
    if (isServiceRunning != isServiceRunningNew) {
      isServiceRunning = isServiceRunningNew;
      setFabVisibility(!isServiceRunning, true);
    }
  }

  public int getFabTopEdgeDistance() {
    return binding.fabMain.getTranslationY() == 0
        && binding.fabMain.getVisibility() == View.VISIBLE ? fabTopEdgeDistance : 0;
  }

  private void setFabVisibility(boolean visible, boolean animated) {
    if (binding == null) {
      return;
    }
    binding.fabMain.setVisibility(View.VISIBLE);
    if (animated) {
      binding.fabMain.animate()
          .translationY(visible ? 0 : fabTopEdgeDistance + bottomInset)
          .setDuration(400)
          .setStartDelay(200)
          .setInterpolator(new FastOutSlowInInterpolator())
          .start();
    } else {
      binding.fabMain.setTranslationY(visible ? 0 : fabTopEdgeDistance + bottomInset);
    }
  }

  public boolean shouldLogoBeVisibleOnOverviewPage() {
    return true;
  }

  public void showSnackbar(@StringRes int resId) {
    showSnackbar(Snackbar.make(binding.coordinatorMain, getString(resId), Snackbar.LENGTH_LONG));
  }

  public void showSnackbar(Snackbar snackbar) {
    snackbar.setAnchorView(binding.fabMain).show();
  }

  public Snackbar getSnackbar(@StringRes int resId, int duration) {
    return Snackbar.make(binding.coordinatorMain, getString(resId), duration);
  }

  public void navigate(NavDirections directions) {
    if (navController == null || directions == null) {
      Log.e(TAG, "navigate: controller or direction is null");
      return;
    }
    try {
      navController.navigate(directions);
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "navigate: " + directions, e);
    }
  }

  public void navigateToFragment(NavDirections directions) {
    if (navController == null || directions == null) {
      Log.e(TAG, "navigate: controller or direction is null");
      return;
    }
    try {
      NavOptions.Builder builder = new NavOptions.Builder();
      if (UiUtil.areAnimationsEnabled(this)) {
        boolean useSliding = getSharedPrefs().getBoolean(PREF.USE_SLIDING, DEF.USE_SLIDING);
        builder.setEnterAnim(
            useSliding ? R.anim.enter_end_slide : R.animator.enter_end_fade
        );
        builder.setExitAnim(
            useSliding ? R.anim.exit_start_slide : R.animator.exit_start_fade
        );
        builder.setPopEnterAnim(
            useSliding ? R.anim.enter_start_slide : R.animator.enter_start_fade
        );
        builder.setPopExitAnim(
            useSliding ? R.anim.exit_end_slide : R.animator.exit_end_fade
        );
      } else {
        builder.setEnterAnim(-1).setExitAnim(-1).setPopEnterAnim(-1).setPopExitAnim(-1);
      }
      navController.navigate(directions, builder.build());
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "navigate: " + directions, e);
    }
  }

  public void navigateUp() {
    if (navController != null) {
      navController.navigateUp();
    } else {
      Log.e(TAG, "navigateUp: controller is null");
    }
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }

  public Locale getLocale() {
    return locale;
  }

  public void requestSettingsRefresh() {
    Intent intent = new Intent();
    intent.setPackage(getPackageName());
    intent.setAction(ACTION.SETTINGS_CHANGED);
    sendBroadcast(intent);
  }

  public void requestThemeRefresh(boolean designMightHaveChanged) {
    Intent intent = new Intent();
    intent.setPackage(getPackageName());
    intent.setAction(
        designMightHaveChanged ? ACTION.THEME_AND_DESIGN_CHANGED : ACTION.THEME_CHANGED
    );
    sendBroadcast(intent);
  }

  public void reset() {
    sharedPrefs.edit().clear().apply();
    requestSettingsRefresh();
    requestThemeRefresh(true);

    boolean isLauncherIconDisabled = getPackageManager().getComponentEnabledSetting(
        new ComponentName(this, LauncherActivity.class)
    ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    if (isLauncherIconDisabled) {
      getPackageManager().setComponentEnabledSetting(
          new ComponentName(this, LauncherActivity.class),
          PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
          0
      );
    }
  }

  public void restartToApply(long delay) {
    restartToApply(delay, new Bundle(), false, true);
  }

  public void restartToApply(
      long delay, @NonNull Bundle bundle, boolean showForceStopRequest, boolean restoreState
  ) {
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (restoreState) {
        onSaveInstanceState(bundle);
      }
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        finish();
      }
      Intent intent = new Intent(this, MainActivity.class);
      if (restoreState) {
        intent.putExtra(EXTRA.INSTANCE_STATE, bundle);
      }
      if (showForceStopRequest) {
        intent.putExtra(EXTRA.SHOW_FORCE_STOP_REQUEST, true);
      }
      startActivity(intent);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        finish();
      }
      if (UiUtil.areAnimationsEnabled(this)) {
        overridePendingTransition(R.anim.fade_in_restart, R.anim.fade_out_restart);
      } else {
        overridePendingTransition(-1, -1);
      }
    }, delay);
  }

  public void showForceStopRequest() {
    if (!LiveWallpaperService.isMainEngineRunning() || binding == null) {
      return;
    }
    showSnackbar(
        getSnackbar(
            R.string.msg_force_stop, Snackbar.LENGTH_LONG
        ).setAction(
            getString(R.string.action_continue), view -> {
              performHapticHeavyClick();
              if (navController != null) {
                navigate(NavMainDirections.actionGlobalApplyDialog());
              } else {
                ViewUtil.showBottomSheet(this, new ApplyBottomSheetDialogFragment());
              }
            }
        )
    );
  }

  private void showInitialBottomSheets() {
    // Changelog
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefs.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      showChangelogBottomSheet();
    }

    // Feedback
    int feedbackCount = sharedPrefs.getInt(PREF.FEEDBACK_POP_UP_COUNT, 1);
    if (feedbackCount > 0) {
      if (feedbackCount < 5) {
        sharedPrefs.edit().putInt(PREF.FEEDBACK_POP_UP_COUNT, feedbackCount + 1).apply();
      } else {
        showFeedbackBottomSheet();
      }
    }
  }

  public void showTextBottomSheet(@RawRes int file, @StringRes int title) {
    showTextBottomSheet(file, title, 0);
  }

  public void showTextBottomSheet(@RawRes int file, @StringRes int title, @StringRes int link) {
    NavMainDirections.ActionGlobalTextDialog action
        = NavMainDirections.actionGlobalTextDialog();
    action.setTitle(title);
    action.setFile(file);
    if (link != 0) {
      action.setLink(link);
    }
    navigate(action);
  }

  public void showFeedbackBottomSheet() {
    navigate(NavMainDirections.actionGlobalFeedbackDialog());
  }

  public void showChangelogBottomSheet() {
    NavMainDirections.ActionGlobalTextDialog action
        = NavMainDirections.actionGlobalTextDialog();
    action.setTitle(R.string.about_changelog);
    action.setFile(R.raw.changelog);
    action.setHighlights(new String[]{"New:", "Improved:", "Fixed:"});
    navigate(action);
  }

  public boolean isLauncherTouchWiz() {
    PackageManager localPackageManager = getPackageManager();
    Intent intent = new Intent("android.intent.action.MAIN");
    intent.addCategory("android.intent.category.HOME");
    String launcher = localPackageManager.resolveActivity(
        intent, PackageManager.MATCH_DEFAULT_ONLY
    ).activityInfo.packageName;
    return launcher != null && launcher.equals("com.sec.android.app.launcher");
  }

  @SuppressWarnings("deprecation")
  public boolean isOneUiWithDynamicColors() {
    if (VERSION.SDK_INT < VERSION_CODES.S) {
      return false;
    } else {
      final String PACKAGE_NAME = "com.sec.android.app.launcher";
      PackageManager packageManager = getPackageManager();
      PackageInfo info;
      try {
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
          info = packageManager.getPackageInfo(
              PACKAGE_NAME, PackageInfoFlags.of(PackageManager.MATCH_SYSTEM_ONLY)
          );
        } else {
          info = packageManager.getPackageInfo(PACKAGE_NAME, 0);
        }
        return info != null;
      } catch (NameNotFoundException e) {
        return false;
      }
    }
  }

  public void performHapticClick() {
    hapticUtil.click();
  }

  public void performHapticHeavyClick() {
    hapticUtil.heavyClick();
  }

  public void performHapticExplosion() {
    hapticUtil.explode();
  }
}
