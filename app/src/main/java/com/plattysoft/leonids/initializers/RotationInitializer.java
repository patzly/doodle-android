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

public class RotationInitializer implements ParticleInitializer {

  private final int mMinAngle;
  private final int mMaxAngle;

  public RotationInitializer(int minAngle, int maxAngle) {
    mMinAngle = minAngle;
    mMaxAngle = maxAngle;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    p.mInitialRotation = (mMinAngle == mMaxAngle)
        ? mMinAngle
        : r.nextInt(mMaxAngle - mMinAngle) + mMinAngle;
  }
}
