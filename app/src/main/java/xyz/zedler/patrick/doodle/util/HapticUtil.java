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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.view.HapticFeedbackConstants;
import android.view.View;

public class HapticUtil {

  private boolean enabled;
  private final View view;

  public HapticUtil(View view) {
    this.view = view;
    setEnabled(true);
  }

  public void click() {
    if (enabled) {
      view.performHapticFeedback(
          VERSION.SDK_INT >= VERSION_CODES.M
              ? HapticFeedbackConstants.CONTEXT_CLICK
              : HapticFeedbackConstants.KEYBOARD_TAP,
          HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
      );
    }
  }

  public void heavyClick() {
    if (enabled) {
      view.performHapticFeedback(
          HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
      );
    }
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public static boolean areSystemHapticsTurnedOn(Context context) {
    int hapticFeedbackEnabled = Settings.System.getInt(
        context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0
    );
    return hapticFeedbackEnabled != 0;
  }
}
