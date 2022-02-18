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
 * Copyright (c) 2019-2022 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.service;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import java.util.ArrayList;
import java.util.List;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.ACTION;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.REQUEST_SOURCE;
import xyz.zedler.patrick.doodle.Constants.USER_PRESENCE;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.util.SensorUtil;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper.WallpaperVariant;

public class LiveWallpaperService extends WallpaperService {

  private static final String TAG = LiveWallpaperService.class.getSimpleName();

  // All things where we need a context or the service's context are done in this Service class
  // All other things should be done in the inner Engine class

  private static LiveWallpaperService serviceInstance = null;
  private static UserAwareEngine nonPreviewEngineInstance = null;

  private SharedPreferences sharedPrefs;
  private SvgDrawable svgDrawable;
  private BaseWallpaper wallpaper;
  private WallpaperVariant variant;
  private int variantIndex;
  private boolean nightMode, followSystem;
  private boolean isPowerSaveMode;
  private BroadcastReceiver receiver;
  private String presence;
  private boolean isReceiverRegistered = false;
  private UserPresenceListener userPresenceListener;
  private RefreshListener refreshListener;
  private SensorManager sensorManager;
  private PowerManager powerManager;

  @Override
  public void onCreate() {
    super.onCreate();

    serviceInstance = this;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    serviceInstance = null;
    unregisterReceiver();
  }

