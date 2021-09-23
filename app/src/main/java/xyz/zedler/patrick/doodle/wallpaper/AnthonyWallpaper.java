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

  @NonNull
  @Override
  public WallpaperVariant[] getVariants() {
    return new WallpaperVariant[]{
        new WallpaperVariant(
            R.raw.wallpaper_anthony1,
            "#b9c1c7",
            "#e1d7cc",
            "#47484a",
            true,
            false
        ),
        new WallpaperVariant(
            R.raw.wallpaper_anthony2,
            "#608ca5",
            "#fccbb5",
            "#fefffe",
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
            null,
            false,
            true
        ),
        new WallpaperVariant(
            R.raw.wallpaper_anthony2_dark,
            "#212121",
            "#997c6e",
            "#32393c",
            false,
            true
        )
    };
  }
}
