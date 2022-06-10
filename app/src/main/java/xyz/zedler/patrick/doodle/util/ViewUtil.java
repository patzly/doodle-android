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

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.elevation.SurfaceColors;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import xyz.zedler.patrick.doodle.R;

public class ViewUtil {

  private static final String TAG = ViewUtil.class.getSimpleName();

  private final long idle;
  private final LinkedList<Timestamp> timestamps;

  private static class Timestamp {

    private final int id;
    private long time;

    public Timestamp(int id, long time) {
      this.id = id;
      this.time = time;
    }
  }

  // Prevent multiple clicks

  public ViewUtil(long minClickIdle) {
    idle = minClickIdle;
    timestamps = new LinkedList<>();
  }

  public ViewUtil() {
    idle = 500;
    timestamps = new LinkedList<>();
  }

  public boolean isClickDisabled(int id) {
    for (int i = 0; i < timestamps.size(); i++) {
      if (timestamps.get(i).id == id) {
        if (SystemClock.elapsedRealtime() - timestamps.get(i).time < idle) {
          return true;
        } else {
          timestamps.get(i).time = SystemClock.elapsedRealtime();
          return false;
        }
      }
    }
    timestamps.add(new Timestamp(id, SystemClock.elapsedRealtime()));
    return false;
  }

  public boolean isClickEnabled(int id) {
    return !isClickDisabled(id);
  }

  public void cleanUp() {
    for (Iterator<Timestamp> iterator = timestamps.iterator(); iterator.hasNext(); ) {
      Timestamp timestamp = iterator.next();
      if (SystemClock.elapsedRealtime() - timestamp.time > idle) {
        iterator.remove();
      }
    }
  }

  // Show keyboard for EditText

  public static void requestFocusAndShowKeyboard(@NonNull final View view) {
    view.requestFocus();
    view.post(() -> {
      InputMethodManager inputMethod = (InputMethodManager) view.getContext().getSystemService(
          Context.INPUT_METHOD_SERVICE
      );
      inputMethod.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    });
  }

  // ClickListeners & OnCheckedChangeListeners

  public static void setOnClickListeners(View.OnClickListener listener, View... views) {
    for (View view : views) {
      view.setOnClickListener(listener);
    }
  }

  public static void setOnCheckedChangeListeners(
      CompoundButton.OnCheckedChangeListener listener,
      CompoundButton... compoundButtons
  ) {
    for (CompoundButton view : compoundButtons) {
      view.setOnCheckedChangeListener(listener);
    }
  }

  // BottomSheets

  public static void showBottomSheet(AppCompatActivity activity, BottomSheetDialogFragment sheet) {
    sheet.show(activity.getSupportFragmentManager(), sheet.toString());
  }

  public static void showBottomSheet(
      AppCompatActivity activity, BottomSheetDialogFragment sheet, @Nullable Bundle bundle) {
    if (bundle != null) {
      sheet.setArguments(bundle);
    }
    sheet.show(activity.getSupportFragmentManager(), sheet.toString());
  }

  // OnGlobalLayoutListeners

  public static void addOnGlobalLayoutListener(
      @Nullable View view, @NonNull OnGlobalLayoutListener listener) {
    if (view != null) {
      view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }
  }

  public static void removeOnGlobalLayoutListener(
      @Nullable View view, @NonNull OnGlobalLayoutListener victim) {
    if (view != null) {
      view.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
    }
  }

  // Animated icons

  public static void startIcon(ImageView imageView) {
    if (imageView == null) {
      return;
    }
    startIcon(imageView.getDrawable());
  }

  public static void startIcon(Drawable drawable) {
    if (drawable == null) {
      return;
    }
    try {
      ((Animatable) drawable).start();
    } catch (ClassCastException e) {
      Log.e(TAG, "icon animation requires AnimVectorDrawable");
    }
  }

  public static void resetAnimatedIcon(ImageView imageView) {
    if (imageView == null) {
      return;
    }
    try {
      Animatable animatable = (Animatable) imageView.getDrawable();
      if (animatable != null) {
        animatable.stop();
      }
      imageView.setImageDrawable(null);
      imageView.setImageDrawable((Drawable) animatable);
    } catch (ClassCastException e) {
      Log.e(TAG, "resetting animated icon requires AnimVectorDrawable");
    }
  }

  // Blink MaterialCardView

