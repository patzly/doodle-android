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
 * Copyright (c) 2019-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.PackageManager;
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
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.activity.SplashActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentOtherBinding;
import xyz.zedler.patrick.doodle.fragment.dialog.LanguagesBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.model.Language;
import xyz.zedler.patrick.doodle.util.LocaleUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class OtherFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener {

  private FragmentOtherBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentOtherBinding.inflate(inflater, container, false);
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
    systemBarBehavior.setAppBar(binding.appBarOther);
    systemBarBehavior.setScroll(binding.scrollOther, binding.linearOtherContainer);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarOther, binding.scrollOther, true
    );

    binding.toolbarOther.setNavigationOnClickListener(v -> {
      if (getViewUtil().isClickEnabled()) {
        performHapticClick();
        getNavController().navigateUp();
      }
    });
    binding.toolbarOther.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
        performHapticClick();
        return true;
      } else if (id == R.id.action_feedback) {
        performHapticClick();
        getNavController().navigate(OtherFragmentDirections.actionOtherToFeedbackDialog());
        return true;
      } else {
        return false;
      }
    });

    binding.textOtherLanguage.setText(getLanguage());

    binding.linearOtherGpu.setVisibility(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.VISIBLE : View.GONE
    );
    binding.switchOtherGpu.setChecked(getSharedPrefs().getBoolean(PREF.GPU, DEF.GPU));

    binding.switchOtherLauncher.setChecked(
        activity.getPackageManager().getComponentEnabledSetting(
            new ComponentName(activity, SplashActivity.class)
        ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    );

    ViewUtil.setOnClickListeners(
        this,
        binding.linearOtherLanguage,
        binding.linearOtherLauncher,
        binding.linearOtherReset
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchOtherGpu,
        binding.switchOtherLauncher
    );

    setGpuOptionEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
  }

  @SuppressLint("ShowToast")
  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.linear_other_language) {
      ViewUtil.startIcon(binding.imageOtherLanguage);
      performHapticClick();
      ViewUtil.showBottomSheet(activity, new LanguagesBottomSheetDialogFragment());
    } else if (id == R.id.linear_other_gpu) {
      ViewUtil.startIcon(binding.imageOtherGpu);
      performHapticClick();
      binding.switchOtherGpu.setChecked(!binding.switchOtherGpu.isChecked());
    } else if (id == R.id.linear_other_launcher) {
      ViewUtil.startIcon(binding.imageOtherLauncher);
      performHapticClick();
      binding.switchOtherLauncher.setChecked(!binding.switchOtherLauncher.isChecked());
    } else if (id == R.id.linear_other_reset && getViewUtil().isClickEnabled()) {
      ViewUtil.startIcon(binding.imageOtherReset);
      performHapticClick();
      activity.showSnackbar(
          Snackbar.make(
              binding.getRoot(), getString(R.string.msg_reset), Snackbar.LENGTH_LONG
          ).setActionTextColor(
              ResUtil.getColorAttr(activity, R.attr.colorPrimaryInverse)
          ).setAction(
              getString(R.string.action_reset), view -> {
                performHapticHeavyClick();
                activity.reset();
              }
          )
      );
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_other_gpu) {
      getSharedPrefs().edit().putBoolean(PREF.GPU, isChecked).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
    } else if (id == R.id.switch_other_launcher) {
      performHapticClick();
      activity.getPackageManager().setComponentEnabledSetting(
          new ComponentName(activity, SplashActivity.class),
          isChecked
              ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
              : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
          PackageManager.DONT_KILL_APP
      );
    }
  }

  public void setLanguage(Language language) {
    Locale locale = language != null
        ? LocaleUtil.getLocaleFromCode(language.getCode())
        : LocaleUtil.getNearestSupportedLocale(activity, LocaleUtil.getDeviceLocale());
    binding.textOtherLanguage.setText(
        language != null
            ? locale.getDisplayName()
            : getString(R.string.setting_language_system, locale.getDisplayName())
    );
  }

  public String getLanguage() {
    String code = getSharedPrefs().getString(PREF.LANGUAGE, DEF.LANGUAGE);
    Locale locale = code != null
        ? LocaleUtil.getLocaleFromCode(code)
        : LocaleUtil.getNearestSupportedLocale(activity, LocaleUtil.getDeviceLocale());
    return code != null
        ? locale.getDisplayName()
        : getString(R.string.setting_language_system, locale.getDisplayName());
  }

  private void setGpuOptionEnabled(boolean enabled) {
    if (enabled) {
      binding.linearOtherGpu.setOnClickListener(this);
    }
    binding.linearOtherGpu.setEnabled(enabled);
    binding.switchOtherGpu.setEnabled(enabled);
    binding.linearOtherGpu.setAlpha(enabled ? 1 : 0.5f);
    binding.cardOtherGpu.setVisibility(enabled ? View.GONE : View.VISIBLE);
  }
}