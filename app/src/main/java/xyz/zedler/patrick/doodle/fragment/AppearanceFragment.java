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

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentAppearanceBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.wallpaper.AnthonyWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper.WallpaperVariant;
import xyz.zedler.patrick.doodle.wallpaper.FloralWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.FogWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.JohannaWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.LeafyWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.MonetWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.PixelWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.ReikoWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.StoneWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.WaterWallpaper;

public class AppearanceFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener {

  private final static String TAG = AppearanceFragment.class.getSimpleName();

  private FragmentAppearanceBinding binding;
  private MainActivity activity;
  private BaseWallpaper currentWallpaper;
  private WallpaperVariant currentVariant;
  private int currentVariantIndex;

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

    binding.toolbarAppearance.setNavigationOnClickListener(v -> {
      if (getViewUtil().isClickEnabled()) {
        performHapticClick();
        getNavController().navigateUp();
      }
    });
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

    binding.switchAppearanceNightMode.setChecked(
        getSharedPrefs().getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE)
    );

    binding.switchAppearanceFollowSystem.setChecked(
        getSharedPrefs().getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM)
    );
    binding.switchAppearanceFollowSystem.setEnabled(binding.switchAppearanceNightMode.isChecked());
    binding.linearAppearanceFollowSystem.setEnabled(binding.switchAppearanceNightMode.isChecked());
    binding.linearAppearanceFollowSystem.setAlpha(
        binding.switchAppearanceNightMode.isChecked() ? 1 : 0.5f
    );

    binding.imageAppearanceNightMode.setImageResource(
        getSharedPrefs().getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE)
            ? R.drawable.ic_round_dark_mode_to_light_mode_anim
            : R.drawable.ic_round_light_mode_to_dark_mode_anim
    );

    binding.switchAppearanceWhiteText.setChecked(
        getSharedPrefs().getBoolean(PREF.USE_WHITE_TEXT, DEF.USE_WHITE_TEXT)
    );

    setUpDesignSelections();
    if (DynamicColors.isDynamicColorAvailable()) {
      setUpColorsContainer();
    } else {
      binding.linearAppearanceColors.setVisibility(View.GONE);
    }

    ViewUtil.setOnClickListeners(
        this,
        // Other
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
    if (id == R.id.linear_appearance_night_mode) {
      binding.switchAppearanceNightMode.setChecked(!binding.switchAppearanceNightMode.isChecked());
    } else if (id == R.id.linear_appearance_follow_system) {
      if (binding.switchAppearanceNightMode.isChecked()) {
        binding.switchAppearanceFollowSystem.setChecked(
            !binding.switchAppearanceFollowSystem.isChecked()
        );
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
      binding.linearAppearanceFollowSystem.animate()
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

  private void setUpDesignSelections() {
    BaseWallpaper[] baseWallpapers;
    ViewGroup container;

    for (int i = 0; i < 3; i++) {
      if (i == 0) {
        baseWallpapers = new BaseWallpaper[]{
            new PixelWallpaper(),
            new JohannaWallpaper(),
            new ReikoWallpaper(),
            new AnthonyWallpaper()
        };
        container = binding.linearAppearanceWallpaperContainerDoodle;
      } else if (i == 1) {
        baseWallpapers = new BaseWallpaper[]{
            new WaterWallpaper(),
            new StoneWallpaper(),
            new FloralWallpaper(),
            new MonetWallpaper()
        };
        container = binding.linearAppearanceWallpaperContainerMonet;
      } else {
        baseWallpapers = new BaseWallpaper[]{
            new LeafyWallpaper(),
            new FogWallpaper()
        };
        container = binding.linearAppearanceWallpaperContainerAnna;
      }

      for (BaseWallpaper wallpaper : baseWallpapers) {
        MaterialCardView card = getNewSelectionCard(activity);
        ImageView thumbnail = new ImageView(activity);
        thumbnail.setLayoutParams(
            new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        );
        thumbnail.setImageResource(wallpaper.getThumbnailResId());
        card.addView(thumbnail);
        card.setOnClickListener(v -> {
          if (!card.isChecked()) {
            ViewUtil.startIcon(binding.imageAppearanceWallpaper);
            ViewUtil.startIcon(card.getCheckedIcon());
            performHapticClick();
            uncheckAllChildren(
                binding.linearAppearanceWallpaperContainerDoodle,
                binding.linearAppearanceWallpaperContainerMonet,
                binding.linearAppearanceWallpaperContainerAnna
            );
            card.setChecked(true);
            int oldCount = currentWallpaper != null ? currentWallpaper.getVariants().length : 0;
            currentWallpaper = wallpaper;
            refreshVariantSelection(oldCount, wallpaper, true);
            getSharedPrefs().edit().putString(PREF.WALLPAPER, wallpaper.getName()).apply();
            activity.requestThemeRefresh();
          }
        });

        boolean isSelected = getSharedPrefs().getString(
            PREF.WALLPAPER, WALLPAPER.PIXEL
        ).equals(wallpaper.getName());
        card.setChecked(isSelected);
        if (isSelected) {
          int oldCount = currentWallpaper != null ? currentWallpaper.getVariants().length : 0;
          currentWallpaper = wallpaper;
          refreshVariantSelection(oldCount, wallpaper, false);
        }
        container.addView(card);
      }
    }
  }

  private void refreshVariantSelection(int oldCount, BaseWallpaper wallpaper, boolean animated) {
    if (animated) {
      if (oldCount == wallpaper.getVariants().length) {
        replaceVariantContainer(wallpaper, true);
      } else {
        binding.linearAppearanceVariantContainer.animate().alpha(0).withEndAction(() -> {
          replaceVariantContainer(wallpaper, false);
          binding.linearAppearanceVariantContainer.animate().alpha(1).setDuration(150).start();
        }).setDuration(150).start();
      }
    } else {
      binding.linearAppearanceVariantContainer.setAlpha(1);
      replaceVariantContainer(wallpaper, oldCount == wallpaper.getVariants().length);
    }
  }

  private void replaceVariantContainer(BaseWallpaper wallpaper, boolean sameCount) {
    if (!sameCount) {
      binding.linearAppearanceVariantContainer.removeAllViews();
    }
    boolean isNightMode = isWallpaperNightMode();
    for (int i = 0; i < wallpaper.getVariants().length; i++) {
      final int iFinal = i;
      WallpaperVariant variant = isNightMode
          ? wallpaper.getDarkVariants()[iFinal]
          : wallpaper.getVariants()[iFinal];
      WallpaperVariant variantLight = wallpaper.getVariants()[iFinal];

      MaterialCardView card;
      if (sameCount) {
        MaterialCardView child
            = (MaterialCardView) binding.linearAppearanceVariantContainer.getChildAt(i);
        card = child != null ? child : getNewSelectionCard(activity);
      } else {
        card = getNewSelectionCard(activity);
      }

      if (sameCount) {
        ValueAnimator animator = ValueAnimator.ofArgb(
            card.getCardBackgroundColor().getDefaultColor(), variantLight.getPrimaryColor()
        );
        animator.addUpdateListener(
            animation -> card.setCardBackgroundColor((int) animation.getAnimatedValue()));
        animator.setDuration(300);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
      } else {
        card.setCardBackgroundColor(variantLight.getPrimaryColor());
      }
      card.setOnClickListener(v -> {
        if (!card.isChecked()) {
          ViewUtil.startIcon(binding.imageAppearanceVariant);
          ViewUtil.startIcon(card.getCheckedIcon());
          performHapticClick();
          uncheckAllChildren(binding.linearAppearanceVariantContainer);
          card.setChecked(true);
          currentVariant = variant;
          currentVariantIndex = iFinal;
          if (DynamicColors.isDynamicColorAvailable()) {
            refreshColors(); // reload theme colors
          }
          getSharedPrefs().edit()
              .putInt(Constants.VARIANT_PREFIX + wallpaper.getName(), iFinal)
              .apply();
          activity.requestThemeRefresh();
        }
      });
      boolean isSelected = getSharedPrefs().getInt(
          Constants.VARIANT_PREFIX + wallpaper.getName(), 0
      ) == iFinal;
      card.setChecked(isSelected);
      if (isSelected) {
        currentVariant = variant;
        currentVariantIndex = iFinal;
      }
      if (!sameCount) {
        binding.linearAppearanceVariantContainer.addView(card);
      }
    }
    if (DynamicColors.isDynamicColorAvailable()) {
      refreshColors(); // reload theme colors
    }
  }

  public static MaterialCardView getNewSelectionCard(Activity activity) {
    int size = SystemUiUtil.dpToPx(activity, 48);
    MaterialCardView card = new MaterialCardView(activity);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
    if (ResUtil.isLayoutRtl(activity)) {
      params.leftMargin = SystemUiUtil.dpToPx(activity, 12);
    } else {
      params.rightMargin = SystemUiUtil.dpToPx(activity, 12);
    }
    card.setLayoutParams(params);
    card.setCheckable(true);
    card.setStrokeColor(ResUtil.getColorAttr(activity, R.attr.colorOutline));
    card.setStrokeWidth(SystemUiUtil.dpToPx(activity, 2));
    card.setRippleColorResource(R.color.selector_highlight);
    card.setCardBackgroundColor(ResUtil.getBgColor(activity));
    card.setCheckedIcon(
        ResourcesCompat.getDrawable(
            activity.getResources(), R.drawable.ic_round_check_circle_anim, null
        )
    );
    card.setCheckedIconTint(null);
    card.setCheckedIconSize(size);
    card.setCheckedIconMargin(0);
    card.setCardElevation(0);
    card.setRadius(SystemUiUtil.dpToPx(activity, 16));
    return card;
  }

  private void setUpColorsContainer() {
    binding.linearAppearanceColorsContainer.removeAllViews();
    for (int i = 0; i < 3; i++) {
      final int iFinal = i;
      MaterialCardView card = getNewSelectionCard(activity);
      card.setCardBackgroundColor(Color.BLACK);
      card.setOnClickListener(v -> {
        ViewUtil.startIcon(binding.imageAppearanceColors);
        performHapticClick();
        AppearanceFragmentDirections.ActionAppearanceToColorsDialog action
            = AppearanceFragmentDirections.actionAppearanceToColorsDialog();
        switch (iFinal) {
          case 1:
            action.setTitle(R.string.appearance_colors_secondary);
            break;
          case 2:
            action.setTitle(R.string.appearance_colors_tertiary);
            break;
          default:
            action.setTitle(R.string.appearance_colors_primary);
            break;
        }
        action.setColors(TextUtils.join(" ", currentVariant.getColors()));
        action.setSelection(
            getSharedPrefs().getString(
                Constants.getThemeColorPref(
                    currentWallpaper.getName(), currentVariantIndex, iFinal, isWallpaperNightMode()
                ),
                currentVariant.getColorHex(iFinal)
            )
        );
        action.setPriority(iFinal);
        getNavController().navigate(action);
      });
      binding.linearAppearanceColorsContainer.addView(card);
      refreshColor(iFinal, false);
    }
  }

  private void refreshColor(int priority, boolean animated) {
    String colorHex = getSharedPrefs().getString(
        Constants.getThemeColorPref(
            currentWallpaper.getName(), currentVariantIndex, priority, isWallpaperNightMode()
        ),
        null
    );
    int color;
    if (colorHex != null) {
      color = Color.parseColor(colorHex);
    } else {
      switch (priority) {
        case 1:
          color = currentVariant.getSecondaryColor();
          break;
        case 2:
          color = currentVariant.getTertiaryColor();
          break;
        default:
          color = currentVariant.getPrimaryColor();
          break;
      }
    }
    MaterialCardView card
        = (MaterialCardView) binding.linearAppearanceColorsContainer.getChildAt(priority);
    if (card == null) {
      return;
    }
    if (animated) {
      ValueAnimator animator = ValueAnimator.ofArgb(
          card.getCardBackgroundColor().getDefaultColor(), color
      );
      animator.addUpdateListener(
          animation -> card.setCardBackgroundColor((int) animation.getAnimatedValue()));
      animator.setDuration(300);
      animator.setInterpolator(new FastOutSlowInInterpolator());
      animator.start();
    } else {
      card.setCardBackgroundColor(color);
    }
  }

  private void refreshColors() {
    refreshColor(0, true);
    refreshColor(1, true);
    refreshColor(2, true);
  }

  public void setColor(int priority, String color) {
    getSharedPrefs().edit().putString(
        Constants.getThemeColorPref(
            currentWallpaper.getName(), currentVariantIndex, priority, isWallpaperNightMode()
        ),
        color
    ).apply();
    refreshColor(priority, true);
    activity.requestThemeRefresh();
  }

  private boolean isWallpaperNightMode() {
    boolean nightMode = getSharedPrefs().getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
    boolean followSystem = getSharedPrefs().getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM);
    if (nightMode && !followSystem) {
      return true;
    }
    int flags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightMode && flags == Configuration.UI_MODE_NIGHT_YES;
  }

  public static void uncheckAllChildren(ViewGroup... viewGroups) {
    for (ViewGroup viewGroup : viewGroups) {
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View child = viewGroup.getChildAt(i);
        if (child instanceof MaterialCardView) {
          ((MaterialCardView) child).setChecked(false);
        }
      }
    }
  }
}