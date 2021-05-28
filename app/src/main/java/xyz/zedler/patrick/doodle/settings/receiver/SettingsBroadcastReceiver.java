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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SettingsBroadcastReceiver extends BroadcastReceiver {

  protected static final String SETTINGS_UPDATE = "xyz.zedler.patrick.doodle.settings.update";
  protected static final String WALLPAPER_ACTION = "wallpaper.action";
  public static final String WALLPAPER_BOOT_ACTION = "wallpaper.boot.action";

  public static Intent createIntent(Context context, String action) {
    Intent intent = new Intent(context, SettingsBroadcastReceiver.class);
    intent.setAction(SETTINGS_UPDATE);
    intent.putExtra(WALLPAPER_ACTION, action);
    return intent;
  }

  public static PendingIntent createPendingIntent(Context context, Intent intent, int requestCode) {
    return PendingIntent.getBroadcast(
        context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT
    );
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (SETTINGS_UPDATE.equals(intent.getAction())) {
      String action2 = intent.getStringExtra(WALLPAPER_ACTION);
      Intent intentToSend = new Intent();
      intentToSend.setAction(action2);
      Bundle bundle = intent.getExtras();
      if (bundle != null) {
        intentToSend.putExtras(bundle);
      }
      LocalBroadcastManager.getInstance(context).sendBroadcast(intentToSend);
    }
  }
}
