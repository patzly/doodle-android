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
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.builders.GridRowBuilder;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.settings.receiver.SettingsBroadcastReceiver;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public abstract class BaseSettingsProvider extends SliceProvider {

  public static final String BASE_SLICE_URI = "content://xyz.zedler.patrick.doodle.settings";
  public static final int COLOR_A = 0;
  public static final int COLOR_B = 1;
  public static final int COLOR_C = 2;
  public static final int COLOR_D = 3;
  private int requestCodeCounter = 100;

  public abstract Slice createSettingsSlice(Uri uri, Context context);

  public abstract String settingsPath();

  public SharedPreferences getSharedPreferences() {
    return new PrefsUtil(getContext()).getSharedPrefs();
  }

  public BaseSettingsProvider() {
    super("android.permission.BIND_WALLPAPER");
  }

  @Override
  public Slice onBindSlice(@NonNull Uri sliceUri) {
    if (getContext() == null || sliceUri.getPath() == null) {
      return null;
    }
    if (("/" + settingsPath()).equals(sliceUri.getPath())) {
      return createSettingsSlice(sliceUri, getContext());
    }
    return null;
  }

  public ListBuilder getList(Context context, Uri sliceUri) {
    ListBuilder listBuilder = new ListBuilder(context, sliceUri, -1);
    listBuilder.setAccentColor(-1);
    return listBuilder;
  }

  /* access modifiers changed from: protected */
  public int getNewRequestCode() {
    this.requestCodeCounter++;
    return this.requestCodeCounter;
  }

  /* access modifiers changed from: protected */
  public ListBuilder.RowBuilder generateButtonRow(Context context, Intent intent, String label,
      int icon) {
    PendingIntent broadcast = PendingIntent
        .getBroadcast(context, getNewRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    SliceAction primaryAction = SliceAction.create(
        SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode()),
        IconCompat.createWithResource(context, icon), ListBuilder.ICON_IMAGE, (CharSequence) "Toggle ");
    IconCompat btn = IconCompat.createWithResource(context, icon);
    btn.setTint(16777215);
    btn.setTintMode(PorterDuff.Mode.SRC);
    return new ListBuilder.RowBuilder().setTitle(label).setPrimaryAction(primaryAction).addEndItem(
        SliceAction.create(
            SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode()),
            btn, ListBuilder.ICON_IMAGE, (CharSequence) label));
  }

  /* access modifiers changed from: protected */
  public ListBuilder.RowBuilder generateToggleRow(Context context, Intent intent, String label,
      boolean currentState) {
    SliceAction toggleAction = SliceAction.createToggle(
        SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode()),
        (CharSequence) label, currentState);
    return new ListBuilder.RowBuilder().setTitle(label).setPrimaryAction(toggleAction)
        .addEndItem(toggleAction);
  }

  /* access modifiers changed from: protected */
  public GridRowBuilder generateRadioGridRow(Context context, String action, String extra,
      String description, String[] descriptions, String[] options, int selectedIndex) {
    GridRowBuilder gridRow = new GridRowBuilder();
    Intent primaryIntent = SettingsBroadcastReceiver.createIntent(context, action);
    primaryIntent.putExtra(extra, (selectedIndex + 1) % options.length);
    gridRow.setPrimaryAction(SliceAction.create(
        SettingsBroadcastReceiver.createPendingIntent(context, primaryIntent, getNewRequestCode()),
        IconCompat.createWithResource(context, R.drawable.abc_ic_arrow_forward), ListBuilder.ICON_IMAGE,
        (CharSequence) "Select next item"));
    gridRow.setContentDescription(description);
    for (int i2 = 0; i2 < options.length; i2++) {
      Intent intent = SettingsBroadcastReceiver.createIntent(context, action);
      intent.putExtra(extra, i2);
      IconCompat radio = IconCompat.createWithResource(context, getRadioButtonIcon(selectedIndex, i2));
      radio.setTint(16776960);
      radio.setTintMode(PorterDuff.Mode.SRC);
      gridRow.addCell(new GridRowBuilder.CellBuilder().addText(options[i2])
          .setContentDescription(descriptions[i2]).addImage(radio, ListBuilder.ICON_IMAGE).setContentIntent(
              SettingsBroadcastReceiver
                  .createPendingIntent(context, intent, getNewRequestCode())));
    }
    return gridRow;
  }

  /* access modifiers changed from: protected */
  public GridRowBuilder generateRadioColorGridRow(Context context, String action, String extra,
      int selectedIndex) {
    GridRowBuilder gridRow = new GridRowBuilder();
    Intent primaryIntent = SettingsBroadcastReceiver.createIntent(context, action);
    primaryIntent.putExtra(extra, (selectedIndex + 1) % 4);
    gridRow.setPrimaryAction(SliceAction.create(
        SettingsBroadcastReceiver.createPendingIntent(context, primaryIntent, getNewRequestCode()),
        IconCompat.createWithResource(context, R.drawable.abc_ic_arrow_forward), ListBuilder.ICON_IMAGE,
        (CharSequence) "Select next item"));
    int i = 0;
    while (i < 4) {
      Intent intent = SettingsBroadcastReceiver.createIntent(context, action);
      intent.putExtra(extra, i);
      gridRow.addCell(new GridRowBuilder.CellBuilder()
          .addImage(IconCompat.createWithResource(context, getColorRadio(i, i == selectedIndex)), ListBuilder.ICON_IMAGE)
          .setContentIntent(
              SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode())));
      i++;
    }
    return gridRow;
  }

  /* access modifiers changed from: protected */
  public GridRowBuilder generateImageGridRow(Context context, String action, String extra,
      int selectedIndex) {
    GridRowBuilder gridRow = new GridRowBuilder();
    Intent primaryIntent = SettingsBroadcastReceiver.createIntent(context, action);
    primaryIntent.putExtra(extra, (selectedIndex + 1) % 4);
    gridRow.setPrimaryAction(SliceAction
        .create(SettingsBroadcastReceiver
                .createPendingIntent(context, primaryIntent, getNewRequestCode()), IconCompat
                .createWithResource(context, R.drawable.abc_ic_arrow_forward), ListBuilder.ICON_IMAGE,
            (CharSequence) "Select next item"));
    int i = 0;
    while (i < 4) {
      Intent intent = SettingsBroadcastReceiver.createIntent(context, action);
      intent.putExtra(extra, i);
      gridRow.addCell(new GridRowBuilder.CellBuilder()
          .addImage(IconCompat.createWithResource(context, getColorRadio(i, i == selectedIndex)), ListBuilder.SMALL_IMAGE)
          .setContentIntent(
              SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode())));
      i++;
    }
    return gridRow;
  }

  /* access modifiers changed from: protected */
  public ListBuilder.RowBuilder generateRadioRow(Context context, String action, String extra,
      String label, String[] options, int selectedIndex) {
    Intent primaryIntent = SettingsBroadcastReceiver.createIntent(context, action);
    primaryIntent.putExtra(extra, (selectedIndex + 1) % options.length);
    ListBuilder.RowBuilder row = new ListBuilder.RowBuilder().setTitle(label)
        .setSubtitle(options[selectedIndex]).setPrimaryAction(SliceAction.create(
            SettingsBroadcastReceiver
                .createPendingIntent(context, primaryIntent, getNewRequestCode()),
            IconCompat.createWithResource(context, R.drawable.abc_ic_arrow_forward), ListBuilder.ICON_IMAGE,
            (CharSequence) "Select next item"));
    for (int i = 0; i < options.length; i++) {
      Intent intent = SettingsBroadcastReceiver.createIntent(context, action);
      intent.putExtra(extra, i);
      row.addEndItem(SliceAction.create(
          SettingsBroadcastReceiver.createPendingIntent(context, intent, getNewRequestCode()),
          IconCompat.createWithResource(context, getRadioButtonIcon(i, selectedIndex)), ListBuilder.ICON_IMAGE,
          (CharSequence) options[i]));
    }
    return row;
  }

  /* access modifiers changed from: protected */
  public int getColorRadio(int color, boolean selected) {
    if (color != 0) {
      if (color == 1) {
        return selected ? R.drawable.ic_button_checked_doodle_b
            : R.drawable.ic_button_unchecked_doodle_b;
      }
      if (color == 2) {
        return selected ? R.drawable.ic_button_checked_doodle_c
            : R.drawable.ic_button_unchecked_doodle_c;
      }
      if (color != 3) {
        return R.drawable.ic_button_unchecked_doodle_a;
      }
      return selected ? R.drawable.ic_button_checked_doodle_d
          : R.drawable.ic_button_unchecked_doodle_d;
    } else if (selected) {
      return R.drawable.ic_button_checked_doodle_a;
    } else {
      return R.drawable.ic_button_unchecked_doodle_a;
    }
  }

  /* access modifiers changed from: protected */
  public int getRadioButtonIcon(int speed, int selectedSpeed) {
    return speed == selectedSpeed ? R.drawable.ic_button_checked_doodle_a
        : R.drawable.ic_button_unchecked_doodle_a;
  }
}
