package xyz.zedler.patrick.doodle.service;

import android.app.WallpaperColors;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.DrawableRes;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.Random;

import xyz.zedler.patrick.doodle.R;

public class LiveWallpaperService extends WallpaperService {

	private final static boolean DEBUG = false;
	private final static String TAG = "LiveWallpaperService";

	private SharedPreferences sharedPrefs;
	private Resources.Theme themeRes;
	private String theme, variant;
	private boolean nightMode, followSystem, isNight, isSizeBig;
	private int colorBackground, parallax;

	private VectorDrawableCompat doodleArc, doodleDot, doodleU, doodleRect, doodleRing, doodleMoon, doodlePoly;
	private VectorDrawableCompat neonKidneyFront, neonCircleFront, neonPill, neonLine, neonKidneyBack, neonCircleBack, neonDot;
	private VectorDrawableCompat geometricRect, geometricLine, geometricPoly, geometricCircle, geometricSheet;

	private float zDoodleArc, zDoodleDot, zDoodleU, zDoodleRect, zDoodleRing, zDoodleMoon, zDoodlePoly;
	private float zNeonKidneyFront, zNeonCircleFront, zNeonPill, zNeonLine, zNeonKidneyBack, zNeonCircleBack, zNeonDot;
	private float zGeometricRect, zGeometricLine, zGeometricPoly, zGeometricCircle, zGeometricSheet;

	@Override
	public Engine onCreateEngine() {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		theme = sharedPrefs.getString("theme", "doodle");
		variant = sharedPrefs.getString("variant", "black");
		nightMode = sharedPrefs.getBoolean("night_mode", true);
		followSystem = sharedPrefs.getBoolean("follow_system", true);
		isNight = isNightMode();
		parallax = sharedPrefs.getInt("parallax", 100);
		isSizeBig = sharedPrefs.getBoolean("size_big", false);
		themeRes = getResources().newTheme();
		newRandomZ();
		refreshTheme();
		return new CustomEngine();
	}

	class CustomEngine extends Engine  {
		private float xOffset = 0;
		private final Paint paint = new Paint();

		CustomEngine() {}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(false);
		}

		@Override
		public WallpaperColors onComputeColors() {
			int background = 0xFF232323;
			if(isNightMode()) {
				switch (theme) {
					case "doodle":
						background = 0xFF272628;
						break;
					case "nature":
						background = 0xFF32373a;
						break;
					case "neon":
						background = 0xFF0e032d;
						break;
					case "geometric":
						background = 0xFF212121;
						break;
				}
			} else {
				switch (theme) {
					case "doodle":
						switch (variant) {
							case "black":
								background = 0xFF232323;
								break;
							case "white":
								background = 0xFFdbd7ce;
								break;
							case "orange":
								background = 0xFFf98a6c;
								break;
						}
						break;
					case "nature":
						background = 0xFFfcf4e9;
						break;
					case "neon":
						background = 0xFFcbcbef;
						break;
					case "geometric":
						background = 0xFFb9c1c7;
						break;
				}
			}
			return WallpaperColors.fromDrawable(new ColorDrawable(background));
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			if(visible) {
				if(sharedPrefs == null) {
					sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				}
				theme = sharedPrefs.getString("theme", "doodle");
				variant = sharedPrefs.getString("variant", "black");
				nightMode = sharedPrefs.getBoolean("night_mode", true);
				followSystem = sharedPrefs.getBoolean("follow_system", true);
				parallax = sharedPrefs.getInt("parallax", 100);
				isSizeBig = sharedPrefs.getBoolean("size_big", false);

				if(!theme.equals(sharedPrefs.getString("theme", "doodle"))) {
					theme = sharedPrefs.getString("theme", "doodle");
					refreshTheme();
					notifyColorsChanged();
				} else if(isNight != isNightMode()){
					isNight = isNightMode();
					refreshTheme();
					notifyColorsChanged();
				} else if(!variant.equals(sharedPrefs.getString("variant", "black"))) {
					variant = sharedPrefs.getString("variant", "black");
					refreshTheme();
					notifyColorsChanged();
				} else if(sharedPrefs.getBoolean("should_refresh", true)) {
					sharedPrefs.edit().putBoolean("should_refresh", false).apply();
					refreshTheme();
					notifyColorsChanged();
				}

				newRandomZ();
				drawFrame(xOffset);

				if(DEBUG) Log.i(TAG, "onVisibilityChanged: method call");
			}
		}

