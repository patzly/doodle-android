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
import android.annotation.SuppressLint;
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import java.util.LinkedList;
import java.util.Objects;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.ACTION;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.NIGHT_MODE;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.RANDOM;
import xyz.zedler.patrick.doodle.Constants.REQUEST_SOURCE;
import xyz.zedler.patrick.doodle.Constants.USER_PRESENCE;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.drawable.SvgDrawable;
import xyz.zedler.patrick.doodle.util.PrefsUtil;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper.WallpaperVariant;

public class LiveWallpaperService extends WallpaperService {

  private static final String TAG = LiveWallpaperService.class.getSimpleName();

  // All things which depend on BroadcastReceiver and global service management are contained
  // in this outer class because of onDestroy, other stuff should be contained in the inner class

  private static LiveWallpaperService serviceInstance = null;
  @SuppressLint("StaticFieldLeak")
  private static UserAwareEngine nonPreviewEngineInstance = null;

  private BroadcastReceiver receiver;
  private boolean isReceiverRegistered = false;

  private String userPresence;
  private UserPresenceListener userPresenceListener;
  private RefreshListener refreshListener;

  private boolean isPowerSaveMode;
  private PowerManager powerManager;
  private KeyguardManager keyguardManager;

  private interface UserPresenceListener {

    void onPresenceChange(String presence);
  }

  private interface RefreshListener {

    void onRefreshTheme(boolean designMightHaveChanged);
    void onRefreshSettings();
    void onRefreshDaily();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    serviceInstance = this;

    keyguardManager = (KeyguardManager)  getSystemService(Context.KEYGUARD_SERVICE);
    powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

    isPowerSaveMode = powerManager.isPowerSaveMode();

    receiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        if (intent == null) {
          return;
        }
        switch (intent.getAction()) {
          case Intent.ACTION_SCREEN_OFF:
            setUserPresence(USER_PRESENCE.OFF);
            break;
          case Intent.ACTION_SCREEN_ON:
            setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);
            break;
          case Intent.ACTION_USER_PRESENT:
            setUserPresence(USER_PRESENCE.UNLOCKED);
            break;
          case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
            if (powerManager != null) {
              isPowerSaveMode = powerManager.isPowerSaveMode();
            }
            break;
          case ACTION.THEME_AND_DESIGN_CHANGED:
            if (refreshListener != null) {
              refreshListener.onRefreshTheme(true);
            }
            break;
          case ACTION.THEME_CHANGED:
            if (refreshListener != null) {
              refreshListener.onRefreshTheme(false);
            }
            break;
          case ACTION.SETTINGS_CHANGED:
            if (refreshListener != null) {
              refreshListener.onRefreshSettings();
            }
            break;
          case ACTION.NEW_DAILY:
            if (refreshListener != null) {
              refreshListener.onRefreshDaily();
            }
            break;
        }
      }
    };

    if (!isReceiverRegistered) {
      IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_USER_PRESENT);
      filter.addAction(Intent.ACTION_SCREEN_OFF);
      filter.addAction(Intent.ACTION_SCREEN_ON);
      filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
      filter.addAction(ACTION.THEME_AND_DESIGN_CHANGED);
      filter.addAction(ACTION.THEME_CHANGED);
      filter.addAction(ACTION.SETTINGS_CHANGED);
      filter.addAction(ACTION.NEW_DAILY);
      try {
        registerReceiver(receiver, filter);
        isReceiverRegistered = true;
      } catch (Exception e) {
        Log.e(TAG, "onCreate", e);
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (isReceiverRegistered) {
      try {
        unregisterReceiver(receiver);
        isReceiverRegistered = false;
      } catch (Exception e) {
        Log.e(TAG, "onDestroy", e);
      }
    }
    serviceInstance = null;
  }

  @Override
  public Engine onCreateEngine() {
    setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);
    return new UserAwareEngine();
  }

  /**
   * Returns true if the lockscreen is shown to the user
   */
  private boolean isKeyguardLocked() {
    if (keyguardManager != null) {
      return keyguardManager.isKeyguardLocked();
    } else {
      // To animate zoom kind of normally
      return true;
    }
  }

  /**
   * Notifies the registered listener about user presence changes
   */
  private void setUserPresence(String presence) {
    if (presence.equals(userPresence)) {
      return;
    }
    userPresence = presence;
    if (userPresenceListener != null) {
      userPresenceListener.onPresenceChange(presence);
    }
  }

  /**
   * Returns true if a non-preview engine is running, otherwise false
   */
  public static boolean isMainEngineRunning() {
    try {
      // If instance was not cleared but the service was destroyed an exception will be thrown
      if (serviceInstance != null && serviceInstance.ping()) {
        return nonPreviewEngineInstance != null && nonPreviewEngineInstance.ping();
      }
      return false;
    } catch (Exception e) {
      // destroyed or not started
      return false;
    }
  }

  /**
   * Dummy method to signal that the current instance is running and can be called
   */
  private boolean ping() {
    return true;
  }

  // ENGINE

  private class UserAwareEngine extends Engine implements UserPresenceListener, RefreshListener {

    private static final int MAX_TILT_HISTORY_SIZE = 30;

    private Context context;
    private SharedPreferences sharedPrefs;

    // General rendering
    private boolean useGpu;
    private boolean isSurfaceAvailable;
    private boolean isVisible;
    private float fps;
    private int screenRotation;
    private Display display;

    // Appearance
    private SvgDrawable svgDrawable;
    private BaseWallpaper wallpaper;
    private WallpaperVariant variant;
    private int variantIndex;
    private int nightModePref;
    private boolean isNightMode;
    private boolean useDarkText, forceLightText;
    private String randomMode;
    private boolean isNewDailyPending;

    // Parallax
    private int parallaxIntensity;
    private boolean isRtl;
    private float offsetX;
    private boolean isTiltEnabled;
    private int dampingTilt, tiltThreshold, tiltRefreshRate;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;
    private boolean isSensorListenerRegistered;
    private float[] accelerationValues;
    private final LinkedList<Pair<Float, Float>> tiltHistory = new LinkedList<>();
    private float tiltX, tiltY;
    private long lastDrawSwipe, lastDrawTilt;
    private boolean powerSaveSwipe, powerSaveTilt;

    // Size
    private float scale;
    private int zoomIntensity;
    private float zoomLauncher, zoomUnlock;
    private boolean useSystemZoom;
    private boolean isZoomLauncherEnabled, isZoomUnlockEnabled;
    private int zoomRotation;
    private int zoomDuration;
    private boolean useZoomDamping;
    private int dampingZoom;
    private long lastDrawZoomLauncher, lastDrawZoomUnlock;
    private boolean powerSaveZoom;
    private ValueAnimator zoomAnimator;
    private final TimeInterpolator zoomInterpolator = new FastOutSlowInInterpolator();

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      setUserPresence(isKeyguardLocked() ? USER_PRESENCE.LOCKED : USER_PRESENCE.UNLOCKED);
      if (!isPreview()) {
        nonPreviewEngineInstance = this;
      }

      context = LiveWallpaperService.this;
      sharedPrefs = new PrefsUtil(context).checkForMigrations().getSharedPrefs();

      sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          if (isVisible && isTiltEnabled && animTilt()) {
            accelerationValues = lowPassAcceleration(event.values, accelerationValues);
            tiltX = accelerationValues[0];
            tiltY = -accelerationValues[1];

            tiltHistory.add(new Pair<>(tiltX, tiltY));
            while (tiltHistory.size() > MAX_TILT_HISTORY_SIZE) {
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
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
      };

      WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
      display = windowManager.getDefaultDisplay();
      fps = display.getRefreshRate();
      screenRotation = display.getRotation();

      // Load this only once on creation, else it would cause a crash caused by OpenGL
      useGpu = sharedPrefs.getBoolean(PREF.GPU, DEF.GPU);

      loadSettings();
      loadThemeAndDesign(!randomMode.equals(RANDOM.OFF));

      zoomLauncher = 0;
      // This starts the zoom effect already in wallpaper preview
      zoomUnlock = useSystemZoom ? 0 : 1;
      if (!useSystemZoom) {
        animateZoom(0);
      }

      // After all necessary variables have been set
      userPresenceListener = this;
      refreshListener = this;

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
      if (sensorManager != null && isSensorListenerRegistered) {
        sensorManager.unregisterListener(sensorListener);
        isSensorListenerRegistered = false;
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
      int screenRotationOld = screenRotation;
      screenRotation = display.getRotation();
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
            useDarkText, forceLightText
        );
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

      if (isNightMode != isNightMode()) {
        loadThemeOnly();
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
        updateOffset(false, REQUEST_SOURCE.SWIPE);
      }
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
          if (randomMode.equals(RANDOM.SCREEN_OFF) ||
              (randomMode.equals(RANDOM.DAILY) && isNewDailyPending)) {
            sharedPrefs.edit().remove(PREF.RANDOM_CURRENT).apply();
            loadThemeAndDesign(true);
            isNewDailyPending = false;
          }
          if (isZoomUnlockEnabled && animZoom()) {
            zoomUnlock = 1;
            zoomLauncher = 0; // 1 or 0?
          }
          if (!randomMode.equals(RANDOM.OFF) || (isZoomUnlockEnabled && animZoom())) {
            drawFrame(true, null);
          }
          setTiltListenerRegistered(false);
          System.gc();
          break;
        case USER_PRESENCE.LOCKED:
          setTiltListenerRegistered(true);
          if (isZoomUnlockEnabled && animZoom()) {
            zoomLauncher = 0;
            animateZoom(0.5f);
          }
          break;
        case USER_PRESENCE.UNLOCKED:
          setTiltListenerRegistered(true);
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
    public void onRefreshTheme(boolean designMightHaveChanged) {
      if (designMightHaveChanged) {
        sharedPrefs.edit().remove(PREF.RANDOM_CURRENT).apply();
      }
      loadTheme(designMightHaveChanged, !randomMode.equals(RANDOM.OFF));
    }

    @Override
    public void onRefreshSettings() {
      loadSettings();
    }

    @Override
    public void onRefreshDaily() {
      if (randomMode.equals(RANDOM.DAILY)) {
        String presence = LiveWallpaperService.this.userPresence;
        if (Objects.equals(presence, USER_PRESENCE.OFF)) {
          sharedPrefs.edit().remove(PREF.RANDOM_CURRENT).apply();
          loadThemeAndDesign(true);
          isNewDailyPending = false; // just to make it sure
          if (isZoomUnlockEnabled && animZoom()) {
            zoomUnlock = 1;
            zoomLauncher = 0; // 1 or 0?
          }
          drawFrame(true, null);
          System.gc();
        } else {
          isNewDailyPending = true;
        }
      }
    }

    /**
     * Loads all required preferences for the variables of the engine, except theme components.
     */
    private void loadSettings() {
      parallaxIntensity = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
      // disables zooming so this should not be disabled
      // setOffsetNotificationsEnabled(parallaxIntensity != 0);

      randomMode = sharedPrefs.getString(PREF.RANDOM, DEF.RANDOM);
      isTiltEnabled = sharedPrefs.getBoolean(PREF.TILT, DEF.TILT);
      dampingTilt = sharedPrefs.getInt(PREF.DAMPING_TILT, DEF.DAMPING_TILT);
      dampingZoom = sharedPrefs.getInt(PREF.DAMPING_ZOOM, DEF.DAMPING_ZOOM);
      useZoomDamping = sharedPrefs.getBoolean(PREF.USE_ZOOM_DAMPING, DEF.USE_ZOOM_DAMPING);
      tiltThreshold = sharedPrefs.getInt(PREF.THRESHOLD, DEF.THRESHOLD);
      tiltRefreshRate = sharedPrefs.getInt(PREF.TILT_REFRESH_RATE, DEF.TILT_REFRESH_RATE);

      // restart tilt listener (if previously enabled) or start it, all only if tilt is enabled
      setTiltListenerRegistered(false);
      setTiltListenerRegistered(true);

      scale = sharedPrefs.getFloat(PREF.SCALE, SvgDrawable.getDefaultScale(context));
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

    /**
     * Loads all required preferences for theme components but reload the design only if necessary.
     */
    private void loadTheme(boolean designMightHaveChanged, boolean useRandomDesign) {
      if (designMightHaveChanged) {
        if (useRandomDesign) {
          String randomCurrent = sharedPrefs.getString(PREF.RANDOM_CURRENT, null);
          if (randomCurrent != null) {
            // Don't change design after device restart or after engine changes to non-preview
            wallpaper = Constants.getWallpaper(randomCurrent);
          } else {
            String previous = wallpaper != null ? wallpaper.getName() : "";
            wallpaper = Constants.getRandomWallpaper(
                sharedPrefs.getStringSet(PREF.RANDOM_LIST, DEF.RANDOM_LIST), previous
            );
            sharedPrefs.edit().putString(PREF.RANDOM_CURRENT, wallpaper.getName()).apply();
          }
        } else {
          wallpaper = Constants.getWallpaper(sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER));
        }
      }
      nightModePref = sharedPrefs.getInt(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
      isNightMode = isNightMode();

      useDarkText = sharedPrefs.getBoolean(PREF.USE_DARK_TEXT, DEF.USE_DARK_TEXT);
      forceLightText = sharedPrefs.getBoolean(PREF.FORCE_LIGHT_TEXT, DEF.FORCE_LIGHT_TEXT);

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

    /**
     * Loads all required preferences for theme components but leaves the design untouched.
     */
    private void loadThemeOnly() {
      loadTheme(false, false);
    }

    /**
     * Loads all required preferences for theme components and design.
     */
    private void loadThemeAndDesign(boolean useRandomDesign) {
      loadTheme(true, useRandomDesign);
    }

    /**
     * Fills the wallpaper component with all parts specified by the current theme/design variables.
     */
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
            new SvgDrawable(context, variant.getSvgResId()), variantIndex, true
        );
      } else {
        variant = wallpaper.getVariants()[variantIndex];
        svgDrawable = wallpaper.getPreparedSvg(
            new SvgDrawable(context, variant.getSvgResId()), variantIndex, false
        );
      }

      if (svgDrawable == null) {
        // Prevent NullPointerExceptions
        svgDrawable = wallpaper.getPreparedSvg(
            new SvgDrawable(context, R.raw.wallpaper_pixel1), 1, false
        );
      }
      if (wallpaper.isDepthStatic()) {
        svgDrawable.applyRelativeElevationToAll(0.2f);
      }
    }

    /**
     * Draws the final frame onto the surface.
     */
    private void drawFrame(boolean force, String source) {
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
              case REQUEST_SOURCE.SWIPE:
                lastDrawSwipe = SystemClock.elapsedRealtime();
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

    /**
     * Returns true if drawing is forced or if enough time is passed since th last rendered frame.
     */
    private boolean isDrawingAllowed(boolean force, String source) {
      if (force) {
        return true;
      } else if (source != null) {
        switch (source) {
          case REQUEST_SOURCE.SWIPE:
            return SystemClock.elapsedRealtime() - lastDrawSwipe >= 1000 / fps;
          case REQUEST_SOURCE.TILT:
            return SystemClock.elapsedRealtime() - lastDrawTilt >= 1000 / fps;
          case REQUEST_SOURCE.ZOOM_LAUNCHER:
            if (zoomLauncher == 0 || zoomLauncher == 1) {
              return true;
            } else {
              return SystemClock.elapsedRealtime() - lastDrawZoomLauncher >= 1000 / fps;
            }
          case REQUEST_SOURCE.ZOOM_UNLOCK:
            if (zoomUnlock == 0 || zoomUnlock == 1) {
              return true;
            } else {
              return SystemClock.elapsedRealtime() - lastDrawZoomUnlock >= 1000 / fps;
            }
          default:
            Log.e(TAG, "isDrawingAllowed: source must be constant from REQUEST_SOURCE");
            return true;
        }
      } else {
        Log.e(TAG, "isDrawingAllowed: source must be defined if drawing is not forced");
        return true;
      }
    }

    /**
     * Returns true if night mode is activated explicitly by the user or if the UI uses night mode
     * when the user set it to NIGHT_MODE.AUTO (follow system).
     */
    private boolean isNightMode() {
      if (nightModePref == NIGHT_MODE.ON) {
        return true;
      }
      int flags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
      return nightModePref == NIGHT_MODE.AUTO && flags == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Returns the associated theme color for the given arguments.
     *
     * @param priority Either 0 for primary, 1 for secondary or 2 for tertiary color.
     * @param isNightMode If the returned color should be from the light or the dark variant.
     */
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

    /**
     * Registers or unregisters the acceleration event listener dependent of the current config.
     */
    private void setTiltListenerRegistered(boolean registered) {
      if (accelerometer == null) {
        return;
      }
      if (registered && !isSensorListenerRegistered && isTiltEnabled) {
        // SENSOR_DELAY_GAME = 20000
        // SENSOR_DELAY_UI = 66667
        sensorManager.registerListener(sensorListener, accelerometer, tiltRefreshRate);
        isSensorListenerRegistered = true;
      } else if (!registered && isSensorListenerRegistered) {
        sensorManager.unregisterListener(sensorListener);
        isSensorListenerRegistered = false;
      }
    }

    /**
     * Returns an array with the three values from the acceleration sensor, in relation to the
     * previous values (low pass filter).
     */
    private float[] lowPassAcceleration(float[] input, float[] output) {
      if (output == null) {
        return input.clone();
      }
      for (int i = 0; i < input.length; i++) {
        output[i] = output[i] + (dampingTilt / 100f) * (input[i] - output[i]);
      }
      return output;
    }

    /**
     * Returns the new zoom amount from input in relation to output, so that the returned value
     * is always smooth regarding the previous value. Also to hide a jerky animation pause in
     * Android if quick settings are nearly closed after being opened, the zoom amount tends toward
     * 0 when input is nearly 0.
     */
    private float lowPassZoom(float input, float output) {
      return (output + (dampingZoom / 100f) * (input - output)) * input;
    }

    /**
     * Cancel any running zoom animation and animate zoom from current zoom amount to the given
     * amount.
     *
     * @param valueTo The final zoom amount at the end of the animation, 0 is no zoom, 1 is max.
     */
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

    /**
     * An overridden method from the Engine class, will not be recognized as overridden but logging
     * shows that the method is called.
     * WallpaperService.Engine#shouldZoomOutWallpaper()
     */
    public boolean shouldZoomOutWallpaper() {
      // Return true and clear onZoomChanged if we don't want a custom zoom animation
      return isZoomLauncherEnabled && useSystemZoom && animZoom();
    }

    /**
     * Updates the wallpaper offset according to the current parallax and tilt values.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOffset(boolean force, String source) {
      float xOffset = parallaxIntensity != 0 ? offsetX : 0;
      int tiltFactor = 18 * parallaxIntensity * (isTiltEnabled ? 1 : 0);
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
          xOffset * parallaxIntensity * 100 + finalTiltX * tiltFactor,
          finalTiltY * tiltFactor
      );
      drawFrame(force, source);
    }

    /**
     * Returns if swipe should either be animated or not depending on battery saver preferences.
     */
    private boolean animSwipe() {
      return !(isPowerSaveMode && powerSaveSwipe);
    }

    /**
     * Returns if tilt should either be activated or not depending on battery saver preferences.
     */
    private boolean animTilt() {
      return !(isPowerSaveMode && powerSaveTilt);
    }

    /**
     * Returns if zoom should either be animated or not depending on battery saver preferences.
     */
    private boolean animZoom() {
      return !(isPowerSaveMode && powerSaveZoom);
    }

    /**
     * Dummy method to signal that this instance is running and can be called
     */
    public boolean ping() {
      return true;
    }
  }
}
