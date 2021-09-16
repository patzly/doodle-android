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

package xyz.zedler.patrick.doodle.wallpaper;

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

public abstract class BaseWallpaper {

  public static class WallpaperVariant {
    private final int svgResId;
    private int primaryColor, secondaryColor, tertiaryColor;
    private final boolean isDarkTextSupported;

    public WallpaperVariant(
        @RawRes int resId,
        @NonNull String primary,
        @Nullable String secondary,
        @Nullable String tertiary,
        boolean isDarkTextSupported
    ) {
      svgResId = resId;
      primaryColor = Color.parseColor(primary);
      if (secondary != null) {
        secondaryColor = Color.parseColor(secondary);
      }
      if (tertiary != null) {
        tertiaryColor = Color.parseColor(tertiary);
      }
      this.isDarkTextSupported = isDarkTextSupported;
    }

    public int getSvgResId() {
      return svgResId;
    }

    public int getPrimaryColor() {
      return primaryColor;
    }

    @RequiresApi(api = VERSION_CODES.O_MR1)
    public WallpaperColors getWallpaperColors(boolean useWhiteText) {
      /*if (VERSION.SDK_INT >= 31) {
        int hints = 0;
        if (!useWhiteText && isDarkTextSupported) {
          hints |= WallpaperColors.HINT_SUPPORTS_DARK_THEME;
          hints |= WallpaperColors.HINT_SUPPORTS_DARK_TEXT;
        }
        return new WallpaperColors(
            Color.valueOf(primaryColor),
            secondaryColor != 0 ? Color.valueOf(secondaryColor) : null,
            tertiaryColor != 0 ? Color.valueOf(tertiaryColor) : null,
            hints
        );
      } else {
        */if (useWhiteText) {
          float[] hsl = new float[3];
          ColorUtils.colorToHSL(primaryColor, hsl);
          hsl[2] = 0.7f;
          primaryColor = ColorUtils.HSLToColor(hsl);
        }
        // Fix required for older versions, color constructor does not work here
        // Bitmap is more efficient than Drawable here because Drawable would be converted to Bitmap
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        new Canvas(bitmap).drawColor(primaryColor);
        return WallpaperColors.fromBitmap(bitmap);
      //}
    }
  }

  @NonNull
  public abstract String getName();

  public abstract int getThumbnailResId();

  @NonNull
  public abstract WallpaperVariant[] getVariants();

  @NonNull
  public abstract WallpaperVariant[] getDarkVariants();
}
