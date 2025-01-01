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

package xyz.zedler.patrick.doodle.wallpaper;

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;
import java.util.Arrays;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;

public abstract class BaseWallpaper {

  public static class WallpaperVariant {

    private final int svgResId;
    private final String primaryColor;
    private final String secondaryColor;
    private final String tertiaryColor;
    private final String[] otherColors;
    private final boolean isDarkTextSupported;
    private final boolean isDarkThemeSupported;

    public WallpaperVariant(
        @RawRes int resId,
        @NonNull String primary,
        @NonNull String secondary,
        @NonNull String tertiary,
        @NonNull String[] colors,
        boolean isDarkTextSupported,
        boolean isDarkThemeSupported
    ) {
      svgResId = resId;
      primaryColor = primary;
      secondaryColor = secondary;
      tertiaryColor = tertiary;
      this.otherColors = colors;
      this.isDarkTextSupported = isDarkTextSupported;
      this.isDarkThemeSupported = isDarkThemeSupported;
    }

    public int getSvgResId() {
      return svgResId;
    }

    public int getColor(int priority) {
      switch (priority) {
        case 1:
          return Color.parseColor(secondaryColor);
        case 2:
          return Color.parseColor(tertiaryColor);
        default:
          return Color.parseColor(primaryColor);
      }
    }

    public String getColorHex(int priority) {
      switch (priority) {
        case 1:
          return secondaryColor;
        case 2:
          return tertiaryColor;
        default:
          return primaryColor;
      }
    }

    public int getPrimaryColor() {
      return Color.parseColor(primaryColor);
    }

    public int getSecondaryColor() {
      return Color.parseColor(secondaryColor);
    }

    public int getTertiaryColor() {
      return Color.parseColor(tertiaryColor);
    }

    public String[] getColors() {
      String[] main = new String[]{primaryColor, secondaryColor, tertiaryColor};
      String[] all = Arrays.copyOf(main, main.length + otherColors.length);
      System.arraycopy(otherColors, 0, all, main.length, otherColors.length);
      return all;
    }

    @RequiresApi(api = VERSION_CODES.O_MR1)
    public WallpaperColors getWallpaperColors(
        int primary, int secondary, int tertiary, boolean darkText, boolean lightText
    ) {
      if (VERSION.SDK_INT >= 31) {
        int hints = 0;
        if (isDarkTextSupported && darkText) {
          hints |= WallpaperColors.HINT_SUPPORTS_DARK_TEXT;
        }
        if (isDarkThemeSupported) {
          hints |= WallpaperColors.HINT_SUPPORTS_DARK_THEME;
        }
        return new WallpaperColors(
            Color.valueOf(primary),
            secondary != 0 ? Color.valueOf(secondary) : null,
            tertiary != 0 ? Color.valueOf(tertiary) : null,
            hints
        );
      } else {
        if (lightText) {
          float[] hsl = new float[3];
          ColorUtils.colorToHSL(primary, hsl);
          hsl[2] = 0.7f;
          primary = ColorUtils.HSLToColor(hsl);
        }
        // Fix required for older versions, color constructor only calculates dark theme support
        // We need a way to set dark text support, the bitmap method calls the calculation method
        // Bitmap is more efficient than Drawable here because Drawable would be converted to Bitmap
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        new Canvas(bitmap).drawColor(primary);
        return WallpaperColors.fromBitmap(bitmap);
      }
    }
  }

  @NonNull
  public abstract String getName();

  public abstract int getThumbnailResId();

  @NonNull
  public abstract WallpaperVariant[] getVariants();

  @NonNull
  public abstract WallpaperVariant[] getDarkVariants();

  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    return svgDrawable;
  }

  public boolean isDepthStatic() {
    return false;
  }
}
