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

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.Snackbar.Callback;
import java.util.Locale;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.Constants.THEME.MODE;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.LauncherActivity;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentOtherBinding;
import xyz.zedler.patrick.doodle.fragment.dialog.LanguagesBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.model.Language;
import xyz.zedler.patrick.doodle.util.LocaleUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.SelectionCardView;

public class OtherFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener {

  private static final String TAG = OtherFragment.class.getSimpleName();

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
        navigateUp();
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
        navigate(OtherFragmentDirections.actionOtherToFeedbackDialog());
        return true;
      } else {
        return false;
      }
    });

    binding.textOtherLanguage.setText(getLanguage());

    setUpThemeSelection();

    ViewUtil.setEnabledAlpha(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
        false,
        binding.linearOtherGpu
    );
    ViewUtil.setEnabled(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
        binding.switchOtherGpu
    );
    binding.cardOtherGpu.setVisibility(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.GONE : View.VISIBLE
    );
    binding.switchOtherGpu.setChecked(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && getSharedPrefs().getBoolean(PREF.GPU, DEF.GPU)
    );

    binding.switchOtherLauncher.setChecked(
        activity.getPackageManager().getComponentEnabledSetting(
            new ComponentName(activity, LauncherActivity.class)
        ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    );

    int id;
    switch (getSharedPrefs().getInt(PREF.MODE, MODE.AUTO)) {
      case MODE.LIGHT:
        id = R.id.button_other_theme_light;
        break;
      case MODE.DARK:
        id = R.id.button_other_theme_dark;
        break;
      default:
        id = R.id.button_other_theme_auto;
        break;
    }
    binding.toggleOtherTheme.check(id);
    binding.toggleOtherTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
      int pref;
      if (checkedId == R.id.button_other_theme_light) {
        pref = MODE.LIGHT;
      } else if (checkedId == R.id.button_other_theme_dark) {
        pref = MODE.DARK;
      } else {
        pref = MODE.AUTO;
      }
      getSharedPrefs().edit().putInt(PREF.MODE, pref).apply();
      activity.restartToApply(0, getInstanceState(), false);
    });

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
          activity.getSnackbar(
              R.string.msg_reset, Snackbar.LENGTH_LONG
          ).setAction(
              getString(R.string.action_reset), view -> {
                performHapticHeavyClick();
                activity.reset();
                activity.restartToApply(100, getInstanceState(), true);
              }
          )
      );
    }
  }

  @SuppressLint("ShowToast")
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_other_gpu) {
      getSharedPrefs().edit().putBoolean(PREF.GPU, isChecked).apply();
      performHapticClick();
      activity.showForceStopRequest(OtherFragmentDirections.actionOtherToApplyDialog());
    } else if (id == R.id.switch_other_launcher) {
      performHapticClick();
      if (isChecked) {
        activity.showSnackbar(
            activity.getSnackbar(
                R.string.msg_hide, Snackbar.LENGTH_LONG
            ).setAction(
                getString(R.string.action_hide), view -> {
                  performHapticHeavyClick();
                  activity.getPackageManager().setComponentEnabledSetting(
                      new ComponentName(activity, LauncherActivity.class),
                      PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                      PackageManager.DONT_KILL_APP
                  );
                }
            ).addCallback(new Callback() {
              @Override
              public void onDismissed(Snackbar transientBottomBar, int event) {
                if (binding == null || activity == null) {
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
            })
        );
      } else {
        activity.getPackageManager().setComponentEnabledSetting(
            new ComponentName(activity, LauncherActivity.class),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        );
      }
    }
  }

  public void setLanguage(Language language) {
    Locale locale = language != null
        ? LocaleUtil.getLocaleFromCode(language.getCode())
        : LocaleUtil.getNearestSupportedLocale(activity, LocaleUtil.getDeviceLocale());
    binding.textOtherLanguage.setText(
        language != null
            ? locale.getDisplayName()
            : getString(R.string.other_language_system, locale.getDisplayName())
    );
  }

  public String getLanguage() {
    String code = getSharedPrefs().getString(PREF.LANGUAGE, DEF.LANGUAGE);
    Locale locale = code != null
        ? LocaleUtil.getLocaleFromCode(code)
        : LocaleUtil.getNearestSupportedLocale(activity, LocaleUtil.getDeviceLocale());
    return code != null
        ? locale.getDisplayName()
        : getString(R.string.other_language_system, locale.getDisplayName());
  }

  private void setUpThemeSelection() {
    boolean hasDynamic = DynamicColors.isDynamicColorAvailable();
    ViewGroup container = binding.linearOtherThemeContainer;
    for (int i = hasDynamic ? -1 : 0; i < 7; i++) {
      String name;
      int resId;
      if (i == -1) {
        name = THEME.DYNAMIC;
        resId = -1;
      } else if (i == 0) {
        name = THEME.RED;
        resId = R.style.Theme_Doodle_Red;
      } else if (i == 1) {
        name = THEME.YELLOW;
        resId = R.style.Theme_Doodle_Yellow;
      } else if (i == 2) {
        name = THEME.GREEN;
        resId = R.style.Theme_Doodle_Green;
      } else if (i == 3) {
        name = THEME.BLUE;
        resId = R.style.Theme_Doodle_Blue;
      } else if (i == 4) {
        name = THEME.GOOGLE;
        resId = R.style.Theme_Doodle_Google;
      } else if (i == 5) {
        name = THEME.PURPLE;
        resId = R.style.Theme_Doodle_Purple;
      } else if (i == 6) {
        name = THEME.AMOLED;
        resId = R.style.Theme_Doodle_Amoled;
      } else {
        name = THEME.GOOGLE;
        resId = R.style.Theme_Doodle_Google;
      }

      SelectionCardView card = new SelectionCardView(activity);
      int color = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && i == -1
          ? ContextCompat.getColor(
              activity,
              SystemUiUtil.isDarkModeActive(activity)
                  ? android.R.color.system_accent1_700
                  : android.R.color.system_accent1_100
          )
          : ResUtil.getColorAttr(
              new ContextThemeWrapper(activity, resId), R.attr.colorPrimaryContainer
          );
      if (i == 6) {
        // Amoled theme selection card
        color = Color.BLACK;
      }
      card.setCardBackgroundColor(color);
      card.setOnClickListener(v -> {
        if (!card.isChecked()) {
          card.startCheckedIcon();
          ViewUtil.startIcon(binding.imageOtherTheme);
          performHapticClick();
          ViewUtil.uncheckAllChildren(container);
          card.setChecked(true);
          getSharedPrefs().edit().putString(PREF.THEME, name).apply();
          activity.restartToApply(100, getInstanceState(), false);
        }
      });

      String selected = getSharedPrefs().getString(PREF.THEME, DEF.THEME);
      boolean isSelected;
      if (selected.isEmpty()) {
        isSelected = hasDynamic ? name.equals(THEME.DYNAMIC) : name.equals(THEME.GOOGLE);
      } else {
        isSelected = selected.equals(name);
      }
      card.setChecked(isSelected);
      container.addView(card);
    }

    Bundle bundleInstanceState = activity.getIntent().getBundleExtra(EXTRA.INSTANCE_STATE);
    if (bundleInstanceState != null) {
      binding.scrollHorizOtherTheme.scrollTo(
          bundleInstanceState.getInt(EXTRA.SCROLL_POSITION, 0),
           0
      );
    }
  }

  private Bundle getInstanceState() {
    Bundle bundle = new Bundle();
    if (binding != null) {
      bundle.putInt(EXTRA.SCROLL_POSITION, binding.scrollHorizOtherTheme.getScrollX());
    }
    return bundle;
  }
}