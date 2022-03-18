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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentOverviewBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class OverviewFragment extends BaseFragment implements OnClickListener {

  private static final String TAG = OverviewFragment.class.getSimpleName();

  private FragmentOverviewBinding binding;
  private MainActivity activity;
  private ViewUtil viewUtilLogo;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentOverviewBinding.inflate(inflater, container, false);
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

    viewUtilLogo = new ViewUtil(1010);

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarOverview);
    systemBarBehavior.setScroll(binding.scrollOverview, binding.coordinatorOverviewContainer);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarOverview, binding.scrollOverview, true
    );

    binding.toolbarOverview.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_feedback) {
        activity.showFeedbackBottomSheet();
      } else if (id == R.id.action_help) {
        activity.showTextBottomSheet(R.raw.help, R.string.action_help);
      } else if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
      }
      performHapticClick();
      return true;
    });

    boolean shouldLogoBeVisible = activity.shouldLogoBeVisibleOnOverviewPage();

    binding.frameOverviewClose.setVisibility(shouldLogoBeVisible ? View.GONE : View.VISIBLE);
    binding.frameOverviewLogo.setVisibility(shouldLogoBeVisible ? View.VISIBLE : View.GONE);
    if (activity.shouldLogoBeVisibleOnOverviewPage()) {
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        binding.appBarOverview.setOnClickListener(v -> {
          if (viewUtilLogo.isClickEnabled()) {
            ViewUtil.startIcon(binding.imageOverviewLogo);
            performHapticClick();
          }
        });
      }
    } else {
      binding.frameOverviewClose.setOnClickListener(v -> {
        if (getViewUtil().isClickEnabled()) {
          performHapticClick();
          activity.finish();
        }
      });
    }

    ViewUtil.setOnClickListeners(
        this,
        binding.cardOverviewInfo,
        binding.linearOverviewAppearance,
        binding.linearOverviewParallax,
        binding.linearOverviewSize,
        binding.linearOverviewOther,
        binding.linearOverviewAbout
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.card_overview_info && getViewUtil().isClickEnabled()) {
      activity.showTextBottomSheet(R.raw.information, R.string.title_info);
      performHapticClick();
    } else if (id == R.id.linear_overview_appearance && getViewUtil().isClickEnabled()) {
      navigate(OverviewFragmentDirections.actionOverviewToAppearance());
      performHapticClick();
    } else if (id == R.id.linear_overview_parallax && getViewUtil().isClickEnabled()) {
      navigate(OverviewFragmentDirections.actionOverviewToParallax());
      performHapticClick();
    } else if (id == R.id.linear_overview_size && getViewUtil().isClickEnabled()) {
      navigate(OverviewFragmentDirections.actionOverviewToSize());
      performHapticClick();
    } else if (id == R.id.linear_overview_other && getViewUtil().isClickEnabled()) {
      navigate(OverviewFragmentDirections.actionOverviewToOther());
      performHapticClick();
    } else if (id == R.id.linear_overview_about && getViewUtil().isClickEnabled()) {
      navigate(OverviewFragmentDirections.actionOverviewToAbout());
      performHapticClick();
    }
  }
}