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
 * Copyright (c) 2019-2022 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.color.DynamicColors;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.Constants.THEME.MODE;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class LauncherActivity extends MainActivity {

  @Override
  public void onCreate(Bundle bundle) {
    if (Build.VERSION.SDK_INT >= 31) {
      super.onCreate(bundle);
      getSplashScreen().setOnExitAnimationListener(view -> {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0),
            ObjectAnimator.ofFloat(view.getIconView(), "alpha", 0)
        );
        set.setDuration(400);
        set.setStartDelay(550);
        set.addListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation, boolean isReverse) {
            view.remove();
          }
        });
        set.start();
      });
    } else {
      SharedPreferences sharedPrefs = new PrefsUtil(this)
          .checkForMigrations()
          .getSharedPrefs();

      // DARK MODE

      int modeNight;
      int uiMode = getResources().getConfiguration().uiMode;
      switch (sharedPrefs.getInt(PREF.MODE, MODE.AUTO)) {
        case MODE.LIGHT:
          modeNight = AppCompatDelegate.MODE_NIGHT_NO;
          uiMode = Configuration.UI_MODE_NIGHT_NO;
          break;
        case MODE.DARK:
          modeNight = AppCompatDelegate.MODE_NIGHT_YES;
          uiMode = Configuration.UI_MODE_NIGHT_YES;
          break;
        default:
          modeNight = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
          break;
      }
      AppCompatDelegate.setDefaultNightMode(modeNight);
      // Apply config to resources
      Resources resBase = getBaseContext().getResources();
      Configuration configBase = resBase.getConfiguration();
      configBase.uiMode = uiMode;
      resBase.updateConfiguration(configBase, resBase.getDisplayMetrics());

      // THEME

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
        case THEME.GOOGLE:
          setTheme(R.style.Theme_Doodle_Google);
          break;
        case THEME.PURPLE:
          setTheme(R.style.Theme_Doodle_Purple);
          break;
        default:
          if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyIfAvailable(this);
          } else {
            setTheme(R.style.Theme_Doodle_Google);
          }
          break;
      }

      if (bundle == null) {
        bundle = new Bundle();
      }
      bundle.putBoolean(EXTRA.RUN_AS_SUPER_CLASS, true);
      super.onCreate(bundle);

      new SystemBarBehavior(this).setUp();

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        LayerDrawable splashContent = (LayerDrawable) ResourcesCompat.getDrawable(
            getResources(), R.drawable.splash_content, getTheme()
        );
        getWindow().getDecorView().setBackground(splashContent);
        try {
          assert splashContent != null;
          ViewUtil.startIcon(splashContent.findDrawableByLayerId(R.id.splash_logo));
          new Handler(getMainLooper()).postDelayed(this::startNewMainActivity, 900);
        } catch (Exception e) {
          startNewMainActivity();
        }
      } else {
        startNewMainActivity();
      }
    }
  }

  @Override
  protected void attachBaseContext(Context base) {
    if (Build.VERSION.SDK_INT >= 31) {
      super.attachBaseContext(base);
      return;
    }
    SharedPreferences sharedPrefs = new PrefsUtil(base).checkForMigrations().getSharedPrefs();
    // Night mode
    int modeNight;
    int uiMode = base.getResources().getConfiguration().uiMode;
    switch (sharedPrefs.getInt(PREF.MODE, MODE.AUTO)) {
      case MODE.LIGHT:
        modeNight = AppCompatDelegate.MODE_NIGHT_NO;
        uiMode = Configuration.UI_MODE_NIGHT_NO;
        break;
      case MODE.DARK:
        modeNight = AppCompatDelegate.MODE_NIGHT_YES;
        uiMode = Configuration.UI_MODE_NIGHT_YES;
        break;
      default:
        modeNight = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
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

  private void startNewMainActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    startActivity(intent);
    overridePendingTransition(0, R.anim.fade_out);
    finish();
  }
}
