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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.builders.GridRowBuilder;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import xyz.zedler.patrick.doodle.Constants.EXTRA;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.settings.receiver.SettingsBroadcastReceiver;

public class SettingsSliceProvider extends SliceProvider {

  private static final Uri URI = Uri.parse("content://xyz.zedler.patrick.doodle/settings");
  private static int currentTheme = 0;
  private int requestCodeCounter = 100;

  /**
   * Instantiate any required objects. Return true if the provider was successfully created, false
   * otherwise.
   */
  @Override
  public boolean onCreateSliceProvider() {
    return true;
  }

  /**
   * Converts URL to content URI (i.e. content://xyz.zedler.patrick.doodle...)
   */
  @Override
  @NonNull
  public Uri onMapIntentToUri(@Nullable Intent intent) {
    // Note: implementing this is only required if you plan on catching URL requests.
    // This is an example solution.
    Uri.Builder uriBuilder = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT);
    if (intent == null) {
      return uriBuilder.build();
    }
    Uri data = intent.getData();
    if (data != null && data.getPath() != null) {
      String path = data.getPath().replace("/", "");
      uriBuilder = uriBuilder.path(path);
    }
    Context context = getContext();
    if (context != null) {
      uriBuilder = uriBuilder.authority(context.getPackageName());
    }
    return uriBuilder.build();
  }

  public Slice onBindSlice(@NonNull Uri sliceUri) {
    if (getContext() == null || sliceUri.getPath() == null) {
      return null;
    }
    if (sliceUri.getPath().equals("/settings")) {
      return getSettingsSlice(sliceUri, getContext());
    } else {
      return null;
    }
  }

  private Slice getSettingsSlice(Uri sliceUri, Context context) {
    ListBuilder listBuilder = getList(context, sliceUri);
    listBuilder.setHeader(
        new ListBuilder.HeaderBuilder().setTitle(context.getString(R.string.setting_theme))
    );
    listBuilder.addGridRow(
        getImageGridRow(context, "xyz.zedler.patrick.doodle.settings.update",
            EXTRA.THEME, currentTheme));
    return listBuilder.build();
  }

  public ListBuilder getList(Context context, Uri sliceUri) {
    @SuppressLint("Slices")
    ListBuilder listBuilder = new ListBuilder(context, sliceUri, -1);
    listBuilder.setAccentColor(-1);
    return listBuilder;
  }

  public int getNewRequestCode() {
    requestCodeCounter++;
    return requestCodeCounter;
  }

  public static void notifyChange(Context context, Intent intent) {
    if (intent.hasExtra(EXTRA.THEME)) {
      currentTheme = intent.getIntExtra(EXTRA.THEME, currentTheme);
    }
    context.getContentResolver().notifyChange(URI, null);
  }

  public GridRowBuilder getImageGridRow(Context context, String action, String extra, int selected) {
    GridRowBuilder gridRow = new GridRowBuilder();
    Intent primaryIntent = SettingsBroadcastReceiver.createIntent(context, action);
    primaryIntent.putExtra(extra, (selected + 1) % 4);
    gridRow.setPrimaryAction(
        SliceAction.create(
            SettingsBroadcastReceiver.createPendingIntent(
                context, primaryIntent, getNewRequestCode()
            ),
            IconCompat.createWithResource(context, R.drawable.abc_ic_arrow_forward),
            ListBuilder.ICON_IMAGE,
            "Select next item"
        )
    );
    int i = 0;
    while (i < 4) {
      Intent intent = SettingsBroadcastReceiver.createIntent(context, action);
      intent.putExtra(extra, i);
      gridRow.addCell(
          new GridRowBuilder.CellBuilder().addImage(
              IconCompat.createWithResource(context, getColorRadio(i, i == selected)),
              ListBuilder.SMALL_IMAGE
          ).setContentIntent(
              SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode())
          )
      );
      i++;
    }
    return gridRow;
  }

  public int getColorRadio(int color, boolean selected) {
    if (color != 0) {
      if (color == 1) {
        return selected ? R.drawable.button_checked_johanna
            : R.drawable.button_unchecked_johanna;
      }
      if (color == 2) {
        return selected ? R.drawable.button_checked_reiko
            : R.drawable.button_unchecked_reiko;
      }
      if (color != 3) {
        return R.drawable.button_unchecked_pixel;
      }
      return selected ? R.drawable.button_checked_anthony
          : R.drawable.button_unchecked_anthony;
    } else if (selected) {
      return R.drawable.button_checked_pixel;
    } else {
      return R.drawable.button_unchecked_pixel;
    }
  }
}