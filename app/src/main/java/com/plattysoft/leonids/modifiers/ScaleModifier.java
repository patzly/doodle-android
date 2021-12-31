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
 * Copyright (c) 2019-2021 by Patrick Zedler
 */

package com.plattysoft.leonids.modifiers;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.plattysoft.leonids.Particle;

public class ScaleModifier implements ParticleModifier {

  private final float mInitialValue;
  private final float mFinalValue;
  private final long mEndTime;
  private final long mStartTime;
  private final long mDuration;
  private final float mValueIncrement;
  private final Interpolator mInterpolator;

  public ScaleModifier(float initialValue, float finalValue, long startMillis, long endMillis,
      Interpolator interpolator) {
    mInitialValue = initialValue;
    mFinalValue = finalValue;
    mStartTime = startMillis;
    mEndTime = endMillis;
    mDuration = mEndTime - mStartTime;
    mValueIncrement = mFinalValue - mInitialValue;
    mInterpolator = interpolator;
  }

  public ScaleModifier(float initialValue, float finalValue, long startMillis, long endMillis) {
    this(initialValue, finalValue, startMillis, endMillis, new LinearInterpolator());
  }

  @Override
  public void apply(Particle particle, long milliseconds) {
    if (milliseconds < mStartTime) {
      particle.mScale = mInitialValue;
    } else if (milliseconds > mEndTime) {
      particle.mScale = mFinalValue;
    } else {
      float interpolatedValue = mInterpolator
          .getInterpolation((milliseconds - mStartTime) * 1f / mDuration);
      particle.mScale = mInitialValue + mValueIncrement * interpolatedValue;
    }
  }

}
