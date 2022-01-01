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

package com.plattysoft.leonids;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.plattysoft.leonids.modifiers.ParticleModifier;
import java.util.List;

public class Particle {

  protected Bitmap mImage;

  public float mCurrentX;
  public float mCurrentY;

  public float mScale = 1f;
  public int mAlpha = 255;

  public float mInitialRotation = 0f;

  public float mRotationSpeed = 0f;

  public float mSpeedX = 0f;
  public float mSpeedY = 0f;

  public float mAccelerationX;
  public float mAccelerationY;

  private final Matrix mMatrix;
  private final Paint mPaint;

  private float mInitialX;
  private float mInitialY;

  private float mRotation;

  private long mTimeToLive;

  protected long mStartingMillisecond;

  private int mBitmapHalfWidth;
  private int mBitmapHalfHeight;

  private List<ParticleModifier> mModifiers;


  protected Particle() {
    mMatrix = new Matrix();
    mPaint = new Paint();
  }

  public Particle(Bitmap bitmap) {
    this();
    mImage = bitmap;
  }

  public void init() {
    mScale = 1;
    mAlpha = 255;
  }

  public void configure(long timeToLive, float emitterX, float emitterY) {
    mBitmapHalfWidth = mImage.getWidth() / 2;
    mBitmapHalfHeight = mImage.getHeight() / 2;

    mInitialX = emitterX - mBitmapHalfWidth;
    mInitialY = emitterY - mBitmapHalfHeight;
    mCurrentX = mInitialX;
    mCurrentY = mInitialY;

    mTimeToLive = timeToLive;
  }

  public boolean update(long milliseconds) {
    long realMilliseconds = milliseconds - mStartingMillisecond;
    if (realMilliseconds > mTimeToLive) {
      return false;
    }
    mCurrentX = mInitialX + mSpeedX * realMilliseconds
        + mAccelerationX * realMilliseconds * realMilliseconds;
    mCurrentY = mInitialY + mSpeedY * realMilliseconds
        + mAccelerationY * realMilliseconds * realMilliseconds;
    mRotation = mInitialRotation + mRotationSpeed * realMilliseconds / 1000;
    for (int i = 0; i < mModifiers.size(); i++) {
      mModifiers.get(i).apply(this, realMilliseconds);
    }
    return true;
  }

  public void draw(Canvas c) {
    mMatrix.reset();
    mMatrix.postRotate(mRotation, mBitmapHalfWidth, mBitmapHalfHeight);
    mMatrix.postScale(mScale, mScale, mBitmapHalfWidth, mBitmapHalfHeight);
    mMatrix.postTranslate(mCurrentX, mCurrentY);
    mPaint.setAlpha(mAlpha);
    c.drawBitmap(mImage, mMatrix, mPaint);
  }

  public Particle activate(long startingMillisecond, List<ParticleModifier> modifiers) {
    mStartingMillisecond = startingMillisecond;
    // We do store a reference to the list, there is no need to copy, since the modifiers do not carte about states
    mModifiers = modifiers;
    return this;
  }
}
