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

package xyz.zedler.patrick.doodle.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResUtil {

  private final static String TAG = ResUtil.class.getSimpleName();

  @NonNull
  public static String getRawText(Context context, @RawRes int resId) {
    InputStream inputStream = context.getResources().openRawResource(resId);
    BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder text = new StringBuilder();
    try {
      for (String line; (line = bufferedReader.readLine()) != null; ) {
        text.append(line).append('\n');
      }
      text.deleteCharAt(text.length() - 1);
      inputStream.close();
    } catch (Exception e) {
      Log.e(TAG, "getRawText: ", e);
    }
    return text.toString();
  }

  public static void share(Context context, @StringRes int resId) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_TEXT, context.getString(resId));
    intent.setType("text/plain");
    context.startActivity(Intent.createChooser(intent, null));
  }
}
