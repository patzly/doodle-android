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

package xyz.zedler.patrick.doodle.wallpaper;

import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable.SvgObject;

public class AutumnWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.AUTUMN;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_autumn;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    SvgObject leaf = svgDrawable.requireObjectById("leaf");
    leaf.isRotatable = true;
    leaf.pivotOffsetX = 300;
    leaf.pivotOffsetY = 300;

    SvgObject triangle = svgDrawable.requireObjectById("triangle");
    triangle.isRotatable = true;
    triangle.pivotOffsetX = -300;
    triangle.pivotOffsetY = -300;

    svgDrawable.requireObjectById("quad").isRotatable = true;
    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            "wallpaper_autumn",
            "#f3e7c8",
            "#ead373",
            "#b07029",
            new String[]{"#d1ab7d", "#c69735"},
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
            "wallpaper_autumn_dark",
            "#151718",
            "#ead373",
            "#b07029",
            new String[]{"#d1ab7d", "#c69735"},
            false,
            true
        )
    };
  }
}
