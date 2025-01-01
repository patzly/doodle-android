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

import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;

public class OrioleWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.ORIOLE;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_oriole;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    svgDrawable.requireObjectById("circle").isRotatable = true;
    svgDrawable.requireObjectById("oval").isRotatable = true;
    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_oriole1,
            "#ffad8d",
            "#ffc7b2",
            "#392d28",
            new String[]{},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_oriole2,
            "#bdd0ff",
            "#e3ecff",
            "#2e2e2e",
            new String[]{},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_oriole3,
            "#adff90",
            "#d6ffb2",
            "#1b1e1b",
            new String[]{"#b3eaa0"},
            true,
            false
        )
    };
  }

  @NonNull
  @Override
  public WallpaperVariant[] getDarkVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_oriole1_dark,
            "#d88071",
            "#ffad8d",
            "#4a4a4a",
            new String[]{},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_oriole2_dark,
            "#434380",
            "#bdd0ff",
            "#363c4a",
            new String[]{},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_oriole3_dark,
            "#44885d",
            "#adff90",
            "#444a44",
            new String[]{"#d6ffb2"},
            false,
            true
        )
    };
  }
}
