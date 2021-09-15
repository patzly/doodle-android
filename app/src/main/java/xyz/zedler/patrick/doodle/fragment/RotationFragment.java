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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnChangeListener;
import java.util.Locale;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentRotationBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class RotationFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener, OnChangeListener {

  private FragmentRotationBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentRotationBinding.inflate(inflater, container, false);
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

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarRotation);
    systemBarBehavior.setScroll(binding.scrollRotation, binding.linearRotationContainer);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarRotation, binding.scrollRotation, true
    );

    binding.toolbarRotation.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
        performHapticClick();
        return true;
      } else if (id == R.id.action_feedback) {
        performHapticClick();
        getNavController().navigate(RotationFragmentDirections.actionRotationToFeedbackDialog());
        return true;
      } else {
        return false;
      }
    });

    binding.sliderRotationDegrees.setValue(getSharedPrefs().getInt(PREF.ROTATION, DEF.ROTATION));
    binding.sliderRotationDegrees.addOnChangeListener(this);
    binding.sliderRotationDegrees.setLabelFormatter(
        value -> String.format(Locale.getDefault(), "+/-%.0f°", value)
    );

    binding.switchRotationSwipe.setChecked(
        getSharedPrefs().getBoolean(PREF.ROTATION_SWIPE, DEF.ROTATION_SWIPE)
    );
    binding.switchRotationZoom.setChecked(
        getSharedPrefs().getBoolean(PREF.ROTATION_ZOOM, DEF.ROTATION_ZOOM)
    );

    ViewUtil.setOnClickListeners(
        this,
        binding.frameRotationBack,
        binding.linearRotationSwipe,
        binding.linearRotationZoom
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchRotationSwipe,
        binding.switchRotationZoom
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.frame_rotation_back && getViewUtil().isClickEnabled()) {
      performHapticClick();
      getNavController().navigateUp();
    } else if (id == R.id.linear_rotation_swipe) {
      ViewUtil.startIcon(binding.imageRotationSwipe);
      binding.switchRotationSwipe.setChecked(!binding.switchRotationSwipe.isChecked());
    } else if (id == R.id.linear_rotation_zoom) {
      ViewUtil.startIcon(binding.imageRotationZoom);
      binding.switchRotationZoom.setChecked(!binding.switchRotationZoom.isChecked());
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_rotation_swipe) {
      getSharedPrefs().edit().putBoolean(PREF.ROTATION_SWIPE, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.switch_rotation_zoom) {
      getSharedPrefs().edit().putBoolean(PREF.ROTATION_ZOOM, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
    }
  }

  @Override
  public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
    if (!fromUser) {
      return;
    }
    int id = slider.getId();
    if (id == R.id.slider_rotation_degrees) {
      getSharedPrefs().edit().putInt(PREF.ROTATION, (int) value).apply();
      ViewUtil.startIcon(binding.imageRotationDegrees);
      activity.requestSettingsRefresh();
      performHapticClick();
    }
  }
}