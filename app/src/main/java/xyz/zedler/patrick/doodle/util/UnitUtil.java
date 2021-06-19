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
import android.graphics.Insets;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

@Deprecated
public class UnitUtil {

  @Deprecated
  public static int getDp(Context context, float dp) {
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.getResources().getDisplayMetrics()
    );
  }

  @Deprecated
  public static int getSp(Context context, float sp) {
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        context.getResources().getDisplayMetrics()
    );
  }

  @Deprecated
  public static int getDisplayHeight(WindowManager windowManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
      Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(
          WindowInsets.Type.systemBars()
      );
      return windowMetrics.getBounds().height() - insets.top - insets.bottom;
    } else {
      DisplayMetrics displayMetrics = new DisplayMetrics();
      windowManager.getDefaultDisplay().getMetrics(displayMetrics);
      return displayMetrics.heightPixels;
    }
  }
}
