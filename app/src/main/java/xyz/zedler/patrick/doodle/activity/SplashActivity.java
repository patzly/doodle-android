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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.color.DynamicColors;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.Constants.THEME.MODE;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle bundle) {
    SharedPreferences sharedPrefs = new PrefsUtil(
        this
    ).checkForMigrations().getSharedPrefs();

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

    super.onCreate(bundle);

    new SystemBarBehavior(this).setUp();

    if (Build.VERSION.SDK_INT >= 31) {
      startSettingsActivity(false);
    } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      LayerDrawable splashContent = (LayerDrawable) ResourcesCompat.getDrawable(
          getResources(), R.drawable.splash_content, getTheme()
      );
      getWindow().getDecorView().setBackground(splashContent);
      try {
        assert splashContent != null;
        ViewUtil.startIcon(splashContent.findDrawableByLayerId(R.id.splash_logo));
        new Handler(getMainLooper()).postDelayed(
            () -> startSettingsActivity(true), 900
        );
      } catch (Exception e) {
        startSettingsActivity(true);
      }
    } else {
      startSettingsActivity(true);
    }
  }

  private void startSettingsActivity(boolean fadeOut) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    startActivity(intent);
    overridePendingTransition(0, fadeOut ? R.anim.fade_out : 0);
    finish();
  }
}
