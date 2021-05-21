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

import android.app.WallpaperColors;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import java.util.Random;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.Constants.VARIANT;
import xyz.zedler.patrick.doodle.Constants.WALLPAPER;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.MigrationUtil;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class LiveWallpaperService extends WallpaperService {

  private final static String TAG = LiveWallpaperService.class.getSimpleName();

  private SharedPreferences sharedPrefs;
  private Theme theme;
  private String wallpaper, variant;
  private boolean nightMode, followSystem, isNight;
  private int colorBackground;
  private int parallax;
  private float size;
  private float fps;
  private int zoomIntensity;

  private VectorDrawableCompat doodleArc;
  private VectorDrawableCompat doodleDot;
  private VectorDrawableCompat doodleU;
  private VectorDrawableCompat doodleRect;
  private VectorDrawableCompat doodleRing;
  private VectorDrawableCompat doodleMoon;
  private VectorDrawableCompat doodlePoly;

  private VectorDrawableCompat neonKidneyFront;
  private VectorDrawableCompat neonCircleFront;
  private VectorDrawableCompat neonPill;
  private VectorDrawableCompat neonLine;
  private VectorDrawableCompat neonKidneyBack;
  private VectorDrawableCompat neonCircleBack;
  private VectorDrawableCompat neonDot;

  private VectorDrawableCompat geometricRect;
  private VectorDrawableCompat geometricLine;
  private VectorDrawableCompat geometricPoly;
  private VectorDrawableCompat geometricCircle;
  private VectorDrawableCompat geometricSheet;

  private float zDoodleArc;
  private float zDoodleDot;
  private float zDoodleU;
  private float zDoodleRect;
  private float zDoodleRing;
  private float zDoodleMoon;
  private float zDoodlePoly;

  private float zNeonKidneyFront;
  private float zNeonCircleFront;
  private float zNeonPill;
  private float zNeonLine;
  private float zNeonKidneyBack;
  private float zNeonCircleBack;
  private float zNeonDot;

  private float zGeometricRect;
  private float zGeometricLine;
  private float zGeometricPoly;
  private float zGeometricCircle;
  private float zGeometricSheet;

  @Override
  public Engine onCreateEngine() {
    sharedPrefs = new PrefsUtil(this).getSharedPrefs();
    new MigrationUtil(sharedPrefs).checkForMigrations();

    wallpaper = sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER);
    variant = sharedPrefs.getString(PREF.VARIANT, DEF.VARIANT);
    nightMode = sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
    followSystem = sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM);
    parallax = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
    size = sharedPrefs.getFloat(PREF.SIZE, DEF.SIZE);
    zoomIntensity = sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM);

    isNight = isNightMode();
    theme = getResources().newTheme();
    fps = getFrameRate();

    newRandomZ();
    refreshTheme();

    return new CustomEngine();
  }

  private void refreshTheme() {
    if (isNightMode()) {
      switch (wallpaper) {
        case WALLPAPER.DOODLE:
          theme.applyStyle(R.style.Wallpaper_Doodle_Night, true);
          colorBackground = getCompatColor(R.color.wp_bg_doodle_night);
          break;
        case WALLPAPER.NEON:
          theme.applyStyle(R.style.Wallpaper_Neon_Night, true);
          colorBackground = getCompatColor(R.color.wp_bg_neon_night);
          break;
        case WALLPAPER.GEOMETRIC:
          theme.applyStyle(R.style.Wallpaper_Geometric_Night, true);
          colorBackground = getCompatColor(R.color.wp_bg_geometric_night);
          break;
      }
    } else {
      switch (wallpaper) {
        case WALLPAPER.DOODLE:
          switch (variant) {
            case Constants.VARIANT.BLACK:
              theme.applyStyle(R.style.Wallpaper_Doodle_Black, true);
              colorBackground = getCompatColor(R.color.wp_bg_doodle_black);
              break;
            case Constants.VARIANT.WHITE:
              theme.applyStyle(R.style.Wallpaper_Doodle_White, true);
              colorBackground = getCompatColor(R.color.wp_bg_doodle_white);
              break;
            case Constants.VARIANT.ORANGE:
              theme.applyStyle(R.style.Wallpaper_Doodle_Orange, true);
              colorBackground = getCompatColor(R.color.wp_bg_doodle_orange);
              break;
          }
          break;
        case WALLPAPER.NEON:
          theme.applyStyle(R.style.Wallpaper_Neon, true);
          colorBackground = getCompatColor(R.color.wp_bg_neon);
          break;
        case WALLPAPER.GEOMETRIC:
          theme.applyStyle(R.style.Wallpaper_Geometric, true);
          colorBackground = getCompatColor(R.color.wp_bg_geometric);
          break;
      }
    }

    switch (wallpaper) {
      case WALLPAPER.DOODLE:
        doodleArc = getVectorDrawable(R.drawable.doodle_shape_arc);
        doodleDot = getVectorDrawable(R.drawable.doodle_shape_dot);
        doodleU = getVectorDrawable(R.drawable.doodle_shape_u);
        doodleRect = getVectorDrawable(R.drawable.doodle_shape_rect);
        doodleRing = getVectorDrawable(R.drawable.doodle_shape_ring);
        doodleMoon = getVectorDrawable(R.drawable.doodle_shape_moon);
        doodlePoly = getVectorDrawable(R.drawable.doodle_shape_poly);
        break;
      case WALLPAPER.NEON:
        neonKidneyFront = getVectorDrawable(R.drawable.neon_shape_kidney_front);
        neonCircleFront = getVectorDrawable(R.drawable.neon_shape_circle_front);
        neonPill = getVectorDrawable(R.drawable.neon_shape_pill);
        neonLine = getVectorDrawable(R.drawable.neon_shape_line);
        neonKidneyBack = getVectorDrawable(R.drawable.neon_shape_kidney_back);
        neonCircleBack = getVectorDrawable(R.drawable.neon_shape_circle_back);
        neonDot = getVectorDrawable(R.drawable.neon_shape_dot);
        break;
      case WALLPAPER.GEOMETRIC:
        geometricRect = getVectorDrawable(R.drawable.geometric_shape_rect);
        geometricLine = getVectorDrawable(R.drawable.geometric_shape_line);
        geometricPoly = getVectorDrawable(R.drawable.geometric_shape_poly);
        geometricCircle = getVectorDrawable(R.drawable.geometric_shape_circle);
        geometricSheet = getVectorDrawable(R.drawable.geometric_shape_sheet);
        break;
    }
  }

  private void newRandomZ() {
    Random random = new Random();
    switch (wallpaper) {
      case WALLPAPER.DOODLE:
        zDoodleArc = random.nextFloat();
        zDoodleDot = random.nextFloat();
        zDoodleU = random.nextFloat();
        zDoodleRect = random.nextFloat();
        zDoodleRing = random.nextFloat();
        zDoodleMoon = random.nextFloat();
        zDoodlePoly = random.nextFloat();
        break;
      case WALLPAPER.NEON:
        zNeonKidneyFront = random.nextFloat();
        zNeonCircleFront = random.nextFloat();
        zNeonPill = random.nextFloat();
        zNeonLine = random.nextFloat();
        zNeonKidneyBack = random.nextFloat();
        zNeonCircleBack = random.nextFloat();
        zNeonDot = random.nextFloat();
        break;
      case WALLPAPER.GEOMETRIC:
        zGeometricRect = random.nextFloat();
        zGeometricLine = random.nextFloat();
        zGeometricPoly = random.nextFloat();
        zGeometricCircle = random.nextFloat();
        zGeometricSheet = random.nextFloat();
        break;
    }
  }

  private boolean isNightMode() {
    if (nightMode && !followSystem) {
      return true;
    }
    int flags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightMode && flags == Configuration.UI_MODE_NIGHT_YES;
  }

  private int getCompatColor(@ColorRes int resId) {
    return ContextCompat.getColor(this, resId);
  }

  private float getFrameRate() {
    WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
    return windowManager != null ? windowManager.getDefaultDisplay().getRefreshRate() : 60;
  }

  private VectorDrawableCompat getVectorDrawable(@DrawableRes int resId) {
    return VectorDrawableCompat.create(getResources(), resId, theme);
  }

  class CustomEngine extends Engine {
    private float xOffset = 0;
    private float zoom = 0;
    private long lastDraw;

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      setTouchEventsEnabled(false);
    }

    @Override
    public WallpaperColors onComputeColors() {
      int background = 0xFF232323;
      if (isNightMode()) {
        switch (wallpaper) {
          case WALLPAPER.DOODLE:
            background = 0xFF272628;
            break;
          case WALLPAPER.NEON:
            background = 0xFF0e032d;
            break;
          case WALLPAPER.GEOMETRIC:
            background = 0xFF212121;
            break;
        }
      } else {
        switch (wallpaper) {
          case WALLPAPER.DOODLE:
            switch (variant) {
              case VARIANT.BLACK:
                background = 0xFF232323;
                break;
              case VARIANT.WHITE:
                background = 0xFFdbd7ce;
                break;
              case VARIANT.ORANGE:
                background = 0xFFfbb29e;
                break;
            }
            break;
          case WALLPAPER.NEON:
            background = 0xFFcbcbef;
            break;
          case WALLPAPER.GEOMETRIC:
            background = 0xFFb9c1c7;
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
      super.onVisibilityChanged(visible);
      if (!visible) {
        return;
      }

      String wallpaperNew = sharedPrefs.getString(PREF.WALLPAPER, DEF.WALLPAPER);
      String variantNew = sharedPrefs.getString(Constants.PREF.VARIANT, DEF.VARIANT);

      nightMode = sharedPrefs.getBoolean(PREF.NIGHT_MODE, DEF.NIGHT_MODE);
      followSystem = sharedPrefs.getBoolean(PREF.FOLLOW_SYSTEM, DEF.FOLLOW_SYSTEM);
      parallax = sharedPrefs.getInt(PREF.PARALLAX, DEF.PARALLAX);
      size = sharedPrefs.getFloat(PREF.SIZE, DEF.SIZE);
      zoomIntensity = sharedPrefs.getInt(PREF.ZOOM, DEF.ZOOM);

      if (!wallpaper.equals(wallpaperNew)) {
        wallpaper = wallpaperNew;
        variant = variantNew;
        refreshTheme();
        colorsHaveChanged();
      } else if (isNight != isNightMode()) {
        isNight = isNightMode();
        refreshTheme();
        colorsHaveChanged();
      } else if (!variant.equals(variantNew)) {
        variant = variantNew;
        refreshTheme();
        colorsHaveChanged();
      }

      newRandomZ();
      drawFrame(true);
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
        this.xOffset = xOffset;
        drawFrame(false);
      }
    }

    @Override
    public void onZoomChanged(float zoom) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && zoomIntensity > 0) {
        this.zoom = zoom;
        drawFrame(false);
      }
    }

    void drawFrame(boolean force) {
      if (!force && SystemClock.elapsedRealtime() - lastDraw < 1000 / fps) {
        return;
      }

      final SurfaceHolder surfaceHolder = getSurfaceHolder();
      Canvas canvas = null;
      try {
        canvas = surfaceHolder.lockCanvas();

        if (canvas != null) {
          canvas.drawColor(colorBackground);

          switch (wallpaper) {
            case WALLPAPER.DOODLE:
              drawShape(doodleArc, 0.25, 0.28, zDoodleArc);
              drawShape(doodleDot, 0.142, 0.468, zDoodleDot);
              drawShape(doodleU, 0.25, 0.72, zDoodleU);
              drawShape(doodleRect, 0.7, 0.8, zDoodleRect);
              drawShape(doodleRing, 0.66, 0.5, zDoodleRing);
              drawShape(doodleMoon, 0.75, 0.56, zDoodleMoon);
              drawShape(doodlePoly, 0.7, 0.2, zDoodlePoly);
              drawOnCanvas(
                  canvas,
                  doodleArc, doodleDot, doodleU, doodleRect,
                  doodleRing, doodleMoon, doodlePoly
              );
              break;
            case WALLPAPER.NEON:
              drawShape(neonKidneyFront, 0.85, 0.65, zNeonKidneyFront);
              drawShape(neonCircleFront, 0.98, 0.468, zNeonCircleFront);
              drawShape(neonPill, 0.26, 0.58, zNeonPill);
              drawShape(neonLine, 0.55, 0.4, zNeonLine);
              drawShape(neonKidneyBack, 0.63, 0.37, zNeonKidneyBack);
              drawShape(neonCircleBack, 0.5, 0.63, zNeonCircleBack);
              drawShape(neonDot, 0.6, 0.15, zNeonDot);
              drawOnCanvas(
                  canvas,
                  neonDot, neonCircleBack, neonKidneyBack, neonLine,
                  neonPill, neonCircleFront, neonKidneyFront
              );
              break;
            case WALLPAPER.GEOMETRIC:
              drawShape(geometricRect, 0.35, 0.78, zGeometricRect);
              drawShape(geometricLine, 0.5, 0.82, zGeometricLine);
              drawShape(geometricPoly, 0.8, 0.67, zGeometricPoly);
              drawShape(geometricCircle, 0.6, 0.2, zGeometricCircle);
              drawShape(geometricSheet, 0.4, 0.21, zGeometricSheet, 1.3);
              drawOnCanvas(
                  canvas,
                  geometricSheet, geometricCircle, geometricPoly,
                  geometricLine, geometricRect
              );
              break;
          }

          lastDraw = SystemClock.elapsedRealtime();
        }
      } finally {
        if (canvas != null) {
          surfaceHolder.unlockCanvasAndPost(canvas);
        }
      }
    }

    private void drawOnCanvas(Canvas canvas, Drawable... drawables) {
      for (Drawable drawable : drawables) {
        if (drawable != null) {
          drawable.draw(canvas);
        }
      }
    }

    private void drawShape(Drawable drawable, double x, double y, double z) {
      drawShape(drawable, x, y, z, 1);
    }

    private void drawShape(Drawable drawable, double x, double y, double z, double scale) {
      scale *= size;

      float intensity;
      switch (zoomIntensity) {
        case 1:
          intensity = 0.3f;
          break;
        case 2:
          intensity = 0.5f;
          break;
        default:
          intensity = 0;
          break;
      }

      scale = scale - (zoom * z * intensity);
      int width = (int) (scale * drawable.getIntrinsicWidth());
      int height = (int) (scale * drawable.getIntrinsicHeight());

      int xPos, yPos, offset;
      Rect frame = getSurfaceHolder().getSurfaceFrame();
      offset = (int) (xOffset * z * parallax);
      xPos = ((int) (x * frame.width())) - offset;
      yPos = (int) (y * frame.height());

      drawable.setBounds(
          xPos - width / 2,
          yPos - height / 2,
          xPos + width / 2,
          yPos + height / 2
      );
    }

    private void colorsHaveChanged() {
      if (VERSION.SDK_INT >= VERSION_CODES.O_MR1) {
        notifyColorsChanged();
        new Handler(Looper.getMainLooper()).postDelayed(this::notifyColorsChanged, 300);
      }
    }
  }
}
