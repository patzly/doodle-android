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

package com.plattysoft.leonids.initializers;

import com.plattysoft.leonids.Particle;
import java.util.Random;

public class SpeedModuleAndRangeInitializer implements ParticleInitializer {

  private final float mSpeedMin;
  private final float mSpeedMax;
  private int mMinAngle;
  private int mMaxAngle;

  public SpeedModuleAndRangeInitializer(float speedMin, float speedMax, int minAngle,
      int maxAngle) {
    mSpeedMin = speedMin;
    mSpeedMax = speedMax;
    mMinAngle = minAngle;
    mMaxAngle = maxAngle;
    // Make sure the angles are in the [0-360) range
    while (mMinAngle < 0) {
      mMinAngle += 360;
    }
    while (mMaxAngle < 0) {
      mMaxAngle += 360;
    }
    // Also make sure that mMinAngle is the smaller
    if (mMinAngle > mMaxAngle) {
      int tmp = mMinAngle;
      mMinAngle = mMaxAngle;
      mMaxAngle = tmp;
    }
  }

  @Override
  public void initParticle(Particle p, Random r) {
    float speed = r.nextFloat() * (mSpeedMax - mSpeedMin) + mSpeedMin;
    int angle;
    if (mMaxAngle == mMinAngle) {
      angle = mMinAngle;
    } else {
      angle = r.nextInt(mMaxAngle - mMinAngle) + mMinAngle;
    }
    double angleInRads = Math.toRadians(angle);
    p.mSpeedX = (float) (speed * Math.cos(angleInRads));
    p.mSpeedY = (float) (speed * Math.sin(angleInRads));
    p.mInitialRotation = angle + 90;
  }
}
