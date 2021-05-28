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
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import xyz.zedler.patrick.doodle.settings.CustomizableWallpaper;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class SettingsController extends ViewController {

  public static final String EXTRA_SAVE_SETTINGS = "extra_save_settings";
  public static final String SHARED_PREFERENCES_KEY = "xyz.zedler.patrick.doodle.shared_preferences";
  private static final String TAG = SettingsController.class.toString();
  private final Intent defaults;
  private final Intent inPreview = new Intent();
  private final Intent inWallpaper = new Intent();
  private final CustomizableWallpaper listener;
  public SharedPreferences sharedPref;
  private final SharedPreferences.Editor sharedPrefEditor;

  public SettingsController(Context context, CustomizableWallpaper wallpaper, Intent defaults2,
      boolean shouldReset, boolean isPreview) {
    super(context);
    this.listener = wallpaper;
    this.defaults = defaults2;
    this.sharedPref = new PrefsUtil(context).getSharedPrefs();
    this.sharedPrefEditor = sharedPref.edit();
    if (shouldReset) {
      removeSharedPreferences();
    }
    fetch();
    this.inPreview.putExtras(this.inWallpaper);
    this.listener.updateData(this.inPreview, this.inWallpaper, isPreview);
  }

  private void removeSharedPreferences() {
    Bundle bundle = this.defaults.getExtras();
    if (bundle != null) {
      for (String key : bundle.keySet()) {
        this.sharedPrefEditor.remove(key);
      }
    }
    this.sharedPrefEditor.commit();
  }

  /* access modifiers changed from: protected */
  public boolean init() {
    return true;
  }

  public void setIsPreview(boolean isPreview) {
    this.listener.updateData(this.inPreview, this.inWallpaper, isPreview);
  }

  /* access modifiers changed from: package-private */
  public void onBroadcastReceived(Context context, Intent intent, String action) {
    if (this.listener.getAction().equals(action)) {
      Bundle bundle = intent.getExtras();
      if (bundle != null) {
        for (String key : bundle.keySet()) {
          Object value = bundle.get(key);
          if (this.defaults.hasExtra(key)) {
            if (value instanceof Boolean) {
              this.inPreview.putExtra(key, (Boolean) value);
            }
            if (value instanceof String) {
              this.inPreview.putExtra(key, (String) value);
            }
            if (value instanceof Integer) {
              this.inPreview.putExtra(key, ((Integer) value).intValue());
            }
            if (value instanceof Long) {
              this.inPreview.putExtra(key, ((Long) value).longValue());
            }
            if (value instanceof Float) {
              this.inPreview.putExtra(key, ((Float) value).floatValue());
            }
          }
        }
      }
      if (intent.getBooleanExtra(EXTRA_SAVE_SETTINGS, false)) {
        applyChanges();
      }
      this.listener.onSettingsChanged(intent);
    }
  }

  public void discardChanges() {
  }

  public void applyChanges() {
    if (this.inPreview.getExtras() == null || this.inPreview.getExtras().size() == 0) {
      this.inPreview.putExtras(this.listener.getDefaults().getExtras());
    }
    savePreferences(this.inPreview, false);
    CustomizableWallpaper customizableWallpaper = this.listener;
    if (customizableWallpaper != null) {
      customizableWallpaper.updateData(this.inPreview, this.inWallpaper, false);
    }
  }

  private void savePreferences(Intent intent, boolean isPreview) {
    boolean hasChanged = false;
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      for (String key : bundle.keySet()) {
        Object value = bundle.get(key);
        if (!key.equals("wallpaper.action")) {
          if (value instanceof String) {
            hasChanged = true;
            this.sharedPrefEditor.putString(key, intent.getStringExtra(key));
          }
          if (value instanceof Long) {
            hasChanged = true;
            this.sharedPrefEditor.putLong(key, intent.getLongExtra(key, 0));
          }
          if (value instanceof Integer) {
            hasChanged = true;
            this.sharedPrefEditor.putInt(key, intent.getIntExtra(key, 0));
          }
          if (value instanceof Float) {
            hasChanged = true;
            this.sharedPrefEditor.putFloat(key, intent.getFloatExtra(key, 0.0f));
          }
          if (value instanceof Boolean) {
            hasChanged = true;
            this.sharedPrefEditor.putBoolean(key, intent.getBooleanExtra(key, false));
          }
        }
      }
      if (hasChanged) {
        this.sharedPrefEditor.commit();
      }
    }
    this.inWallpaper.putExtras(intent);
    this.inPreview.putExtras(this.inWallpaper);
  }

  private void fetch() {
    Bundle bundle = this.defaults.getExtras();
    if (bundle != null) {
      for (String key : bundle.keySet()) {
        Object value = bundle.get(key);
        if (value instanceof String) {
          this.inWallpaper
              .putExtra(key, this.sharedPref.getString(key, this.defaults.getStringExtra(key)));
        }
        if (value instanceof Long) {
          this.inWallpaper
              .putExtra(key, this.sharedPref.getLong(key, this.defaults.getLongExtra(key, 0)));
        }
        if (value instanceof Integer) {
          this.inWallpaper
              .putExtra(key, this.sharedPref.getInt(key, this.defaults.getIntExtra(key, 0)));
        }
        if (value instanceof Boolean) {
          this.inWallpaper.putExtra(key,
              this.sharedPref.getBoolean(key, this.defaults.getBooleanExtra(key, false)));
        }
      }
    }
  }

  /* access modifiers changed from: protected */
  public IntentFilter onRegister(boolean fireStraightAway) {
    return new IntentFilter(this.listener.getAction());
  }

  /* access modifiers changed from: protected */
  public void registerReceiver(Context context, BroadcastReceiver broadcastReceiver,
      IntentFilter filter) {
    LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);
  }

  /* access modifiers changed from: protected */
  public void unregisterReceiver(Context context, BroadcastReceiver broadcastReceiver) {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
  }

  /* access modifiers changed from: protected */
  public void onUnregister() {
  }
}
