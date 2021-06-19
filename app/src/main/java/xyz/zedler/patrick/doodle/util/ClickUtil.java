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

import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;

@Deprecated
public class ClickUtil {

  private long lastClick;

  @Deprecated
  public ClickUtil() {
    lastClick = 0;
  }

  @Deprecated
  public static void setOnClickListeners(View.OnClickListener listener, View... views) {
    for (View view : views) {
      view.setOnClickListener(listener);
    }
  }

  @Deprecated
  public static void setOnCheckedChangeListeners(
      CompoundButton.OnCheckedChangeListener listener,
      CompoundButton... compoundButtons
  ) {
    for (CompoundButton view : compoundButtons) {
      view.setOnCheckedChangeListener(listener);
    }
  }

  @Deprecated
  public void update() {
    lastClick = SystemClock.elapsedRealtime();
  }

  @Deprecated
  public boolean isDisabled() {
    if (SystemClock.elapsedRealtime() - lastClick < 500) {
      return true;
    }
    update();
    return false;
  }

  @Deprecated
  public boolean isEnabled() {
    return !isDisabled();
  }
}
