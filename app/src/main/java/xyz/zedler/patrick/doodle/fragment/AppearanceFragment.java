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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.VARIANT;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentAppearanceBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class AppearanceFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener {

  private FragmentAppearanceBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentAppearanceBinding.inflate(inflater, container, false);
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
    systemBarBehavior.setAppBar(binding.appBarAppearance);
    systemBarBehavior.setScroll(binding.scrollAppearance, binding.linearAppearanceContainer);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarAppearance, binding.scrollAppearance, true
    );

    binding.toolbarAppearance.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
        performHapticClick();
        return true;
      } else if (id == R.id.action_feedback) {
        performHapticClick();
        getNavController().navigate(
            AppearanceFragmentDirections.actionAppearanceToFeedbackDialog()
        );
        return true;
      } else {
        return false;
      }
    });

    binding.switchAppearanceNightMode.setChecked(getSharedPrefs().getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE));

    binding.switchAppearanceFollowSystem.setChecked(
        getSharedPrefs().getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM)
    );
    binding.switchAppearanceFollowSystem.setEnabled(binding.switchAppearanceNightMode.isChecked());

    binding.linearAppearanceFollowSystem.setEnabled(binding.switchAppearanceNightMode.isChecked());
    binding.linearAppearanceFollowSystemContainer.setAlpha(binding.switchAppearanceNightMode.isChecked() ? 1 : 0.5f);

    binding.imageAppearanceNightMode.setImageResource(
        getSharedPrefs().getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE)
            ? R.drawable.ic_round_dark_mode_to_light_mode_anim
            : R.drawable.ic_round_light_mode_to_dark_mode_anim
    );

    binding.switchAppearanceWhiteText.setChecked(
        getSharedPrefs().getBoolean(PREF.USE_WHITE_TEXT, DEF.USE_WHITE_TEXT)
    );

    refreshSelectionTheme(
        getSharedPrefs().getString(PREF.WALLPAPER, WALLPAPER.PIXEL), false
    );

    String[] wallpapers = new String[]{
        WALLPAPER.PIXEL, WALLPAPER.JOHANNA, WALLPAPER.REIKO, WALLPAPER.ANTHONY
    };
    for (String wallpaper : wallpapers) {
      refreshSelectionVariant(
          wallpaper,
          getSharedPrefs().getString(Constants.VARIANT_PREFIX + wallpaper, wallpaper + "1"),
          false
      );
    }

    ViewUtil.setOnClickListeners(
        this,
        // Wallpapers
        binding.cardAppearancePixel, binding.cardAppearanceJohanna, binding.cardAppearanceReiko, binding.cardAppearanceAnthony,
        // Variants
        binding.cardAppearancePixel1, binding.cardAppearancePixel2, binding.cardAppearancePixel3,
        binding.cardAppearancePixel4, binding.cardAppearancePixel5,
        binding.cardAppearanceJohanna1,
        binding.cardAppearanceReiko1, binding.cardAppearanceReiko2,
        binding.cardAppearanceAnthony1,
        // Other
        binding.frameAppearanceBack,
        binding.linearAppearanceNightMode,
        binding.linearAppearanceFollowSystem,
        binding.linearAppearanceWhiteText
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchAppearanceNightMode,
        binding.switchAppearanceFollowSystem,
        binding.switchAppearanceWhiteText
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.frame_appearance_back) {
      performHapticClick();
      getNavController().navigateUp();
    } else if (id == R.id.card_appearance_pixel) {
      refreshSelectionTheme(WALLPAPER.PIXEL, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_johanna) {
      refreshSelectionTheme(WALLPAPER.JOHANNA, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_reiko) {
      refreshSelectionTheme(WALLPAPER.REIKO, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_anthony) {
      refreshSelectionTheme(WALLPAPER.ANTHONY, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_pixel1) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL1, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_pixel2) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL2, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_pixel3) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL3, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_pixel4) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL4, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_pixel5) {
      refreshSelectionVariant(WALLPAPER.PIXEL, VARIANT.PIXEL5, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_johanna1) {
      refreshSelectionVariant(WALLPAPER.JOHANNA, VARIANT.JOHANNA1, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_reiko1) {
      refreshSelectionVariant(WALLPAPER.REIKO, VARIANT.REIKO1, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_reiko2) {
      refreshSelectionVariant(WALLPAPER.REIKO, VARIANT.REIKO2, true);
      performHapticClick();
    } else if (id == R.id.card_appearance_anthony1) {
      refreshSelectionVariant(WALLPAPER.ANTHONY, VARIANT.ANTHONY1, true);
      performHapticClick();
    } else if (id == R.id.linear_appearance_night_mode) {
      binding.switchAppearanceNightMode.setChecked(!binding.switchAppearanceNightMode.isChecked());
    } else if (id == R.id.linear_appearance_follow_system) {
      if (binding.switchAppearanceNightMode.isChecked()) {
        binding.switchAppearanceFollowSystem.setChecked(!binding.switchAppearanceFollowSystem.isChecked());
      }
    } else if (id == R.id.linear_appearance_white_text) {
      binding.switchAppearanceWhiteText.setChecked(!binding.switchAppearanceWhiteText.isChecked());
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_appearance_night_mode) {
      ViewUtil.startIcon(binding.imageAppearanceNightMode);
      performHapticClick();
      binding.switchAppearanceFollowSystem.setEnabled(isChecked);
      binding.linearAppearanceFollowSystem.setEnabled(isChecked);
      binding.linearAppearanceFollowSystemContainer.animate()
          .alpha(isChecked ? 1 : 0.5f)
          .setDuration(200)
          .start();
      getSharedPrefs().edit().putBoolean(PREF.NIGHT_MODE, isChecked).apply();
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> binding.imageAppearanceNightMode.setImageResource(
              isChecked
                  ? R.drawable.ic_round_dark_mode_to_light_mode_anim
                  : R.drawable.ic_round_light_mode_to_dark_mode_anim
          ),
          300
      );
      activity.requestThemeRefresh();
    } else if (id == R.id.switch_appearance_follow_system) {
      ViewUtil.startIcon(binding.imageAppearanceFollowSystem);
      performHapticClick();
      getSharedPrefs().edit().putBoolean(PREF.FOLLOW_SYSTEM, isChecked).apply();
      activity.requestThemeRefresh();
    } else if (id == R.id.switch_appearance_white_text) {
      performHapticClick();
      getSharedPrefs().edit().putBoolean(PREF.USE_WHITE_TEXT, isChecked).apply();
      activity.requestThemeRefresh();
    }
  }

  private void refreshSelectionTheme(String selection, boolean animated) {
    binding.linearAppearanceVariantPixel.setVisibility(View.GONE);
    binding.linearAppearanceVariantJohanna.setVisibility(View.GONE);
    binding.linearAppearanceVariantReiko.setVisibility(View.GONE);
    binding.linearAppearanceVariantAnthony.setVisibility(View.GONE);

    MaterialCardView mcv1 = binding.cardAppearancePixel;
    MaterialCardView mcv2 = binding.cardAppearanceJohanna;
    MaterialCardView mcv3 = binding.cardAppearanceReiko;
    MaterialCardView mcv4 = binding.cardAppearanceAnthony;
    MaterialCardView mcvSelected = null;

    switch (selection) {
      case WALLPAPER.PIXEL:
        mcvSelected = binding.cardAppearancePixel;
        binding.linearAppearanceVariantPixel.setVisibility(View.VISIBLE);
        break;
      case WALLPAPER.JOHANNA:
        mcvSelected = binding.cardAppearanceJohanna;
        binding.linearAppearanceVariantJohanna.setVisibility(View.VISIBLE);
        break;
      case WALLPAPER.REIKO:
        mcvSelected = binding.cardAppearanceReiko;
        binding.linearAppearanceVariantReiko.setVisibility(View.VISIBLE);
        break;
      case WALLPAPER.ANTHONY:
        mcvSelected = binding.cardAppearanceAnthony;
        binding.linearAppearanceVariantAnthony.setVisibility(View.VISIBLE);
        break;
    }
    if (mcvSelected == null || mcvSelected.isChecked()) {
      return;
    }
    ViewUtil.setChecked(false, mcv1, mcv2, mcv3, mcv4);
    mcvSelected.setChecked(true);

    if (animated) {
      ViewUtil.startIcon(binding.imageAppearanceWallpaper);
      ViewUtil.startIcon(mcvSelected.getCheckedIcon());
      getSharedPrefs().edit().putString(PREF.WALLPAPER, selection).apply();
      activity.requestThemeRefresh();
    }
  }

  private void refreshSelectionVariant(String wallpaper, String selection, boolean animated) {
    MaterialCardView mcv1, mcv2, mcv3, mcv4, mcv5, mcvSelected;
    mcvSelected = mcv1 = mcv2 = mcv3 = mcv4 = mcv5 = null;

    switch (wallpaper) {
      case WALLPAPER.PIXEL: {
        mcv1 = binding.cardAppearancePixel1;
        mcv2 = binding.cardAppearancePixel2;
        mcv3 = binding.cardAppearancePixel3;
        mcv4 = binding.cardAppearancePixel4;
        mcv5 = binding.cardAppearancePixel5;
        switch (selection) {
          case VARIANT.PIXEL1:
            mcvSelected = binding.cardAppearancePixel1;
            break;
          case VARIANT.PIXEL2:
            mcvSelected = binding.cardAppearancePixel2;
            break;
          case VARIANT.PIXEL3:
            mcvSelected = binding.cardAppearancePixel3;
            break;
          case VARIANT.PIXEL4:
            mcvSelected = binding.cardAppearancePixel4;
            break;
          case VARIANT.PIXEL5:
            mcvSelected = binding.cardAppearancePixel5;
            break;
        }
      }
      break;
      case WALLPAPER.JOHANNA: {
        mcv1 = binding.cardAppearanceJohanna1;
        switch (selection) {
          case VARIANT.JOHANNA1:
            mcvSelected = binding.cardAppearanceJohanna1;
            break;
        }
      }
      break;
      case WALLPAPER.REIKO: {
        mcv1 = binding.cardAppearanceReiko1;
        mcv2 = binding.cardAppearanceReiko2;
        switch (selection) {
          case VARIANT.REIKO1:
            mcvSelected = binding.cardAppearanceReiko1;
            break;
          case VARIANT.REIKO2:
            mcvSelected = binding.cardAppearanceReiko2;
            break;
        }
      }
      break;
      case WALLPAPER.ANTHONY: {
        mcv1 = binding.cardAppearanceAnthony1;
        switch (selection) {
          case VARIANT.ANTHONY1:
            mcvSelected = binding.cardAppearanceAnthony1;
            break;
        }
      }
      break;
    }
    if (mcvSelected == null || mcvSelected.isChecked()) {
      return;
    }
    ViewUtil.setChecked(false, mcv1, mcv2, mcv3, mcv4, mcv5);
    mcvSelected.setChecked(true);

    if (animated) {
      ViewUtil.startIcon(binding.imageAppearanceVariant);
      ViewUtil.startIcon(mcvSelected.getCheckedIcon());
      getSharedPrefs().edit().putString(Constants.VARIANT_PREFIX + wallpaper, selection).apply();
      activity.requestThemeRefresh();
    }
  }
}