  public static void blinkCard(MaterialCardView cardView, @ColorRes int resId, long duration) {
    Context context = cardView.getContext();
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
    valueAnimator.addUpdateListener(animation -> {
      cardView.setStrokeColor(
          ColorUtils.blendARGB(
              ResUtil.getColorOutline(context),
              ContextCompat.getColor(context, resId),
              (float) valueAnimator.getAnimatedValue()
          )
      );
      cardView.setStrokeWidth(
          SystemUiUtil.dpToPx(context, (float) valueAnimator.getAnimatedValue() + 1)
      );
    });
    valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
    valueAnimator.setRepeatCount(1);
    valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
    valueAnimator.setDuration(duration).start();
  }

  // Check MaterialCardViews

  public static void setChecked(boolean checked, MaterialCardView... cardViews) {
    for (MaterialCardView cardView : cardViews) {
      if (cardView != null) {
        cardView.setChecked(checked);
      }
    }
  }

  public interface OnWidthListener {

    void onWidth(int width);
  }

  public static void calculateWidth(@NonNull View view, OnWidthListener listener) {
    view.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            if (listener != null) {
              listener.onWidth(view.getMeasuredWidth());
            }
            if (view.getViewTreeObserver().isAlive()) {
              view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
          }
        });
  }

  public static void centerToolbarTitleOnLargeScreens(MaterialToolbar toolbar) {
    calculateWidth(toolbar, width -> {
      Resources resources = toolbar.getContext().getResources();
      int maxWidth = resources.getDimensionPixelSize(R.dimen.max_content_width);
      boolean isFullWidth = maxWidth >= width;
      toolbar.setTitleCentered(!isFullWidth);
    });
  }

  public static void styleExpandedToolbarOnLargeScreens(CollapsingToolbarLayout ctl) {
    calculateWidth(ctl, width -> {
      // is full width
      Resources resources = ctl.getContext().getResources();
      int maxWidth = resources.getDimensionPixelSize(R.dimen.max_content_width);
      boolean isFullWidth = maxWidth >= width;
      // expanded margin start
      int margin = isFullWidth ? 0 : (width - maxWidth) / 2;
      ctl.setExpandedTitleMarginStart(SystemUiUtil.dpToPx(ctl.getContext(), 16) + margin);
      // expanded margin start
      ctl.setCollapsedTitleGravity(isFullWidth ? Gravity.START : Gravity.CENTER_HORIZONTAL);
    });
  }

  // Ripple background for surface list items

  public static Drawable getRippleBgListItemSurface(Context context) {
    float[] radii = new float[8];
    Arrays.fill(radii, SystemUiUtil.dpToPx(context, 16));
    RoundRectShape rect = new RoundRectShape(radii, null, null);
    ShapeDrawable shape = new ShapeDrawable(rect);
    shape.getPaint().setColor(SurfaceColors.SURFACE_3.getColor(context));
    LayerDrawable layers = new LayerDrawable(new ShapeDrawable[]{shape});
    layers.setLayerInset(
        0,
        SystemUiUtil.dpToPx(context, 8),
        SystemUiUtil.dpToPx(context, 2),
        SystemUiUtil.dpToPx(context, 8),
        SystemUiUtil.dpToPx(context, 2)
    );
    return new RippleDrawable(
        ColorStateList.valueOf(ResUtil.getColorHighlight(context)), null, layers
    );
  }

  public static Drawable getRippleBgListItemSurfaceRecyclerItem(Context context) {
    float[] radii = new float[8];
    Arrays.fill(radii, SystemUiUtil.dpToPx(context, 16));
    RoundRectShape rect = new RoundRectShape(radii, null, null);
    ShapeDrawable shape = new ShapeDrawable(rect);
    shape.getPaint().setColor(SurfaceColors.SURFACE_3.getColor(context));
    return new RippleDrawable(
        ColorStateList.valueOf(ResUtil.getColorHighlight(context)), null, shape
    );
  }

  public static void uncheckAllChildren(ViewGroup... viewGroups) {
    for (ViewGroup viewGroup : viewGroups) {
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View child = viewGroup.getChildAt(i);
        if (child instanceof MaterialCardView) {
          ((MaterialCardView) child).setChecked(false);
        }
      }
    }
  }

  public static void setEnabled(boolean enabled, View... views) {
    for (View view : views) {
      view.setEnabled(enabled);
    }
  }

  public static void setEnabledAlpha(boolean enabled, boolean animated, View... views) {
    for (View view : views) {
      view.setEnabled(enabled);
      if (animated) {
        view.animate().alpha(enabled ? 1 : 0.5f).setDuration(200).start();
      } else {
        view.setAlpha(enabled ? 1 : 0.5f);
      }
    }
  }
}
