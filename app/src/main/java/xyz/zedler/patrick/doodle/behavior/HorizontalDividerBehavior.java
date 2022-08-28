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

package xyz.zedler.patrick.doodle.behavior;

import android.animation.ValueAnimator;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import androidx.annotation.NonNull;

public class HorizontalDividerBehavior {

  private static final String TAG = HorizontalDividerBehavior.class.getSimpleName();
  private static final boolean DEBUG = false;

  private static final int STATE_SCROLLED_END = 1; // scrolled to the right (rtl left)
  private static final int STATE_SCROLLED_START = 2; // scrolled to the left (rtl right)
  // distance gets divided to prevent cutoff of edge effect
  private final int pufferDivider = 2;
  private int currentState = STATE_SCROLLED_START;
  // distance before top scroll when overScroll is turned off
  private int pufferSize = 0;
  private boolean isAbsoluteStartScroll = false;
  private final HorizontalScrollView scrollView;
  private final View dividerView;
  private ValueAnimator alphaAnimator;

  public HorizontalDividerBehavior(
      @NonNull HorizontalScrollView scrollView, @NonNull View dividerView) {
    this.scrollView = scrollView;
    this.dividerView = dividerView;

    if (VERSION.SDK_INT < VERSION_CODES.M) {
      // OnScrollChangeListener not available
      dividerView.setVisibility(View.GONE);
      return;
    }

    currentState = STATE_SCROLLED_START;
    measureScrollView();

    dividerView.setAlpha(0);
    scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

    scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
      if (!isAbsoluteStartScroll && scrollX == 0) { // ABSOLUTE START
        onAbsoluteStartScroll();
      } else {
        if (scrollX < oldScrollX) { // START
          if (currentState != STATE_SCROLLED_START) {
            onScrollStart();
          }
          if (scrollX < pufferSize) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
              if (scrollX > 0) {
                scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
              }
            }, 1);
          }
        } else if (scrollX > oldScrollX) {
          if (currentState != STATE_SCROLLED_END) { // END
            onScrollEnd();
          }
        }
      }
    });
  }

  private void onAbsoluteStartScroll() {
    isAbsoluteStartScroll = true;
    setDividerVisibility(false);
    if (DEBUG) {
      Log.i(TAG, "onAbsoluteStartScroll: ABSOLUTE START");
    }
  }

  private void onScrollStart() {
    currentState = STATE_SCROLLED_START;
    setDividerVisibility(true);
    if (DEBUG) {
      Log.i(TAG, "onScrollUp: START");
    }
  }

  private void onScrollEnd() {
    // second top scroll is unrealistic before down scroll
    isAbsoluteStartScroll = false;
    currentState = STATE_SCROLLED_END;
    setDividerVisibility(true);
    scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
    if (DEBUG) {
      Log.i(TAG, "onScrollEnd: END");
    }
  }

  private void measureScrollView() {
    scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            int scrollViewWidth = scrollView.getMeasuredWidth();
            if (scrollView.getChildAt(0) != null) {
              int scrollContentWidth = scrollView.getChildAt(0).getWidth();
              pufferSize = (scrollContentWidth - scrollViewWidth) / pufferDivider;
            } else if (DEBUG) {
              Log.e(TAG, "measureScrollView: no child");
            }
            // Kill ViewTreeObserver
            if (scrollView.getViewTreeObserver().isAlive()) {
              scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
          }
        });
  }

  private void setDividerVisibility(boolean visible) {
    float alpha = dividerView.getAlpha();
    if ((visible && alpha == 1) || (!visible && alpha == 0)) {
      return;
    } else if (alphaAnimator != null) {
      alphaAnimator.pause();
      alphaAnimator.cancel();
      alphaAnimator = null;
    }
    alphaAnimator = ValueAnimator.ofFloat(dividerView.getAlpha(), visible ? 1 : 0);
    alphaAnimator.addUpdateListener(
        animation -> dividerView.setAlpha((float) alphaAnimator.getAnimatedValue())
    );
    alphaAnimator.setDuration(200).start();
  }
}
