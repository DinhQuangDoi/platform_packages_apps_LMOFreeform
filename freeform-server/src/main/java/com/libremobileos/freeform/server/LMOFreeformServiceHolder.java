package com.libremobileos.freeform.server;

import static com.libremobileos.freeform.server.Debug.dlog;

import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import com.libremobileos.freeform.ILMOFreeformDisplayCallback;
import com.libremobileos.freeform.server.ui.AppConfig;
import com.libremobileos.freeform.server.ui.FreeformConfig;
import com.libremobileos.freeform.server.ui.WindowConfigStore;

public class LMOFreeformServiceHolder {
    private static final String TAG = "LMOFreeform/LMOFreeformServiceManager";

    @SuppressLint("StaticFieldLeak")
    private static LMOFreeformUIService lmoFreeformUIService = null;
    private static LMOFreeformService lmoFreeformService = null;

    public static void init(LMOFreeformUIService uiService, LMOFreeformService freeformService) {
        lmoFreeformUIService = uiService;
        lmoFreeformService = freeformService;
    }

    public static boolean ping() {
        try {
            return lmoFreeformUIService.ping();
        } catch (Exception e) {
            Slog.e(TAG, e.toString());
            return false;
        }
    }

    public static void createDisplay(FreeformConfig freeformConfig, AppConfig appConfig, Surface surface, ILMOFreeformDisplayCallback callback) {
        String displayName;
        displayName = appConfig.getPackageName() + "," + appConfig.getActivityName() + "," + appConfig.getUserId();
        lmoFreeformService.createFreeform(
                displayName,
                callback,
                freeformConfig.getFreeformWidth(),
                freeformConfig.getFreeformHeight(),
                freeformConfig.getDensityDpi(),
                freeformConfig.getSecure(),
                freeformConfig.getOwnContentOnly(),
                freeformConfig.getShouldShowSystemDecorations(),
                surface,
                freeformConfig.getRefreshRate(),
                freeformConfig.getPresentationDeadlineNanos()
                );
    }

    public static boolean startApp(Context context, AppConfig appConfig, int displayId) {
        dlog(TAG, "startApp $appConfig displayId=$displayId");
        try {
            ComponentName component = new ComponentName(appConfig.getPackageName(), appConfig.getActivityName());
            logResizeability(context, component);
            Intent intent = new Intent();
            intent.setComponent(component);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ActivityOptions activityOptions = ActivityOptions.makeBasic();
            activityOptions.setLaunchDisplayId(displayId);
            setCallerDisplayId(activityOptions, displayId);
            startActivityAsUser(context, intent, activityOptions.toBundle(),
                    appConfig.getUserId());
            return true;
        } catch (Exception e) {
            Slog.e(TAG, "startApp failed", e);
            return false;
        }
    }

    private static void logResizeability(Context context, ComponentName component) {
        try {
            android.content.pm.ActivityInfo info =
                    context.getPackageManager().getActivityInfo(component, 0);
            int mode = getResizeMode(info);
            // RESIZE_MODE_RESIZEABLE == 1, RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION == 2
            boolean resizeable = mode == 1 || mode == 2;
            boolean forced = WindowConfigStore.isForceResizeable(component.getPackageName());
            Slog.i(TAG, "startApp resize check: " + component + " resizeMode=" + mode
                    + " isResizeable=" + resizeable + " forceResizeableRule=" + forced
                    + (resizeable ? "" : " (may be letterboxed/reloaded)"));
        } catch (Exception e) {
            Slog.e(TAG, "failed to query activity info for " + component, e);
        }
    }

    public static void startPendingIntent(PendingIntent pendingIntent, int displayId) {
        ActivityOptions activityOptions = ActivityOptions.makeBasic();
        activityOptions.setLaunchDisplayId(displayId);
        setCallerDisplayId(activityOptions, displayId);

        try {
            Object thread = ActivityThread.currentActivityThread();
            IApplicationThread app = (IApplicationThread) thread.getClass()
                    .getMethod("getApplicationThread").invoke(thread);
            IIntentSender target = (IIntentSender) PendingIntent.class
                    .getMethod("getTarget").invoke(pendingIntent);
            IBinder whitelistToken = (IBinder) PendingIntent.class
                    .getMethod("getWhitelistToken").invoke(pendingIntent);
            SystemServiceHolder.activityManager.sendIntentSender(
                    app,
                    target,
                    whitelistToken,
                    0,
                    null,
                    null,
                    null,
                    null,
                    activityOptions.toBundle()
            );
        } catch (Exception e) {
            Slog.e(TAG, "startPendingIntent failed!", e);
        }
    }

    // ActivityInfo#resizeMode is @hide; read it reflectively.
    private static int getResizeMode(android.content.pm.ActivityInfo info) {
        try {
            java.lang.reflect.Field field =
                    android.content.pm.ActivityInfo.class.getDeclaredField("resizeMode");
            field.setAccessible(true);
            return field.getInt(info);
        } catch (Exception e) {
            return -1;
        }
    }

    // ActivityOptions#setCallerDisplayId is @hide; call it reflectively.
    private static void setCallerDisplayId(ActivityOptions activityOptions, int displayId) {
        try {
            ActivityOptions.class.getMethod("setCallerDisplayId", int.class)
                    .invoke(activityOptions, displayId);
        } catch (Exception e) {
            Slog.e(TAG, "setCallerDisplayId failed", e);
        }
    }

    // Context#startActivityAsUser and the UserHandle(int) constructor are @hide; call reflectively.
    private static void startActivityAsUser(Context context, Intent intent, Bundle options,
                                            int userId) {
        try {
            Object user = UserHandle.class.getConstructor(int.class).newInstance(userId);
            Context.class.getMethod("startActivityAsUser",
                            Intent.class, Bundle.class, UserHandle.class)
                    .invoke(context, intent, options, user);
        } catch (Exception e) {
            Slog.e(TAG, "startActivityAsUser failed", e);
        }
    }

    public static void touch(MotionEvent event, int displayId) {
        lmoFreeformService.injectInputEvent(event, displayId);
    }

    public static void back(int displayId) {
        KeyEvent down = new KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_BACK,
                0
        );
        down.setSource(InputDevice.SOURCE_KEYBOARD);
        KeyEvent up = new KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_BACK,
                0
        );
        up.setSource(InputDevice.SOURCE_KEYBOARD);
        try {
            lmoFreeformService.injectInputEvent(down, displayId);
            lmoFreeformService.injectInputEvent(up, displayId);
        } catch (Exception ignored) {

        }
    }

    public static void resizeFreeform(IBinder token, int width, int height, int density) {
        lmoFreeformUIService.resizeFreeform(token, width, height, density);
    }

    public static void releaseFreeform(IBinder token) {
        lmoFreeformUIService.releaseFreeform(token);
    }
}
