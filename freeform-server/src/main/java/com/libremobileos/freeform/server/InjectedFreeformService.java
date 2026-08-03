package com.libremobileos.freeform.server;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.view.Surface;

import com.libremobileos.freeform.ILMOFreeformDisplayCallback;
import com.libremobileos.freeform.ILMOFreeformUIService;

public class InjectedFreeformService extends ILMOFreeformUIService.Stub {
    private final LMOFreeformUIService delegate;

    public InjectedFreeformService(LMOFreeformUIService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void startAppInFreeform(String packageName, String activityName, int userId,
                                   int taskId, PendingIntent pendingIntent,
                                   int width, int height, int densityDpi) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.startAppInFreeform(packageName, activityName, userId, taskId,
                    pendingIntent, width, height, densityDpi);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void removeFreeform(String freeformId) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.removeFreeform(freeformId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void createFreeformInUser(String name, int width, int height, int densityDpi,
                                     float refreshRate, long presentationDeadlineNanos,
                                     boolean secure, boolean ownContentOnly,
                                     boolean shouldShowSystemDecorations, Surface surface,
                                     ILMOFreeformDisplayCallback callback) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.createFreeformInUser(name, width, height, densityDpi, refreshRate,
                    presentationDeadlineNanos, secure, ownContentOnly,
                    shouldShowSystemDecorations, surface, callback);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void resizeFreeform(IBinder appToken, int width, int height, int densityDpi) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.resizeFreeform(appToken, width, height, densityDpi);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void releaseFreeform(IBinder appToken) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.releaseFreeform(appToken);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public boolean ping() {
        long token = Binder.clearCallingIdentity();
        try {
            return delegate.ping();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void setWindowConfig(String packageName, int width, int height,
                                int x, int y, boolean forceResizeable) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.setWindowConfig(packageName, width, height, x, y, forceResizeable);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void removeWindowConfig(String packageName) {
        long token = Binder.clearCallingIdentity();
        try {
            delegate.removeWindowConfig(packageName);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public String getWindowConfigs() {
        long token = Binder.clearCallingIdentity();
        try {
            return delegate.getWindowConfigs();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
