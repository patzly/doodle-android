/*
 * This file is part of Doodle Android.
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 * Copyright 2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.util;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

public class IconUtil {

    private final static String TAG = IconUtil.class.getSimpleName();
    private final static boolean DEBUG = false;

    public static void start(ImageView imageView) {
        if (imageView == null) return;
        start(imageView.getDrawable());
    }

    public static void start(Drawable drawable) {
        if (drawable == null) return;
        try {
            ((Animatable) drawable).start();
        } catch (ClassCastException cla) {
            if (DEBUG) Log.e(TAG, "start() requires AnimVectorDrawable");
        }
    }
}
