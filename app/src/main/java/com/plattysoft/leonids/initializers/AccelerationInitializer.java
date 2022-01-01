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

package com.plattysoft.leonids.initializers;

import com.plattysoft.leonids.Particle;
import java.util.Random;

public class AccelerationInitializer implements ParticleInitializer {

  private final float mMinValue;
  private final float mMaxValue;
  private final int mMinAngle;
  private final int mMaxAngle;

  public AccelerationInitializer(float minAcceleration, float maxAcceleration, int minAngle,
      int maxAngle) {
    mMinValue = minAcceleration;
    mMaxValue = maxAcceleration;
    mMinAngle = minAngle;
    mMaxAngle = maxAngle;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    float angle = mMinAngle;
    if (mMaxAngle != mMinAngle) {
      angle = r.nextInt(mMaxAngle - mMinAngle) + mMinAngle;
    }
    float angleInRads = (float) (angle * Math.PI / 180f);
    float value = r.nextFloat() * (mMaxValue - mMinValue) + mMinValue;
    p.mAccelerationX = (float) (value * Math.cos(angleInRads));
    p.mAccelerationY = (float) (value * Math.sin(angleInRads));
  }

}
