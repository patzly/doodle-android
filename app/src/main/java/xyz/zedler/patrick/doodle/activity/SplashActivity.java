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
 * Copyright (c) 2020-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.activity;

import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class SplashActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    new SystemBarBehavior(this).setUp();

    LayerDrawable splashContent = (LayerDrawable) ResourcesCompat.getDrawable(
        getResources(), R.drawable.splash_content, null
    );

    getWindow().setBackgroundDrawable(splashContent);

    try {
      assert splashContent != null;
      ViewUtil.startIcon(splashContent.findDrawableByLayerId(R.id.splash_logo));
      new Handler(Looper.getMainLooper()).postDelayed(
          this::startSettingsActivity, 900
      );
    } catch (Exception e) {
      startSettingsActivity();
    }
  }

  private void startSettingsActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addCategory("android.intent.category.LAUNCHER");
    startActivity(intent);
    overridePendingTransition(0, R.anim.fade_out);
    finish();
  }
}
