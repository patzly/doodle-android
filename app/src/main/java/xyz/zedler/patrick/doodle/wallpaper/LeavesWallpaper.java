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
 * Copyright (c) 2019-2024 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.wallpaper;

import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable.SvgObject;

public class LeavesWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.LEAVES;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_leaves;
  }

  @Override
  public boolean isDepthStatic() {
    return false;
  }

  @Override
  public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
    SvgObject blue = svgDrawable.requireObjectById("blue");
    blue.willBeIntersected = true;

    SvgObject yellow = svgDrawable.requireObjectById("yellow");
    yellow.addPathIntersection("blue", isNightMode ? "#353a6c" : "#2f366c");
    yellow.willBeIntersected = true;

    SvgObject orange = svgDrawable.requireObjectById("orange");
    orange.addPathIntersection("yellow", isNightMode ? "#bb6551" : "#f48e77");
    orange.addPathIntersection("blue", isNightMode ? "#353a6c" : "#2f366c");

    return svgDrawable;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_leaves,
            "#eac880",
            "#6f8fae",
            "#f48e77",
            new String[]{"#f3e3cc", "#f0a775", "#f6bdac", "#2f366c"},
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
            R.raw.wallpaper_leaves_dark,
            "#bf9951",
            "#3e74a7",
            "#bb6551",
            new String[]{"#272628", "#c58457", "#a18078", "#353a6c"},
            false,
            true
        )
    };
  }
}
