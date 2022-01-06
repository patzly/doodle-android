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

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.elevation.SurfaceColors;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

  private static final String TAG = "BaseBottomSheet";

  private MainActivity activity;
  private Dialog dialog;
  private View decorView;
  private ViewUtil viewUtil;
  private boolean isExpanded;
  private boolean lightNavBar;
  private int backgroundColor;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = (MainActivity) requireActivity();
    viewUtil = new ViewUtil();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    dialog = new BottomSheetDialog(requireContext());

    decorView = dialog.getWindow().getDecorView();
    if (decorView == null) {
      return dialog;
    }

    decorView.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            ViewGroup container = dialog.findViewById(R.id.container);
            View sheet = dialog.findViewById(R.id.design_bottom_sheet);
            if (container == null || sheet == null) {
              return;
            }

            container.setClipChildren(false);
            container.setClipToPadding(false);

            backgroundColor = SurfaceColors.SURFACE_1.getColor(activity);
            PaintDrawable background = new PaintDrawable(backgroundColor);
            int radius = SystemUiUtil.dpToPx(requireContext(), 16);
            setCornerRadius(background, radius);
            sheet.setBackground(background);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setPeekHeight(
                SystemUiUtil.getDisplayHeight(requireContext()) / 2
                + SystemUiUtil.dpToPx(activity, 64) // height of bottom sheet top bar
            );

            boolean keepBelowStatusBar =
                SystemUiUtil.getDisplayWidth(requireContext()) > behavior.getMaxWidth();

            ViewCompat.setOnApplyWindowInsetsListener(decorView, (view, insets) -> {
              int insetTop = insets.getInsets(Type.systemBars()).top;
              int insetBottom = insets.getInsets(Type.systemBars()).bottom;

              layoutEdgeToEdge(dialog.getWindow(), insetBottom);

              if (lightNavBar) {
                // Below API 30 it does not work for non-gesture if we take the normal method
                SystemUiUtil.setLightNavigationBar(dialog.getWindow(), sheet);
              }

              applyBottomInset(insetBottom);

              behavior.addBottomSheetCallback(new BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                  isExpanded = newState == BottomSheetBehavior.STATE_EXPANDED;

                  if (bottomSheet.getTop() < insetTop) {
                    if (!keepBelowStatusBar) {
                      float fraction = (float) bottomSheet.getTop() / (float) insetTop;
                      setCornerRadius(background, radius * fraction);
                    }
                  } else if (bottomSheet.getTop() != 0) {
                    if (!keepBelowStatusBar) {
                      setCornerRadius(background, radius);
                    }
                  }
                  if (keepBelowStatusBar) {
                    // Undo padding applied by BottomSheetBehavior and add container padding
                    sheet.setPadding(0, 0, 0, 0);
                    container.setPadding(
                        0, insetTop, 0, 0
                    );
                  }
                  removeWeirdBottomPadding(sheet, container);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                  if (bottomSheet.getTop() < insetTop) {
                    if (!keepBelowStatusBar) {
                      float fraction = (float) bottomSheet.getTop() / (float) insetTop;
                      setCornerRadius(background, radius * fraction);
                    }
                  } else if (bottomSheet.getTop() != 0) {
                    if (!keepBelowStatusBar) {
                      setCornerRadius(background, radius);
                    }
                  }
                  if (keepBelowStatusBar) {
                    // Undo padding applied by BottomSheetBehavior and add container padding
                    sheet.setPadding(0, 0, 0, 0);
                    container.setPadding(
                        0, insetTop, 0, 0
                    );
                  }
                  removeWeirdBottomPadding(sheet, container);
                }
              });
              if (isExpanded && !keepBelowStatusBar) {
                // Layout behind status bar if it was there before screen rotation
                sheet.setPadding(
                    sheet.getPaddingLeft(),
                    insetTop - sheet.getTop(),
                    sheet.getPaddingRight(),
                    0
                );
              }
              // Ugly, but we have to remove the padding after it is applied by the behavior
              new Handler(Looper.getMainLooper()).postDelayed(
                  () -> removeWeirdBottomPadding(sheet, container),
                  10
              );
              return insets;
            });

            removeWeirdBottomPadding(sheet, container);

            if (decorView.getViewTreeObserver().isAlive()) {
              ViewUtil.removeOnGlobalLayoutListener(decorView, this);
            }
          }
        });

    return dialog;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean("expanded", isExpanded);
  }

  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      isExpanded = savedInstanceState.getBoolean("expanded", false);
    }
  }

  /**
   * Fix weird behavior of BottomSheetBehavior Bottom padding is applied although we have turned
   * that off in the theme... We have to apply it manually to the scroll content container, so we
   * remove it here again
   */
  private void removeWeirdBottomPadding(View... views) {
    for (View view : views) {
      view.setPadding(0, view.getPaddingTop(), 0, 0);
    }
  }

  private void setCornerRadius(PaintDrawable drawable, float radius) {
    drawable.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
  }

  private void layoutEdgeToEdge(Window window, int insetBottom) {
    boolean isOrientationPortraitOrNavAtBottom =
        SystemUiUtil.isOrientationPortrait(requireContext()) || insetBottom > 0;
    boolean isDarkModeActive = SystemUiUtil.isDarkModeActive(requireContext());

    int colorScrim = ColorUtils.setAlphaComponent(backgroundColor, (int) (0.7f * 255));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 29
      window.setStatusBarColor(Color.TRANSPARENT);
      if (SystemUiUtil.isNavigationModeGesture(requireContext())) {
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setNavigationBarContrastEnforced(true);
      } else {
        lightNavBar = !isDarkModeActive && isOrientationPortraitOrNavAtBottom;
        if (isOrientationPortraitOrNavAtBottom) {
          window.setNavigationBarColor(colorScrim);
        } else {
          window.setNavigationBarColor(
              isDarkModeActive ? SystemUiUtil.SCRIM_DARK_DIALOG : SystemUiUtil.SCRIM_LIGHT_DIALOG
          );
          window.setNavigationBarDividerColor(ResUtil.getColorOutlineSecondary(activity));
        }
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 28
      window.setStatusBarColor(Color.TRANSPARENT);
      lightNavBar = !isDarkModeActive && isOrientationPortraitOrNavAtBottom;
      if (isOrientationPortraitOrNavAtBottom) {
        window.setNavigationBarColor(colorScrim);
      } else {
        window.setNavigationBarColor(
            isDarkModeActive ? SystemUiUtil.SCRIM_DARK_DIALOG : SystemUiUtil.SCRIM_LIGHT_DIALOG
        );
        window.setNavigationBarDividerColor(ResUtil.getColorOutlineSecondary(activity));
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 26
      window.setStatusBarColor(Color.TRANSPARENT);
      lightNavBar = !isDarkModeActive && isOrientationPortraitOrNavAtBottom;
      if (isOrientationPortraitOrNavAtBottom) {
        window.setNavigationBarColor(colorScrim);
      } else {
        window.setNavigationBarColor(
            isDarkModeActive ? SystemUiUtil.SCRIM_DARK_DIALOG : SystemUiUtil.SCRIM_LIGHT_DIALOG
        );
      }
    } else  { // down to 21
      if (isOrientationPortraitOrNavAtBottom) {
        window.setNavigationBarColor(isDarkModeActive ? colorScrim : SystemUiUtil.SCRIM);
      } else {
        window.setNavigationBarColor(
            isDarkModeActive ? SystemUiUtil.SCRIM_DARK_DIALOG : SystemUiUtil.SCRIM_LIGHT_DIALOG
        );
      }
    }
  }

  public SharedPreferences getSharedPrefs() {
    return activity.getSharedPrefs();
  }

  public ViewUtil getViewUtil() {
    return viewUtil;
  }

  public void performHapticClick() {
    activity.performHapticClick();
  }

  public void performHapticHeavyClick() {
    activity.performHapticHeavyClick();
  }

  public void applyBottomInset(int bottom) {
  }
}
