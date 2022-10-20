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
import xyz.zedler.patrick.doodle.databinding.FragmentSizeBinding;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class SizeFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener, OnChangeListener {

  private FragmentSizeBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentSizeBinding.inflate(inflater, container, false);
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
    systemBarBehavior.setAppBar(binding.appBarSize);
    systemBarBehavior.setScroll(binding.scrollSize, binding.constraintSize);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarSize, binding.scrollSize, true
    );

    binding.toolbarSize.setNavigationOnClickListener(getNavigationOnClickListener());
    binding.toolbarSize.setOnMenuItemClickListener(getOnMenuItemClickListener());

    binding.sliderSizeScale.setValue(
        getSharedPrefs().getFloat(PREF.SCALE, SvgDrawable.getDefaultScale(activity)) * 10
    );
    binding.sliderSizeScale.addOnChangeListener(this);
    binding.sliderSizeScale.setLabelFormatter(value -> {
      float scale = value / 10f;
      return String.format(
          activity.getLocale(), scale == 1 || scale == 2 ? "×%.0f" : "×%.1f", scale
      );
    });

    binding.sliderSizeZoom.setValue(getSharedPrefs().getInt(PREF.ZOOM, DEF.ZOOM));
    binding.sliderSizeZoom.addOnChangeListener(this);
    binding.sliderSizeZoom.setLabelFormatter(
        value -> String.format(activity.getLocale(), "%.0f", value)
    );

    binding.sliderSizeZoomRotation.setValue(
        getSharedPrefs().getInt(PREF.ZOOM_ROTATION, DEF.ZOOM_ROTATION)
    );
    binding.sliderSizeZoomRotation.addOnChangeListener(this);
    binding.sliderSizeZoomRotation.setLabelFormatter(
        value -> getString(
            R.string.label_degrees, String.format(activity.getLocale(), "%.0f", value)
        )
    );

    binding.switchSizeZoomPowerSave.setChecked(
        getSharedPrefs().getBoolean(PREF.POWER_SAVE_ZOOM, DEF.POWER_SAVE_ZOOM)
    );

    int launcherZoom = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? View.VISIBLE : View.GONE;
    binding.linearSizeZoomLauncher.setVisibility(launcherZoom);
    binding.switchSizeZoomLauncher.setChecked(
        getSharedPrefs().getBoolean(PREF.ZOOM_LAUNCHER, DEF.ZOOM_LAUNCHER)
    );

    boolean useZoomDamping = getSharedPrefs().getBoolean(
        PREF.USE_ZOOM_DAMPING, DEF.USE_ZOOM_DAMPING
    );
    binding.switchSizeZoomDamping.setChecked(useZoomDamping);
    binding.sliderSizeZoomDamping.setValue(
        getSharedPrefs().getInt(PREF.DAMPING_ZOOM, DEF.DAMPING_ZOOM)
    );
    binding.sliderSizeZoomDamping.addOnChangeListener(this);
    binding.sliderSizeZoomDamping.setLabelFormatter(
        value -> String.format(activity.getLocale(), "%.0f", value)
    );

    setZoomDampingEnabled(binding.switchSizeZoomLauncher.isChecked(), false);
    binding.sliderSizeZoomDamping.setEnabled(
        binding.linearSizeZoomDamping.isEnabled() && useZoomDamping
    );

    boolean isZoomUnlockEnabled = getSharedPrefs().getBoolean(PREF.ZOOM_UNLOCK, DEF.ZOOM_UNLOCK);
    binding.switchSizeZoomUnlock.setChecked(isZoomUnlockEnabled);
    binding.buttonSizeZoomUnlockIn.setEnabled(isZoomUnlockEnabled);
    binding.buttonSizeZoomUnlockOut.setEnabled(isZoomUnlockEnabled);

    int id;
    if (getSharedPrefs().getBoolean(PREF.ZOOM_UNLOCK_IN, DEF.ZOOM_UNLOCK_IN)) {
      id = R.id.button_size_zoom_unlock_in;
    } else {
      id = R.id.button_size_zoom_unlock_out;
    }
    binding.toggleSizeZoomUnlock.check(id);
    binding.toggleSizeZoomUnlock.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
      if (!isChecked) {
        return;
      }
      boolean pref;
      if (checkedId == R.id.button_size_zoom_unlock_in) {
        pref = true;
      } else if (checkedId == R.id.button_size_zoom_unlock_out) {
        pref = false;
      } else {
        pref = false;
      }
      getSharedPrefs().edit().putBoolean(PREF.ZOOM_UNLOCK_IN, pref).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
    });

    binding.sliderSizeZoomDuration.setValue(
        getSharedPrefs().getInt(PREF.ZOOM_DURATION, DEF.ZOOM_DURATION)
    );
    binding.sliderSizeZoomDuration.addOnChangeListener(this);
    binding.sliderSizeZoomDuration.setLabelFormatter(
        value -> getString(
            R.string.label_ms, String.format(activity.getLocale(), "%.0f", value)
        )
    );

    ViewUtil.setOnClickListeners(
        this,
        binding.frameSizeScaleReset,
        binding.linearSizeZoomPowerSave,
        binding.linearSizeZoomLauncher,
        binding.linearSizeZoomDamping,
        binding.linearSizeZoomUnlock
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchSizeZoomPowerSave,
        binding.switchSizeZoomLauncher,
        binding.switchSizeZoomDamping,
        binding.switchSizeZoomUnlock
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.frame_size_scale_reset) {
      float def = SvgDrawable.getDefaultScale(activity);
      float scaleOld = binding.sliderSizeScale.getValue();
      float scaleNew = def * 10;
      binding.sliderSizeScale.setValue(scaleNew);
      getSharedPrefs().edit().putFloat(PREF.SCALE, def).apply();
      if (scaleNew != scaleOld) {
        activity.requestSettingsRefresh();
      }
      performHapticClick();
      ViewUtil.startIcon(binding.imageSizeScaleReset);
    } else if (id == R.id.linear_size_zoom_power_save) {
      binding.switchSizeZoomPowerSave.setChecked(
          !binding.switchSizeZoomPowerSave.isChecked()
      );
    } else if (id == R.id.linear_size_zoom_launcher) {
      binding.switchSizeZoomLauncher.setChecked(!binding.switchSizeZoomLauncher.isChecked());
    } else if (id == R.id.linear_size_zoom_damping) {
      binding.switchSizeZoomDamping.setChecked(!binding.switchSizeZoomDamping.isChecked());
    } else if (id == R.id.linear_size_zoom_unlock) {
      binding.switchSizeZoomUnlock.setChecked(!binding.switchSizeZoomUnlock.isChecked());
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_size_zoom_power_save) {
      getSharedPrefs().edit().putBoolean(PREF.POWER_SAVE_ZOOM, isChecked).apply();
      activity.requestSettingsRefresh();
      ViewUtil.startIcon(binding.imageSizeZoomPowerSave);
      performHapticClick();
    } else if (id == R.id.switch_size_zoom_launcher) {
      getSharedPrefs().edit().putBoolean(PREF.ZOOM_LAUNCHER, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
      setZoomDampingEnabled(isChecked, true);
    } else if (id == R.id.switch_size_zoom_damping) {
      getSharedPrefs().edit().putBoolean(PREF.USE_ZOOM_DAMPING, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
      binding.sliderSizeZoomDamping.setEnabled(isChecked);
      ViewUtil.startIcon(binding.imageSizeZoomDamping);
    } else if (id == R.id.switch_size_zoom_unlock) {
      getSharedPrefs().edit().putBoolean(PREF.ZOOM_UNLOCK, isChecked).apply();
      binding.buttonSizeZoomUnlockIn.setEnabled(isChecked);
      binding.buttonSizeZoomUnlockOut.setEnabled(isChecked);
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
    if (id == R.id.slider_size_scale) {
      getSharedPrefs().edit().putFloat(PREF.SCALE, value / 10).apply();
      ViewUtil.startIcon(binding.imageSizeScale);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_size_zoom) {
      getSharedPrefs().edit().putInt(PREF.ZOOM, (int) value).apply();
      ViewUtil.startIcon(binding.imageSizeZoom);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_size_zoom_rotation) {
      getSharedPrefs().edit().putInt(PREF.ZOOM_ROTATION, (int) value).apply();
      ViewUtil.startIcon(binding.imageSizeZoomRotation);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_size_zoom_damping) {
      getSharedPrefs().edit().putInt(PREF.DAMPING_ZOOM, (int) value).apply();
      ViewUtil.startIcon(binding.imageSizeZoomDamping);
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.slider_size_zoom_duration) {
      getSharedPrefs().edit().putInt(PREF.ZOOM_DURATION, (int) value).apply();
      ViewUtil.startIcon(binding.imageSizeZoomDuration);
      activity.requestSettingsRefresh();
      performHapticClick();
    }
  }

  private void setZoomDampingEnabled(boolean enabled, boolean animated) {
    ViewUtil.setEnabledAlpha(enabled, animated, binding.linearSizeZoomDamping);
    binding.switchSizeZoomDamping.setEnabled(enabled);
    binding.sliderSizeZoomDamping.setEnabled(enabled && binding.switchSizeZoomDamping.isChecked());
  }
}