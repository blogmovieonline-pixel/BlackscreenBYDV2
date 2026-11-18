package com.example.blackscreenlock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * วางชั้นโปร่งใส (หรือดำ) ทับทั้งจอ กินทุกการสัมผัส
 * ปลดล็อก: แตะ 5 ครั้งติดกันภายใน 4 วินาทีที่มุมขวาบน
 * ปุ่มฮาร์ดแวร์ (พวงมาลัย/ปุ่มจริง) ยังทำงานตามปกติ
 */
public class LockService extends Service {

    private static final String CHANNEL = "lock";
    private static final int REQUIRED_TAPS = 5;
    private static final long WINDOW_MS = 4000;

    private WindowManager wm;
    private View overlay;
    private int tapCount = 0;
    private long firstTapTime = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, buildNotification());
        boolean black = intent != null && intent.getBooleanExtra("black", false);
        removeOverlay();
        try {
            addOverlay(black);
        } catch (Throwable t) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void addOverlay(boolean black) {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlay = new View(this);
        // โปร่งใสแต่ใส่สีจางมากๆ เพื่อให้รู้ว่าล็อกอยู่
        overlay.setBackgroundColor(black ? Color.BLACK : 0x14000000);
        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    handleTap(e.getRawX(), e.getRawY());
                }
                return true;
            }
        });

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        wm.addView(overlay, lp);
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
            stopSelf();
        }
    }

    private void removeOverlay() {
        if (overlay != null && wm != null) {
            try {
                wm.removeView(overlay);
            } catch (Throwable ignored) {
            }
        }
        overlay = null;
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL, "ล็อกจอ", NotificationManager.IMPORTANCE_LOW));
        }
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);
        nb.setContentTitle("ล็อกจออยู่")
                .setContentText("แตะ 5 ครั้งที่มุมขวาบนเพื่อปลดล็อก")
                .setSmallIcon(android.R.drawable.ic_lock_lock);
        return nb.build();
    }

    @Override
    public void onDestroy() {
        removeOverlay();
        super.onDestroy();
    }
}
