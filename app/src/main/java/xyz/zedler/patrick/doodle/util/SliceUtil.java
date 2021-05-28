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

package xyz.zedler.patrick.doodle.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import androidx.slice.SliceManager;
import androidx.slice.builders.ListBuilder;

public class SliceUtil {

  public static void grantSlicePermission(Context context) {
    Uri sliceProviderUri = new Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(context.getPackageName())
        .build();

    String liveWallpaper = "com.android.wallpaper.livepicker";

    SliceManager.getInstance(context).grantSlicePermission(liveWallpaper, sliceProviderUri);
  }
}
