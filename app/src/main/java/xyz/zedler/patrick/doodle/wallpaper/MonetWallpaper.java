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

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_monet1,
            "#fcedea",
            "#924642",
            "#fbdeac",
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet2,
            "#634b67",
            "#dbd870",
            "#ff45c9",
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet3,
            "#e9eec5",
            "#f2ea45",
            "#acba78",
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
            "#924642",
            null,
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet2_dark,
            "#231f24",
            "#ff45c9",
            "#dbd870",
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_monet3_dark,
            "#22241f",
            "#acba78",
            "#f2ea45",
            false,
            true
        )
    };
  }
}
