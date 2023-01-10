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
 * Copyright (c) 2019-2023 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class ColorLuminanceView extends View {

  private Paint paint;
  private Shader luar;
  private final float[] color = {1, 1, 1};
  private boolean hasChanged = true;

  public ColorLuminanceView(Context context) {
    super(context);
  }

  public ColorLuminanceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (paint == null) {
      paint = new Paint();
      luar = new LinearGradient(
          0, 0, 0, getHeight(), 0xffffffff, 0xff000000, TileMode.CLAMP
      );
    }
    if (hasChanged) {
      int rgb = Color.HSVToColor(color);
      Shader dalam = new LinearGradient(
          0, 0, getWidth(), 0, 0xffffffff, rgb, TileMode.CLAMP
      );
      paint.setShader(new ComposeShader(luar, dalam, PorterDuff.Mode.MULTIPLY));
      hasChanged = false;
    }
    canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
  }

  public void setHue(float hue) {
    color[0] = hue;
    hasChanged = true;
    invalidate();
  }
}
