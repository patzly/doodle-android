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

public class SpeedByComponentsInitializer implements ParticleInitializer {

  private final float mMinSpeedX;
  private final float mMaxSpeedX;
  private final float mMinSpeedY;
  private final float mMaxSpeedY;

  public SpeedByComponentsInitializer(float speedMinX, float speedMaxX, float speedMinY,
      float speedMaxY) {
    mMinSpeedX = speedMinX;
    mMaxSpeedX = speedMaxX;
    mMinSpeedY = speedMinY;
    mMaxSpeedY = speedMaxY;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    p.mSpeedX = r.nextFloat() * (mMaxSpeedX - mMinSpeedX) + mMinSpeedX;
    p.mSpeedY = r.nextFloat() * (mMaxSpeedY - mMinSpeedY) + mMinSpeedY;
  }
}
