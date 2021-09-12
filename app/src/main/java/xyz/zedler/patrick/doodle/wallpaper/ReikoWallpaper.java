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

import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;

public class ReikoWallpaper extends BaseWallpaper {

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.REIKO;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_reiko;
  }

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_reiko1,
            "#cbcbef",
            null,
            null,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_reiko2,
            "#fef7ed",
            null,
            null,
            true
        )
    };
  }

  @NonNull
  @Override
  public WallpaperVariant[] getDarkVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_reiko1_dark,
            "#0e032d",
            null,
            null,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_reiko2_dark,
            "#0e1f3b",
            null,
            null,
            false
        )
    };
  }
}