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

package xyz.zedler.patrick.doodle.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import xyz.zedler.patrick.doodle.databinding.ViewColorPickerBinding;
import xyz.zedler.patrick.doodle.util.HapticUtil;
import xyz.zedler.patrick.doodle.util.UiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class ColorPickerView extends FrameLayout {

  private ViewColorPickerBinding binding;
  private boolean isRtl;
  private final float[] currentColorHsv = new float[3];
  private HapticUtil hapticUtil;

  public ColorPickerView(Context context) {
    super(context);
    init(context);
  }

  public ColorPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void init(Context context) {
    binding = ViewColorPickerBinding.inflate(
        LayoutInflater.from(context), this, true
    );
    isRtl = UiUtil.isLayoutRtl(context);

    hapticUtil = new HapticUtil(context);
    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(context));

    binding.imageColorPickerHue.setOnTouchListener((v, event) -> {
      if (event.getAction() == MotionEvent.ACTION_MOVE
          || event.getAction() == MotionEvent.ACTION_DOWN
          || event.getAction() == MotionEvent.ACTION_UP) {
        float y = event.getY();
        if (y < 0) y = 0;
        if (y > binding.imageColorPickerHue.getMeasuredHeight()) {
          // to avoid jumping the cursor from bottom to top
          y = binding.imageColorPickerHue.getMeasuredHeight() - 0.001f;
        }
        float hue = 360 - 360f / binding.imageColorPickerHue.getMeasuredHeight() * y;
        if (hue == 360) hue = 0;
        setHue(hue);

        // update view
        binding.viewColorPickerLuminance.setHue(getHue());
        moveCursor();
        binding.cardColorPickerColorNew.setCardBackgroundColor(getColor());

        if (event.getAction() == MotionEvent.ACTION_DOWN
            || event.getAction() == MotionEvent.ACTION_UP) {
          hapticUtil.tick();
        }
        return true;
      }
      return false;
    });

    binding.viewColorPickerLuminance.setOnTouchListener((v, event) -> {
      if (event.getAction() == MotionEvent.ACTION_MOVE
          || event.getAction() == MotionEvent.ACTION_DOWN
          || event.getAction() == MotionEvent.ACTION_UP) {
        float x = event.getX(); // touch event are in dp units.
        float y = event.getY();

        if (x < 0) {
          x = 0;
        }
        if (x > binding.viewColorPickerLuminance.getMeasuredWidth()) {
          x = binding.viewColorPickerLuminance.getMeasuredWidth();
        }
        if (y < 0) {
          y = 0;
        }
        if (y > binding.viewColorPickerLuminance.getMeasuredHeight()) {
          y = binding.viewColorPickerLuminance.getMeasuredHeight();
        }

        setSat(1f / binding.viewColorPickerLuminance.getMeasuredWidth() * x);
        setVal(1 - (1f / binding.viewColorPickerLuminance.getMeasuredHeight() * y));

        // update view
        moveTarget();
        binding.cardColorPickerColorNew.setCardBackgroundColor(getColor());

        if (event.getAction() == MotionEvent.ACTION_DOWN
            || event.getAction() == MotionEvent.ACTION_UP) {
          hapticUtil.tick();
        }
        return true;
      }
      return false;
    });

    binding.cardColorPickerColorOld.setOnClickListener(v -> {
      setColor(binding.cardColorPickerColorOld.getCardBackgroundColor().getDefaultColor());
      hapticUtil.click();
    });

    // move cursor & target on first draw
    ViewUtil.addOnGlobalLayoutListener(this, new OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        moveCursor();
        moveTarget();
        ViewUtil.removeOnGlobalLayoutListener(ColorPickerView.this, this);
      }
    });
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    binding = null;
  }

  @ColorInt
  public int getColor() {
    return 255 << 24 | (Color.HSVToColor(currentColorHsv) & 0x00ffffff);
  }

  public void setColor(@ColorInt int color) {
    color = color | 0xff000000; // remove alpha
    Color.colorToHSV(color, currentColorHsv);
    if (binding != null) {
      binding.viewColorPickerLuminance.setHue(getHue());
      binding.cardColorPickerColorNew.setCardBackgroundColor(color);
      binding.cardColorPickerColorOld.setCardBackgroundColor(color);
    }
    moveCursor();
    moveTarget();
  }

  public void setColor(@ColorInt int colorOld, @ColorInt int colorNew) {
    colorOld = colorOld | 0xff000000; // remove alpha
    colorNew = colorNew | 0xff000000;
    Color.colorToHSV(colorNew, currentColorHsv);
    if (binding != null) {
      binding.viewColorPickerLuminance.setHue(getHue());
      binding.cardColorPickerColorOld.setCardBackgroundColor(colorOld);
      binding.cardColorPickerColorNew.setCardBackgroundColor(colorNew);
    }
    moveCursor();
    moveTarget();
  }

  private void moveCursor() {
    if (binding == null) {
      return;
    }
    float y = binding.imageColorPickerHue.getMeasuredHeight() - (getHue() * binding.imageColorPickerHue.getMeasuredHeight() / 360);
    if (y == binding.imageColorPickerHue.getMeasuredHeight()) {
      y = 0;
    }
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.imageColorPickerCursor.getLayoutParams();
    layoutParams.topMargin =
        (int) (binding.imageColorPickerHue.getTop() + y - Math.floor(binding.imageColorPickerCursor.getMeasuredHeight() / 2f));
    binding.imageColorPickerCursor.setLayoutParams(layoutParams);
  }

  private void moveTarget() {
    if (binding == null) {
      return;
    }
    float x = getSat() * binding.viewColorPickerLuminance.getMeasuredWidth();
    float y = (1 - getVal()) * binding.viewColorPickerLuminance.getMeasuredHeight();
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.cardColorPickerTarget.getLayoutParams();
    if (isRtl) {
      layoutParams.rightMargin =
          (int) (binding.viewColorPickerLuminance.getRight() + x
              - Math.floor(binding.cardColorPickerTarget.getMeasuredWidth() / 2f));
    } else {
      layoutParams.leftMargin =
          (int) (binding.viewColorPickerLuminance.getLeft() + x
              - Math.floor(binding.cardColorPickerTarget.getMeasuredWidth() / 2f));
    }
    layoutParams.topMargin =
        (int) (binding.viewColorPickerLuminance.getTop() + y
            - Math.floor(binding.cardColorPickerTarget.getMeasuredHeight() / 2f));
    binding.cardColorPickerTarget.setLayoutParams(layoutParams);
  }

  private float getHue() {
    return currentColorHsv[0];
  }

  private float getSat() {
    return currentColorHsv[1];
  }

  private float getVal() {
    return currentColorHsv[2];
  }

  private void setHue(float hue) {
    currentColorHsv[0] = hue;
  }

  private void setSat(float sat) {
    currentColorHsv[1] = sat;
  }

  private void setVal(float val) {
    currentColorHsv[2] = val;
  }
}
