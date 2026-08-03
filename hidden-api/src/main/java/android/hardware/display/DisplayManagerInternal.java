package android.hardware.display;

import android.os.IBinder;
import android.view.Surface;

import com.libremobileos.freeform.ILMOFreeformDisplayCallback;

/**
 * Hidden API {@code android.hardware.display.DisplayManagerInternal}. The freeform
 * display adapter is wired in through this interface at runtime; the stub only exists
 * so the module compiles against the public SDK.
 */
public abstract class DisplayManagerInternal {
    public void createFreeformLocked(String name, ILMOFreeformDisplayCallback callback,
                                     int width, int height, int densityDpi, boolean secure,
                                     boolean ownContentOnly, boolean shouldShowSystemDecorations,
                                     Surface surface, float refreshRate,
                                     long presentationDeadlineNanos) {
        throw new RuntimeException("Stub!");
    }

    public void resizeFreeform(IBinder appToken, int width, int height, int densityDpi) {
        throw new RuntimeException("Stub!");
    }

    public void releaseFreeform(IBinder appToken) {
        throw new RuntimeException("Stub!");
    }
}
