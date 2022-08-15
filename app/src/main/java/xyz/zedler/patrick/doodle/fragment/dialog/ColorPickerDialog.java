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

package xyz.zedler.patrick.doodle.fragment.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.ColorPickerSquareView;

public class ColorPickerDialog {

  private final Context context;
  private final AlertDialog dialog;
  private final View viewHue;
  private final ColorPickerSquareView viewSatVal;
  private final ImageView viewCursor;
  private final MaterialCardView viewNewColor;
  private final MaterialCardView viewTarget;
  private final float[] currentColorHsv = new float[3];
  private final int alpha;

  public interface OnApplyListener {

    void onApply(ColorPickerDialog dialog, int color);

    void onCancel();
  }

  @SuppressLint("ClickableViewAccessibility")
  public ColorPickerDialog(final Context context, int color, OnApplyListener listener) {
    this.context = context;

    // remove alpha
    color = color | 0xff000000;

    Color.colorToHSV(color, currentColorHsv);
    alpha = Color.alpha(color);

    final View view = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null);
    viewHue = view.findViewById(R.id.image_color_picker_hue);
    viewSatVal = view.findViewById(R.id.card_color_picker_square);
    viewCursor = view.findViewById(R.id.image_color_picker_cursor);
    MaterialCardView viewOldColor = view.findViewById(R.id.card_color_picker_color_old);
    viewNewColor = view.findViewById(R.id.card_color_picker_color_new);
    viewTarget = view.findViewById(R.id.card_color_picker_target);

    viewSatVal.setHue(getHue());
    viewOldColor.setCardBackgroundColor(color);
    viewNewColor.setCardBackgroundColor(color);

    viewHue.setOnTouchListener((v, event) -> {
      if (event.getAction() == MotionEvent.ACTION_MOVE
          || event.getAction() == MotionEvent.ACTION_DOWN
          || event.getAction() == MotionEvent.ACTION_UP) {

        float y = event.getY();
        if (y < 0) y = 0;
        if (y > viewHue.getMeasuredHeight()) {
          y = viewHue.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
        }
        float hue = 360 - 360f / viewHue.getMeasuredHeight() * y;
        if (hue == 360) hue = 0;
        setHue(hue);

        // update view
        viewSatVal.setHue(getHue());
        moveCursor();
        viewNewColor.setCardBackgroundColor(getColor());
        return true;
      }
      return false;
    });

    viewSatVal.setOnTouchListener((v, event) -> {
      if (event.getAction() == MotionEvent.ACTION_MOVE
          || event.getAction() == MotionEvent.ACTION_DOWN
          || event.getAction() == MotionEvent.ACTION_UP) {

        float x = event.getX(); // touch event are in dp units.
        float y = event.getY();

        if (x < 0) {
          x = 0;
        }
        if (x > viewSatVal.getMeasuredWidth()) {
          x = viewSatVal.getMeasuredWidth();
        }
        if (y < 0) {
          y = 0;
        }
        if (y > viewSatVal.getMeasuredHeight()) {
          y = viewSatVal.getMeasuredHeight();
        }

        setSat(1f / viewSatVal.getMeasuredWidth() * x);
        setVal(1 - (1f / viewSatVal.getMeasuredHeight() * y));

        // update view
        moveTarget();
        viewNewColor.setCardBackgroundColor(getColor());

        return true;
      }
      return false;
    });

    // if back button is used, call back our listener.
    dialog = new MaterialAlertDialogBuilder(context)
        .setTitle(R.string.appearance_colors_custom)
        .setPositiveButton(R.string.action_apply, (dialog, which) -> {
          if (listener != null) {
            listener.onApply(this, getColor());
          }
        }).setNegativeButton(R.string.action_cancel, (dialog, which) -> {
          if (listener != null) {
            listener.onCancel();
          }
        })
        .setOnCancelListener(dialog -> {
          if (listener != null) {
            listener.onCancel();
          }
        })
        .setView(view)
        .create();

    // move cursor & target on first draw
    ViewUtil.addOnGlobalLayoutListener(view, new OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        moveCursor();
        moveTarget();
        ViewUtil.removeOnGlobalLayoutListener(view, this);
      }
    });
  }

  protected void moveCursor() {
    float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360);
    if (y == viewHue.getMeasuredHeight()) {
      y = 0;
    }
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewCursor.getLayoutParams();
    layoutParams.topMargin =
        (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2f));
    viewCursor.setLayoutParams(layoutParams);
  }

  protected void moveTarget() {
    float x = getSat() * viewSatVal.getMeasuredWidth();
    float y = (1 - getVal()) * viewSatVal.getMeasuredHeight();
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewTarget.getLayoutParams();
    if (ResUtil.isLayoutRtl(context)) {
      layoutParams.rightMargin =
          (int) (viewSatVal.getRight() + x - Math.floor(viewTarget.getMeasuredWidth() / 2f));
    } else {
      layoutParams.leftMargin =
          (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2f));
    }
    layoutParams.topMargin =
        (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2f));
    viewTarget.setLayoutParams(layoutParams);
  }

  private int getColor() {
    final int argb = Color.HSVToColor(currentColorHsv);
    return alpha << 24 | (argb & 0x00ffffff);
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

  public void show() {
    dialog.show();
  }

  public void dismiss() {
    dialog.dismiss();
  }

  public boolean isShowing() {
    return dialog.isShowing();
  }
}