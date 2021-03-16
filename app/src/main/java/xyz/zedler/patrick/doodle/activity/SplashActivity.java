package xyz.zedler.patrick.doodle.activity;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;

public class SplashActivity extends AppCompatActivity {

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        new SystemBarBehavior(this).setUp();

        LayerDrawable splashContent = (LayerDrawable) ResourcesCompat.getDrawable(
                getResources(), R.drawable.splash_content, null
        );

        getWindow().setBackgroundDrawable(splashContent);

        try {
            assert splashContent != null;
            Drawable splashLogo = splashContent.findDrawableByLayerId(R.id.splash_logo);
            AnimatedVectorDrawable logo = (AnimatedVectorDrawable) splashLogo;
            logo.start();
            new Handler(Looper.getMainLooper()).postDelayed(
                this::startNextActivity, 900
            );
        } catch (Exception e) {
            startNextActivity();
        }
    }

    private void startNextActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(0, R.anim.fade_out);
        finish();
    }
}
