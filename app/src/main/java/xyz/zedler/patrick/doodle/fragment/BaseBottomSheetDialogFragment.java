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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
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
import xyz.zedler.patrick.doodle.util.UnitUtil;

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
            int radius = UnitUtil.getDp(requireContext(), 16);
            setCornerRadius(background, radius);
            sheet.setBackground(background);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setPeekHeight(
                UnitUtil.getDisplayHeight(
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
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                  if (bottomSheet.getTop() < insetTop) {
                    float fraction = (float) bottomSheet.getTop() / (float) insetTop;
                    setCornerRadius(background, radius * fraction);
                    // Fix awkward bottom inset applied by BottomSheetBehavior
                    sheet.setPadding(
                        sheet.getPaddingLeft(),
                        sheet.getPaddingTop(),
                        sheet.getPaddingRight(),
                        0
                    );
                  } else if (bottomSheet.getTop() != 0) {
                    setCornerRadius(background, radius);
                  }
                }
              });
              if (isExpanded) {
                sheet.setPadding(
                    sheet.getPaddingLeft(),
                    insetTop - sheet.getTop(),
                    sheet.getPaddingRight(),
                    0
                );
              }
              return insets;
            });

            if (decorView.getViewTreeObserver().isAlive()) {
              decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
          }
        });

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

  private void setCornerRadius(PaintDrawable drawable, float radius) {
    drawable.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
  }

  private void layoutEdgeToEdge(Window window) {
    int statusBarColor = VERSION.SDK_INT < VERSION_CODES.M
        ? SystemUiUtil.COLOR_SCRIM
        : Color.TRANSPARENT;
    int navbarColor = VERSION.SDK_INT < VERSION_CODES.O_MR1
        ? SystemUiUtil.COLOR_SCRIM
        : Color.TRANSPARENT;

    boolean lightBg = isColorLight(ContextCompat.getColor(requireContext(), R.color.surface));
    boolean lightNavbar = isColorLight(navbarColor);
    boolean showDarkNavbarIcons = lightNavbar || (navbarColor == Color.TRANSPARENT && lightBg);

    int currentStatusBar = VERSION.SDK_INT >= VERSION_CODES.M
        ? decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        : 0;
    int currentNavBar = showDarkNavbarIcons && VERSION.SDK_INT >= VERSION_CODES.O
        ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        : 0;

    window.setNavigationBarColor(navbarColor);
    window.setStatusBarColor(statusBarColor);

    decorView.setSystemUiVisibility(
        (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            | currentStatusBar
            | currentNavBar
    );
  }

  private static boolean isColorLight(@ColorInt int color) {
    return color != Color.TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5;
  }

  public void applyBottomInset(int bottom) {}
}
