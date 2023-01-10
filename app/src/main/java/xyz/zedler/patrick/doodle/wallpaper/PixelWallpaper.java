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
 * Copyright (c) 2019-2023 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.wallpaper;

import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;

public class PixelWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.PIXEL;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_pixel;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    svgDrawable.requireObjectById("moon").isRotatable = true;

    svgDrawable.requireObjectById("arc").isRotatable = true;
    svgDrawable.requireObjectById("arc").pivotOffsetX = 100;
    svgDrawable.requireObjectById("arc").pivotOffsetY = 180;

    svgDrawable.requireObjectById("poly").isRotatable = true;
    svgDrawable.requireObjectById("poly").pivotOffsetX = -40;
    svgDrawable.requireObjectById("poly").pivotOffsetY = 80;
    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_pixel1,
            "#232323",
            "#f2c5b1",
            "#bdd6bd",
            new String[]{
                "#f58551", "#b5b1a3", "#9393c1", "#fde8ca",
                "#3a3837", "#ddc1b3", "#ff9052"
            },
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel2,
            "#e0dcd3",
            "#ffb9a1",
            "#f4e3c9",
            new String[]{"#f58551", "#ffffff", "#ff6550", "#ffe5c5", "#b5afa1", "#fdea98"},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel3,
            "#f98a6b",
            "#e5e1a3",
            "#bcddba",
            new String[]{"#f7d2c4"},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel4,
            "#eae5bf",
            "#789f8a",
            "#e06a4e",
            new String[]{"#deb853", "#ffffff", "#e69986", "#0a373a"},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel5,
            "#fff5ec",
            "#052464",
            "#fe765e",
            new String[]{"#dadce0"},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel6,
            "#282a36",
            "#ffb86c",
            "#ff79c6",
            new String[]{"#f1fa8c", "#6272a4", "#8be9fd", "#50fa7b", "#bd93f9", "#ff5555"},
            false,
            true
        )
    };
  }

  @NonNull
  @Override
  public WallpaperVariant[] getDarkVariants() {
    WallpaperVariant pixelOriginalDark = new WallpaperVariant(
        R.raw.wallpaper_pixel123_dark,
        "#272628",
        "#ff9052",
        "#bdd6bd",
        new String[]{"#9393c1", "#ddc1b3"},
        false,
        true
    );
    return new WallpaperVariant[]{
        pixelOriginalDark,
        pixelOriginalDark,
        pixelOriginalDark,
        new WallpaperVariant(
            R.raw.wallpaper_pixel4_dark,
            "#272628",
            "#e06a4e",
            "#6c8e7a",
            new String[]{"#ddc179", "#1d3836", "#0f0f0f"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel5_dark,
            "#272628",
            "#d4634f",
            "#324b84",
            new String[]{"#1a294a", "#dfd9d0", "#0f0f0f"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel6_dark,
            "#282a36",
            "#ffb86c",
            "#ff79c6",
            new String[]{"#f1fa8c", "#6272a4", "#8be9fd", "#50fa7b", "#bd93f9", "#ff5555"},
            false,
            true
        )
    };
  }
}
