package com.android.server.display;

import android.content.Context;
import android.os.IBinder;
import android.view.Surface;
import android.view.SurfaceControl;

abstract class DisplayDevice {

    public DisplayDevice(DisplayAdapter displayAdapter, IBinder displayToken, String uniqueId,
                         Context context) {
        throw new RuntimeException("Stub!");
    }

    public DisplayDevice(DisplayAdapter displayAdapter, IBinder displayToken, String uniqueId) {
        throw new RuntimeException("Stub!");
    }

    public final IBinder getDisplayTokenLocked() {
        throw new RuntimeException("Stub!");
    }

    public final String getUniqueId() {
        throw new RuntimeException("Stub!");
    }

    public abstract boolean hasStableUniqueId();

    public abstract DisplayDeviceInfo getDisplayDeviceInfoLocked();

    public void performTraversalLocked(SurfaceControl.Transaction t) {
        throw new RuntimeException("Stub!");
    }

    public final void setSurfaceLocked(SurfaceControl.Transaction t, Surface surface) {
        throw new RuntimeException("Stub!");
    }
}