		@Override
		public void notifyColorsChanged() {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) super.notifyColorsChanged();
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
			super.onOffsetsChanged(xOffset, yOffset,xStep, yStep, xPixels, yPixels);
			if(parallax != 0) drawFrame(xOffset);
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
					paint.setStyle(Paint.Style.FILL);
					canvas.drawRect(0, 0, frame.width(), frame.height(), paint);
					switch (theme) {
						case "doodle":
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
						case "neon":
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
						case "geometric":
							drawShape(geometricRect, 0.35, 0.78, zGeometricRect, xOffset);
							drawShape(geometricLine, 0.5, 0.82, zGeometricLine, xOffset);
							drawShape(geometricPoly, 0.8, 0.67, zGeometricPoly, xOffset);
							drawShape(geometricCircle, 0.6, 0.2, zGeometricCircle, xOffset);
							drawShape(geometricSheet, 0.4, 0.25, zGeometricSheet, xOffset);
							drawOnCanvas(
									canvas,
									geometricSheet, geometricCircle, geometricPoly,
									geometricLine, geometricRect
							);
							break;
					}
				}
			} finally {
				if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}

		private void drawOnCanvas(Canvas canvas, VectorDrawableCompat... vectors) {
			for (VectorDrawableCompat vector : vectors) {
				if(vector != null) vector.draw(canvas);
			}
		}

		private void drawShape(VectorDrawableCompat vdc, double x, double y, double z, double xOffset) {
			int xPos, yPos, offset;
			Rect frame = getSurfaceHolder().getSurfaceFrame();
			offset = (int) (xOffset * z * parallax);
			xPos = ((int) (x * frame.width()) - vdc.getIntrinsicWidth() / 2) - offset;
			yPos = (int) (y * frame.height()) - vdc.getIntrinsicHeight() / 2;
			double scale = isSizeBig ? 1.2 : 1;
			vdc.setBounds(
					xPos, yPos,
					(int) (scale * vdc.getIntrinsicWidth()) + xPos,
					(int) (scale * vdc.getIntrinsicHeight()) + yPos
			);
		}
	}

	private VectorDrawableCompat getVectorDrawable(@DrawableRes int resId, Resources.Theme theme) {
		return VectorDrawableCompat.create(getApplicationContext().getResources(), resId, theme);
	}

	private void refreshTheme() {
		if(isNightMode()) {
			switch (theme) {
				case "doodle":
					themeRes.applyStyle(R.style.Wallpaper_Doodle_Night, true);
					colorBackground = getResources().getColor(R.color.wp_bg_doodle_night);
					break;
				case "nature":
					//themeRes.applyStyle(R.style.Wallpaper_Doodle_Night, true);
					colorBackground = getResources().getColor(R.color.wp_bg_nature_night);
					break;
				case "neon":
					themeRes.applyStyle(R.style.Wallpaper_Neon_Night, true);
					colorBackground = getResources().getColor(R.color.wp_bg_neon_night);
					break;
				case "geometric":
					themeRes.applyStyle(R.style.Wallpaper_Geometric_Night, true);
					colorBackground = getResources().getColor(R.color.wp_bg_geometric_night);
					break;
			}
		} else {
			switch (theme) {
				case "doodle":
					switch (variant) {
						case "black":
							themeRes.applyStyle(R.style.Wallpaper_Doodle_Black, true);
							colorBackground = getResources().getColor(R.color.wp_bg_doodle_black);
							break;
						case "white":
							themeRes.applyStyle(R.style.Wallpaper_Doodle_White, true);
							colorBackground = getResources().getColor(R.color.wp_bg_doodle_white);
							break;
						case "orange":
							themeRes.applyStyle(R.style.Wallpaper_Doodle_Orange, true);
							colorBackground = getResources().getColor(R.color.wp_bg_doodle_orange);
							break;
					}
					break;
				case "nature":
					//themeRes.applyStyle(R.style.Wallpaper_Doodle_White, true);
					colorBackground = getResources().getColor(R.color.wp_bg_nature);
					break;
				case "neon":
					themeRes.applyStyle(R.style.Wallpaper_Neon, true);
					colorBackground = getResources().getColor(R.color.wp_bg_neon);
					break;
				case "geometric":
					themeRes.applyStyle(R.style.Wallpaper_Geometric, true);
					colorBackground = getResources().getColor(R.color.wp_bg_geometric);
					break;
			}
		}

		switch (theme) {
			case "doodle":
				doodleArc = getVectorDrawable(R.drawable.doodle_shape_arc, themeRes);
				doodleDot = getVectorDrawable(R.drawable.doodle_shape_dot, themeRes);
				doodleU = getVectorDrawable(R.drawable.doodle_shape_u, themeRes);
				doodleRect = getVectorDrawable(R.drawable.doodle_shape_rect, themeRes);
				doodleRing = getVectorDrawable(R.drawable.doodle_shape_ring, themeRes);
				doodleMoon = getVectorDrawable(R.drawable.doodle_shape_moon, themeRes);
				doodlePoly = getVectorDrawable(R.drawable.doodle_shape_poly, themeRes);
				break;
			case "nature":
				break;
			case "neon":
				neonKidneyFront = getVectorDrawable(R.drawable.neon_shape_kidney_front, null);
				neonCircleFront = getVectorDrawable(R.drawable.neon_shape_circle_front, themeRes);
				neonPill = getVectorDrawable(R.drawable.neon_shape_pill, themeRes);
				neonLine = getVectorDrawable(R.drawable.neon_shape_line, themeRes);
				neonKidneyBack = getVectorDrawable(R.drawable.neon_shape_kidney_back, null);
				neonCircleBack = getVectorDrawable(R.drawable.neon_shape_circle_back, themeRes);
				neonDot = getVectorDrawable(R.drawable.neon_shape_dot, themeRes);
				break;
			case "geometric":
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
			case "doodle":
				zDoodleArc = random.nextFloat();
				zDoodleDot = random.nextFloat();
				zDoodleU = random.nextFloat();
				zDoodleRect = random.nextFloat();
				zDoodleRing = random.nextFloat();
				zDoodleMoon = random.nextFloat();
				zDoodlePoly = random.nextFloat();
				break;
			case "nature":
				break;
			case "neon":
				zNeonKidneyFront = random.nextFloat();
				zNeonCircleFront = random.nextFloat();
				zNeonPill = random.nextFloat();
				zNeonLine = random.nextFloat();
				zNeonKidneyBack = random.nextFloat();
				zNeonCircleBack = random.nextFloat();
				zNeonDot = random.nextFloat();
				break;
			case "geometric":
				zGeometricRect = random.nextFloat();
				zGeometricLine = random.nextFloat();
				zGeometricPoly = random.nextFloat();
				zGeometricCircle = random.nextFloat();
				zGeometricSheet = random.nextFloat();
				break;
		}
	}

	private boolean isNightMode() {
		if(nightMode && !followSystem) return true;
		return nightMode && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
	}
}
