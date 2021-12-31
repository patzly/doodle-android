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

package com.plattysoft.leonids;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;

public class AnimatedParticle extends Particle {

  private final AnimationDrawable mAnimationDrawable;
  private int mTotalTime;

  public AnimatedParticle(AnimationDrawable animationDrawable) {
    mAnimationDrawable = animationDrawable;
    mImage = ((BitmapDrawable) mAnimationDrawable.getFrame(0)).getBitmap();
    // If it is a repeating animation, calculate the time
    mTotalTime = 0;
    for (int i = 0; i < mAnimationDrawable.getNumberOfFrames(); i++) {
      mTotalTime += mAnimationDrawable.getDuration(i);
    }
  }

  @Override
  public boolean update(long milliseconds) {
    boolean active = super.update(milliseconds);
    if (active) {
      long animationElapsedTime = 0;
      long realMilliseconds = milliseconds - mStartingMillisecond;
      if (realMilliseconds > mTotalTime) {
        if (mAnimationDrawable.isOneShot()) {
          return false;
        } else {
          realMilliseconds = realMilliseconds % mTotalTime;
        }
      }
      for (int i = 0; i < mAnimationDrawable.getNumberOfFrames(); i++) {
        animationElapsedTime += mAnimationDrawable.getDuration(i);
        if (animationElapsedTime > realMilliseconds) {
          mImage = ((BitmapDrawable) mAnimationDrawable.getFrame(i)).getBitmap();
          break;
        }
      }
    }
    return active;
  }
}
