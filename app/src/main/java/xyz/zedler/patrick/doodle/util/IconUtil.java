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
