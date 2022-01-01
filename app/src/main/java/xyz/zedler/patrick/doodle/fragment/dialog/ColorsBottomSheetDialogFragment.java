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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.elevation.SurfaceColors;
import java.util.Objects;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetColorsBinding;
import xyz.zedler.patrick.doodle.fragment.AppearanceFragment;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.SelectionCardView;

public class ColorsBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

  private static final String TAG = "ColorsBottomSheet";

  private FragmentBottomsheetColorsBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    binding = FragmentBottomsheetColorsBinding.inflate(inflater, container, false);

    activity = (MainActivity) requireActivity();

    ColorsBottomSheetDialogFragmentArgs args
        = ColorsBottomSheetDialogFragmentArgs.fromBundle(getArguments());

    binding.toolbarColors.setTitle(getString(args.getTitle()));

    String[] colors = args.getColors().split(" ");

    for (int i = 0; i < colors.length; i++) {
      final int iFinal = i;
      SelectionCardView card = new SelectionCardView(activity);
      card.setOuterCardBackgroundColor(SurfaceColors.SURFACE_2.getColor(activity));
      card.setCardBackgroundColor(Color.parseColor(colors[iFinal]));
      card.setOnClickListener(v -> {
        if (!card.isChecked()) {
          card.startCheckedIcon();
          performHapticClick();
          ViewUtil.uncheckAllChildren(binding.linearColorsContainerColors);
          card.setChecked(true);
          Fragment current = activity.getCurrentFragment();
          if (current instanceof AppearanceFragment) {
            ((AppearanceFragment) current).setColor(args.getPriority(), colors[iFinal]);
          }
        }
      });
      boolean isSelected = Objects.equals(args.getSelection(), colors[i]);
      card.setChecked(isSelected);
      binding.linearColorsContainerColors.addView(card);
    }

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
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
