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
 * Copyright (c) 2019-2024 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.Snackbar.Callback;
import xyz.zedler.patrick.doodle.Constants.CONTRAST;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.LauncherActivity;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentOtherBinding;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.DialogUtil;
import xyz.zedler.patrick.doodle.util.LocaleUtil;
import xyz.zedler.patrick.doodle.util.UiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.SelectionCardView;

public class OtherFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener {

  private static final String TAG = OtherFragment.class.getSimpleName();

  private FragmentOtherBinding binding;
  private MainActivity activity;
  private DialogUtil dialogUtilReset;

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
    dialogUtilReset.dismiss();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    activity = (MainActivity) requireActivity();

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarOther);
    systemBarBehavior.setScroll(binding.scrollOther, binding.constraintOther);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior().setUpScroll(binding.appBarOther, binding.scrollOther, true);

    binding.toolbarOther.setNavigationOnClickListener(getNavigationOnClickListener());
    binding.toolbarOther.setOnMenuItemClickListener(getOnMenuItemClickListener());

    binding.textOtherLanguage.setText(
        LocaleUtil.followsSystem()
            ? getString(R.string.other_language_system)
            : LocaleUtil.getLocaleName()
    );

    setUpThemeSelection();

    int id;
    switch (getSharedPrefs().getInt(PREF.UI_MODE, DEF.UI_MODE)) {
      case AppCompatDelegate.MODE_NIGHT_NO:
        id = R.id.button_other_theme_light;
        break;
      case AppCompatDelegate.MODE_NIGHT_YES:
        id = R.id.button_other_theme_dark;
        break;
      default:
        id = R.id.button_other_theme_auto;
        break;
    }
    binding.toggleOtherTheme.check(id);
    binding.toggleOtherTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
      if (!isChecked) {
        return;
      }
      int pref;
      if (checkedId == R.id.button_other_theme_light) {
        pref = AppCompatDelegate.MODE_NIGHT_NO;
      } else if (checkedId == R.id.button_other_theme_dark) {
        pref = AppCompatDelegate.MODE_NIGHT_YES;
      } else {
        pref = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
      }
      getSharedPrefs().edit().putInt(PREF.UI_MODE, pref).apply();
      performHapticClick();
      activity.restartToApply(
          0, getInstanceState(), false, true
      );
    });

    int idContrast;
    switch (getSharedPrefs().getString(PREF.UI_CONTRAST, DEF.UI_CONTRAST)) {
      case CONTRAST.MEDIUM:
        idContrast = R.id.button_other_contrast_medium;
        break;
      case CONTRAST.HIGH:
        idContrast = R.id.button_other_contrast_high;
        break;
      default:
        idContrast = R.id.button_other_contrast_standard;
        break;
    }
    binding.toggleOtherContrast.check(idContrast);
    binding.toggleOtherContrast.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
      if (!isChecked) {
        return;
      }
      String pref;
      if (checkedId == R.id.button_other_contrast_medium) {
        pref = CONTRAST.MEDIUM;
      } else if (checkedId == R.id.button_other_contrast_high) {
        pref = CONTRAST.HIGH;
      } else {
        pref = CONTRAST.STANDARD;
      }
      getSharedPrefs().edit().putString(PREF.UI_CONTRAST, pref).apply();
      performHapticClick();
      ViewUtil.startIcon(binding.imageSettingsContrast);
      activity.restartToApply(
          0, getInstanceState(), true, false
      );
    });
    boolean enabled = !getSharedPrefs().getString(PREF.THEME, DEF.THEME).equals(THEME.DYNAMIC);
    binding.toggleOtherContrast.setEnabled(enabled);
    binding.textSettingsContrastDynamic.setVisibility(enabled ? View.GONE : View.VISIBLE);
    binding.textSettingsContrastDynamic.setText(
        VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE
            ? R.string.other_contrast_dynamic
            : R.string.other_contrast_dynamic_unsupported
    );

    boolean gpuOptionEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    ViewUtil.setEnabledAlpha(gpuOptionEnabled, false, binding.linearOtherGpu);
    ViewUtil.setEnabled(gpuOptionEnabled, binding.switchOtherGpu);
    binding.cardOtherGpu.setVisibility(gpuOptionEnabled ? View.GONE : View.VISIBLE);
    if (gpuOptionEnabled) {
      binding.linearOtherGpu.setOnClickListener(this);
    }
    binding.switchOtherGpu.setChecked(
        gpuOptionEnabled && getSharedPrefs().getBoolean(PREF.GPU, DEF.GPU)
    );
    binding.switchOtherGpu.jumpDrawablesToCurrentState();

    binding.switchOtherLauncher.setChecked(
        activity.getPackageManager().getComponentEnabledSetting(
            new ComponentName(activity, LauncherActivity.class)
        ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    );
    binding.switchOtherLauncher.jumpDrawablesToCurrentState();

    binding.sliderScreenOffDelay.setValue(
        getSharedPrefs().getInt(PREF.SCREEN_OFF_DELAY, 0)
    );
    binding.sliderScreenOffDelay.addOnChangeListener((slider, value, fromUser) -> {
      getSharedPrefs().edit().putInt(PREF.SCREEN_OFF_DELAY, (int) value).apply();
      activity.requestSettingsRefresh();
      performHapticClick();
    });
    binding.sliderScreenOffDelay.setLabelFormatter(
        value -> getString(
            R.string.label_ms, String.format(activity.getLocale(), "%.0f", value)
        )
    );

    dialogUtilReset = new DialogUtil(activity, "reset");
    dialogUtilReset.createCaution(
        R.string.msg_reset,
        R.string.msg_reset_description,
        R.string.action_reset,
        () -> {
          performHapticHeavyClick();
          activity.reset();
          activity.restartToApply(
              100,
              getInstanceState(),
              LiveWallpaperService.isMainEngineRunning(),
              true
          );
        });
    dialogUtilReset.showIfWasShown(savedInstanceState);

    ViewUtil.setOnClickListeners(
        this,
        binding.linearOtherLanguage,
        binding.linearOtherLauncher,
        binding.linearOtherLog,
        binding.linearOtherReset
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchOtherGpu,
        binding.switchOtherLauncher
    );
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (dialogUtilReset != null) {
      dialogUtilReset.saveState(outState);
    }
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.linear_other_language && getViewUtil().isClickEnabled(id)) {
      performHapticClick();
      ViewUtil.startIcon(binding.imageOtherLanguage);
      navigate(OtherFragmentDirections.actionOtherToLanguagesDialog());
    } else if (id == R.id.linear_other_gpu) {
      binding.switchOtherGpu.toggle();
    } else if (id == R.id.linear_other_launcher) {
      binding.switchOtherLauncher.toggle();
    } else if (id == R.id.linear_other_log) {
      performHapticClick();
      navigateToFragment(OtherFragmentDirections.actionOtherToLog());
    } else if (id == R.id.linear_other_reset && getViewUtil().isClickEnabled(id)) {
      performHapticClick();
      dialogUtilReset.show();
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    performHapticClick();
    if (id == R.id.switch_other_gpu) {
      //ViewUtil.startIcon(binding.imageOtherGpu);
      getSharedPrefs().edit().putBoolean(PREF.GPU, isChecked).apply();
      activity.showForceStopRequest();
    } else if (id == R.id.switch_other_launcher) {
      ViewUtil.startIcon(binding.imageOtherLauncher);
      if (isChecked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          activity.showSnackbar(R.string.other_launcher_unsupported);
          binding.switchOtherLauncher.setOnCheckedChangeListener(null);
          binding.switchOtherLauncher.setChecked(false);
          binding.switchOtherLauncher.setOnCheckedChangeListener(this);
        } else {
          Snackbar snackbar = activity.getSnackbar(R.string.msg_hide, Snackbar.LENGTH_LONG);
          snackbar.setAction(getString(R.string.action_hide), view -> {
            performHapticHeavyClick();
            activity.getPackageManager().setComponentEnabledSetting(
                new ComponentName(activity, LauncherActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            );
          });
          snackbar.addCallback(new Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
              if (binding == null || activity == null
                  || event == BaseCallback.DISMISS_EVENT_CONSECUTIVE) {
                return;
              }
              try {
                binding.switchOtherLauncher.setOnCheckedChangeListener(null);
                binding.switchOtherLauncher.setChecked(
                    activity.getPackageManager().getComponentEnabledSetting(
                        new ComponentName(activity, LauncherActivity.class)
                    ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                );
                binding.switchOtherLauncher.setOnCheckedChangeListener(OtherFragment.this);
              } catch (NullPointerException e) {
                Log.e(TAG, "onDismissed: error when the snackbar was dismissed", e);
              }
            }
          });
          activity.showSnackbar(snackbar);
        }
      } else {
        activity.getPackageManager().setComponentEnabledSetting(
            new ComponentName(activity, LauncherActivity.class),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        );
      }
    }
  }

  private void setUpThemeSelection() {
    boolean hasDynamic = DynamicColors.isDynamicColorAvailable();
    ViewGroup container = binding.linearOtherThemeContainer;
    for (int i = hasDynamic ? -1 : 0; i < 4; i++) {
      String name;
      int resId;
      switch (i) {
        case -1:
          name = THEME.DYNAMIC;
          resId = -1;
          break;
        case 0:
          name = THEME.RED;
          resId = getContrastThemeResId(
              R.style.Theme_Doodle_Red,
              R.style.ThemeOverlay_Doodle_Red_MediumContrast,
              R.style.ThemeOverlay_Doodle_Red_HighContrast
          );
          break;
        case 2:
          name = THEME.GREEN;
          resId = getContrastThemeResId(
              R.style.Theme_Doodle_Green,
              R.style.ThemeOverlay_Doodle_Green_MediumContrast,
              R.style.ThemeOverlay_Doodle_Green_HighContrast
          );
          break;
        case 3:
          name = THEME.BLUE;
          resId = getContrastThemeResId(
              R.style.Theme_Doodle_Blue,
              R.style.ThemeOverlay_Doodle_Blue_MediumContrast,
              R.style.ThemeOverlay_Doodle_Blue_HighContrast
          );
          break;
        default:
          name = THEME.YELLOW;
          resId = getContrastThemeResId(
              R.style.Theme_Doodle_Yellow,
              R.style.ThemeOverlay_Doodle_Yellow_MediumContrast,
              R.style.ThemeOverlay_Doodle_Yellow_HighContrast
          );
          break;
      }

      SelectionCardView card = new SelectionCardView(activity);
      card.setNestedContext(
          i == -1 && VERSION.SDK_INT >= VERSION_CODES.S
              ? DynamicColors.wrapContextIfAvailable(activity)
              : new ContextThemeWrapper(activity, resId)
      );
      card.setOnClickListener(v -> {
        if (!card.isChecked()) {
          card.startCheckedIcon();
          ViewUtil.startIcon(binding.imageOtherTheme);
          performHapticClick();
          ViewUtil.uncheckAllChildren(container);
          card.setChecked(true);
          getSharedPrefs().edit().putString(PREF.THEME, name).apply();
          activity.restartToApply(
              100, getInstanceState(), true, false
          );
        }
      });

      String selected = getSharedPrefs().getString(PREF.THEME, DEF.THEME);
      boolean isSelected;
      if (selected.isEmpty()) {
        isSelected = hasDynamic ? name.equals(THEME.DYNAMIC) : name.equals(THEME.YELLOW);
      } else {
        isSelected = selected.equals(name);
      }
      card.setChecked(isSelected);
      container.addView(card);

      if (hasDynamic && i == -1) {
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
        container.addView(divider);
      }
    }

    Bundle bundleInstanceState = activity.getIntent().getBundleExtra(EXTRA.INSTANCE_STATE);
    if (bundleInstanceState != null) {
      binding.scrollOtherTheme.scrollTo(
          bundleInstanceState.getInt(EXTRA.SCROLL_POSITION, 0),
          0
      );
    }
  }

  private int getContrastThemeResId(
      @StyleRes int resIdStandard,
      @StyleRes int resIdMedium,
      @StyleRes int resIdHigh
  ) {
    switch (getSharedPrefs().getString(PREF.UI_CONTRAST, DEF.UI_CONTRAST)) {
      case CONTRAST.MEDIUM:
        return resIdMedium;
      case CONTRAST.HIGH:
        return resIdHigh;
      default:
        return resIdStandard;
    }
  }

  private Bundle getInstanceState() {
    Bundle bundle = new Bundle();
    if (binding != null) {
      bundle.putInt(EXTRA.SCROLL_POSITION + 1, binding.scrollOtherTheme.getScrollX());
    }
    return bundle;
  }
}