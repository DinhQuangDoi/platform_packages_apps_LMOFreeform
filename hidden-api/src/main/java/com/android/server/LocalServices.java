package com.android.server;

public class LocalServices {
    private LocalServices() {
    }

    public static <T> T getService(Class<T> type) {
        throw new RuntimeException("Stub!");
    }
}
