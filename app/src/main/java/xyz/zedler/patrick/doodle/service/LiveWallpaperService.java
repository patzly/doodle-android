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

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.app.WallpaperColors;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader.TileMode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import java.util.ArrayList;
import java.util.List;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.USER_PRESENCE;
import xyz.zedler.patrick.doodle.Constants.VARIANT;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable.SvgObject;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class LiveWallpaperService extends WallpaperService {

  private final static String TAG = LiveWallpaperService.class.getSimpleName();

  // All things where we need a context or the service's context are done in this Service class
  // All other things should be done in the inner Engine class

  private SharedPreferences sharedPrefs;
  // Wallpaper
  private SvgDrawable svgDrawable;
  private String wallpaper, variant;
  private boolean nightMode, followSystem;
  // User presence
  private String presence;
  private boolean isReceiverRegistered = false;
  private UserPresenceListener userPresenceListener;
  private BroadcastReceiver presenceReceiver;
  private SensorManager sensorManager;

  @Override
  public Engine onCreateEngine() {
    sharedPrefs = new PrefsUtil(LiveWallpaperService.this)
        .checkForMigrations()
        .getSharedPrefs();

    presenceReceiver = new BroadcastReceiver() {
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
    registerReceiver();
    setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    return new UserAwareEngine();
  }

  @Override
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
      try {
        unregisterReceiver(presenceReceiver);
      } catch (Exception e) {
        Log.e(TAG, "unregisterReceiver: ", e);
      }
      isReceiverRegistered = false;
    }
  }

  private void loadWallpaper() {
    if (isNightMode()) {
      switch (wallpaper) {
        case WALLPAPER.PIXEL:
          if (variant.equals(VARIANT.PIXEL4)) {
            svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel4_dark);
          } else if (variant.equals(VARIANT.PIXEL5)) {
            svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel5_dark);
          } else {
            svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel123_dark);
          }
          break;
        case WALLPAPER.JOHANNA:
          svgDrawable = new SvgDrawable(this, R.raw.wallpaper_johanna1_dark);
          break;
        case WALLPAPER.REIKO:
          if (variant.equals(VARIANT.REIKO1)) {
            svgDrawable = new SvgDrawable(this, R.raw.wallpaper_reiko1_dark);
            setKidneyGradientReiko("#a0b0fb", "#d8d4fe");
          } else if (variant.equals(VARIANT.REIKO2)) {
            svgDrawable = new SvgDrawable(this, R.raw.wallpaper_reiko2_dark);
            setKidneyGradientReiko("#eb902b", "#ecc12f");
          }
          break;
        case WALLPAPER.ANTHONY:
          svgDrawable = new SvgDrawable(this, R.raw.wallpaper_anthony1_dark);
          break;
      }
    } else {
      switch (wallpaper) {
        case WALLPAPER.PIXEL:
          switch (variant) {
            case VARIANT.PIXEL1:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel1);
              break;
            case VARIANT.PIXEL2:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel2);
              break;
            case VARIANT.PIXEL3:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel3);
              break;
            case VARIANT.PIXEL4:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel4);
              break;
            case VARIANT.PIXEL5:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel5);
              break;
          }
          break;
        case WALLPAPER.JOHANNA:
          svgDrawable = new SvgDrawable(this, R.raw.wallpaper_johanna1);
          break;
        case WALLPAPER.REIKO:
          switch (variant) {
            case VARIANT.REIKO1:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_reiko1);
              setKidneyGradientReiko("#a0b0fb", "#d8d4fe");
              break;
            case VARIANT.REIKO2:
              svgDrawable = new SvgDrawable(this, R.raw.wallpaper_reiko2);
              setKidneyGradientReiko("#ff931e", "#fbc318");
              break;
          }
          break;
        case WALLPAPER.ANTHONY:
          svgDrawable = new SvgDrawable(this, R.raw.wallpaper_anthony1);
          break;
      }
    }
    if (svgDrawable == null) {
      // Prevent NullPointerExceptions
      svgDrawable = new SvgDrawable(this, R.raw.wallpaper_pixel123_dark);
    }
  }

  private void setKidneyGradientReiko(String start, String end) {
    int colorStart = Color.parseColor(start);
    int colorEnd = Color.parseColor(end);
    SvgObject kidneyFront = svgDrawable.findObjectById("kidney_front");
    if (kidneyFront != null) {
      kidneyFront.shader = new LinearGradient(
          700, 0, 1100, 0, colorStart, colorEnd, TileMode.CLAMP
      );
    }
    SvgObject kidneyBack = svgDrawable.findObjectById("kidney_back");
    if (kidneyBack != null) {
      kidneyBack.shader = new LinearGradient(
          400, 0, 800, 0, colorStart, colorEnd, TileMode.CLAMP
      );
    }
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

  private void setUserPresence(final String presence) {
    if (presence.equals(this.presence)) {
      return;
    }
    this.presence = presence;
    if (userPresenceListener != null) {
      userPresenceListener.onPresenceChange(presence);
    }
  }

  private interface UserPresenceListener {

    void onPresenceChange(String presence);
  }

  // ENGINE ------------------------------------------------------------

  class UserAwareEngine extends Engine implements UserPresenceListener {
    private boolean useWhiteText;
    private int zoomIntensity;
    private boolean isZoomLauncherEnabled, isZoomUnlockEnabled;
    private float zoomLauncher;
    private float zoomUnlock;
    private float scale;
    private int parallax;
    private boolean isTiltEnabled;
    private float tiltX, tiltY;
    private float[] accelerationValues;
    private float offsetX;
    private long lastDraw;
    private boolean isVisible;
    private boolean isNight;
    private boolean useGpu;
    private boolean isListenerRegistered = false;
    private boolean isSurfaceAvailable = false;
    private boolean iconDropConsumed = true;
    private float fps;
    private final TimeInterpolator zoomInterpolator = new FastOutSlowInInterpolator();
    private ValueAnimator zoomAnimator;
    private SensorEventListener sensorListener;
    private final List<Pair<Float, Float>> tiltHistory = new ArrayList<>();

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      fps = getFrameRate();

      userPresenceListener = this;

      sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          if (isVisible && isTiltEnabled) {
            accelerationValues = lowPass(event.values, accelerationValues);
            tiltX = accelerationValues[0];
            tiltY = -accelerationValues[1];

            tiltHistory.add(new Pair<>(tiltX, tiltY));
            while (tiltHistory.size() > 30) {
              tiltHistory.remove(0);
            }

            float sumX = 0, sumY = 0;
            for (Pair<Float, Float> tilt : tiltHistory) {
              sumX += tilt.first;
              sumY += tilt.second;
            }
            float averageX = sumX / tiltHistory.size();
            float averageY = sumY / tiltHistory.size();
            float tolerance = 0.045f; // Allow small deviations caused by the sensor
            for (Pair<Float, Float> tilt : tiltHistory) {
              boolean isMovingX = averageX >= 0
                  ? tilt.first > averageX + tolerance
                  : tilt.first < averageX - tolerance;
              boolean isMovingY = averageY >= 0
                  ? tilt.second > averageY + tolerance
                  : tilt.second < averageY - tolerance;
              if (isMovingX || isMovingY) {
                updateOffset(false);
                return;
              }
            }
          }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };

      // Load this only once on creation, else it would cause a crash caused by OpenGL
      useGpu = sharedPrefs.getBoolean(PREF.GPU, DEF.GPU);

      loadSettings();
      loadTheme();

      zoomLauncher = 0;
      // This starts the zoom effect already in wallpaper preview
      zoomUnlock = 1;
      animateZoom(0);

      setTouchEventsEnabled(false);
      setOffsetNotificationsEnabled(true);
    }

    @Override
    public void onDestroy() {
      if (zoomAnimator != null) {
        zoomAnimator.cancel();
        zoomAnimator.removeAllUpdateListeners();
        zoomAnimator = null;
      }
      if (sensorManager != null && isListenerRegistered) {
        sensorManager.unregisterListener(sensorListener);
        isListenerRegistered = false;
      }
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
      isSurfaceAvailable = true;
    }

    @Override
    public void onSurfaceDestroyed(SurfaceHolder holder) {
      isSurfaceAvailable = false;
    }

    @Override
    public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
      // Not necessarily needed but recommended
      drawFrame(true);
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
              case VARIANT.PIXEL1:
                background = Color.parseColor("#232323");
                break;
              case VARIANT.PIXEL2:
                background = Color.parseColor("#dbd7ce");
                break;
              case VARIANT.PIXEL3:
                background = Color.parseColor("#fbb29e");
                break;
              case VARIANT.PIXEL4:
                background = Color.parseColor("#eae5bf");
                break;
              case VARIANT.PIXEL5:
                background = Color.parseColor("#fff5ec");
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
        // Bitmap is more efficient than Drawable here because Drawable would be converted to Bitmap
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (!isNightMode() && useWhiteText) {
          float[] hsl = new float[3];
          ColorUtils.colorToHSL(background, hsl);
          hsl[2] = 0.7f;
          canvas.drawColor(ColorUtils.HSLToColor(hsl));
        } else {
          canvas.drawColor(background);
        }
        return WallpaperColors.fromBitmap(bitmap);
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
      updateOffset(true);
    }

    @Override
    public Bundle onCommand (
        final String action, final int x, final int y, final int z, final Bundle extras,
        final boolean resultRequested
    ) {
      if (action.equals("android.home.drop")){
        iconDropConsumed = false;
        notifyIconDropped(x, y);
      }
      return super.onCommand(action, x, y, z, extras, resultRequested);
    }

    protected void notifyIconDropped( int x, int y) {
      if (!iconDropConsumed) {
        iconDropConsumed = true;
      }
    }

    private void loadSettings() {
      parallax = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
      setOffsetNotificationsEnabled(parallax != 0);

      isTiltEnabled = sharedPrefs.getBoolean(PREF.TILT, DEF.TILT);
      if (isTiltEnabled && !isListenerRegistered) {
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
            SensorManager.SENSOR_DELAY_GAME
        );
        isListenerRegistered = true;
      } else if (!isTiltEnabled && isListenerRegistered) {
        sensorManager.unregisterListener(sensorListener);
        isListenerRegistered = false;
      }

      scale = sharedPrefs.getFloat(PREF.SCALE, DEF.SCALE);
      if (svgDrawable != null) {
        svgDrawable.setScale(scale);
      }
      zoomIntensity = sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM);
      isZoomLauncherEnabled = sharedPrefs.getBoolean(PREF.ZOOM_LAUNCHER, DEF.ZOOM_LAUNCHER);
      isZoomUnlockEnabled = sharedPrefs.getBoolean(PREF.ZOOM_UNLOCK, DEF.ZOOM_UNLOCK);

      if (isZoomUnlockEnabled) {
        registerReceiver();
      } else {
        unregisterReceiver();
      }
    }

    private void loadTheme() {
      wallpaper = sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER);
      variant = sharedPrefs.getString(
          Constants.VARIANT_PREFIX + wallpaper, wallpaper + "1"
      );
      nightMode = sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
      followSystem = sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM);
      useWhiteText = sharedPrefs.getBoolean(PREF.USE_WHITE_TEXT, DEF.USE_WHITE_TEXT);
      isNight = isNightMode();

      loadWallpaper();
      svgDrawable.setScale(scale);

      colorsHaveChanged();
    }

    private void handleRefreshRequests() {
      boolean settingsApplied = sharedPrefs.getBoolean(PREF.SETTINGS_APPLIED, true);
      if (!settingsApplied) {
        sharedPrefs.edit().putBoolean(PREF.SETTINGS_APPLIED, true).apply();
        loadSettings();
      }

      boolean themeApplied = sharedPrefs.getBoolean(PREF.THEME_APPLIED, true);
      if (!themeApplied || isNight != isNightMode()) {
        sharedPrefs.edit().putBoolean(PREF.THEME_APPLIED, true).apply();
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
      offsetX = xOffset;
      updateOffset(true);
    }

    private void updateOffset(boolean force) {
      float xOffset = parallax != 0 ? offsetX : 0;
      int tiltFactor = 18 * parallax * (isTiltEnabled ? 1 : 0);
      svgDrawable.setOffset(
          xOffset * parallax * 100 + tiltX * tiltFactor,
          tiltY * tiltFactor
      );
      drawFrame(force);
    }

    public boolean shouldZoomOutWallpaper() {
      // Return true and clear onZoomChanged if we don't want a custom zoom animation
      return false;
    }

    @Override
    public void onZoomChanged(float zoom) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && zoomIntensity > 0) {
        zoomLauncher = zoomInterpolator.getInterpolation(zoom);
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
      if (((!force && !isDrawNecessary()) && SystemClock.elapsedRealtime() - lastDraw < 1000 / fps)
          || !isSurfaceAvailable || getSurfaceHolder().getSurface() == null
          // Prevents IllegalStateException when surface is not ready
          || !getSurfaceHolder().getSurface().isValid()
      ) {
        // Cancel drawing request
        return;
      }
      final SurfaceHolder surfaceHolder = getSurfaceHolder();
      Canvas canvas = null;
      try {
        if (VERSION.SDK_INT >= VERSION_CODES.O && useGpu) {
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
      } catch (Exception e) {
        Log.e(TAG, "drawFrame: unexpected exception: " + e);
      } finally {
        if (canvas != null && isSurfaceAvailable) {
          surfaceHolder.unlockCanvasAndPost(canvas);
        }
      }
    }

    private boolean isDrawNecessary() {
      return zoomLauncher == 0 || zoomLauncher == 1 || zoomUnlock == 0 || zoomUnlock == 1;
    }

    private float[] lowPass(float[] input, float[] output) {
      if (output == null) {
        return input.clone();
      }
      for (int i = 0; i < 2; i++) {
        output[i] = output[i] + 0.08f * (input[i] - output[i]);
      }
      return output;
    }

    private void animateZoom(float valueTo) {
      if (zoomAnimator != null) {
        zoomAnimator.cancel();
        zoomAnimator.removeAllUpdateListeners();
        zoomAnimator = null;
      }
      zoomAnimator = ValueAnimator.ofFloat(zoomUnlock, valueTo);
      zoomAnimator.addUpdateListener(animation -> {
        zoomUnlock = (float) animation.getAnimatedValue();
        drawFrame(false);
      });
      zoomAnimator.setInterpolator(zoomInterpolator);
      zoomAnimator.setDuration(1250).start();
    }

    private void colorsHaveChanged() {
      if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
        // NullPointerException on many devices!?
        try {
          notifyColorsChanged();
          // We have to call it again to take any effect, causes a warning...
          notifyColorsChanged();
        } catch (Exception e) {
          Log.e(TAG, "colorsHaveChanged: ", e);
        }
      }
    }
  }

  private float getFrameRate() {
    WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
    return windowManager != null ? windowManager.getDefaultDisplay().getRefreshRate() : 60;
  }
}
