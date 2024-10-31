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

package xyz.zedler.patrick.doodle.util;

import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;

public class DialogUtil {

  private static final String IS_SHOWING = "is_showing_dialog_";

  private final MainActivity activity;
  private final String tag;
  private AlertDialog dialog;
  private Bundle savedInstanceState;

  public DialogUtil(@NonNull MainActivity activity, @NonNull String tag) {
    this.activity = activity;
    this.tag = tag;
  }

  public void createActionRaw(
      @StringRes int titleResId, @RawRes int msgResId,
      @StringRes int actionResId, @NonNull Runnable task
  ) {
    createActions(
        activity.getString(titleResId), ResUtil.getRawText(activity, msgResId),
        activity.getString(actionResId), task,
        null, null
    );
  }

  public void createAction(
      @StringRes int titleResId, @StringRes int msgResId,
      @StringRes int actionResId, @NonNull Runnable task
  ) {
    createActions(
        activity.getString(titleResId), activity.getString(msgResId),
        activity.getString(actionResId), task,
        null, null
    );
  }

  public void createAction(
      String title, String msg, @StringRes int actionResId, @NonNull Runnable task
  ) {
    createActions(
        title, msg,
        activity.getString(actionResId), task,
        null, null
    );
  }

  public void createAction(
      String title, String msg, String action, @NonNull Runnable task
  ) {
    createActions(title, msg, action, task, null, null);
  }

  public void createActions(
      @StringRes int titleResId, @StringRes int msgResId,
      @StringRes int actionPositiveResId, @NonNull Runnable taskPositive,
      @StringRes int actionNegativeResId, @Nullable Runnable taskNegative
  ) {
    createActions(
        activity.getString(titleResId), activity.getString(msgResId),
        activity.getString(actionPositiveResId), taskPositive,
        activity.getString(actionNegativeResId), taskNegative
    );
  }

  public void createActions(
      String title, String msg,
      String actionPositive, @NonNull Runnable taskPositive,
      String actionNegative, @Nullable Runnable taskNegative
  ) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
        activity, R.style.ThemeOverlay_Doodle_AlertDialog
    );
    builder.setTitle(title);
    builder.setMessage(msg);
    builder.setPositiveButton(actionPositive, (dialog, which) -> {
      activity.performHapticClick();
      taskPositive.run();
    });
    if (taskNegative != null) {
      builder.setNegativeButton(actionNegative, (dialog, which) -> {
        activity.performHapticClick();
        taskNegative.run();
      });
    } else {
      builder.setNegativeButton(
          R.string.action_cancel,
          (dialog, which) -> activity.performHapticClick()
      );
    }
    builder.setOnCancelListener(dialog -> activity.performHapticTick());
    dialog = builder.create();
  }

  public void createClose(@StringRes int titleResId, @StringRes int msgResId) {
    createClose(activity.getString(titleResId), activity.getString(msgResId));
  }

  public void createCloseRaw(@StringRes int titleResId, @RawRes int msgResId) {
    createClose(activity.getString(titleResId), ResUtil.getRawText(activity, msgResId));
  }

  public void createClose(String title, String msg) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
        activity, R.style.ThemeOverlay_Doodle_AlertDialog
    );
    builder.setTitle(title);
    builder.setMessage(msg);
    builder.setPositiveButton(
        R.string.action_close,
        (dialog, which) -> activity.performHapticClick()
    );
    builder.setOnCancelListener(dialog -> activity.performHapticTick());
    dialog = builder.create();
  }

  public void createCloseCustom(@StringRes int titleResId, @NonNull View view) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
        activity, R.style.ThemeOverlay_Doodle_AlertDialog
    );
    builder.setTitle(titleResId);
    builder.setView(view);
    builder.setPositiveButton(
        R.string.action_close,
        (dialog, which) -> activity.performHapticClick()
    );
    builder.setOnCancelListener(dialog -> activity.performHapticTick());
    dialog = builder.create();
  }

  public void createCaution(
      @StringRes int titleResId, @StringRes int msgResId,
      @StringRes int actionResId, @NonNull Runnable action
  ) {
    createCaution(titleResId, msgResId, actionResId, action, -1, null);
  }

  public void createCaution(
      @StringRes int titleResId, @StringRes int msgResId,
      @StringRes int actionPositionResId, @NonNull Runnable taskPositive,
      @StringRes int actionNegativeResId, @Nullable Runnable taskNegative
  ) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
        activity, R.style.ThemeOverlay_Doodle_AlertDialog_Caution
    );
    builder.setTitle(titleResId);
    builder.setMessage(msgResId);
    builder.setPositiveButton(actionPositionResId, (dialog, which) -> {
      taskPositive.run();
      activity.performHapticHeavyClick();
    });
    if (taskNegative != null) {
      builder.setNegativeButton(actionNegativeResId, (dialog, which) -> {
        taskNegative.run();
        activity.performHapticClick();
      });
    } else {
      builder.setNegativeButton(
          R.string.action_cancel,
          (dialog, which) -> activity.performHapticClick()
      );
    }
    builder.setOnCancelListener(dialog -> activity.performHapticTick());
    dialog = builder.create();
  }

  public void createMultiChoice(
      @StringRes int titleResId, @NonNull String[] choices,
      @NonNull boolean[] initial, @NonNull OnMultiChoiceClickListener listener
  ) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
        activity, R.style.ThemeOverlay_Doodle_AlertDialog
    );
    builder.setTitle(titleResId);
    builder.setMultiChoiceItems(choices, initial, listener);
    builder.setPositiveButton(
        R.string.action_close,
        (dialog, which) -> activity.performHapticClick()
    );
    builder.setOnCancelListener(dialog -> activity.performHapticTick());
    dialog = builder.create();
  }

  public void createSingleChoice(
      @StringRes int titleResId, @NonNull String[] choices,
      int initial, @NonNull OnClickListener listener
  ) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
        activity, R.style.ThemeOverlay_Doodle_AlertDialog
    );
    builder.setTitle(titleResId);
    builder.setSingleChoiceItems(choices, initial, listener);
    builder.setPositiveButton(
        R.string.action_close,
        (dialog, which) -> activity.performHapticClick()
    );
    builder.setOnCancelListener(dialog -> activity.performHapticTick());
    dialog = builder.create();
  }

  public void show() {
    if (dialog != null && !dialog.isShowing()) {
      dialog.show();
    }
  }

  public void showIfWasShown(@Nullable Bundle state) {
    if (state != null && state.getBoolean(IS_SHOWING + tag)) {
      new Handler(Looper.getMainLooper()).postDelayed(this::show, 10);
    }
  }

  public void showIfWasShown() {
    if (savedInstanceState != null && savedInstanceState.getBoolean(IS_SHOWING + tag)) {
      new Handler(Looper.getMainLooper()).postDelayed(this::show, 10);
    }
  }

  public void setSavedInstanceState(@Nullable Bundle state) {
    savedInstanceState = state;
  }

  public void saveState(@NonNull Bundle outState) {
    outState.putBoolean(IS_SHOWING + tag, dialog != null && dialog.isShowing());
  }

  /**
   * Call in onDestroy, else it will throw an exception
   */
  public void dismiss() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  public void setCancelable(boolean cancelable) {
    if (dialog != null) {
      dialog.setCancelable(cancelable);
      dialog.setCanceledOnTouchOutside(cancelable);
    }
  }
}
