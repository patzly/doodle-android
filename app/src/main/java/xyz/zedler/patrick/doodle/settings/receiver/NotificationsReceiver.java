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

package xyz.zedler.patrick.doodle.settings.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import xyz.zedler.patrick.doodle.settings.view.SettingsController;

public class NotificationsReceiver extends BroadcastReceiver {
  public static final String ACTION = "xyz.zedler.patrick.doodle";
  public static final String CUSTOM_NOTIFICATION_ACTION = "xyz.zedler.patrick.doodle.NEW_NOTIFICATION";
  public static final String EXTRA_DATA = "extra_doodle_data";
  public static final String EXTRA_IS_DIY = "extra_doodle_is_diy";
  public static final String EXTRA_THEME = "extra_doodle_theme";
  public static final String NOTIFICATION_TINT_EXTRA = "notification_color";
  public static final String TAG = NotificationsReceiver.class.getName();

  public void onReceive(Context context, Intent intent) {
    Resources resources = context.getResources();
    String extra = "extra_setting";
    if (intent.getAction().equals("xyz.zedler.patrick.doodle.settings") && intent.hasExtra(extra)) {
      String[] themeString = intent.getStringExtra(extra).split(":");
      if (themeString.length == 2 && isInteger(themeString[1])) {
        Intent intentToSend = new Intent();
        intentToSend.setAction("xyz.zedler.patrick.doodle");
        intentToSend.putExtra("extra_doodle_theme", Integer.valueOf(themeString[1]));
        intentToSend.putExtra("extra_doodle_is_diy", false);
        intentToSend.putExtra(SettingsController.EXTRA_SAVE_SETTINGS, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentToSend);
      }
    }
  }

  public static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  public static boolean isInteger(String s, int radix) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        }
      } else if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }
}
