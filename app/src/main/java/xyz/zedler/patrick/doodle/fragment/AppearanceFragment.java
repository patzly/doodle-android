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

import android.animation.ValueAnimator;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.NIGHT_MODE;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.RANDOM;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentAppearanceBinding;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.DailyUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;
import xyz.zedler.patrick.doodle.view.SelectionCardView;
import xyz.zedler.patrick.doodle.wallpaper.AnthonyWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.AutumnWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper.WallpaperVariant;
import xyz.zedler.patrick.doodle.wallpaper.FloralWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.FogWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.JohannaWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.LeafyWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.LeavesWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.MonetWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.OrioleWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.PixelWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.ReikoWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.SandWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.StoneWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.WaterWallpaper;

public class AppearanceFragment extends BaseFragment
    implements OnClickListener, OnCheckedChangeListener, OnButtonCheckedListener {

  private static final String TAG = AppearanceFragment.class.getSimpleName();

  private FragmentAppearanceBinding binding;
  private MainActivity activity;
  private BaseWallpaper currentWallpaper;
  private WallpaperVariant currentVariant;
  private int currentVariantIndex;
  private boolean isWallpaperNightMode;
  private String randomMode;
  private Set<String> randomList = new HashSet<>();
  private final HashMap<String, SelectionCardView> designSelections = new HashMap<>();
  private DailyUtil dailyUtil;
  private java.text.DateFormat dateFormatPref, dateFormatDisplay;

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
    systemBarBehavior.setScroll(binding.scrollAppearance, binding.constraintAppearance);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarAppearance, binding.scrollAppearance, true
    );

    dailyUtil = new DailyUtil(activity);
    dateFormatPref = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    dateFormatDisplay = DateFormat.getTimeFormat(activity);

    ViewUtil.centerToolbarTitleOnLargeScreens(binding.toolbarAppearance);
    binding.toolbarAppearance.setNavigationOnClickListener(getNavigationOnClickListener());
    binding.toolbarAppearance.setOnMenuItemClickListener(getOnMenuItemClickListener());

    boolean isOneUiWithDynamicColors = activity.isOneUiWithDynamicColors();
    binding.cardAppearanceOneUi.setVisibility(isOneUiWithDynamicColors ? View.VISIBLE : View.GONE);

    binding.buttonAppearanceSetStatic.setOnClickListener(v -> {
      WallpaperManager manager = WallpaperManager.getInstance(activity);
      SharedPreferences sharedPrefs = getSharedPrefs();
      BaseWallpaper wallpaper = Constants.getWallpaper(
          sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER)
      );
      int variantIndex = sharedPrefs.getInt(
          Constants.VARIANT_PREFIX + wallpaper.getName(), 0
      );
      // This method is more efficient
      if (variantIndex >= wallpaper.getVariants().length
          || variantIndex >= wallpaper.getDarkVariants().length) {
        variantIndex = 0;
      }

      WallpaperVariant variant;
      SvgDrawable svgDrawable;
      if (isWallpaperNightMode()) {
        variant = wallpaper.getDarkVariants()[variantIndex];
        svgDrawable = wallpaper.getPreparedSvg(
            new SvgDrawable(activity, variant.getSvgResId()), variantIndex, true
        );
      } else {
        variant = wallpaper.getVariants()[variantIndex];
        svgDrawable = wallpaper.getPreparedSvg(
            new SvgDrawable(activity, variant.getSvgResId()), variantIndex, false
        );
      }
      if (svgDrawable == null) {
        // Prevent NullPointerExceptions
        svgDrawable = wallpaper.getPreparedSvg(
            new SvgDrawable(activity, R.raw.wallpaper_pixel1), 1, false
        );
      }

      float scale = getSharedPrefs().getFloat(
          PREF.SCALE, SvgDrawable.getDefaultScale(getContext())
      );
      svgDrawable.setScale(scale);

      Bitmap bitmap = Bitmap.createBitmap(
          SystemUiUtil.getDisplayWidth(activity),
          SystemUiUtil.getDisplayHeight(activity),
          Bitmap.Config.ARGB_8888
      );
      Canvas canvas = new Canvas(bitmap);
      svgDrawable.draw(canvas);
      try {
        manager.setBitmap(bitmap);
      } catch (IOException e) {
        Log.e(TAG, "onViewCreated: ", e);
      }
    });

    int idNightMode;
    switch (getSharedPrefs().getInt(PREF.NIGHT_MODE, DEF.NIGHT_MODE)) {
      case NIGHT_MODE.ON:
        idNightMode = R.id.button_appearance_night_mode_on;
        break;
      case NIGHT_MODE.OFF:
        idNightMode = R.id.button_appearance_night_mode_off;
        break;
      default:
        idNightMode = R.id.button_appearance_night_mode_auto;
        break;
    }
    binding.toggleAppearanceNightMode.check(idNightMode);
    binding.toggleAppearanceNightMode.addOnButtonCheckedListener(this);
    isWallpaperNightMode = isWallpaperNightMode();
    binding.imageAppearanceNightMode.setImageResource(
        isWallpaperNightMode
            ? R.drawable.ic_round_dark_mode_to_light_mode_anim
            : R.drawable.ic_round_light_mode_to_dark_mode_anim
    );

    binding.switchAppearanceDarkText.setChecked(
        getSharedPrefs().getBoolean(PREF.USE_DARK_TEXT, DEF.USE_DARK_TEXT)
    );
    binding.switchAppearanceLightText.setChecked(
        getSharedPrefs().getBoolean(PREF.FORCE_LIGHT_TEXT, DEF.FORCE_LIGHT_TEXT)
    );
    if (VERSION.SDK_INT >= 31) {
      binding.linearAppearanceDarkText.setVisibility(View.VISIBLE);
      binding.linearAppearanceLightText.setVisibility(View.GONE);
    } else {
      binding.linearAppearanceDarkText.setVisibility(View.GONE);
      binding.linearAppearanceLightText.setVisibility(View.VISIBLE);
    }

    randomMode = getSharedPrefs().getString(PREF.RANDOM, DEF.RANDOM);
    ViewUtil.setEnabledAlpha(
        randomMode.equals(RANDOM.OFF),
        false,
        binding.linearAppearanceVariant,
        binding.linearAppearanceColors
    );

    int idRandomMode;
    switch (randomMode) {
      case RANDOM.DAILY:
        idRandomMode = R.id.button_appearance_random_daily;
        break;
      case RANDOM.SCREEN_OFF:
        idRandomMode = R.id.button_appearance_random_screen_off;
        break;
      default:
        idRandomMode = R.id.button_appearance_random_off;
        break;
    }
    binding.toggleAppearanceRandom.check(idRandomMode);
    binding.toggleAppearanceRandom.addOnButtonCheckedListener(this);

    String time = getSharedPrefs().getString(PREF.DAILY_TIME, DEF.DAILY_TIME);
    try {
      Date date = dateFormatPref.parse(time);
      if (date != null) {
        time = dateFormatDisplay.format(date);
      }
    } catch (ParseException e) {
      Log.e(TAG, "onViewCreated: " + e);
    }
    binding.buttonAppearanceRandomDaily.setText(getString(R.string.appearance_random_daily, time));
    binding.buttonAppearanceRandomTime.setEnabled(randomMode.equals(RANDOM.DAILY));

    setUpDesignSelections();
    randomList = getSharedPrefs().getStringSet(PREF.RANDOM_LIST, DEF.RANDOM_LIST);

    if (!randomMode.equals(RANDOM.OFF)) {
      refreshDesignSelections();
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      if (isOneUiWithDynamicColors) {
        binding.textAppearanceColorsDescription.setText(
            R.string.appearance_colors_description_one_ui
        );
        binding.textAppearanceColorsDescription.setTextColor(
            ResUtil.getColorAttr(activity, R.attr.colorError)
        );
        binding.textAppearanceColorsDescription.setAlpha(1);
      } else {
        binding.textAppearanceColorsDescription.setText(
            DynamicColors.isDynamicColorAvailable()
                ? R.string.appearance_colors_description_dynamic
                : R.string.appearance_colors_description
        );
      }
      setUpColorsContainer();
    } else {
      binding.linearAppearanceColors.setVisibility(View.GONE);
    }

    ViewUtil.setOnClickListeners(
        this,
        // Other
        binding.linearAppearanceDarkText,
        binding.linearAppearanceLightText,
        binding.buttonAppearanceRandomTime
    );

    ViewUtil.setOnCheckedChangeListeners(
        this,
        binding.switchAppearanceDarkText,
        binding.switchAppearanceLightText
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.linear_appearance_dark_text) {
      binding.switchAppearanceDarkText.setChecked(
          !binding.switchAppearanceDarkText.isChecked()
      );
    } else if (id == R.id.linear_appearance_light_text) {
      binding.switchAppearanceLightText.setChecked(
          !binding.switchAppearanceLightText.isChecked()
      );
    } else if (id == R.id.button_appearance_random_time) {
      openTimePicker();
      performHapticClick();
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == R.id.switch_appearance_dark_text) {
      getSharedPrefs().edit().putBoolean(PREF.USE_DARK_TEXT, isChecked).apply();
      activity.requestThemeRefresh(false);
      performHapticClick();
      ViewUtil.startIcon(binding.imageAppearanceDarkText);
    } else if (id == R.id.switch_appearance_light_text) {
      getSharedPrefs().edit().putBoolean(PREF.FORCE_LIGHT_TEXT, isChecked).apply();
      activity.requestThemeRefresh(false);
      performHapticClick();
      ViewUtil.startIcon(binding.imageAppearanceLightText);
    }
  }

  @Override
  public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
    if (!isChecked) {
      return;
    }
    performHapticClick();
    ViewUtil.startIcon(binding.imageAppearanceRandom);
    if (group.getId() == R.id.toggle_appearance_night_mode) {
      int pref;
      if (checkedId == R.id.button_appearance_night_mode_on) {
        pref = NIGHT_MODE.ON;
      } else if (checkedId == R.id.button_appearance_night_mode_off) {
        pref = NIGHT_MODE.OFF;
      } else {
        pref = NIGHT_MODE.AUTO;
      }
      getSharedPrefs().edit().putInt(PREF.NIGHT_MODE, pref).apply();
      activity.requestThemeRefresh(false);
      refreshDarkLightVariant();
      refreshColors();
      boolean isNewWallpaperNightMode = isWallpaperNightMode();
      if (isWallpaperNightMode != isNewWallpaperNightMode) {
        showMonetInfoIfRequired();
        ViewUtil.startIcon(binding.imageAppearanceNightMode);
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> binding.imageAppearanceNightMode.setImageResource(
                isNewWallpaperNightMode
                    ? R.drawable.ic_round_dark_mode_to_light_mode_anim
                    : R.drawable.ic_round_light_mode_to_dark_mode_anim
            ),
            300
        );
        isWallpaperNightMode = isNewWallpaperNightMode;
      }
    } else if (group.getId() == R.id.toggle_appearance_random) {
      if (checkedId == R.id.button_appearance_random_daily) {
        randomMode = RANDOM.DAILY;
      } else if (checkedId == R.id.button_appearance_random_screen_off) {
        randomMode = RANDOM.SCREEN_OFF;
      } else {
        randomMode = RANDOM.OFF;
      }
      SharedPreferences.Editor editor = getSharedPrefs().edit();
      editor.putString(PREF.RANDOM, randomMode);
      if (randomMode.equals(RANDOM.DAILY)) {
        editor.putBoolean(PREF.CHANGE_DAILY_NOW, true);
      }
      editor.apply();

      dailyUtil.setDailyEnabled(randomMode.equals(RANDOM.DAILY));
      activity.requestSettingsRefresh();
      activity.requestThemeRefresh(true);

      refreshDesignSelections();
      ViewUtil.setEnabledAlpha(
          randomMode.equals(RANDOM.OFF),
          true,
          binding.linearAppearanceVariant,
          binding.linearAppearanceColors
      );
      binding.buttonAppearanceRandomTime.setEnabled(randomMode.equals(RANDOM.DAILY));
      if (!randomMode.equals(RANDOM.OFF)) {
        Snackbar snackbar = activity.getSnackbar(R.string.msg_random, Snackbar.LENGTH_LONG);
        if (randomList.size() < Constants.getAllWallpapers().length) {
          snackbar.setAction(
              getString(R.string.action_select_all),
              view -> {
                if (binding == null) {
                  return;
                }
                randomList = new HashSet<>(Arrays.asList(Constants.getAllWallpapers()));
                getSharedPrefs().edit().putStringSet(PREF.RANDOM_LIST, randomList).apply();
                refreshDesignSelections();
                if (!randomMode.equals(RANDOM.OFF)) {
                  activity.requestSettingsRefresh();
                  activity.requestThemeRefresh(true);
                }
              }
          );
        }
        activity.showSnackbar(snackbar);
      }
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
            new FloralWallpaper(),
            new AutumnWallpaper(),
            new StoneWallpaper(),
            new WaterWallpaper(),
            new SandWallpaper(),
            new MonetWallpaper(),
            new OrioleWallpaper()
        };
        container = binding.linearAppearanceWallpaperContainerMonet;
      } else {
        baseWallpapers = new BaseWallpaper[]{
            new LeafyWallpaper(),
            new FogWallpaper(),
            new LeavesWallpaper()
        };
        container = binding.linearAppearanceWallpaperContainerAnna;
      }

      for (int wallpaperIndex = 0; wallpaperIndex < baseWallpapers.length; wallpaperIndex++) {
        BaseWallpaper wallpaper = baseWallpapers[wallpaperIndex];

        SelectionCardView card = new SelectionCardView(activity);
        card.setScrimEnabled(true, false);
        card.setCardImageResource(wallpaper.getThumbnailResId(), false);
        card.setOnClickListener(v -> {
          if (!randomMode.equals(RANDOM.OFF)) {
            if (card.isChecked() && randomList.size() == 1) {
              return;
            }
            card.setChecked(!card.isChecked());
            if (card.isChecked()) {
              randomList.add(wallpaper.getName());
            } else {
              randomList.remove(wallpaper.getName());
            }
            getSharedPrefs().edit().putStringSet(PREF.RANDOM_LIST, randomList).apply();
            activity.requestThemeRefresh(true);
            ViewUtil.startIcon(binding.imageAppearanceWallpaper);
            card.startCheckedIcon();
            performHapticClick();
          } else {
            if (card.isChecked()) {
              return;
            }
            ViewUtil.startIcon(binding.imageAppearanceWallpaper);
            card.startCheckedIcon();
            performHapticClick();
            ViewUtil.uncheckAllChildren(
                binding.linearAppearanceWallpaperContainerDoodle,
                binding.linearAppearanceWallpaperContainerMonet,
                binding.linearAppearanceWallpaperContainerAnna
            );
            card.setChecked(true);
            int oldCount = currentWallpaper != null ? currentWallpaper.getVariants().length : 0;
            currentWallpaper = wallpaper;
            refreshVariantSelection(oldCount, wallpaper, true);
            getSharedPrefs().edit().putString(PREF.WALLPAPER, wallpaper.getName()).apply();
            activity.requestThemeRefresh(true);
            showMonetInfoIfRequired();
          }
        });

        if (!randomMode.equals(RANDOM.OFF)) {
          if (randomList.contains(wallpaper.getName())) {
            card.setChecked(true);
          }
          if (wallpaperIndex == baseWallpapers.length - 1) {
            // Choose last selected wallpaper for variant selection setup
            int oldCount = currentWallpaper != null ? currentWallpaper.getVariants().length : 0;
            currentWallpaper = wallpaper;
            refreshVariantSelection(oldCount, wallpaper, false);
          }
        } else {
          boolean isSelected = getSharedPrefs().getString(
              PREF.WALLPAPER, WALLPAPER.PIXEL
          ).equals(wallpaper.getName());
          card.setChecked(isSelected);
          if (isSelected) {
            int oldCount = currentWallpaper != null ? currentWallpaper.getVariants().length : 0;
            currentWallpaper = wallpaper;
            refreshVariantSelection(oldCount, wallpaper, false);
          }
        }

        container.addView(card);
        designSelections.put(wallpaper.getName(), card);
      }

      if (!randomMode.equals(RANDOM.OFF)) {
        int oldCount = currentWallpaper != null ? currentWallpaper.getVariants().length : 0;
        currentWallpaper = Constants.getWallpaper(
            getSharedPrefs().getString(PREF.WALLPAPER, DEF.WALLPAPER)
        );
        refreshVariantSelection(oldCount, currentWallpaper, false);
      }
    }
  }

  private void refreshDesignSelections() {
    ViewUtil.uncheckAllChildren(
        binding.linearAppearanceWallpaperContainerDoodle,
        binding.linearAppearanceWallpaperContainerMonet,
        binding.linearAppearanceWallpaperContainerAnna
    );
    if (!randomMode.equals(RANDOM.OFF)) {
      for (String element : randomList) {
        SelectionCardView card = designSelections.get(element);
        if (card != null) {
          card.setChecked(true);
        }
      }
    } else {
      if (currentWallpaper == null) {
        return;
      }
      SelectionCardView card = designSelections.get(currentWallpaper.getName());
      if (card != null) {
        card.setChecked(true);
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
    if (binding == null) {
      return;
    }
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

      SelectionCardView card;
      if (sameCount) {
        SelectionCardView child
            = (SelectionCardView) binding.linearAppearanceVariantContainer.getChildAt(i);
        card = child != null ? child : new SelectionCardView(activity);
      } else {
        card = new SelectionCardView(activity);
      }

      if (sameCount) {
        ValueAnimator animator = ValueAnimator.ofArgb(
            card.getCardBackgroundColor().getDefaultColor(), variantLight.getPrimaryColor()
        );
        animator.addUpdateListener(animation -> {
          card.setCardBackgroundColor((int) animation.getAnimatedValue());
          card.setScrimEnabled(false, true);
        });
        animator.setDuration(300);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
      } else {
        card.setCardBackgroundColor(variantLight.getPrimaryColor());
        card.setScrimEnabled(false, true);
      }
      card.setOnClickListener(v -> {
        if (!randomMode.equals(RANDOM.OFF)) {
          activity.showSnackbar(
              activity.getSnackbar(
                  R.string.msg_random_warning, Snackbar.LENGTH_LONG
              ).setAction(
                  getString(R.string.action_deactivate), view -> {
                    if (binding != null) {
                      binding.toggleAppearanceRandom.check(R.id.button_appearance_random_off);
                    }
                  }
              )
          );
        } else if (!card.isChecked()) {
          ViewUtil.startIcon(binding.imageAppearanceVariant);
          card.startCheckedIcon();
          performHapticClick();
          ViewUtil.uncheckAllChildren(binding.linearAppearanceVariantContainer);
          card.setChecked(true);
          currentVariant = variant;
          currentVariantIndex = iFinal;
          refreshColors();
          getSharedPrefs().edit()
              .putInt(Constants.VARIANT_PREFIX + wallpaper.getName(), iFinal)
              .apply();
          activity.requestThemeRefresh(false);
          showMonetInfoIfRequired();
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
    refreshColors();
  }

  private void setUpColorsContainer() {
    binding.linearAppearanceColorsContainer.removeAllViews();
    for (int i = 0; i < 3; i++) {
      final int iFinal = i;
      SelectionCardView card = new SelectionCardView(activity);
      card.setScrimEnabled(true, false);
      card.setCardBackgroundColor(Color.BLACK);
      card.setOnClickListener(v -> {
        if (binding == null || currentWallpaper == null || currentVariant == null) {
          return;
        }
        if (!randomMode.equals(RANDOM.OFF)) {
          activity.showSnackbar(
              activity.getSnackbar(
                  R.string.msg_random_warning, Snackbar.LENGTH_LONG
              ).setAction(
                  getString(R.string.action_deactivate),
                  view -> binding.toggleAppearanceRandom.check(R.id.button_appearance_random_off)
              )
          );
          return;
        }
        card.startCheckedIcon();
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
        navigate(action);
      });
      binding.linearAppearanceColorsContainer.addView(card);
      refreshColor(iFinal, false);
    }
  }

  private void refreshColor(int priority, boolean animated) {
    if (binding == null || currentWallpaper == null || currentVariant == null) {
      return;
    }
    String colorHex = null;
    if (getSharedPrefs() != null) {
      colorHex = getSharedPrefs().getString(
          Constants.getThemeColorPref(
              currentWallpaper.getName(), currentVariantIndex, priority, isWallpaperNightMode()
          ),
          currentVariant.getColorHex(priority)
      );
    }
    int color;
    if (colorHex != null) {
      color = Color.parseColor(colorHex);
    } else {
      color = currentVariant.getColor(priority);
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      refreshColor(0, true);
      refreshColor(1, true);
      refreshColor(2, true);
    }
  }

  private void refreshDarkLightVariant() {
    int index = currentVariantIndex;
    if (index >= currentWallpaper.getVariants().length
        || index >= currentWallpaper.getDarkVariants().length) {
      index = 0;
    }
    currentVariant = isWallpaperNightMode()
        ? currentWallpaper.getDarkVariants()[index]
        : currentWallpaper.getVariants()[index];
  }

  private void showMonetInfoIfRequired() {
    if (activity == null || binding == null || !DynamicColors.isDynamicColorAvailable()
        || !LiveWallpaperService.isMainEngineRunning()) {
      return;
    }
    activity.showSnackbar(activity.getSnackbar(R.string.msg_apply_colors, Snackbar.LENGTH_LONG));
  }

  public String getColor(int priority, boolean custom) {
    String pref = Constants.getThemeColorPref(
      currentWallpaper.getName(), currentVariantIndex, priority, isWallpaperNightMode()
    ) + (custom ? "_custom" : "");
    return getSharedPrefs().getString(pref, "#000000");
  }

  public void setColor(int priority, String color, boolean custom) {
    String pref = Constants.getThemeColorPref(
      currentWallpaper.getName(), currentVariantIndex, priority, isWallpaperNightMode()
    );
    getSharedPrefs().edit().putString(pref, color).apply();
    if (custom)  {
      getSharedPrefs().edit().putString(pref + "_custom", color).apply();
    }
    refreshColor(priority, true);
    activity.requestThemeRefresh(false);
    showMonetInfoIfRequired();
    if (binding != null) {
      ViewUtil.startIcon(binding.imageAppearanceColors);
    }
  }

  private void openTimePicker() {
    Calendar calendar = Calendar.getInstance();
    String time = getSharedPrefs().getString(PREF.DAILY_TIME, DEF.DAILY_TIME);
    try {
      Date date = dateFormatPref.parse(time);
      if (date != null) {
        calendar.setTime(date);
      }
    } catch (ParseException e) {
      Log.e(TAG, "openTimePicker: " + e);
    }
    MaterialTimePicker picker = new MaterialTimePicker.Builder()
        .setTimeFormat(
            DateFormat.is24HourFormat(activity) ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H
        )
        .setHour(calendar.get(Calendar.HOUR_OF_DAY))
        .setMinute(calendar.get(Calendar.MINUTE))
        .setTheme(R.style.ThemeOverlay_Doodle_TimePicker)
        .build();
    picker.show(activity.getSupportFragmentManager(), "time");
    picker.addOnPositiveButtonClickListener(view -> {
      activity.performHapticClick();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
      calendar.set(Calendar.MINUTE, picker.getMinute());
      calendar.set(Calendar.SECOND, 0);
      String timeNew = dateFormatPref.format(calendar.getTime());
      getSharedPrefs().edit().putString(PREF.DAILY_TIME, timeNew).apply();
      dailyUtil.scheduleReminder(timeNew);
      if (binding != null) {
        binding.buttonAppearanceRandomDaily.setText(
            getString(
                R.string.appearance_random_daily, dateFormatDisplay.format(calendar.getTime())
            )
        );
        ViewUtil.startIcon(binding.imageAppearanceRandom);
      }
    });
    picker.addOnNegativeButtonClickListener(view -> activity.performHapticClick());
  }

  private boolean isWallpaperNightMode() {
    int nightMode = getSharedPrefs().getInt(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
    if (nightMode == NIGHT_MODE.ON) {
      return true;
    }
    int flags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightMode == NIGHT_MODE.AUTO && flags == Configuration.UI_MODE_NIGHT_YES;
  }
}