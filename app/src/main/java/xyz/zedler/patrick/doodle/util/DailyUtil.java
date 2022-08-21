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

package xyz.zedler.patrick.doodle.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import java.util.Calendar;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.RANDOM;
import xyz.zedler.patrick.doodle.receiver.BootReceiver;
import xyz.zedler.patrick.doodle.receiver.DailyReceiver;

public class DailyUtil {

  private static final String TAG = DailyUtil.class.getSimpleName();

  private final Context context;
  private final SharedPreferences sharedPrefs;
  private final AlarmManager alarmManager;
  private PendingIntent pendingIntent;

  public DailyUtil(Context context) {
    this.context = context;
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  public void scheduleReminder(@Nullable String time) {
    if (time == null) {
      time = DEF.DAILY_TIME;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());

    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
    calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
      calendar.add(Calendar.DATE, 1);
    }

    pendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        new Intent(context, DailyReceiver.class),
        VERSION.SDK_INT >= VERSION_CODES.M
            ? PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_UPDATE_CURRENT
    );

    if (alarmManager == null) {
      return;
    }

    alarmManager.cancel(pendingIntent);
    new Handler(Looper.getMainLooper()).postDelayed(
        () -> alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        ),
        100
    );
  }

  public void scheduleDailyIfEnabled() {
    if (sharedPrefs.getString(PREF.RANDOM, DEF.RANDOM).equals(RANDOM.DAILY)) {
      scheduleReminder(sharedPrefs.getString(PREF.DAILY_TIME, DEF.DAILY_TIME));
    }
  }

  public void setDailyEnabled(boolean enabled) {
    if (enabled) {
      scheduleReminder(sharedPrefs.getString(PREF.DAILY_TIME, DEF.DAILY_TIME));
    } else {
      if (pendingIntent != null && alarmManager != null) {
        alarmManager.cancel(pendingIntent);
      }
    }
    startOnBootCompleted(enabled);
  }

  public void startOnBootCompleted(boolean enabled) {
    context.getPackageManager().setComponentEnabledSetting(
        new ComponentName(context, BootReceiver.class),
        enabled
            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    );
  }
}
