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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.util.Log;

public class PrefsUtil {

  private final static String TAG = PrefsUtil.class.getSimpleName();

  private final SharedPreferences sharedPrefs;

  public PrefsUtil(Context context) {
    Context storageContext;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      final Context deviceContext = context.createDeviceProtectedStorageContext();
      boolean success = deviceContext.moveSharedPreferencesFrom(
          // TODO: don't rely on this name, old getDefaultSharedPreferencesName was better
          context, context.getPackageName() + "_preferences"
      );
      if (!success) {
        Log.w(TAG, "Failed to migrate shared preferences");
      }
      storageContext = deviceContext;
    } else {
      storageContext = context;
    }
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(storageContext);
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }
}