  @Override
  public Engine onCreateEngine() {
    sharedPrefs = new PrefsUtil(this).checkForMigrations().getSharedPrefs();

    receiver = new BroadcastReceiver() {
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
          case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
            isPowerSaveMode = powerManager.isPowerSaveMode();
            break;
          case ACTION.THEME_CHANGED:
            if (refreshListener != null) {
              refreshListener.onRefreshTheme();
            }
            break;
          case ACTION.SETTINGS_CHANGED:
            if (refreshListener != null) {
              refreshListener.onRefreshSettings();
            }
            break;
        }
      }
    };
    registerReceiver();

    setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    isPowerSaveMode = powerManager.isPowerSaveMode();

    return new UserAwareEngine();
  }

  public static boolean isMainEngineRunning() {
    try {
      // If instance was not cleared but the service was destroyed an exception will be thrown
      if (serviceInstance != null && serviceInstance.ping()) {
        return nonPreviewEngineInstance != null
            && nonPreviewEngineInstance.ping();
      } else {
        return false;
      }
    } catch (Exception e) {
      // destroyed/not-started
      return false;
    }
  }

  private boolean ping() {
    return true;
  }

  private void registerReceiver() {
    if (!isReceiverRegistered) {
      IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_USER_PRESENT);
      filter.addAction(Intent.ACTION_SCREEN_OFF);
      filter.addAction(Intent.ACTION_SCREEN_ON);
      filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
      filter.addAction(ACTION.THEME_CHANGED);
      filter.addAction(ACTION.SETTINGS_CHANGED);
      registerReceiver(receiver, filter);
      isReceiverRegistered = true;
    }
  }

  private void unregisterReceiver() {
    if (isReceiverRegistered) {
      try {
        unregisterReceiver(receiver);
      } catch (Exception e) {
        Log.e(TAG, "unregisterReceiver", e);
      }
      isReceiverRegistered = false;
    }
  }

  private void loadWallpaper() {
    variantIndex = sharedPrefs.getInt(
        Constants.VARIANT_PREFIX + wallpaper.getName(), 0
    );
    // This method is more efficient
    if (variantIndex >= wallpaper.getVariants().length
        || variantIndex >= wallpaper.getDarkVariants().length) {
      variantIndex = 0;
    }

    if (isNightMode()) {
      variant = wallpaper.getDarkVariants()[variantIndex];
      svgDrawable = wallpaper.getPreparedSvg(
          new SvgDrawable(this, variant.getSvgResId()), variantIndex, true
      );
    } else {
      variant = wallpaper.getVariants()[variantIndex];
      svgDrawable = wallpaper.getPreparedSvg(
          new SvgDrawable(this, variant.getSvgResId()), variantIndex, false
      );
    }

    if (svgDrawable == null) {
      // Prevent NullPointerExceptions
      svgDrawable = wallpaper.getPreparedSvg(
          new SvgDrawable(this, R.raw.wallpaper_pixel1), 1, false
      );
    }
    if (wallpaper.isDepthStatic()) {
      svgDrawable.applyRelativeElevationToAll(0.2f);
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

  private float getFrameRate() {
    WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
    return windowManager != null ? windowManager.getDefaultDisplay().getRefreshRate() : 60;
  }

  private interface UserPresenceListener {

    void onPresenceChange(String presence);
  }

  private interface RefreshListener {

    void onRefreshTheme();
    void onRefreshSettings();
  }

  // ENGINE ------------------------------------------------------------

  class UserAwareEngine extends Engine implements UserPresenceListener, RefreshListener {

    private boolean useWhiteText;
    private int zoomIntensity;
    private boolean isZoomLauncherEnabled, isZoomUnlockEnabled;
    private float zoomLauncher;
    private float zoomUnlock;
    private boolean useSystemZoom;
    private float scale;
    private int parallax;
    private int zoomRotation;
    private int zoomDuration;
    private int dampingTilt, dampingZoom;
    private boolean useZoomDamping;
    private boolean hasAccelerometer;
    private boolean isTiltEnabled;
    private float tiltX, tiltY;
    private int screenRotation;
    private int tiltThreshold;
    private float[] accelerationValues;
    private float offsetX;
    private long lastDrawZoomLauncher, lastDrawZoomUnlock, lastDrawTilt;
    private boolean isVisible;
    private boolean isNight;
    private boolean useGpu;
    private boolean useRandom;
    private boolean isListenerRegistered = false;
    private boolean isSurfaceAvailable = false;
    private boolean iconDropConsumed = true;
    private boolean isRtl = false;
    private boolean powerSaveSwipe, powerSaveTilt, powerSaveZoom;
    private float fps;
    private final TimeInterpolator zoomInterpolator = new FastOutSlowInInterpolator();
    private ValueAnimator zoomAnimator;
    private SensorEventListener sensorListener;
    private final List<Pair<Float, Float>> tiltHistory = new ArrayList<>();

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      if (!isPreview()) {
        nonPreviewEngineInstance = this;
      }

      fps = getFrameRate();

      userPresenceListener = this;
      refreshListener = this;

      sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          if (isVisible && isTiltEnabled && animTilt()) {
            accelerationValues = lowPassAcceleration(event.values, accelerationValues);
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
            float tolerance = tiltThreshold / 100f; // Allow small deviations caused by the sensor
            for (Pair<Float, Float> tilt : tiltHistory) {
              boolean isMovingX = averageX >= 0
                  ? tilt.first > averageX + tolerance
                  : tilt.first < averageX - tolerance;
              boolean isMovingY = averageY >= 0
                  ? tilt.second > averageY + tolerance
                  : tilt.second < averageY - tolerance;
              if (isMovingX || isMovingY) {
                updateOffset(false, REQUEST_SOURCE.TILT);
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

      hasAccelerometer = SensorUtil.hasAccelerometer(LiveWallpaperService.this);

      loadSettings();
      loadTheme(useRandom);

      zoomLauncher = 0;
      // This starts the zoom effect already in wallpaper preview
      zoomUnlock = useSystemZoom ? 0 : 1;
      if (!useSystemZoom) {
        animateZoom(0);
      }

      setTouchEventsEnabled(false);
      setOffsetNotificationsEnabled(true);
    }

    @Override
    public void onDestroy() {
      if (!isPreview()) {
        nonPreviewEngineInstance = null;
      }
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
      WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
      int screenRotationOld = screenRotation;
      screenRotation = window.getDefaultDisplay().getRotation();
      if (screenRotation != screenRotationOld) {
        accelerationValues = null;
        updateOffset(true, null);
      } else {
        // Not necessarily needed but recommended
        drawFrame(true, null);
      }
    }

    @Override
    public WallpaperColors onComputeColors() {
      if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
        boolean isNightMode = isNightMode();
        return variant.getWallpaperColors(
            getThemeColor(0, isNightMode),
            getThemeColor(1, isNightMode),
            getThemeColor(2, isNightMode),
            !isNightMode() && useWhiteText
        );
      } else {
        return super.onComputeColors();
      }
    }

    public boolean ping() {
      return true;
    }

    private int getThemeColor(int priority, boolean isNightMode) {
      String colorHex = sharedPrefs.getString(
          Constants.getThemeColorPref(wallpaper.getName(), variantIndex, priority, isNightMode),
          null
      );
      if (colorHex != null) {
        return Color.parseColor(colorHex);
      } else {
        return variant.getColor(priority);
      }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
      isVisible = visible;
      if (!visible) {
        return;
      }

      if (isNight != isNightMode()) {
        loadTheme(useRandom);
      }

      isRtl = getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

      if (!wallpaper.isDepthStatic()) {
        svgDrawable.applyRandomElevationToAll(0.1f);
      }

      svgDrawable.applyRandomZoomRotationToAll(-zoomRotation, zoomRotation);

      updateOffset(true, null);
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
      if (isRtl && !isPreview()) {
        offsetX = xOffset - 1;
      } else {
        offsetX = xOffset;
      }
      if (animSwipe()) {
        updateOffset(true, null);
      }
    }

    @Override
    public Bundle onCommand(
        final String action, final int x, final int y, final int z, final Bundle extras,
        final boolean resultRequested
    ) {
      switch (action) {
        case WallpaperManager.COMMAND_DROP:
          iconDropConsumed = false;
          notifyIconDropped(x, y);
          break;
        case WallpaperManager.COMMAND_TAP:
        case "android.wallpaper.wakingup":
        case "android.wallpaper.goingtosleep":
        case "android.wallpaper.reapply":
          break;
      }
      return null;
    }

    private void notifyIconDropped(int x, int y) {
      if (!iconDropConsumed) {
        iconDropConsumed = true;
      }
    }

    private void loadSettings() {
      useRandom = sharedPrefs.getBoolean(PREF.RANDOM, DEF.RANDOM);

      parallax = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
      setOffsetNotificationsEnabled(parallax != 0);

      isTiltEnabled = sharedPrefs.getBoolean(PREF.TILT, DEF.TILT);
      dampingTilt = sharedPrefs.getInt(PREF.DAMPING_TILT, DEF.DAMPING_TILT);
      dampingZoom = sharedPrefs.getInt(PREF.DAMPING_ZOOM, DEF.DAMPING_ZOOM);
      useZoomDamping = sharedPrefs.getBoolean(PREF.USE_ZOOM_DAMPING, DEF.USE_ZOOM_DAMPING);
      tiltThreshold = sharedPrefs.getInt(PREF.THRESHOLD, DEF.THRESHOLD);
      if (hasAccelerometer && isTiltEnabled && !isListenerRegistered) {
        sensorManager.registerListener(
            sensorListener,
            SensorUtil.getAccelerometer(LiveWallpaperService.this),
            // SENSOR_DELAY_GAME = 20000
            // SENSOR_DELAY_UI = 66667
            sharedPrefs.getInt(PREF.REFRESH_RATE, DEF.REFRESH_RATE)
        );
        isListenerRegistered = true;
      } else if (hasAccelerometer && isTiltEnabled) {
        sensorManager.unregisterListener(sensorListener);
        isListenerRegistered = false;
        sensorManager.registerListener(
            sensorListener,
            SensorUtil.getAccelerometer(LiveWallpaperService.this),
            // SENSOR_DELAY_GAME = 20000
            // SENSOR_DELAY_UI = 66667
            sharedPrefs.getInt(PREF.REFRESH_RATE, DEF.REFRESH_RATE)
        );
        isListenerRegistered = true;
      } else if (isListenerRegistered) {
        sensorManager.unregisterListener(sensorListener);
        isListenerRegistered = false;
      }

      scale = sharedPrefs.getFloat(
          PREF.SCALE, SvgDrawable.getDefaultScale(LiveWallpaperService.this)
      );
      if (svgDrawable != null) {
        svgDrawable.setScale(scale);
      }
      zoomIntensity = sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM);
      isZoomLauncherEnabled = sharedPrefs.getBoolean(PREF.ZOOM_LAUNCHER, DEF.ZOOM_LAUNCHER);
      isZoomUnlockEnabled = sharedPrefs.getBoolean(PREF.ZOOM_UNLOCK, DEF.ZOOM_UNLOCK);
      useSystemZoom = sharedPrefs.getBoolean(PREF.ZOOM_SYSTEM, DEF.ZOOM_SYSTEM);
      zoomDuration = sharedPrefs.getInt(PREF.ZOOM_DURATION, DEF.ZOOM_DURATION);
      zoomRotation = sharedPrefs.getInt(PREF.ZOOM_ROTATION, DEF.ZOOM_ROTATION);

      powerSaveSwipe = sharedPrefs.getBoolean(PREF.POWER_SAVE_SWIPE, DEF.POWER_SAVE_SWIPE);
      powerSaveTilt = sharedPrefs.getBoolean(PREF.POWER_SAVE_TILT, DEF.POWER_SAVE_TILT);
      powerSaveZoom = sharedPrefs.getBoolean(PREF.POWER_SAVE_ZOOM, DEF.POWER_SAVE_ZOOM);
    }

    private void loadTheme(boolean random) {
      if (random) {
        String previous = wallpaper != null ? wallpaper.getName() : "";
        wallpaper = Constants.getRandomWallpaper(
            sharedPrefs.getStringSet(PREF.RANDOM_LIST, DEF.RANDOM_LIST), previous
        );
      } else {
        wallpaper = Constants.getWallpaper(sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER));
      }
      nightMode = sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
      followSystem = sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM);
      useWhiteText = sharedPrefs.getBoolean(PREF.USE_WHITE_TEXT, DEF.USE_WHITE_TEXT);
      isNight = isNightMode();

      loadWallpaper();
      svgDrawable.setScale(scale);

      if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
        // NullPointerException on many devices!?
        try {
          notifyColorsChanged();
          if (VERSION.SDK_INT < VERSION_CODES.S) {
            notifyColorsChanged();
          }
        } catch (Exception e) {
          Log.e(TAG, "colorsHaveChanged", e);
        }
      }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOffset(boolean force, String source) {
      float xOffset = parallax != 0 ? offsetX : 0;
      int tiltFactor = 18 * parallax * (isTiltEnabled ? 1 : 0);
      float finalTiltX, finalTiltY;
      switch (screenRotation) {
        case Surface.ROTATION_90:
          finalTiltX = tiltY;
          finalTiltY = -tiltX;
          break;
        case Surface.ROTATION_180:
          finalTiltX = -tiltX;
          finalTiltY = -tiltY;
          break;
        case Surface.ROTATION_270:
          finalTiltX = -tiltY;
          finalTiltY = tiltX;
          break;
        case Surface.ROTATION_0:
        default:
          finalTiltX = tiltX;
          finalTiltY = tiltY;
          break;
      }
      svgDrawable.setOffset(
          xOffset * parallax * 100 + finalTiltX * tiltFactor,
          finalTiltY * tiltFactor
      );
      drawFrame(force, source);
    }

    /**
     * WallpaperService.Engine#shouldZoomOutWallpaper()
     */
    public boolean shouldZoomOutWallpaper() {
      // Return true and clear onZoomChanged if we don't want a custom zoom animation
      return isZoomLauncherEnabled && useSystemZoom && animZoom();
    }

    @Override
    public void onZoomChanged(float zoom) {
      if (!useSystemZoom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && zoomIntensity > 0 && animZoom()) {
          if (useZoomDamping) {
            zoomLauncher = lowPassZoom(zoom, zoomLauncher);
          } else {
            zoomLauncher = zoomInterpolator.getInterpolation(zoom);
          }
          drawFrame(false, REQUEST_SOURCE.ZOOM_LAUNCHER);
        }
      }
    }

    @Override
    public void onPresenceChange(String presence) {
      switch (presence) {
        case USER_PRESENCE.OFF:
          if (useRandom) {
            loadTheme(true);
          }
          if (isZoomUnlockEnabled && animZoom()) {
            zoomUnlock = 1;
            zoomLauncher = 0; // 1 or 0?
          }
          if (useRandom || (isZoomUnlockEnabled && animZoom())) {
            drawFrame(true, null);
          }
          break;
        case USER_PRESENCE.LOCKED:
          if (isZoomUnlockEnabled && animZoom()) {
            zoomLauncher = 0;
            animateZoom(0.5f);
          }
          break;
        case USER_PRESENCE.UNLOCKED:
          if (isVisible && animZoom()) {
            animateZoom(0);
          } else {
            zoomUnlock = 0;
            drawFrame(true, null);
          }
          break;
      }
    }

    @Override
    public void onRefreshTheme() {
      loadTheme(false);
    }

    @Override
    public void onRefreshSettings() {
      loadSettings();
    }

    void drawFrame(boolean force, String source) {
      if (!isDrawingAllowed(force, source)) {
        // Cancel drawing request
        return;
      } else if (!isSurfaceAvailable || getSurfaceHolder().getSurface() == null) {
        // Cancel drawing request
        return;
      } else if (!getSurfaceHolder().getSurface().isValid()) {
        // Prevents IllegalStateException when surface is not ready
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

          if (source != null) {
            switch (source) {
              case REQUEST_SOURCE.ZOOM_LAUNCHER:
                lastDrawZoomLauncher = SystemClock.elapsedRealtime();
                break;
              case REQUEST_SOURCE.ZOOM_UNLOCK:
                lastDrawZoomUnlock = SystemClock.elapsedRealtime();
                break;
              case REQUEST_SOURCE.TILT:
                lastDrawTilt = SystemClock.elapsedRealtime();
                break;
            }
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "drawFrame: unexpected exception", e);
      } finally {
        try {
          if (canvas != null && isSurfaceAvailable) {
            surfaceHolder.unlockCanvasAndPost(canvas);
          }
        } catch (Exception e) {
          Log.e(TAG, "drawFrame: unlocking canvas failed", e);
        }
      }
    }

    private boolean isDrawingAllowed(boolean force, String source) {
      if (force || zoomLauncher == 0 || zoomLauncher == 1 || zoomUnlock == 0 || zoomUnlock == 1) {
        return true;
      } else if (source != null) {
        if (source.equals(REQUEST_SOURCE.ZOOM_LAUNCHER)
            && SystemClock.elapsedRealtime() - lastDrawZoomLauncher < 1000 / fps) {
          return true;
        } else if (source.equals(REQUEST_SOURCE.ZOOM_UNLOCK)
            && SystemClock.elapsedRealtime() - lastDrawZoomUnlock < 1000 / fps) {
          return true;
        } else {
          return source.equals(REQUEST_SOURCE.TILT)
              && SystemClock.elapsedRealtime() - lastDrawTilt < 1000 / fps;
        }
      } else {
        return false;
      }
    }

    private float[] lowPassAcceleration(float[] input, float[] output) {
      if (output == null) {
        return input.clone();
      }
      for (int i = 0; i < input.length; i++) {
        output[i] = output[i] + (dampingTilt / 100f) * (input[i] - output[i]);
      }
      return output;
    }

    private float lowPassZoom(float input, float output) {
      return (output + (dampingZoom / 100f) * (input - output)) * input;
    }

    private void animateZoom(float valueTo) {
      if (zoomAnimator != null) {
        zoomAnimator.pause();
        zoomAnimator.cancel();
        zoomAnimator.removeAllUpdateListeners();
        zoomAnimator = null;
      }
      zoomAnimator = ValueAnimator.ofFloat(zoomUnlock, valueTo);
      zoomAnimator.addUpdateListener(animation -> {
        zoomUnlock = (float) animation.getAnimatedValue();
        drawFrame(false, REQUEST_SOURCE.ZOOM_UNLOCK);
      });
      zoomAnimator.setInterpolator(zoomInterpolator);
      zoomAnimator.setDuration(zoomDuration).start();
    }

    private boolean animSwipe() {
      return !(isPowerSaveMode && powerSaveSwipe);
    }

    private boolean animTilt() {
      return !(isPowerSaveMode && powerSaveTilt);
    }

    private boolean animZoom() {
      return !(isPowerSaveMode && powerSaveZoom);
    }
  }
}
