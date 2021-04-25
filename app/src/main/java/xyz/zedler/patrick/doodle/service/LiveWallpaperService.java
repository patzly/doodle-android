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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import java.util.Random;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.THEME;
import xyz.zedler.patrick.doodle.Constants.VARIANT;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.PrefsUtil;

public class LiveWallpaperService extends WallpaperService {

  private final static String TAG = LiveWallpaperService.class.getSimpleName();

  private SharedPreferences sharedPrefs;
  private Resources.Theme themeRes;
  private String theme, variant;
  private boolean nightMode, followSystem, isNight;
  private int colorBackground, parallax, size;

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
    theme = sharedPrefs.getString(Constants.PREF.THEME, Constants.THEME.DOODLE);
    variant = sharedPrefs.getString(Constants.PREF.VARIANT, Constants.VARIANT.BLACK);
    nightMode = sharedPrefs.getBoolean(Constants.PREF.NIGHT_MODE, true);
    followSystem = sharedPrefs.getBoolean(Constants.PREF.FOLLOW_SYSTEM, true);
    isNight = isNightMode();
    parallax = sharedPrefs.getInt(Constants.PREF.PARALLAX, 100);
    size = sharedPrefs.getInt(Constants.PREF.SIZE, 0);
    themeRes = getResources().newTheme();
    newRandomZ();
    refreshTheme();
    return new CustomEngine();
  }

  private VectorDrawableCompat getVectorDrawable(@DrawableRes int resId, Resources.Theme theme) {
    return VectorDrawableCompat.create(getApplicationContext().getResources(), resId, theme);
  }

  private void refreshTheme() {
    if (isNightMode()) {
      switch (theme) {
        case Constants.THEME.DOODLE:
          themeRes.applyStyle(R.style.Wallpaper_Doodle_Night, true);
          colorBackground = getCompatColor(R.color.wp_bg_doodle_night);
          break;
        case Constants.THEME.NEON:
          themeRes.applyStyle(R.style.Wallpaper_Neon_Night, true);
          colorBackground = getCompatColor(R.color.wp_bg_neon_night);
          break;
        case Constants.THEME.GEOMETRIC:
          themeRes.applyStyle(R.style.Wallpaper_Geometric_Night, true);
          colorBackground = getCompatColor(R.color.wp_bg_geometric_night);
          break;
      }
    } else {
      switch (theme) {
        case Constants.THEME.DOODLE:
          switch (variant) {
            case Constants.VARIANT.BLACK:
              themeRes.applyStyle(R.style.Wallpaper_Doodle_Black, true);
              colorBackground = getCompatColor(R.color.wp_bg_doodle_black);
              break;
            case Constants.VARIANT.WHITE:
              themeRes.applyStyle(R.style.Wallpaper_Doodle_White, true);
              colorBackground = getCompatColor(R.color.wp_bg_doodle_white);
              break;
            case Constants.VARIANT.ORANGE:
              themeRes.applyStyle(R.style.Wallpaper_Doodle_Orange, true);
              colorBackground = getCompatColor(R.color.wp_bg_doodle_orange);
              break;
          }
          break;
        case Constants.THEME.NEON:
          themeRes.applyStyle(R.style.Wallpaper_Neon, true);
          colorBackground = getCompatColor(R.color.wp_bg_neon);
          break;
        case Constants.THEME.GEOMETRIC:
          themeRes.applyStyle(R.style.Wallpaper_Geometric, true);
          colorBackground = getCompatColor(R.color.wp_bg_geometric);
          break;
      }
    }

    switch (theme) {
      case Constants.THEME.DOODLE:
        doodleArc = getVectorDrawable(R.drawable.doodle_shape_arc, themeRes);
        doodleDot = getVectorDrawable(R.drawable.doodle_shape_dot, themeRes);
        doodleU = getVectorDrawable(R.drawable.doodle_shape_u, themeRes);
        doodleRect = getVectorDrawable(R.drawable.doodle_shape_rect, themeRes);
        doodleRing = getVectorDrawable(R.drawable.doodle_shape_ring, themeRes);
        doodleMoon = getVectorDrawable(R.drawable.doodle_shape_moon, themeRes);
        doodlePoly = getVectorDrawable(R.drawable.doodle_shape_poly, themeRes);
        break;
      case Constants.THEME.NEON:
        neonKidneyFront = getVectorDrawable(R.drawable.neon_shape_kidney_front, null);
        neonCircleFront = getVectorDrawable(R.drawable.neon_shape_circle_front, themeRes);
        neonPill = getVectorDrawable(R.drawable.neon_shape_pill, themeRes);
        neonLine = getVectorDrawable(R.drawable.neon_shape_line, themeRes);
        neonKidneyBack = getVectorDrawable(R.drawable.neon_shape_kidney_back, null);
        neonCircleBack = getVectorDrawable(R.drawable.neon_shape_circle_back, themeRes);
        neonDot = getVectorDrawable(R.drawable.neon_shape_dot, themeRes);
        break;
      case Constants.THEME.GEOMETRIC:
        geometricRect = getVectorDrawable(R.drawable.geometric_shape_rect, themeRes);
        geometricLine = getVectorDrawable(R.drawable.geometric_shape_line, themeRes);
        geometricPoly = getVectorDrawable(R.drawable.geometric_shape_poly, themeRes);
        geometricCircle = getVectorDrawable(R.drawable.geometric_shape_circle, themeRes);
        geometricSheet = getVectorDrawable(R.drawable.geometric_shape_sheet, themeRes);
        break;
    }
  }

  private void newRandomZ() {
    Random random = new Random();
    switch (theme) {
      case Constants.THEME.DOODLE:
        zDoodleArc = random.nextFloat();
        zDoodleDot = random.nextFloat();
        zDoodleU = random.nextFloat();
        zDoodleRect = random.nextFloat();
        zDoodleRing = random.nextFloat();
        zDoodleMoon = random.nextFloat();
        zDoodlePoly = random.nextFloat();
        break;
      case Constants.THEME.NEON:
        zNeonKidneyFront = random.nextFloat();
        zNeonCircleFront = random.nextFloat();
        zNeonPill = random.nextFloat();
        zNeonLine = random.nextFloat();
        zNeonKidneyBack = random.nextFloat();
        zNeonCircleBack = random.nextFloat();
        zNeonDot = random.nextFloat();
        break;
      case Constants.THEME.GEOMETRIC:
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

  class CustomEngine extends Engine {

    private final Paint paint;
    private float xOffset = 0;

    CustomEngine() {
      paint = new Paint();
    }

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      setTouchEventsEnabled(false);

      paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public WallpaperColors onComputeColors() {
      int background = 0xFF232323;
      if (isNightMode()) {
        switch (theme) {
          case THEME.DOODLE:
            background = 0xFF272628;
            break;
          case THEME.NEON:
            background = 0xFF0e032d;
            break;
          case THEME.GEOMETRIC:
            background = 0xFF212121;
            break;
        }
      } else {
        switch (theme) {
          case THEME.DOODLE:
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
          case THEME.NEON:
            background = 0xFFcbcbef;
            break;
          case THEME.GEOMETRIC:
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

      String themeNew = sharedPrefs.getString(Constants.PREF.THEME, Constants.THEME.DOODLE);
      String variantNew = sharedPrefs.getString(
          Constants.PREF.VARIANT, Constants.VARIANT.BLACK
      );
      nightMode = sharedPrefs.getBoolean(Constants.PREF.NIGHT_MODE, true);
      followSystem = sharedPrefs.getBoolean(Constants.PREF.FOLLOW_SYSTEM, true);
      parallax = sharedPrefs.getInt(Constants.PREF.PARALLAX, 100);
      size = sharedPrefs.getInt(Constants.PREF.SIZE, 0);

      if (!theme.equals(themeNew)) {
        theme = themeNew;
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
      drawFrame(xOffset);
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
      super.onOffsetsChanged(xOffset, yOffset, xStep, yStep, xPixels, yPixels);
      if (parallax != 0) {
        drawFrame(xOffset);
      }
      this.xOffset = xOffset;
    }

    void drawFrame(float xOffset) {
      final SurfaceHolder surfaceHolder = getSurfaceHolder();
      final Rect frame = surfaceHolder.getSurfaceFrame();
      Canvas canvas = null;
      try {
        canvas = surfaceHolder.lockCanvas();

        if (canvas != null) {
          paint.setColor(colorBackground);
          canvas.drawRect(0, 0, frame.width(), frame.height(), paint);

          switch (theme) {
            case Constants.THEME.DOODLE:
              drawShape(doodleArc, 0.25, 0.28, zDoodleArc, xOffset);
              drawShape(doodleDot, 0.142, 0.468, zDoodleDot, xOffset);
              drawShape(doodleU, 0.25, 0.72, zDoodleU, xOffset);
              drawShape(doodleRect, 0.7, 0.8, zDoodleRect, xOffset);
              drawShape(doodleRing, 0.66, 0.5, zDoodleRing, xOffset);
              drawShape(doodleMoon, 0.75, 0.56, zDoodleMoon, xOffset);
              drawShape(doodlePoly, 0.7, 0.2, zDoodlePoly, xOffset);
              drawOnCanvas(
                  canvas,
                  doodleArc, doodleDot, doodleU, doodleRect,
                  doodleRing, doodleMoon, doodlePoly
              );
              break;
            case Constants.THEME.NEON:
              drawShape(neonKidneyFront, 0.85, 0.65, zNeonKidneyFront, xOffset);
              drawShape(neonCircleFront, 0.98, 0.468, zNeonCircleFront, xOffset);
              drawShape(neonPill, 0.26, 0.58, zNeonPill, xOffset);
              drawShape(neonLine, 0.55, 0.4, zNeonLine, xOffset);
              drawShape(neonKidneyBack, 0.63, 0.37, zNeonKidneyBack, xOffset);
              drawShape(neonCircleBack, 0.5, 0.63, zNeonCircleBack, xOffset);
              drawShape(neonDot, 0.6, 0.15, zNeonDot, xOffset);
              drawOnCanvas(
                  canvas,
                  neonDot, neonCircleBack, neonKidneyBack, neonLine,
                  neonPill, neonCircleFront, neonKidneyFront
              );
              break;
            case Constants.THEME.GEOMETRIC:
              drawShape(geometricRect, 0.35, 0.78, zGeometricRect, xOffset);
              drawShape(geometricLine, 0.5, 0.82, zGeometricLine, xOffset);
              drawShape(geometricPoly, 0.8, 0.67, zGeometricPoly, xOffset);
              drawShape(geometricCircle, 0.6, 0.2, zGeometricCircle, xOffset);
              drawShape(geometricSheet, 0.4, 0.21, zGeometricSheet, xOffset);
              drawOnCanvas(
                  canvas,
                  geometricSheet, geometricCircle, geometricPoly,
                  geometricLine, geometricRect
              );
              break;
          }
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

    private void drawShape(
        Drawable drawable,
        double x,
        double y,
        double z,
        double xOffset
    ) {
      int xPos, yPos, offset;
      Rect frame = getSurfaceHolder().getSurfaceFrame();
      offset = (int) (xOffset * z * parallax);
      xPos = ((int) (x * frame.width()) - drawable.getIntrinsicWidth() / 2) - offset;
      yPos = (int) (y * frame.height()) - drawable.getIntrinsicHeight() / 2;
      double scale;
      switch (size) {
        case 1:
          scale = 1.1;
          break;
        case 2:
          scale = 1.2;
          break;
        default:
          scale = 1;
      }
      drawable.setBounds(
          xPos, yPos,
          (int) (scale * drawable.getIntrinsicWidth()) + xPos,
          (int) (scale * drawable.getIntrinsicHeight()) + yPos
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
