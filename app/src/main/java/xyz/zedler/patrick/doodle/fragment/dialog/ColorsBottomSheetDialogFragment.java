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

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.elevation.SurfaceColors;
import java.util.Objects;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetColorsBinding;
import xyz.zedler.patrick.doodle.fragment.AppearanceFragment;
import xyz.zedler.patrick.doodle.fragment.dialog.ColorPickerDialog.OnSelectListener;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.SelectionCardView;

public class ColorsBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

  private static final String TAG = "ColorsBottomSheet";

  private FragmentBottomsheetColorsBinding binding;
  private MainActivity activity;
  private ColorPickerDialog dialogColorPicker;
  private int colorCustom;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    binding = FragmentBottomsheetColorsBinding.inflate(inflater, container, false);

    activity = (MainActivity) requireActivity();

    ColorsBottomSheetDialogFragmentArgs args
        = ColorsBottomSheetDialogFragmentArgs.fromBundle(getArguments());

    binding.toolbarColors.setTitle(getString(args.getTitle()));

    String[] colors = args.getColors().split(" ");

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
      boolean isSelected = Objects.equals(args.getSelection(), colors[i]);
      card.setChecked(isSelected);
      binding.linearColorsContainerColors.addView(card);
    }

    MaterialDivider divider = new MaterialDivider(activity);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        SystemUiUtil.dpToPx(activity, 1), SystemUiUtil.dpToPx(activity, 40)
    );
    int marginLeft, marginRight;
    if (ResUtil.isLayoutRtl(activity)) {
      marginLeft = SystemUiUtil.dpToPx(activity, 8);
      marginRight = SystemUiUtil.dpToPx(activity, 4);
    } else {
      marginLeft = SystemUiUtil.dpToPx(activity, 4);
      marginRight = SystemUiUtil.dpToPx(activity, 8);
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
      OnSelectListener listener = new OnSelectListener() {
        @Override
        public void onSelect(ColorPickerDialog dialog, int color) {
          color = color & 0xFFFFFF;
          colorCustom = color;
          String colorString = String.format("#%06X", color);
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
        }

        @Override
        public void onCancel() {
          performHapticClick();
        }
      };
      dialogColorPicker = new ColorPickerDialog(requireContext(), colorCustom, listener);
      dialogColorPicker.show();
    });
    boolean isSelected = Objects.equals(
      args.getSelection(), String.format("#%06X", (colorCustom & 0xFFFFFF))
    );
    // TODO: is not selected anymore if custom is new selection after screen rotation
    card.setChecked(isSelected);
    binding.linearColorsContainerColors.addView(card);

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (dialogColorPicker != null) {
      // Else it throws an leak exception because the context is somehow from the activity
      // TODO: should be still opened after screen rotation
      dialogColorPicker.dismiss();
    }
    binding = null;
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
