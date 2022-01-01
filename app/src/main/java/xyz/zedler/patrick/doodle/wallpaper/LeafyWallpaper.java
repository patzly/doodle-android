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

public class LeafyWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.LEAFY;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_leafy;
  }

  @Override
  public boolean isDepthStatic() {
    return true;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    SvgObject red = svgDrawable.requireObjectById("red");
    red.isRotatable = true;
    red.pivotOffsetX = 600;
    red.pivotOffsetY = 100;

    SvgObject green = svgDrawable.requireObjectById("green");
    green.isRotatable = true;
    green.pivotOffsetX = -300;
    green.pivotOffsetY = 550;

    SvgObject blue = svgDrawable.requireObjectById("blue");
    blue.isRotatable = true;
    blue.pivotOffsetX = -600;
    blue.pivotOffsetY = 100;

    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_leafy,
            "#f3e5ca",
            "#c16a57",
            "#93af96",
            new String[]{"#3f484f", "#e5ad9c", "#3f6eb2"},
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
            R.raw.wallpaper_leafy_dark,
            "#0d0f1c",
            "#7c413d",
            "#5b71a2",
            new String[]{"#17152a", "#905750", "#2a315f"},
            false,
            true
        )
    };
  }
}
