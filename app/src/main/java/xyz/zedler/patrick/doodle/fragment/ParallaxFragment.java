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
 * Copyright (c) 2019-2025 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentParallaxBinding;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class ParallaxFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener, OnChangeListener {

  private static final String TAG = ParallaxFragment.class.getSimpleName();

  private FragmentParallaxBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    // NumberFormatException and NotFoundException on Android 5?
    // TODO: Probably fixed (AnimatedVectorDrawable path was broken broken)
    binding = FragmentParallaxBinding.inflate(inflater, container, false);
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
    systemBarBehavior.setAppBar(binding.appBarParallax);
    systemBarBehavior.setScroll(binding.scrollParallax, binding.constraintParallax);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior().setUpScroll(
        binding.appBarParallax, binding.scrollParallax, true
    );

    binding.toolbarParallax.setNavigationOnClickListener(getNavigationOnClickListener());
    binding.toolbarParallax.setOnMenuItemClickListener(getOnMenuItemClickListener());

    binding.cardParallaxTouchWiz.setVisibility(
        activity.isLauncherTouchWiz() ? View.VISIBLE : View.GONE
    );

    // SWIPING

    boolean isSwipeEnabled = getSharedPrefs().getBoolean(PREF.SWIPE, DEF.SWIPE);
    binding.switchParallaxSwipe.setChecked(isSwipeEnabled);
    binding.switchParallaxSwipe.jumpDrawablesToCurrentState();

    binding.sliderParallaxSwipe.setEnabled(isSwipeEnabled);
    binding.sliderParallaxSwipe.setValue(
        getSharedPrefs().getInt(PREF.SWIPE_INTENSITY, DEF.SWIPE_INTENSITY)
    );
    binding.sliderParallaxSwipe.addOnChangeListener(this);
    binding.sliderParallaxSwipe.setLabelFormatter(value -> String.valueOf((int) value));

    binding.switchParallaxSwipePowerSave.setChecked(
        getSharedPrefs().getBoolean(PREF.POWER_SAVE_SWIPE, DEF.POWER_SAVE_SWIPE)
    );
    binding.switchParallaxSwipePowerSave.jumpDrawablesToCurrentState();

    // TILTING

    binding.linearParallaxTiltContainer.setVisibility(
        hasAccelerometer() ? View.VISIBLE : View.GONE
    );
    boolean isTiltEnabled = getSharedPrefs().getBoolean(PREF.TILT, DEF.TILT);
    binding.switchParallaxTilt.setChecked(isTiltEnabled);
    binding.switchParallaxTilt.jumpDrawablesToCurrentState();

    ViewUtil.setEnabledAlpha(
        isTiltEnabled,
        false,
        binding.linearParallaxRefreshRate,
        binding.linearParallaxDamping,
        binding.linearParallaxThreshold,
        binding.linearParallaxTiltPowerSave
    );
    ViewUtil.setEnabled(
        isTiltEnabled,
        binding.sliderParallaxTilt,
        binding.sliderParallaxRefreshRate,
        binding.sliderParallaxDamping,
        binding.sliderParallaxThreshold,
        binding.switchParallaxTiltPowerSave
    );

    binding.sliderParallaxTilt.setValue(
        getSharedPrefs().getInt(PREF.TILT_INTENSITY, DEF.TILT_INTENSITY)
    );
    binding.sliderParallaxTilt.addOnChangeListener(this);
    binding.sliderParallaxTilt.setLabelFormatter(value -> String.valueOf((int) value));

    binding.sliderParallaxRefreshRate.setValue(
        getSharedPrefs().getInt(PREF.TILT_REFRESH_RATE, DEF.TILT_REFRESH_RATE)
    );
    binding.sliderParallaxRefreshRate.addOnChangeListener(this);
    binding.sliderParallaxRefreshRate.setLabelFormatter(
        value -> getString(
            R.string.label_ms,
            String.format(activity.getLocale(), "%.0f", value / 1000)
        )
    );

    binding.sliderParallaxDamping.setValue(
        getSharedPrefs().getInt(PREF.DAMPING_TILT, DEF.DAMPING_TILT)
    );
    binding.sliderParallaxDamping.addOnChangeListener(this);
    binding.sliderParallaxDamping.setLabelFormatter(
        value -> String.format(activity.getLocale(), "%.0f", value)
    );

    binding.sliderParallaxThreshold.setValue(
        getSharedPrefs().getInt(PREF.THRESHOLD, DEF.THRESHOLD)
    );
    binding.sliderParallaxThreshold.addOnChangeListener(this);
    binding.sliderParallaxThreshold.setLabelFormatter(
        value -> String.format(activity.getLocale(), "%.0f", value)
    );

    binding.switchParallaxTiltPowerSave.setChecked(
        getSharedPrefs().getBoolean(PREF.POWER_SAVE_TILT, DEF.POWER_SAVE_TILT)
    );
    binding.switchParallaxTiltPowerSave.jumpDrawablesToCurrentState();

    ViewUtil.setOnClickListeners(
        this,
        binding.linearParallaxSwipe,
        binding.linearParallaxSwipePowerSave,
        binding.linearParallaxTilt,
        binding.linearParallaxTiltPowerSave
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchParallaxSwipe,
        binding.switchParallaxSwipePowerSave,
        binding.switchParallaxTilt,
        binding.switchParallaxTiltPowerSave
    );
  }

  @Override
  public void onResume() {
    super.onResume();

    binding.cardParallaxTouchWiz.setVisibility(
        activity.isLauncherTouchWiz() ? View.VISIBLE : View.GONE
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.linear_parallax_swipe) {
      binding.switchParallaxSwipe.setChecked(!binding.switchParallaxSwipe.isChecked());
    } else if (id == R.id.linear_parallax_swipe_power_save) {
      binding.switchParallaxSwipePowerSave.setChecked(
          !binding.switchParallaxSwipePowerSave.isChecked()
      );
    } else if (id == R.id.linear_parallax_tilt) {
      binding.switchParallaxTilt.setChecked(!binding.switchParallaxTilt.isChecked());
    } else if (id == R.id.linear_parallax_tilt_power_save) {
      binding.switchParallaxTiltPowerSave.setChecked(
          !binding.switchParallaxTiltPowerSave.isChecked()
      );
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_parallax_swipe) {
      getSharedPrefs().edit().putBoolean(PREF.SWIPE, isChecked).apply();
      binding.sliderParallaxSwipe.setEnabled(isChecked);
      activity.requestSettingsRefresh();
      performHapticClick();
      ViewUtil.startIcon(binding.imageParallaxSwipe);
    } else if (id == R.id.switch_parallax_swipe_power_save) {
      getSharedPrefs().edit().putBoolean(PREF.POWER_SAVE_SWIPE, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
      ViewUtil.startIcon(binding.imageParallaxSwipePowerSave);
    } else if (id == R.id.switch_parallax_tilt) {
      getSharedPrefs().edit().putBoolean(PREF.TILT, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
      ViewUtil.startIcon(binding.imageParallaxTilt);
      ViewUtil.setEnabledAlpha(
          isChecked,
          true,
          binding.linearParallaxRefreshRate,
          binding.linearParallaxDamping,
          binding.linearParallaxThreshold,
          binding.linearParallaxTiltPowerSave
      );
      ViewUtil.setEnabled(
          isChecked,
          binding.sliderParallaxTilt,
          binding.sliderParallaxRefreshRate,
          binding.sliderParallaxDamping,
          binding.sliderParallaxThreshold,
          binding.switchParallaxTiltPowerSave
      );
    } else if (id == R.id.switch_parallax_tilt_power_save) {
      getSharedPrefs().edit().putBoolean(PREF.POWER_SAVE_TILT, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
      ViewUtil.startIcon(binding.imageParallaxTiltPowerSave);
    }
  }

  @Override
  public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
    if (!fromUser) {
      return;
    }
    int id = slider.getId();
    if (id == R.id.slider_parallax_swipe) {
      getSharedPrefs().edit().putInt(PREF.SWIPE_INTENSITY, (int) value).apply();
      ViewUtil.startIcon(binding.imageParallaxSwipe);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_parallax_tilt) {
      getSharedPrefs().edit().putInt(PREF.TILT_INTENSITY, (int) value).apply();
      ViewUtil.startIcon(binding.imageParallaxTilt);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_parallax_refresh_rate) {
      getSharedPrefs().edit().putInt(PREF.TILT_REFRESH_RATE, (int) value).apply();
      ViewUtil.startIcon(binding.imageParallaxRefreshRate);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_parallax_damping) {
      getSharedPrefs().edit().putInt(PREF.DAMPING_TILT, (int) value).apply();
      ViewUtil.startIcon(binding.imageParallaxDamping);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_parallax_threshold) {
      getSharedPrefs().edit().putInt(PREF.THRESHOLD, (int) value).apply();
      ViewUtil.startIcon(binding.imageParallaxThreshold);
      activity.requestSettingsRefresh();
      performHapticClick();
    }
  }

  private boolean hasAccelerometer() {
    SensorManager manager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    return manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null;
  }
}