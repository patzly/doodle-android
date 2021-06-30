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

package xyz.zedler.patrick.doodle.behavior;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.appbar.AppBarLayout;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;

public class SystemBarBehavior {

  private final static String TAG = SystemBarBehavior.class.getSimpleName();

  private final Activity activity;
  private final Window window;
  int containerPaddingTop;
  int containerPaddingBottom;
  int scrollContentPaddingBottom;
  private AppBarLayout appBarLayout;
  private ViewGroup container;
  private NestedScrollView scrollView;
  private ViewGroup scrollContent;
  private boolean applyAppBarInsetOnContainer;
  private boolean hasScrollView;
  private boolean isScrollable;

  public SystemBarBehavior(@NonNull Activity activity) {
    this.activity = activity;
    window = activity.getWindow();

    // GOING EDGE TO EDGE
    SystemUiUtil.layoutEdgeToEdge(window);

    applyAppBarInsetOnContainer = true;
    hasScrollView = false;
    isScrollable = false;
  }

  public void setAppBar(AppBarLayout appBarLayout) {
    this.appBarLayout = appBarLayout;
  }

  public void setContainer(ViewGroup container) {
    this.container = container;
    containerPaddingTop = container.getPaddingTop();
    containerPaddingBottom = container.getPaddingBottom();
  }

  public void setScroll(@NonNull NestedScrollView scrollView, @NonNull ViewGroup scrollContent) {
    this.scrollView = scrollView;
    this.scrollContent = scrollContent;
    scrollContentPaddingBottom = scrollContent.getPaddingBottom();
    hasScrollView = true;

    if (container == null) {
      setContainer(scrollView);
    }
  }

