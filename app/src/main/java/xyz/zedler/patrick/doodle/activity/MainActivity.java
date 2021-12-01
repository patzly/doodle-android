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
 * Copyright (c) 2019-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;
import xyz.zedler.patrick.doodle.BuildConfig;
import xyz.zedler.patrick.doodle.Constants.ACTION;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.Constants.THEME.MODE;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.ActivityMainBinding;
import xyz.zedler.patrick.doodle.fragment.BaseFragment;
import xyz.zedler.patrick.doodle.fragment.dialog.ApplyBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.fragment.dialog.ChangelogBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.fragment.dialog.FeedbackBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.HapticUtil;
import xyz.zedler.patrick.doodle.util.LocaleUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class MainActivity extends AppCompatActivity implements OnClickListener {

  private final static String TAG = MainActivity.class.getSimpleName();

  private ActivityMainBinding binding;
  private NavController navController;
  private SharedPreferences sharedPrefs;
  private ViewUtil viewUtil;
  private HapticUtil hapticUtil;
  private ActivityResultLauncher<Intent> wallpaperPickerLauncher;
  private NavHostFragment navHost;
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

    // LANGUAGE

    Locale userLocale = LocaleUtil.getUserLocale(this, sharedPrefs);
    Locale.setDefault(userLocale);
    // base
    Resources resBase = getBaseContext().getResources();
    Configuration configBase = resBase.getConfiguration();
    configBase.setLocale(userLocale);
    resBase.updateConfiguration(configBase, resBase.getDisplayMetrics());
    // app
    Resources resApp = getApplicationContext().getResources();
    Configuration configApp = resApp.getConfiguration();
    configApp.setLocale(userLocale);
    resApp.updateConfiguration(configApp, getResources().getDisplayMetrics());

    int mode;
    switch (sharedPrefs.getInt(PREF.MODE, MODE.AUTO)) {
      case MODE.LIGHT:
        mode = AppCompatDelegate.MODE_NIGHT_NO;
        break;
      case MODE.DARK:
        mode = AppCompatDelegate.MODE_NIGHT_YES;
        break;
      default:
        mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        break;
    }
    AppCompatDelegate.setDefaultNightMode(mode);

    switch (sharedPrefs.getString(PREF.THEME, DEF.THEME)) {
      case THEME.RED:
        setTheme(R.style.Theme_Doodle_Red);
        break;
      case THEME.YELLOW:
        setTheme(R.style.Theme_Doodle_Yellow);
        break;
      case THEME.GREEN:
        setTheme(R.style.Theme_Doodle_Green);
        break;
      case THEME.BLUE:
        setTheme(R.style.Theme_Doodle_Blue);
        break;
      default:
        if (DynamicColors.isDynamicColorAvailable()) {
          DynamicColors.applyIfAvailable(this);
        } else {
          setTheme(R.style.Theme_Doodle_Red);
        }
        break;
    }

    Bundle bundleInstanceState = getIntent().getBundleExtra(EXTRA.INSTANCE_STATE);
    super.onCreate(bundleInstanceState != null ? bundleInstanceState : savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    viewUtil = new ViewUtil();
    hapticUtil = new HapticUtil(binding.getRoot());

    wallpaperPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> setFabVisibility(
            result.getResultCode() != Activity.RESULT_OK, true
        )
    );

    // Calculate FAB top edge distance to bottom (excluding bottom inset)
    int height = SystemUiUtil.dpToPx(this, 32) + SystemUiUtil.spToPx(
        this, 16
    );
    int margin = SystemUiUtil.dpToPx(this, 40);
    fabTopEdgeDistance = height + margin;

    navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_main_nav_host
    );
    assert navHost != null;
    navController = navHost.getNavController();

    SystemBarBehavior.applyBottomInset(binding.fabMain);

    isServiceRunning = isWallpaperServiceRunning(true);
    if (isServiceRunning) {
      binding.fabMain.setVisibility(View.INVISIBLE);
    }

    if (getIntent() != null) {
      if (getIntent().getBooleanExtra(EXTRA.SHOW_FORCE_STOP_REQUEST, false)) {
        new Handler().postDelayed(() -> showForceStopRequest(null), 200);
      }
    }

    ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
      bottomInset = insets.getInsets(Type.systemBars()).bottom;
      setFabVisibility(!isServiceRunning, false);
      return insets;
    });

    binding.fabMain.setRippleColor(
        ColorStateList.valueOf(
            ResUtil.getColorAttr(this, R.attr.colorOnTertiaryContainer, 0.07f)
        )
    );

    ViewUtil.setOnClickListeners(
        this,
        binding.fabMain
    );

    if (savedInstanceState == null) {
      showChangelogIfUpdated();
      showFeedbackAfterSomeUsage();
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

    boolean isServiceRunningNew = isWallpaperServiceRunning(true);
    if (isServiceRunning != isServiceRunningNew) {
      isServiceRunning = isServiceRunningNew;
      setFabVisibility(!isServiceRunning, true);
    }

    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(this));
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.fab_main && viewUtil.isClickEnabled()) {
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
      performHapticHeavyClick();
    }
  }

  @Override
  protected void attachBaseContext(Context base) {
    if (runAsSuperClass) {
      super.attachBaseContext(base);
    } else {
      Locale userLocale = LocaleUtil.getUserLocale(base);
      Locale.setDefault(userLocale);
      Resources resources = base.getResources();
      Configuration configuration = resources.getConfiguration();
      configuration.setLocale(userLocale);
      resources.updateConfiguration(configuration, resources.getDisplayMetrics());
      super.attachBaseContext(base.createConfigurationContext(configuration));
    }
  }

  @Override
  public void applyOverrideConfiguration(Configuration overrideConfiguration) {
    if (!runAsSuperClass && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
      overrideConfiguration.setLocale(LocaleUtil.getUserLocale(this, sharedPrefs));
    }
    super.applyOverrideConfiguration(overrideConfiguration);
  }

  @NonNull
  public BaseFragment getCurrentFragment() {
    return (BaseFragment) navHost.getChildFragmentManager().getFragments().get(0);
  }

  public int getFabTopEdgeDistance() {
    return binding.fabMain.getTranslationY() == 0
        && binding.fabMain.getVisibility() == View.VISIBLE ? fabTopEdgeDistance : 0;
  }

  private void setFabVisibility(boolean visible, boolean animated) {
    binding.fabMain.setVisibility(View.VISIBLE);
    if (binding == null) {
      return;
    }
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

  private void showSnackbar(@StringRes int resId) {
    showSnackbar(
        Snackbar.make(binding.fragmentMainNavHost, getString(resId), Snackbar.LENGTH_LONG)
    );
  }

  public void showSnackbar(Snackbar snackbar) {
    snackbar.setAnchorView(binding.fabMain).show();
  }

  public NavController getNavController() {
    return navController;
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }

  public void requestSettingsRefresh() {
    Intent intent = new Intent();
    intent.setAction(ACTION.SETTINGS_CHANGED);
    sendBroadcast(intent);
  }

  public void requestThemeRefresh() {
    Intent intent = new Intent();
    intent.setAction(ACTION.THEME_CHANGED);
    sendBroadcast(intent);
  }

  public void reset() {
    sharedPrefs.edit().clear().apply();
    requestSettingsRefresh();
    requestThemeRefresh();

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

    restartToApply(100, true);
  }

  public void restartToApply(long delay) {
    restartToApply(delay, false);
  }

  public void restartToApply(long delay, boolean showForceStopRequest) {
    new Handler().postDelayed(() -> {
      Bundle bundle = new Bundle();
      onSaveInstanceState(bundle);
      finishAndRemoveTask();

      Intent intent = new Intent(this, MainActivity.class);
      intent.putExtra(EXTRA.INSTANCE_STATE, bundle);
      if (showForceStopRequest) {
        intent.putExtra(EXTRA.SHOW_FORCE_STOP_REQUEST, true);
      }
      startActivity(intent);
      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }, delay);
  }

  @SuppressLint("ShowToast")
  public void showForceStopRequest(NavDirections directions) {
    if (!isWallpaperServiceRunning(true)) {
      return;
    }
    showSnackbar(
        Snackbar.make(
            binding.getRoot(), getString(R.string.msg_force_stop), Snackbar.LENGTH_LONG
        ).setAction(
            getString(R.string.action_continue), view -> {
              performHapticHeavyClick();
              if (directions != null) {
                navController.navigate(directions);
              } else {
                ViewUtil.showBottomSheet(this, new ApplyBottomSheetDialogFragment());
              }
            }
        )
    );
  }

  public boolean isWallpaperServiceRunning(boolean requireMain) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    if (manager != null) {
      for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (LiveWallpaperService.class.getName().equals(service.service.getClassName())) {
          if (requireMain) {
            return !sharedPrefs.getBoolean(PREF.PREVIEW_RUNNING, false);
          } else {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void showChangelogIfUpdated() {
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefs.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      ViewUtil.showBottomSheet(this, new ChangelogBottomSheetDialogFragment());
    }
  }

  private void showFeedbackAfterSomeUsage() {
    int feedbackCount = sharedPrefs.getInt(PREF.FEEDBACK_POP_UP_COUNT, 1);
    if (feedbackCount > 0) {
      if (feedbackCount < 5) {
        sharedPrefs.edit().putInt(PREF.FEEDBACK_POP_UP_COUNT, feedbackCount + 1).apply();
      } else {
        ViewUtil.showBottomSheet(this, new FeedbackBottomSheetDialogFragment());
      }
    }
  }

  public boolean isTouchWiz() {
    PackageManager localPackageManager = getPackageManager();
    Intent intent = new Intent("android.intent.action.MAIN");
    intent.addCategory("android.intent.category.HOME");
    String launcher = localPackageManager.resolveActivity(
        intent, PackageManager.MATCH_DEFAULT_ONLY
    ).activityInfo.packageName;
    return launcher != null && launcher.equals("com.sec.android.app.launcher");
  }

  public void performHapticClick() {
    hapticUtil.click();
  }

  public void performHapticHeavyClick() {
    hapticUtil.heavyClick();
  }
}
