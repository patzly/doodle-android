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
 * Copyright (c) 2019-2023 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.elevation.SurfaceColors;
import java.util.Objects;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetColorsBinding;
import xyz.zedler.patrick.doodle.fragment.AppearanceFragment;
import xyz.zedler.patrick.doodle.util.UiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.ColorPickerView;
import xyz.zedler.patrick.doodle.view.SelectionCardView;

public class ColorsBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

  private static final String TAG = "ColorsBottomSheet";

  private static final String PICKER_SHOWING = "is_picker_showing";

  private FragmentBottomsheetColorsBinding binding;
  private MainActivity activity;
  private ColorsBottomSheetDialogFragmentArgs args;
  private AlertDialog dialogCustomColor;
  private ColorPickerView colorPicker;
  private int colorCustom;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    binding = FragmentBottomsheetColorsBinding.inflate(inflater, container, false);
    args = ColorsBottomSheetDialogFragmentArgs.fromBundle(getArguments());

    activity = (MainActivity) requireActivity();

    binding.toolbarColors.setTitle(getString(args.getTitle()));

    String[] colors = args.getColors().split(" ");
    String selection = getSharedPrefs().getString(
        args.getThemeColorPref(), args.getThemeColorPrefDef()
    );

    boolean isPresetSelected = false;

    for (int i = 0; i < colors.length; i++) {
      int index = i;
      SelectionCardView card = new SelectionCardView(activity);
      card.setOuterCardBackgroundColor(SurfaceColors.SURFACE_5.getColor(activity));
      card.setCardBackgroundColor(Color.parseColor(colors[index]));
      card.setScrimEnabled(false, true);
      card.setOnClickListener(v -> {
        if (!card.isChecked()) {
          card.startCheckedIcon();
          performHapticClick();
          ViewUtil.uncheckAllChildren(binding.linearColorsContainerColors);
          card.setChecked(true);
          Fragment current = activity.getCurrentFragment();
          if (current instanceof AppearanceFragment) {
            ((AppearanceFragment) current).setColor(
                args.getPriority(), colors[index], false
            );
          }
        }
      });
      boolean isSelected = Objects.equals(selection, colors[i]);
      card.setChecked(isSelected);
      if (!isPresetSelected && isSelected) {
        isPresetSelected = true;
      }
      binding.linearColorsContainerColors.addView(card);
    }

    MaterialDivider divider = new MaterialDivider(activity);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        UiUtil.dpToPx(activity, 1), UiUtil.dpToPx(activity, 40)
    );
    int marginLeft, marginRight;
    if (UiUtil.isLayoutRtl(activity)) {
      marginLeft = UiUtil.dpToPx(activity, 8);
      marginRight = UiUtil.dpToPx(activity, 4);
    } else {
      marginLeft = UiUtil.dpToPx(activity, 4);
      marginRight = UiUtil.dpToPx(activity, 8);
    }
    layoutParams.setMargins(marginLeft, 0, marginRight, 0);
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    divider.setLayoutParams(layoutParams);
    binding.linearColorsContainerColors.addView(divider);

    SelectionCardView card = new SelectionCardView(activity);
    card.setOuterCardBackgroundColor(SurfaceColors.SURFACE_5.getColor(activity));
    Fragment current = activity.getCurrentFragment();
    if (current instanceof AppearanceFragment) {
      String colorCode = ((AppearanceFragment) current).getColor(args.getPriority(), true);
      colorCustom = Color.parseColor(colorCode);
      card.setCardBackgroundColor(colorCustom);
      card.setScrimEnabled(false, true);
    }
    card.setOnClickListener(v -> {
      performHapticClick();
      openColorPickerDialog(false, 0);
    });
    boolean isSelected = Objects.equals(selection, String.format("#%06X", colorCustom & 0xFFFFFF));
    if (!isPresetSelected) {
      // only mark custom color as selected if no preset color matches
      card.setChecked(isSelected);
    }
    binding.linearColorsContainerColors.addView(card);

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (dialogCustomColor != null) {
      // Else it throws an leak exception because the context is somehow from the activity
      dialogCustomColor.dismiss();
    }
    binding = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      if (savedInstanceState.getBoolean(PICKER_SHOWING)) {
        int colorNew = savedInstanceState.getInt(EXTRA.COLOR);
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> openColorPickerDialog(true, colorNew), 1
        );
      }
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    boolean isShowing = dialogCustomColor != null && dialogCustomColor.isShowing();
    outState.putBoolean(PICKER_SHOWING, isShowing);
    if (isShowing) {
      outState.putInt(EXTRA.COLOR, colorPicker.getColor());
    }
  }

  private void openColorPickerDialog(boolean isSavedState, int colorNew) {
    colorPicker = new ColorPickerView(activity);
    if (isSavedState) {
      colorPicker.setColor(colorCustom, colorNew);
    } else {
      colorPicker.setColor(colorCustom);
    }
    dialogCustomColor = new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.appearance_colors_custom)
        .setPositiveButton(R.string.action_select, (dialog, which) -> {
          if (binding == null) {
            return;
          }
          colorCustom = colorPicker.getColor() & 0xFFFFFF;
          String colorString = String.format("#%06X", colorCustom);
          SelectionCardView card =
              (SelectionCardView) binding.linearColorsContainerColors.getChildAt(
                  binding.linearColorsContainerColors.getChildCount() - 1
              );
          card.setCardBackgroundColor(Color.parseColor(colorString));
          card.setScrimEnabled(false, true);
          ViewUtil.uncheckAllChildren(binding.linearColorsContainerColors);
          card.setChecked(true);
          card.startCheckedIcon();
          performHapticClick();
          Fragment currentCustom = activity.getCurrentFragment();
          if (currentCustom instanceof AppearanceFragment) {
            ((AppearanceFragment) currentCustom).setColor(
                args.getPriority(), colorString, true
            );
          }
        }).setNegativeButton(R.string.action_cancel, (dialog, which) -> performHapticClick())
        .setOnCancelListener(dialog -> performHapticClick())
        .setView(colorPicker)
        .create();
    dialogCustomColor.show();
  }

  @Override
  public void applyBottomInset(int bottom) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, bottom);
    binding.linearColorsContainer.setLayoutParams(params);
  }

  @NonNull
  @Override
  public String toString() {
    return TAG;
  }
}
