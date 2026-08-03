package com.android.server.wm;

public abstract class WindowManagerInternal {
    public interface DisplaySecureContentListener {
        void onDisplayHasSecureWindowOnScreenChanged(int displayId, boolean hasSecureWindowOnScreen);
    }

    public void registerDisplaySecureContentListener(DisplaySecureContentListener listener) {
        throw new RuntimeException("Stub!");
    }

    public void unregisterDisplaySecureContentListener(DisplaySecureContentListener listener) {
        throw new RuntimeException("Stub!");
    }
}
