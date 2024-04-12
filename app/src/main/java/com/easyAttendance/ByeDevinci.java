package com.easyAttendance;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;

import com.outplayesilvccc.R;

import java.util.ArrayList;
import java.util.List;

public class ByeDevinci extends AccessibilityService {
    private static final String TAG = "TESTING:";
    private boolean clicked = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() != null && event.getPackageName().equals("fr.devinci.student") &&
                (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                        event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED))
            handleAccessibilityEvent();
    }

    private void handleAccessibilityEvent() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> allButtons = findAllButtons(rootNode);
            for (AccessibilityNodeInfo b : allButtons
            ) {
                Log.d(TAG, "handleAccessibilityEvent: " + b);
                CharSequence contentDescription = b.getContentDescription();
                if (contentDescription != null &&
                        (contentDescription.toString().toLowerCase().contains("validate") ||
                                contentDescription.toString().toLowerCase().contains("présence")) &&
                        b.isClickable() &&
                        !clicked) {
                    Log.d(TAG, "SIZE: " + allButtons.size());
                    this.clicked = true;
                    clickButton(b);
                    handler();
                }
            }
            rootNode.recycle();
        } else {
            Log.e(TAG, "handleAccessibilityEvent: ERROR");
        }
    }

    private void handler() {
        new Handler().postDelayed(() -> {
            this.clicked = false;
            Log.d(TAG, "handler: READY!");
        }, 5000);
    }

    private void clickButton(AccessibilityNodeInfo button) {
        Log.e(TAG, "CLICKING: " + button);
        if (button != null) button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private List<AccessibilityNodeInfo> findAllButtons(AccessibilityNodeInfo node) {
        List<AccessibilityNodeInfo> buttons = new ArrayList<>();
        allButtons(node, 0, buttons);
        return buttons;
    }

    private void allButtons(AccessibilityNodeInfo node, int depth, List<AccessibilityNodeInfo> buttons) {
        if (node == null) return;
        if ("android.widget.Button".contentEquals(node.getClassName())) buttons.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            allButtons(childNode, depth + 1, buttons);
            childNode.recycle();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: Bye!" );
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong!");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        setupOverlay();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        Log.d(TAG, "onServiceConnected: Worked!");
    }

    private void setupOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            View overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, new LinearLayout(this), false);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 0;

            windowManager.addView(overlayView, params);
            Button overlayButton = overlayView.findViewById(R.id.overlayButton);
            overlayButton.setOnClickListener(v -> openApp());
        }
    }

    private void openApp() {
        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;

            if ("fr.devinci.student".equals(packageName)) {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                } else {
                    Log.e(TAG, "Unable to get launch intent for package: " + packageName);
                }
                break;
            }
        }





    }


}
