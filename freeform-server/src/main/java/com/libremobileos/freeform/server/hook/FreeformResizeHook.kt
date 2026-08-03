package com.libremobileos.freeform.server.hook

import android.util.Slog

/**
 * Installs the force-resizeable LSPosed hooks when the Xposed bridge is present in
 * system_server. This class intentionally does NOT reference any Xposed class so it can
 * load even on ROMs without LSPosed.
 */
object FreeformResizeHook {
    private const val TAG = "LMOFreeform/FreeformResizeHook"
    private const val IMPL_CLASS = "com.libremobileos.freeform.server.hook.FreeformResizeHookImpl"

    @JvmStatic
    fun install() {
        try {
            // NoClassDefFoundError / ClassNotFoundException when LSPosed is not installed.
            Class.forName("de.robv.android.xposed.XposedBridge", true, FreeformResizeHook::class.java.classLoader)
            Class.forName(IMPL_CLASS, true, FreeformResizeHook::class.java.classLoader)
                .getMethod("install")
                .invoke(null)
            Slog.i(TAG, "LSPosed available, force-resizeable hook installed")
        } catch (t: Throwable) {
            Slog.i(TAG, "LSPosed not available (${t.javaClass.simpleName}), force-resizeable hook disabled")
        }
    }
}
