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

package xyz.zedler.patrick.doodle.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class DailyReceiver extends BroadcastReceiver {

  public void onReceive(Context context, Intent intent) {
    new PrefsUtil(context).getSharedPrefs()
        .edit()
        .putBoolean(PREF.CHANGE_DAILY_NOW, true)
        .apply();
  }
}
