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

package xyz.zedler.patrick.doodle.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

  private final static String TAG = "BaseBottomSheet";

  private View decorView;
  private boolean isExpanded;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Dialog dialog = new BottomSheetDialog(requireContext());

    decorView = dialog.getWindow().getDecorView();
    if (decorView == null) {
      return dialog;
    }

    decorView.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            View sheet = dialog.findViewById(R.id.design_bottom_sheet);
            if (sheet == null) {
              return;
            }

            PaintDrawable background = new PaintDrawable(
                ContextCompat.getColor(requireContext(), R.color.surface)
            );
            int radius = SystemUiUtil.dpToPx(requireContext(), 16);
            setCornerRadius(background, radius);
            sheet.setBackground(background);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setPeekHeight(
                SystemUiUtil.getDisplayHeight(
                    (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE)
                ) / 2
            );

            ViewCompat.setOnApplyWindowInsetsListener(decorView, (view, insets) -> {
              int insetTop = insets.getInsets(Type.systemBars()).top;
              applyBottomInset(insets.getInsets(Type.systemBars()).bottom);
              behavior.addBottomSheetCallback(new BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                  isExpanded = newState == BottomSheetBehavior.STATE_EXPANDED;
                  removeWeirdBottomPadding(sheet);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                  if (bottomSheet.getTop() < insetTop) {
                    float fraction = (float) bottomSheet.getTop() / (float) insetTop;
                    setCornerRadius(background, radius * fraction);
                  } else if (bottomSheet.getTop() != 0) {
                    setCornerRadius(background, radius);
                  }
                  removeWeirdBottomPadding(sheet);
                }
              });
              if (isExpanded) {
                // Layout below status bar if it was there before screen rotation
                sheet.setPadding(
                    sheet.getPaddingLeft(),
                    insetTop - sheet.getTop(),
                    sheet.getPaddingRight(),
                    0
                );
              }
              // Ugly, but we have to remove the padding after it is applied by the behavior
              new Handler(Looper.getMainLooper()).postDelayed(
                  () -> removeWeirdBottomPadding(sheet),
                  50
              );
              return insets;
            });

            if (decorView.getViewTreeObserver().isAlive()) {
              ViewUtil.removeOnGlobalLayoutListener(decorView, this);
            }
          }
        });

    // TODO: bottom sheet blinks on slower devices before it fades in

    layoutEdgeToEdge(dialog.getWindow());

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
   * Fix weird behavior of BottomSheetBehavior
   * Bottom padding is applied although we have turned that off in the theme...
   * We have to apply it manually to the scroll content container
   * So we remove it here again
   */
  private void removeWeirdBottomPadding(View sheet) {
    sheet.setPadding(
        sheet.getPaddingLeft(), sheet.getPaddingTop(), sheet.getPaddingRight(), 0
    );
  }

  private void setCornerRadius(PaintDrawable drawable, float radius) {
    drawable.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
  }

  private void layoutEdgeToEdge(Window window) {
    SystemUiUtil.layoutEdgeToEdge(window);

    boolean isOrientationPortrait = SystemUiUtil.isOrientationPortrait(requireContext());
    boolean isDarkModeActive = SystemUiUtil.isDarkModeActive(requireContext());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 29
      window.setStatusBarColor(Color.TRANSPARENT);
      if (SystemUiUtil.isNavigationModeGesture(requireContext())) {
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setNavigationBarContrastEnforced(true);
      } else {
        if (!isDarkModeActive) {
          SystemUiUtil.setLightNavigationBar(window);
        }
        if (isOrientationPortrait) {
          window.setNavigationBarColor(
              isDarkModeActive
                  ? SystemUiUtil.COLOR_SCRIM_DARK_SURFACE
                  : SystemUiUtil.COLOR_SCRIM_LIGHT
          );
        } else {
          window.setNavigationBarDividerColor(
              ContextCompat.getColor(requireContext(), R.color.stroke_secondary)
          );
          window.setNavigationBarColor(
              ContextCompat.getColor(requireContext(), R.color.background)
          );
        }
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 28
      window.setStatusBarColor(Color.TRANSPARENT);
      if (!isDarkModeActive) {
        SystemUiUtil.setLightNavigationBar(window);
      }
      if (isOrientationPortrait) {
        window.setNavigationBarColor(
            isDarkModeActive
                ? SystemUiUtil.COLOR_SCRIM_DARK_SURFACE
                : SystemUiUtil.COLOR_SCRIM_LIGHT
        );
      } else {
        window.setNavigationBarDividerColor(
            ContextCompat.getColor(requireContext(), R.color.stroke_secondary)
        );
        window.setNavigationBarColor(ContextCompat.getColor(requireContext(), R.color.background));
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 26
      window.setStatusBarColor(Color.TRANSPARENT);
      if (isOrientationPortrait) {
        window.setNavigationBarColor(
            isDarkModeActive
                ? SystemUiUtil.COLOR_SCRIM_DARK_SURFACE
                : SystemUiUtil.COLOR_SCRIM_LIGHT
        );
        if (!isDarkModeActive) {
          SystemUiUtil.setLightNavigationBar(window);
        }
      } else {
        window.setNavigationBarColor(Color.BLACK);
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23
      window.setStatusBarColor(Color.TRANSPARENT);
      if (isOrientationPortrait) {
        window.setNavigationBarColor(
            isDarkModeActive
                ? SystemUiUtil.COLOR_SCRIM_DARK_SURFACE
                : SystemUiUtil.COLOR_SCRIM
        );
      } else {
        window.setNavigationBarColor(Color.BLACK);
      }
    }
  }

  private static boolean isColorLight(@ColorInt int color) {
    return color != Color.TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5;
  }

  public void applyBottomInset(int bottom) {}
}
