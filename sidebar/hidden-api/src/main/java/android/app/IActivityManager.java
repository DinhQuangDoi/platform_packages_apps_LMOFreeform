package android.app;

import android.os.IBinder;

public class IActivityManager {
    public static class Stub {
        public static IActivityManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }

    public void registerUserSwitchObserver(UserSwitchObserver observer, String name) {
        throw new RuntimeException("Stub!");
    }

    public void unregisterUserSwitchObserver(UserSwitchObserver observer) {
        throw new RuntimeException("Stub!");
    }
}
