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

package xyz.zedler.patrick.doodle.settings;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.SettingsActivity;

public class DoodleSettingsProvider extends BaseSettingsProvider {

  public static final String ACTION = "xyz.zedler.patrick.doodle";
  public static final String EXTRA_CLEAR = "extra_doodle_clear";
  public static final String EXTRA_DATA = "extra_doodle_data";
  public static final String EXTRA_IS_DIY = "extra_doodle_is_diy";
  public static final String EXTRA_THEME = "extra_doodle_theme";
  public static final String PATH = "settings";
  public static final int THEMES_NUM = 4;
  public static final Uri URI = Uri
      .parse("content://xyz.zedler.patrick.doodle.settings");
  public static int sCurrentTheme = 0;
  public static boolean sIsDIY = false;

  public static void notifyChange(Context context, Intent intent) {
    notifyChange(context, intent, true);
  }

  public static void notifyChange(Context context, Intent intent, boolean shouldClear) {
    if (intent.hasExtra("extra_doodle_theme")) {
      sCurrentTheme = intent.getIntExtra("extra_doodle_theme", sCurrentTheme);
    }
    context.getContentResolver().notifyChange(URI, (ContentObserver) null);
  }

  @Override
  public boolean onCreateSliceProvider() {
    return true;
  }

  @Override
  public String settingsPath() {
    return PATH;
  }

  @Override
  public Slice createSettingsSlice(Uri sliceUri, Context context) {
    ListBuilder listBuilder = getList(context, sliceUri);
    listBuilder.setHeader(
        new ListBuilder.HeaderBuilder()
            .setTitle(context.getString(R.string.setting_slice_title))
            .setPrimaryAction(
                SliceAction.create(
                    PendingIntent.getActivity(
                        context, 0, new Intent(context, SettingsActivity.class), 0),
    IconCompat.createWithResource(context, R.drawable.ic_round_close),
        ListBuilder.ICON_IMAGE,
        "Enter app"
            )
            )
    );
    listBuilder.addGridRow(
        generateImageGridRow(context, "xyz.zedler.patrick.doodle",
            "extra_doodle_theme", sCurrentTheme));
    return listBuilder.build();
  }

  private String selectTextFromTheme(Context context, int theme) {
    if (theme == 0) {
      return "Pixel";
    }
    if (theme == 1) {
      return "Johanna";
    }
    if (theme == 2) {
      return "Reiko";
    }
    if (theme != 3) {
      return null;
    }
    return "Anthony";
  }
}