  public void setUp() {
    // TOP INSET
    if (appBarLayout != null) {
      ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
        // STATUS BAR INSET
        appBarLayout.setPadding(
            0, insets.getInsets(Type.systemBars()).top, 0,
            appBarLayout.getPaddingBottom()
        );
        appBarLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // APP BAR INSET
        if (container != null && applyAppBarInsetOnContainer) {
          ViewGroup.MarginLayoutParams params
              = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
          params.topMargin = appBarLayout.getMeasuredHeight();
          container.setLayoutParams(params);
        } else if (container != null) {
          container.setPadding(
              container.getPaddingLeft(),
              containerPaddingTop + insets.getInsets(Type.systemBars()).top,
              container.getPaddingRight(),
              container.getPaddingBottom()
          );
        }
        return insets;
      });
    } else if (container != null) {
      // if no app bar exists, status bar inset is applied to container
      ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
        // STATUS BAR INSET
        container.setPadding(
            container.getPaddingLeft(),
            containerPaddingTop + insets.getInsets(Type.systemBars()).top,
            container.getPaddingRight(),
            container.getPaddingBottom()
        );
        return insets;
      });
    }

    // NAV BAR INSET
    if (SystemUiUtil.isOrientationPortrait(activity) && hasContainer()) {
      View container = hasScrollView ? scrollContent : this.container;
      ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
        int paddingBottom = hasScrollView
            ? scrollContentPaddingBottom
            : containerPaddingBottom;
        container.setPadding(
            container.getPaddingLeft(),
            container.getPaddingTop(),
            container.getPaddingRight(),
            paddingBottom + insets.getInsets(Type.systemBars()).bottom
        );
        return insets;
      });
    } else {
      if (SystemUiUtil.isNavigationModeGesture(activity) && hasContainer()) {
        View container = hasScrollView ? scrollContent : this.container;
        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
          int paddingBottom = hasScrollView
              ? scrollContentPaddingBottom
              : containerPaddingBottom;
          container.setPadding(
              container.getPaddingLeft(),
              container.getPaddingTop(),
              container.getPaddingRight(),
              paddingBottom + insets.getInsets(Type.systemBars()).bottom
          );
          return insets;
        });
      } else {
        View root = window.getDecorView().findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
          root.setPadding(
              root.getPaddingLeft(),
              root.getPaddingTop(),
              insets.getInsets(Type.systemBars()).right,
              root.getPaddingBottom()
          );
          return insets;
        });
      }
    }

    if (hasScrollView) {
      // call viewThreeObserver, this updates the system bar appearance
      measureScrollView();
    } else {
      // call directly because there won't be any changes caused by scroll content
      updateSystemBars();
    }
  }

  private void measureScrollView() {
    scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            int scrollViewHeight = scrollView.getMeasuredHeight();
            int scrollContentHeight = scrollContent.getHeight();
            isScrollable = scrollViewHeight - scrollContentHeight < 0;
            updateSystemBars();
            // Kill ViewTreeObserver
            if (scrollView.getViewTreeObserver().isAlive()) {
              scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(
                  this
              );
            }
          }
        });
  }

  public void applyAppBarInsetOnContainer(boolean apply) {
    applyAppBarInsetOnContainer = apply;
  }

  private void updateSystemBars() {
    boolean isOrientationPortrait = SystemUiUtil.isOrientationPortrait(activity);
    boolean isDarkModeActive = SystemUiUtil.isDarkModeActive(activity);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 29
      window.setStatusBarColor(Color.TRANSPARENT);
      if (!isDarkModeActive) {
        SystemUiUtil.setLightStatusBar(window);
      }
      if (SystemUiUtil.isNavigationModeGesture(activity)) {
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setNavigationBarContrastEnforced(true);
      } else {
        if (!isDarkModeActive) {
          SystemUiUtil.setLightNavigationBar(window);
        }
        if (isOrientationPortrait) {
          window.setNavigationBarColor(
              isScrollable
                  ? (isDarkModeActive
                      ? SystemUiUtil.SCRIM_DARK
                      : SystemUiUtil.SCRIM_LIGHT)
                  : Color.parseColor("#01000000")
          );
        } else {
          window.setNavigationBarDividerColor(
              ContextCompat.getColor(activity, R.color.stroke_secondary)
          );
          window.setNavigationBarColor(
              ContextCompat.getColor(activity, R.color.background)
          );
        }
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 28
      window.setStatusBarColor(Color.TRANSPARENT);
      if (!isDarkModeActive) {
        SystemUiUtil.setLightStatusBar(window);
        SystemUiUtil.setLightNavigationBar(window);
      }
      if (isOrientationPortrait) {
        window.setNavigationBarColor(
            isScrollable
                ? (isDarkModeActive
                    ? SystemUiUtil.SCRIM_DARK
                    : SystemUiUtil.SCRIM_LIGHT)
                : Color.TRANSPARENT
        );
      } else {
        window.setNavigationBarDividerColor(
            ContextCompat.getColor(activity, R.color.stroke_secondary)
        );
        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.background));
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 26
      window.setStatusBarColor(Color.TRANSPARENT);
      if (!isDarkModeActive) {
        SystemUiUtil.setLightStatusBar(window);
      }
      if (isOrientationPortrait) {
        window.setNavigationBarColor(
            isScrollable
                ? (isDarkModeActive
                    ? SystemUiUtil.SCRIM_DARK
                    : SystemUiUtil.SCRIM_LIGHT)
                : Color.TRANSPARENT
        );
        if (!isDarkModeActive) {
          SystemUiUtil.setLightNavigationBar(window);
        }
      } else {
        window.setNavigationBarColor(
            isDarkModeActive
                ? SystemUiUtil.SCRIM_DARK
                : SystemUiUtil.SCRIM
        );
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23
      window.setStatusBarColor(Color.TRANSPARENT);
      if (!isDarkModeActive) {
        SystemUiUtil.setLightStatusBar(window);
      }
      if (isOrientationPortrait) {
        window.setNavigationBarColor(
            isDarkModeActive
                ? (isScrollable ? SystemUiUtil.SCRIM_DARK : Color.TRANSPARENT)
                : SystemUiUtil.SCRIM
        );
      } else {
        window.setNavigationBarColor(
            isDarkModeActive
                ? SystemUiUtil.SCRIM_DARK
                : SystemUiUtil.SCRIM
        );
      }
    }
  }

  private boolean hasContainer() {
    return container != null;
  }
}
