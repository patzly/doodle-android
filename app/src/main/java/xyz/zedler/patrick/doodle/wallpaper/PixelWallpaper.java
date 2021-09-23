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

public class PixelWallpaper extends BaseWallpaper {

  public PixelWallpaper() {
    setRotationSupported(true);
  }

  @NonNull
  @Override
  public String getName() {
    return WALLPAPER.PIXEL;
  }

  @Override
  public int getThumbnailResId() {
    return R.drawable.selection_pixel;
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
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel2,
            "#e0dcd3",
            "#ffb9a1",
            "#f4e3c9",
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel3,
            "#f98a6b",
            "#e5e1a3",
            "#bcddba",
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel4,
            "#eae5bf",
            "#789f8a",
            "#e06a4e",
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel5,
            "#fff5ec",
            "#052464",
            "#fe765e",
            true,
            false
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
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_pixel5_dark,
            "#272628",
            "#d4634f",
            "#324b84",
            false,
            true
        )
    };
  }
}
