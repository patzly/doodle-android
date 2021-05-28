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

package xyz.zedler.patrick.doodle.settings.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ViewController {
  private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction() != null) {
        ViewController.this.onBroadcastReceived(context, intent, intent.getAction());
      }
    }
  };
  private final AtomicBoolean broadcastRegistered;
  protected Context context;
  private final AtomicBoolean initialized;

  /* access modifiers changed from: protected */
  public abstract boolean init();

  /* access modifiers changed from: package-private */
  public abstract void onBroadcastReceived(Context context2, Intent intent, String str);

  /* access modifiers changed from: protected */
  public abstract IntentFilter onRegister(boolean z);

  /* access modifiers changed from: protected */
  public abstract void onUnregister();

  ViewController(Context context2) {
    this.context = context2;
    this.broadcastRegistered = new AtomicBoolean(false);
    this.initialized = new AtomicBoolean(init());
  }

  public void resume(boolean fireStraightAway) {
    if (!this.initialized.get()) {
      if (init()) {
        this.initialized.set(true);
      } else {
        return;
      }
    }
    if (!this.broadcastRegistered.get()) {
      registerReceiver(this.context, this.broadcastReceiver, onRegister(fireStraightAway));
      this.broadcastRegistered.set(true);
    }
  }

  public synchronized void pause() {
    if (this.broadcastRegistered.get()) {
      onUnregister();
      unregisterReceiver(this.context, this.broadcastReceiver);
      this.broadcastRegistered.set(false);
    }
  }

  public synchronized void dispose() {
    pause();
  }

  /* access modifiers changed from: protected */
  public void registerReceiver(Context context2, BroadcastReceiver broadcastReceiver2, IntentFilter filter) {
    context2.registerReceiver(broadcastReceiver2, filter);
  }

  /* access modifiers changed from: protected */
  public void unregisterReceiver(Context context2, BroadcastReceiver broadcastReceiver2) {
    context2.unregisterReceiver(broadcastReceiver2);
  }
}