package com.example.blackscreenlock;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView info = new TextView(this);
        info.setTextSize(18);
        info.setText("ล็อกจอกันมือลั่น\n\nปลดล็อก: แตะ 5 ครั้งติดกัน ภายใน 4 วินาที ที่มุมขวาบนของจอ\n");
        root.addView(info);

        Button clear = new Button(this);
        clear.setText("ล็อกแบบโปร่งใส (ยังเห็นหน้าจอ)");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startLock(false); }
        });
        root.addView(clear);

        Button black = new Button(this);
        black.setText("ล็อกแบบจอดำ");
        black.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startLock(true); }
        });
        root.addView(black);

        Button full = new Button(this);
        full.setText("ล็อกเต็มจอ ซ่อนแถบบน/ล่าง (จอดำ)");
        full.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LockActivity.class));
            }
        });
        root.addView(full);

        Button stop = new Button(this);
        stop.setText("หยุดล็อก");
        stop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                stopService(new Intent(MainActivity.this, LockService.class));
            }
        });
        root.addView(stop);

        setContentView(root);
    }

    private void startLock(boolean black) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this,
                    "กรุณาอนุญาต \"แสดงทับแอปอื่น\" แล้วกลับมากดอีกครั้ง",
                    Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            } catch (Throwable t) {
                Toast.makeText(this, "เครื่องนี้ไม่มีหน้าตั้งค่าสิทธิ์ดังกล่าว",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }
        Intent i = new Intent(this, LockService.class);
        i.putExtra("black", black);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(i);
        } else {
            startService(i);
        }
        finish();
    }
}
