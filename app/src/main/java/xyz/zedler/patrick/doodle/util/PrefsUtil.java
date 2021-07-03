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

package xyz.zedler.patrick.doodle.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.preference.PreferenceManager;
import java.util.Objects;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;

public class PrefsUtil {

  private final static String TAG = PrefsUtil.class.getSimpleName();

  private final Context context;
  private SharedPreferences sharedPrefs;

  public PrefsUtil(Context context) {
    this.context = context;
    migrateToStorageContext();
  }

  private void migrateToStorageContext() {
    Context storageContext;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      Context deviceContext = context.createDeviceProtectedStorageContext();
      boolean success = deviceContext.moveSharedPreferencesFrom(
          context, android.preference.PreferenceManager.getDefaultSharedPreferencesName(context)
      );
      if (!success) {
        Log.w(TAG, "Failed to migrate shared preferences to storage context");
      }
      storageContext = deviceContext;
    } else {
      storageContext = context;
    }
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(storageContext);
  }

  public PrefsUtil checkForMigrations() {

    // theme to wallpaper
    migrateString("theme", PREF.WALLPAPER, DEF.WALLPAPER);

    // size is stored in a new way
    if (sharedPrefs.contains(PREF.SCALE)) {
      try {
        sharedPrefs.getFloat(PREF.SCALE, DEF.SCALE);
      } catch (ClassCastException e) {
        removePreference(PREF.SCALE);
      }
    }

    // parallax is stored in a new way
    if (sharedPrefs.contains(PREF.PARALLAX)) {
      int parallax = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
      if (parallax >= 0 && parallax <= 3) {
        sharedPrefs.edit().putInt(PREF.PARALLAX, parallax).apply();
      } else {
        removePreference(PREF.PARALLAX);
      }
    }

    // zoom is stored in a new way
    if (sharedPrefs.contains(PREF.ZOOM)) {
      int zoom = sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM);
      if (zoom >= 2 && zoom <= 5) {
        sharedPrefs.edit().putInt(PREF.ZOOM, zoom).apply();
      } else {
        removePreference(PREF.ZOOM);
      }
    }
    return this;
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }

  private void migrateString(String keyOld, String keyNew, String def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        String current = sharedPrefs.getString(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putString(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void migrateBoolean(String keyOld, String keyNew, boolean def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        boolean current = sharedPrefs.getBoolean(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putBoolean(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void migrateInteger(String keyOld, String keyNew, int def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        int current = sharedPrefs.getInt(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putInt(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void migrateFloat(String keyOld, String keyNew, float def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        float current = sharedPrefs.getFloat(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putFloat(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void removePreference(String key) {
    if (sharedPrefs.contains(key)) {
      sharedPrefs.edit().remove(key).apply();
    }
  }

  public static void restartToApply(Activity activity, Class<Activity> launchClass) {
    Intent intent = new Intent(activity, launchClass);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    activity.startActivity(intent);
    activity.finish();
    Runtime.getRuntime().exit(0);
  }
}
