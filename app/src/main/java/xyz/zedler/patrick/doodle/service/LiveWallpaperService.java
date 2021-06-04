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

package xyz.zedler.patrick.doodle.service;

import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.app.WallpaperColors;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.USER_PRESENCE;
import xyz.zedler.patrick.doodle.Constants.VARIANT;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.util.MigrationUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class LiveWallpaperService extends WallpaperService {

  private final static String TAG = LiveWallpaperService.class.getSimpleName();

  private SharedPreferences sharedPrefs;
  private String wallpaper, variant;
  private boolean nightMode, followSystem, isNight;
  private int parallax;
  private float fps;
  private int zoomIntensity;
  private boolean isZoomLauncherEnabled, isZoomUnlockEnabled;
  private String presence;
  private boolean isReceiverRegistered = false;
  private UserPresenceListener userPresenceListener;
  private SvgDrawable svgDrawable;

  private final BroadcastReceiver presenceReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case Intent.ACTION_USER_PRESENT:
          setUserPresence(USER_PRESENCE.UNLOCKED);
          break;
        case Intent.ACTION_SCREEN_OFF:
          setUserPresence(USER_PRESENCE.OFF);
          break;
        case Intent.ACTION_SCREEN_ON:
          setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);
          break;
      }
    }
  };

  @Override
  public Engine onCreateEngine() {
    sharedPrefs = new PrefsUtil(this).getSharedPrefs();
    new MigrationUtil(sharedPrefs).checkForMigrations();

    fps = getFrameRate();

    registerReceiver();
    setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);

    return new UserAwareEngine();
  }

  public void onDestroy() {
    unregisterReceiver();
    super.onDestroy();
  }

  private void registerReceiver() {
    if (!isReceiverRegistered) {
      IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
      filter.addAction(Intent.ACTION_SCREEN_OFF);
      filter.addAction(Intent.ACTION_SCREEN_ON);
      registerReceiver(presenceReceiver, filter);
      isReceiverRegistered = true;
    }
  }

  private void unregisterReceiver() {
    if (isReceiverRegistered) {
      unregisterReceiver(presenceReceiver);
      isReceiverRegistered = false;
    }
  }

  private void loadWallpaper() {
    int resId = -1;
    if (isNightMode()) {
      switch (wallpaper) {
        case WALLPAPER.PIXEL:
          resId = R.raw.pixel_dark;
          break;
        case WALLPAPER.JOHANNA:
          resId = R.raw.johanna_dark;
          break;
        case WALLPAPER.REIKO:
          resId = R.raw.pixel_white;
          break;
        case WALLPAPER.ANTHONY:
          resId = R.raw.pixel_white;
          break;
      }
    } else {
      switch (wallpaper) {
        case WALLPAPER.PIXEL:
          switch (variant) {
            case Constants.VARIANT.BLACK:
              resId = R.raw.pixel_black;
              break;
            case Constants.VARIANT.WHITE:
              resId = R.raw.pixel_white;
              break;
            case Constants.VARIANT.ORANGE:
              resId = R.raw.pixel_orange;
              break;
          }
          break;
        case WALLPAPER.JOHANNA:
          resId = R.raw.johanna;
          break;
        case WALLPAPER.REIKO:
          resId = R.raw.reiko;
          break;
        case WALLPAPER.ANTHONY:
          resId = R.raw.pixel_white;
          break;
      }
    }
    if (resId == -1) {
      resId = R.raw.pixel_white;
    }
    svgDrawable = new SvgDrawable(this, resId);
  }

  private boolean isNightMode() {
    if (nightMode && !followSystem) {
      return true;
    }
    int flags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightMode && flags == Configuration.UI_MODE_NIGHT_YES;
  }

  private boolean isKeyguardLocked() {
    return ((KeyguardManager) getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked();
  }

  private float getFrameRate() {
    WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
    return windowManager != null ? windowManager.getDefaultDisplay().getRefreshRate() : 60;
  }

  public void setUserPresence(final String presence) {
    if (presence.equals(this.presence)) {
      return;
    }
    this.presence = presence;
    if (userPresenceListener != null) {
      userPresenceListener.onPresenceChange(presence);
    }
  }

  public interface UserPresenceListener {

    void onPresenceChange(String presence);
  }

  // ENGINE ------------------------------------------------------------

  class UserAwareEngine extends Engine implements UserPresenceListener {
    private float zoomLauncher = 0;
    private float zoomUnlock = 0;
    private long lastDraw;
    private boolean isVisible;
    private ValueAnimator valueAnimator;

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      userPresenceListener = this;

      loadSettings();
      loadTheme();

      zoomUnlock = 1;
      animateZoom(0);

      setTouchEventsEnabled(false);
    }

    @Override
    public void onDestroy() {
      if (valueAnimator != null) {
        valueAnimator.cancel();
        valueAnimator.removeAllUpdateListeners();
        valueAnimator = null;
      }
    }

    @Override
    public WallpaperColors onComputeColors() {
      int background = Color.parseColor("#232323");
      if (isNightMode()) {
        switch (wallpaper) {
          case WALLPAPER.PIXEL:
            background = Color.parseColor("#272628");
            break;
          case WALLPAPER.JOHANNA:
            background = Color.parseColor("#32373a");
            break;
          case WALLPAPER.REIKO:
            background = Color.parseColor("#0e032d");
            break;
          case WALLPAPER.ANTHONY:
            background = Color.parseColor("#212121");
            break;
        }
      } else {
        switch (wallpaper) {
          case WALLPAPER.PIXEL:
            switch (variant) {
              case VARIANT.BLACK:
                background = Color.parseColor("#232323");
                break;
              case VARIANT.WHITE:
                background = Color.parseColor("#dbd7ce");
                break;
              case VARIANT.ORANGE:
                background = Color.parseColor("#fbb29e");
                break;
            }
            break;
          case WALLPAPER.JOHANNA:
            background = Color.parseColor("#fcf4e9");
            break;
          case WALLPAPER.REIKO:
            background = Color.parseColor("#cbcbef");
            break;
          case WALLPAPER.ANTHONY:
            background = Color.parseColor("#b9c1c7");
            break;
        }
      }
      if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
        return WallpaperColors.fromDrawable(new ColorDrawable(background));
      } else {
        return super.onComputeColors();
      }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
      isVisible = visible;
      if (!visible) {
        return;
      }

      handleRefreshRequests();

      svgDrawable.applyRandomElevationToAll(0.1f);
      drawFrame(true);
    }

    @Override
    public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
      // Not necessarily needed
      drawFrame(true);
    }

    private void loadSettings() {
      parallax = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
      if (svgDrawable != null) {
        svgDrawable.setScale(sharedPrefs.getFloat(PREF.SCALE, DEF.SCALE));
      }
      zoomIntensity = sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM);
      isZoomLauncherEnabled = sharedPrefs.getBoolean(PREF.ZOOM_LAUNCHER, DEF.ZOOM_LAUNCHER);
      isZoomUnlockEnabled = sharedPrefs.getBoolean(PREF.ZOOM_UNLOCK, DEF.ZOOM_UNLOCK);

      if (isZoomUnlockEnabled && !isReceiverRegistered) {
        registerReceiver();
      } else if (!isZoomUnlockEnabled && isReceiverRegistered) {
        unregisterReceiver();
      }
    }

    private void loadTheme() {
      wallpaper = sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER);
      variant = sharedPrefs.getString(PREF.VARIANT, DEF.VARIANT);
      nightMode = sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
      followSystem = sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM);
      isNight = isNightMode();

      loadWallpaper();
      svgDrawable.setScale(sharedPrefs.getFloat(PREF.SCALE, DEF.SCALE));

      colorsHaveChanged();
    }

    private void handleRefreshRequests() {
      boolean settingsApplied = sharedPrefs.getBoolean(PREF.SETTINGS_APPLIED, true);
      if (!settingsApplied) {
        sharedPrefs.edit().putBoolean(PREF.SETTINGS_APPLIED, true).apply();
        loadSettings();
      }

      boolean themeApplied = sharedPrefs.getBoolean(PREF.THEME_APPLIED, true);
      if (!themeApplied) {
        sharedPrefs.edit().putBoolean(PREF.THEME_APPLIED, true).apply();
        loadTheme();
      } else if (isNight != isNightMode()) {
        loadTheme();
      }
    }

    @Override
    public void onOffsetsChanged(
        float xOffset,
        float yOffset,
        float xStep,
        float yStep,
        int xPixels,
        int yPixels
    ) {
      if (parallax != 0) {
        svgDrawable.setOffset(xOffset * parallax * 100, 0);
        drawFrame(true);
      }
    }

    @Override
    public void onZoomChanged(float zoom) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && zoomIntensity > 0) {
        this.zoomLauncher = zoom;
        drawFrame(false);
      }
    }

    @Override
    public void onPresenceChange(String presence) {
      switch (presence) {
        case USER_PRESENCE.OFF:
          zoomUnlock = 1;
          drawFrame(true);
          break;
        case USER_PRESENCE.LOCKED:
          animateZoom(0.5f);
          break;
        case USER_PRESENCE.UNLOCKED:
          if (isVisible) {
            animateZoom(0);
          } else {
            zoomUnlock = 0;
            drawFrame(true);
          }
          break;
      }
    }

    void drawFrame(boolean force) {
      if (!force && SystemClock.elapsedRealtime() - lastDraw < 1000 / fps) {
        return;
      }
      final SurfaceHolder surfaceHolder = getSurfaceHolder();
      Canvas canvas = null;
      try {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
          canvas = surfaceHolder.lockHardwareCanvas();
        } else {
          canvas = surfaceHolder.lockCanvas();
        }
        if (canvas != null) {
          // ZOOM
          float intensity = zoomIntensity / 10f;
          double finalZoomLauncher = isZoomLauncherEnabled ? zoomLauncher * intensity : 0;
          double finalZoomUnlock = isZoomUnlockEnabled ? zoomUnlock * intensity : 0;
          svgDrawable.setZoom((float) (finalZoomLauncher + finalZoomUnlock));

          svgDrawable.draw(canvas);
          lastDraw = SystemClock.elapsedRealtime();
        }
      } finally {
        if (canvas != null) {
          surfaceHolder.unlockCanvasAndPost(canvas);
        }
      }
    }

    private void drawShape(Drawable drawable, double x, double y, double z, boolean shouldZoom) {
      if (drawable == null) {
        return;
      }

      float intensity = shouldZoom ? zoomIntensity / 10f : 0;

      double finalZoomLauncher = isZoomLauncherEnabled ? zoomLauncher * z * intensity : 0;
      double finalZoomUnlock = isZoomUnlockEnabled ? zoomUnlock * z * intensity : 0;

      double scale = /*this.scale*/1 - finalZoomLauncher - finalZoomUnlock;
      int width = (int) (scale * drawable.getIntrinsicWidth());
      int height = (int) (scale * drawable.getIntrinsicHeight());

      int xPos, yPos, offset;
      Rect frame = getSurfaceHolder().getSurfaceFrame();
      offset = (int) (/*xOffset*/0 * z * (parallax * 100));
      xPos = ((int) (x * frame.width())) - offset;
      yPos = (int) (y * frame.height());

      // zoom out moves shapes to the center
      int centerX = frame.centerX();
      int centerY = frame.centerY();
      if (xPos < centerX) {
        int dist = centerX - xPos;
        if (isZoomLauncherEnabled) {
          xPos += dist * z * zoomLauncher * intensity;
        }
        if (isZoomUnlockEnabled) {
          xPos += dist * z * zoomUnlock * intensity;
        }
      } else {
        int dist = xPos - centerX;
        if (isZoomLauncherEnabled) {
          xPos -= dist * z * zoomLauncher * intensity;
        }
        if (isZoomUnlockEnabled) {
          xPos -= dist * z * zoomUnlock * intensity;
        }
      }
      if (yPos < centerY) {
        int dist = centerY - yPos;
        if (isZoomLauncherEnabled) {
          yPos += dist * z * zoomLauncher * intensity;
        }
        if (isZoomUnlockEnabled) {
          yPos += dist * z * zoomUnlock * intensity;
        }
      } else {
        int dist = yPos - centerY;
        if (isZoomLauncherEnabled) {
          yPos -= dist * z * zoomLauncher * intensity;
        }
        if (isZoomUnlockEnabled) {
          yPos -= dist * z * zoomUnlock * intensity;
        }
      }

      drawable.setBounds(
          xPos - width / 2,
          yPos - height / 2,
          xPos + width / 2,
          yPos + height / 2
      );
    }

    private void animateZoom(float valueTo) {
      if (valueAnimator != null) {
        valueAnimator.cancel();
        valueAnimator.removeAllUpdateListeners();
        valueAnimator = null;
      }
      valueAnimator = ValueAnimator.ofFloat(zoomUnlock, valueTo);
      valueAnimator.addUpdateListener(animation -> {
        zoomUnlock = (float) animation.getAnimatedValue();
        drawFrame(true);
      });
      valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
      valueAnimator.setDuration(1250).start();
    }

    private void colorsHaveChanged() {
      if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
        notifyColorsChanged();
        // We have to call it again to take any effect, causes a warning...
        notifyColorsChanged();
      }
    }
  }
}
