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

package com.plattysoft.leonids.modifiers;

import com.plattysoft.leonids.Particle;

public class AccelerationModifier implements ParticleModifier {

  private final float mVelocityX;
  private final float mVelocityY;

  public AccelerationModifier(float velocity, float angle) {
    float velocityAngleInRads = (float) (angle * Math.PI / 180f);
    mVelocityX = (float) (velocity * Math.cos(velocityAngleInRads));
    mVelocityY = (float) (velocity * Math.sin(velocityAngleInRads));
  }

  @Override
  public void apply(Particle particle, long milliseconds) {
    particle.mCurrentX += mVelocityX * milliseconds * milliseconds;
    particle.mCurrentY += mVelocityY * milliseconds * milliseconds;
  }
}
