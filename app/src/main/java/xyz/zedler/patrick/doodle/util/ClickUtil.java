package xyz.zedler.patrick.doodle.util;

import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;

public class ClickUtil {

    private long lastClick;

    public ClickUtil() {
        lastClick = 0;
    }

    public void update() {
        lastClick = SystemClock.elapsedRealtime();
    }

    public boolean isDisabled() {
        if (SystemClock.elapsedRealtime() - lastClick < 500) return true;
        update();
        return false;
    }

    public static void setOnClickListeners(View.OnClickListener listener, View... views) {
        for (View view : views) view.setOnClickListener(listener);
    }

    public static void setOnCheckedChangeListeners(
            CompoundButton.OnCheckedChangeListener listener,
            CompoundButton... compoundButtons
    ) {
        for (CompoundButton view : compoundButtons) view.setOnCheckedChangeListener(listener);
    }
}
