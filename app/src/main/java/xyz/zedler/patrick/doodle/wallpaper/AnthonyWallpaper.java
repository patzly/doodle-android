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

public class AnthonyWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.ANTHONY;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_anthony;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    svgDrawable.requireObjectById("sheet").isRotatable = true;
    svgDrawable.requireObjectById("rect").isRotatable = true;
    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_anthony1,
            "#b9c1c7",
            "#e1d7cc",
            "#47484a",
            new String[]{"#e7e9eb", "#000000"},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_anthony2,
            "#608ca5",
            "#fccbb5",
            "#fefffe",
            new String[]{"#496f83", "#181b1c"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_anthony3,
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
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_anthony1_dark,
            "#212121",
            "#5b5a5a",
            "#3d3b3c",
            new String[]{"#6a6966"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_anthony2_dark,
            "#212121",
            "#997c6e",
            "#32393c",
            new String[]{"#494949", "#aeafae"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_anthony3_dark,
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
