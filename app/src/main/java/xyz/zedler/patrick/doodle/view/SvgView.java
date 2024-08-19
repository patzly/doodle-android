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

package xyz.zedler.patrick.doodle.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import androidx.annotation.RawRes;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;

public class SvgView extends View {

  SvgDrawable drawable;

  public SvgView(Context context) {
    super(context);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (drawable != null) {
      drawable.draw(canvas);
    }
  }

  public void setSvg(@RawRes int resId) {
    drawable = new SvgDrawable(getContext(), resId);
    drawable.setScale(SvgDrawable.getDefaultScale(getContext()));

    invalidate();
  }
}
