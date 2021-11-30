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

package xyz.zedler.patrick.doodle.wallpaper;

import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;

public class MonetWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.MONET;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_monet;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    svgDrawable.requireObjectById("circle").isRotatable = true;
    svgDrawable.requireObjectById("quad").isRotatable = true;
    svgDrawable.requireObjectById("pill").isRotatable = true;
    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_monet1,
            "#fcedea",
            "#924642",
            "#fbdeac",
            new String[]{"#f4b6b0", "#fef9f7"},
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet2,
            "#634b67",
            "#dbd870",
            "#ff45c9",
            new String[]{"#d2b4bb", "#b795c3"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet3,
            "#f5f4e2",
            "#dbec8c",
            "#caebdf",
            new String[]{"#bdc984", "#efeac1"},
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
            R.raw.wallpaper_monet1_dark,
            "#222020",
            "#9a726e",
            "#dac095",
            new String[]{"#323131"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet2_dark,
            "#231f24",
            "#634b67",
            "#c9c767",
            new String[]{"#695b5e", "#342f2f"},
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet3_dark,
            "#171815",
            "#909b5d",
            "#badbd0",
            new String[]{"#30312c"},
            false,
            true
        )
    };
  }
}
