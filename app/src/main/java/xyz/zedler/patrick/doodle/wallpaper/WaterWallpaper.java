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

public class WaterWallpaper extends BaseWallpaper {

    @NonNull
    @Override
    public String getName() {
        return WALLPAPER.WATER;
    }

    @Override
    public int getThumbnailResId() {
        return R.drawable.selection_water;
    }

    @Override
    public SvgDrawable getPreparedSvg(SvgDrawable svgDrawable, int variant, boolean isNightMode) {
        svgDrawable.requireObjectById("dotted").isRotatable = true;
        svgDrawable.requireObjectById("kidney").isRotatable = true;
        return svgDrawable;
    }

    @NonNull
    @Override
    public WallpaperVariant[] getVariants() {
        return new WallpaperVariant[]{
                new WallpaperVariant(
                        R.raw.wallpaper_water,
                        "#e0e0df",
                        "#406382",
                        "#d0e3ef",
                        new String[]{"#8495a3", "#405967", "#87a2b0", "#6c8caa"},
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
                        R.raw.wallpaper_water_dark,
                        "#151718",
                        "#476988",
                        "#586a73",
                        new String[]{"#87a2b0", "#313232", "#323d43"},
                        false,
                        true
                )
        };
    }
}
