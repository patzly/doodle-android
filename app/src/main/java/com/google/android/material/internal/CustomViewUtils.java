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

package com.google.android.material.internal;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CustomViewUtils {

  public static boolean isLayoutRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  /**
   * Wrapper around {@link androidx.core.view.OnApplyWindowInsetsListener} that records the initial
   * padding of the view and requests that insets are applied when attached.
   */
  public static void doOnApplyWindowInsets(
      @NonNull View view, @NonNull final OnApplyWindowInsetsListener listener) {
    // Create a snapshot of the view's padding state.
    final RelativePadding initialPadding =
        new RelativePadding(
            ViewCompat.getPaddingStart(view),
            view.getPaddingTop(),
            ViewCompat.getPaddingEnd(view),
            view.getPaddingBottom());
    // Set an actual OnApplyWindowInsetsListener which proxies to the given callback, also passing
    // in the original padding state.
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (v, insets) -> listener.onApplyWindowInsets(
            v, insets, new RelativePadding(initialPadding)
        )
    );
    // Request some insets.
    requestApplyInsetsWhenAttached(view);
  }

  /** Requests that insets should be applied to this view once it is attached. */
  public static void requestApplyInsetsWhenAttached(@NonNull View view) {
    if (ViewCompat.isAttachedToWindow(view)) {
      // We're already attached, just request as normal.
      ViewCompat.requestApplyInsets(view);
    } else {
      // We're not attached to the hierarchy, add a listener to request when we are.
      view.addOnAttachStateChangeListener(
          new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
              v.removeOnAttachStateChangeListener(this);
              ViewCompat.requestApplyInsets(v);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {}
          });
    }
  }

  /** Simple data object to store the initial padding for a view. */
  public static class RelativePadding {
    public int start;
    public int top;
    public int end;
    public int bottom;

    public RelativePadding(int start, int top, int end, int bottom) {
      this.start = start;
      this.top = top;
      this.end = end;
      this.bottom = bottom;
    }

    public RelativePadding(@NonNull RelativePadding other) {
      this.start = other.start;
      this.top = other.top;
      this.end = other.end;
      this.bottom = other.bottom;
    }

    /** Applies this relative padding to the view. */
    public void applyToView(View view) {
      ViewCompat.setPaddingRelative(view, start, top, end, bottom);
    }
  }

  /**
   * Wrapper around {@link androidx.core.view.OnApplyWindowInsetsListener} which also passes the
   * initial padding set on the view. Used with #doOnApplyWindowInsets(View,
   * CustomViewUtils.OnApplyWindowInsetsListener).
   */
  public interface OnApplyWindowInsetsListener {

    /**
     * When {@link View#setOnApplyWindowInsetsListener(View.OnApplyWindowInsetsListener) set} on a
     * View, this listener method will be called instead of the view's own
     * View#onApplyWindowInsets(WindowInsets) method. The {@code initialPadding} is the view's
     * original padding which can be updated and will be applied to the view automatically. This
     * method should return a new {@link WindowInsetsCompat} with any insets consumed.
     */
    WindowInsetsCompat onApplyWindowInsets(
        View view, WindowInsetsCompat insets, RelativePadding initialPadding);
  }
}
