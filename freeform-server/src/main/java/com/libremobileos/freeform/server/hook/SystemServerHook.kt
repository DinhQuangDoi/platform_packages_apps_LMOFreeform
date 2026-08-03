package com.libremobileos.freeform.server.hook

import android.util.Slog
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SystemServerHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android") return
        try {
            FreeformResizeHook.install()
            Slog.i(TAG, "freeform resize hook installed")
        } catch (t: Throwable) {
            Slog.e(TAG, "failed to install freeform resize hook", t)
        }
    }

    companion object {
        private const val TAG = "LMOFreeform/SystemServerHook"
    }
}
