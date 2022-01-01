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

package xyz.zedler.patrick.doodle.util;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.AttrRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.plattysoft.leonids.ParticleSystem;
import xyz.zedler.patrick.doodle.R;

public class ConfettiUtil {

  private final static float speedMin = 0.1f;
  private final static float speedMax = 0.45f;
  private final static float acceleration = 0.0001f;

  private final static int particlesMax = 50;
  private final static int particlesPerSecond = 100;
  private final static int emittingDuration = 500;
  private final static int livingDuration = 4500;
  private final static int fadeOutDuration = 1500;

  private final static int minAngle = 190;
  private final static int maxAngle = 350;

  public static void explode(Activity activity, View emitter) {
    ParticleSystem psRedLeft = getParticleSystem(activity, R.attr.colorPrimary);
    psRedLeft.setSpeedModuleAndAngleRange(
        speedMin, speedMax, minAngle, 270
    );
    psRedLeft.emit(emitter, particlesPerSecond, emittingDuration);

    ParticleSystem psRedRight = getParticleSystem(activity, R.attr.colorPrimary);
    psRedRight.setSpeedModuleAndAngleRange(
        speedMin, speedMax, 270, maxAngle
    );
    psRedRight.emit(emitter, particlesPerSecond, emittingDuration);

    ParticleSystem psYellowLeft = getParticleSystem(activity, R.attr.colorTertiary);
    psYellowLeft.setSpeedModuleAndAngleRange(
        speedMin, speedMax, minAngle, 270
    );
    psYellowLeft.emit(emitter, particlesPerSecond, emittingDuration);

    ParticleSystem psYellowRight = getParticleSystem(activity, R.attr.colorTertiary);
    psYellowRight.setSpeedModuleAndAngleRange(
        speedMin, speedMax, 270, maxAngle
    );
    psYellowRight.emit(emitter, particlesPerSecond, emittingDuration);
  }

  private static ParticleSystem getParticleSystem(Activity activity, @AttrRes int resId) {
    Drawable drawable = ResourcesCompat.getDrawable(
        activity.getResources(),
        R.drawable.shape_pill,
        null
    );
    assert drawable != null;
    DrawableCompat.setTint(drawable, ResUtil.getColorAttr(activity, resId));
    ParticleSystem ps = new ParticleSystem(
        activity,
        particlesMax,
        drawable,
        livingDuration
    );
    ps.setInitialRotationRange(0, 360);
    ps.setRotationSpeedRange(90, 180);
    ps.setAcceleration(acceleration, 90);
    ps.setFadeOut(fadeOutDuration, new DecelerateInterpolator());
    return ps;
  }
}
