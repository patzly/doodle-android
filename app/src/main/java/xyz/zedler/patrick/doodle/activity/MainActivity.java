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
import android.app.ActivityManager.RunningServiceInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import xyz.zedler.patrick.doodle.BuildConfig;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.ActivityMainBinding;
import xyz.zedler.patrick.doodle.fragment.dialog.ChangelogBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.fragment.dialog.FeedbackBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.HapticUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class MainActivity extends AppCompatActivity implements OnClickListener {

  private final static String TAG = MainActivity.class.getSimpleName();

  private ActivityMainBinding binding;
  private NavController navController;
  private SharedPreferences sharedPrefs, sharedPrefsBasic;
  private ViewUtil viewUtil;
  private HapticUtil hapticUtil;
  private ActivityResultLauncher<Intent> wallpaperPickerLauncher;
  private boolean isServiceRunning;
  private boolean settingsApplied;
  private boolean themeApplied;
  private int fabTopEdgeDistance;
  private int bottomInset;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    sharedPrefs = new PrefsUtil(this).checkForMigrations().getSharedPrefs();
    sharedPrefsBasic = getSharedPreferences(Constants.PREFS_NORMAL, Context.MODE_PRIVATE);

    viewUtil = new ViewUtil();
    hapticUtil = new HapticUtil(binding.getRoot());

    wallpaperPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> setFabVisibility(result.getResultCode() != Activity.RESULT_OK, true)
    );

    // Calculate FAB top edge distance to bottom (excluding bottom inset)
    int height = SystemUiUtil.dpToPx(this, 32) + SystemUiUtil.spToPx(this, 16);
    int margin = SystemUiUtil.dpToPx(this, 40);
    fabTopEdgeDistance = height + margin;

    NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_main_nav_host
    );
    assert navHost != null;
    navController = navHost.getNavController();

    SystemBarBehavior.applyBottomInset(binding.fabMain);

    isServiceRunning = isMainWallpaperServiceRunning();

    ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
      bottomInset = insets.getInsets(Type.systemBars()).bottom;
      setFabVisibility(!isServiceRunning, false);
      return insets;
    });

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

    boolean isServiceRunningNew = isMainWallpaperServiceRunning();
    if (isServiceRunning != isServiceRunningNew) {
      isServiceRunning = isServiceRunningNew;
      setFabVisibility(!isServiceRunning, true);
    }

    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(this));

    settingsApplied = sharedPrefs.getBoolean(PREF.SETTINGS_APPLIED, true);
    themeApplied = sharedPrefs.getBoolean(PREF.THEME_APPLIED, true);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.fab_main && viewUtil.isClickEnabled()) {
      try {
        wallpaperPickerLauncher.launch(
            new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(getPackageName(), LiveWallpaperService.class.getCanonicalName())
            )
        );
      } catch (ActivityNotFoundException e) {
        showSnackbar(R.string.msg_preview_missing);
      }
      performHapticHeavyClick();
    }
  }

  public int getFabTopEdgeDistance() {
    return binding.fabMain.getTranslationY() == 0 ? fabTopEdgeDistance : 0;
  }

  private void setFabVisibility(boolean visible, boolean animated) {
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

  public SharedPreferences getSharedPrefsBasic() {
    return sharedPrefsBasic;
  }

  public void requestSettingsRefresh() {
    if (settingsApplied) {
      sharedPrefs.edit().putBoolean(PREF.SETTINGS_APPLIED, false).apply();
      settingsApplied = false;
    }
  }

  public void requestThemeRefresh() {
    if (themeApplied) {
      sharedPrefs.edit().putBoolean(PREF.THEME_APPLIED, false).apply();
      themeApplied = false;
    }
  }

  public void reset() {
    sharedPrefs.edit().clear().apply();
    requestSettingsRefresh();
    requestThemeRefresh();
    new Handler(getMainLooper()).postDelayed(() -> {
      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.addCategory("android.intent.category.LAUNCHER");
      startActivity(intent);
      finish();
      Runtime.getRuntime().exit(0);
    }, 300);
  }

  private boolean isMainWallpaperServiceRunning() {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    if (manager != null) {
      for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (LiveWallpaperService.class.getName().equals(service.service.getClassName())) {
          return !sharedPrefs.getBoolean(PREF.PREVIEW_RUNNING, false);
        }
      }
    }
    return false;
  }

  private void showChangelogIfUpdated() {
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefsBasic.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefsBasic.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefsBasic.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      ViewUtil.showBottomSheet(this, new ChangelogBottomSheetDialogFragment());
    }
  }

  private void showFeedbackAfterSomeUsage() {
    int feedbackCount = sharedPrefsBasic.getInt(PREF.FEEDBACK_POP_UP_COUNT, 1);
    if (feedbackCount > 0) {
      if (feedbackCount < 5) {
        sharedPrefsBasic.edit().putInt(PREF.FEEDBACK_POP_UP_COUNT, feedbackCount + 1).apply();
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
