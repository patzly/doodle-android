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

package xyz.zedler.patrick.doodle.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.RoundedCorner;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.shape.ShapeAppearanceModel;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.databinding.FragmentTwoPaneBinding;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;

public class TwoPaneFragment extends BaseFragment implements OnClickListener {

  private MainActivity activity;
  private FragmentTwoPaneBinding binding;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentTwoPaneBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    activity = (MainActivity) requireActivity();

    int radius;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activity.getWindowManager() != null) {
      radius = activity.getWindowManager()
          .getCurrentWindowMetrics()
          .getWindowInsets()
          .getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
          .getRadius();
    } else {
      radius = SystemUiUtil.dpToPx(activity, 16);
    }

    ShapeAppearanceModel shapeLeft = new ShapeAppearanceModel().toBuilder()
        .setTopRightCornerSize(radius)
        .setBottomRightCornerSize(radius).build();
    ShapeAppearanceModel shapeRight = new ShapeAppearanceModel().toBuilder()
        .setTopLeftCornerSize(radius)
        .setBottomLeftCornerSize(radius).build();

    binding.cardListPane.setShapeAppearanceModel(shapeLeft);
    binding.cardDetailPane.setShapeAppearanceModel(shapeRight);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (getViewUtil().isClickDisabled(id)) {
      return;
    }
    performHapticClick();

    if (id == R.id.button_overview_info) {
      //activity.showTextBottomSheet(R.raw.information, R.string.title_info);
    } else if (id == R.id.button_overview_help) {
      //activity.showTextBottomSheet(R.raw.help, R.string.action_help);
    }/* else if (id == R.id.linear_overview_appearance) {
      navigate(OverviewFragmentDirections.actionOverviewToAppearance());
    } else if (id == R.id.linear_overview_parallax) {
      navigate(OverviewFragmentDirections.actionOverviewToParallax());
    } else if (id == R.id.linear_overview_size) {
      navigate(OverviewFragmentDirections.actionOverviewToSize());
    } else if (id == R.id.linear_overview_other) {
      navigate(OverviewFragmentDirections.actionOverviewToOther());
    } else if (id == R.id.linear_overview_about) {
      navigate(OverviewFragmentDirections.actionOverviewToAbout());
    }*/
  }

  public void openDetails(Class<? extends Fragment> fragment) {
    FragmentTransaction ft = getChildFragmentManager().beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragment_detail_pane, fragment, null);
    if (binding.slidingPane.isOpen()) {
      ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      ft.setCustomAnimations(R.anim.fade_out, android.R.anim.fade_in);
    }
    ft.commit();
    binding.slidingPane.open();
  }

  public boolean isSinglePane() {
    if (binding != null) {
      return binding.slidingPane.isSlideable();
    } else {
      return true;
    }
  }

  public float getListCenter() {
    if (binding != null) {
      return binding.fragmentListPane.getLeft() + binding.fragmentListPane.getMeasuredWidth() / 2f;
    } else {
      return 0;
    }
  }
}
