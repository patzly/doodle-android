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
 * Copyright (c) 2019-2024 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.util;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import xyz.zedler.patrick.doodle.Constants.DEF;

public class RandomUtil {

  private static final String TAG = RandomUtil.class.getSimpleName();

  private static final int REQUEST = 1;

  private long period;
  private long lastChange;
  private final Action action;
  private Timer timer;
  //private final AlarmManager alarmManager;
  //private final PendingIntent pendingIntent;

  public interface Action {

    void execute(boolean force);
  }

  public RandomUtil(@NonNull Action action) {
    this.action = action;

//    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//    pendingIntent = PendingIntent.getBroadcast(
//        context,
//        REQUEST,
//        new Intent(context, RandomReceiver.class),
//        VERSION.SDK_INT >= VERSION_CODES.M
//            ? PendingIntent.FLAG_IMMUTABLE
//            : PendingIntent.FLAG_UPDATE_CURRENT
//    );
  }

  public void scheduleDaily(String time) {
    if (time == null) {
      time = DEF.DAILY_TIME;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());

    String[] parts = time.split(":");
    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
    calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
      calendar.add(Calendar.DATE, 1);
    }

//    if (alarmManager != null) {
//      alarmManager.cancel(pendingIntent);
//      alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//    }

    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            changeIfRequired(false);
          }
        },
        calendar.getTime(),
        AlarmManager.INTERVAL_DAY
    );

    this.period = AlarmManager.INTERVAL_DAY;
    calendar.add(Calendar.DAY_OF_YEAR, -1);
    long difference = System.currentTimeMillis() - calendar.getTimeInMillis();
    lastChange = SystemClock.elapsedRealtime() - difference;
  }

  public void scheduleInterval(long period) {
    this.period = period;
    lastChange = SystemClock.elapsedRealtime();

    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            changeIfRequired(false);
          }
        },
        period,
        period
    );
  }

  public void changeIfRequired(boolean force) {
    long currentTime = SystemClock.elapsedRealtime(); // 1h      | 1h25m |
    long nextChange = lastChange + period;            // 0 + 15m | 1h15m | 1h30m

    if (currentTime >= nextChange) {
      action.execute(force);
      // next change
      long difference = currentTime - nextChange;     // 45m     | 10m
      int multiple = (int) (difference / period);     // 3       | 0
      lastChange += period + multiple * period;       // 1h      | 1h15m
    }
  }

  public void cancel() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  public class RandomReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
      RandomUtil.this.changeIfRequired(false);
    }
  }
}
