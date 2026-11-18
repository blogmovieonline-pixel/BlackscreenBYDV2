package com.example.blackscreenlock;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * โหมดเต็มจอแบบดูวิดีโอ: ซ่อนแถบบน/ล่าง + ตรึงแอป (screen pinning) + จอดำกินสัมผัส
 * ปลดล็อก: แตะ 5 ครั้งติดกันภายใน 4 วินาที ที่มุมขวาบน
 */
public class LockActivity extends Activity {

    private static final int REQUIRED_TAPS = 5;
    private static final long WINDOW_MS = 4000;

    private int tapCount = 0;
    private long firstTapTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View root = new View(this);
        root.setBackgroundColor(Color.BLACK);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    handleTap(e.getX(), e.getY());
                }
                return true;
            }
        });
        setContentView(root);
        hideSystemUi();

        try {
            startLockTask();
        } catch (Throwable ignored) {
        }
    }

    private void handleTap(float x, float y) {
        float corner = 150f * getResources().getDisplayMetrics().density;
        float screenW = getResources().getDisplayMetrics().widthPixels;
        if (x < screenW - corner || y > corner) {
            tapCount = 0;
            return;
        }
        long now = System.currentTimeMillis();
        if (tapCount == 0 || now - firstTapTime > WINDOW_MS) {
            tapCount = 1;
            firstTapTime = now;
        } else {
            tapCount++;
        }
        if (tapCount >= REQUIRED_TAPS) {
            try {
                stopLockTask();
            } catch (Throwable ignored) {
            }
            finish();
        }
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    public void onBackPressed() {
        // ไม่ทำอะไร
    }
}
