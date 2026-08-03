package com.libremobileos.freeform.server.hook

import android.content.Context
import android.hardware.display.DisplayManagerInternal
import android.os.IBinder
import android.os.ServiceManager
import android.util.ArrayMap
import android.util.Slog
import com.libremobileos.freeform.server.InjectedFreeformService
import com.libremobileos.freeform.server.LMOFreeformService
import com.libremobileos.freeform.server.LMOFreeformUIService
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SystemServerHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android") return

        val clazz = lpparam.classLoader
            .loadClass("com.android.server.SystemServer")
        clazz.declaredMethods
            .filter { it.name == "startOtherServices" }
            .forEach { method ->
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            injectFreeformServices(param.thisObject)
                        } catch (t: Throwable) {
                            Slog.e(TAG, "Failed to inject LMOFreeform services", t)
                        }
                    }
                })
            }
    }

    private fun injectFreeformServices(systemServer: Any) {
        val systemContext: Context
        val dmi: DisplayManagerInternal

        try {
            val mscField = systemServer.javaClass.getDeclaredField("mSystemContext")
            mscField.isAccessible = true
            systemContext = mscField.get(systemServer) as Context
        } catch (e: Exception) {
            throw RuntimeException("Cannot access SystemServer.mSystemContext", e)
        }

        try {
            val localServicesClass = Class.forName("com.android.server.LocalServices")
            dmi = localServicesClass.getMethod("getService", Class::class.java)
                .invoke(null, DisplayManagerInternal::class.java) as DisplayManagerInternal
        } catch (e: Exception) {
            throw RuntimeException("Cannot get DisplayManagerInternal from LocalServices", e)
        }

        val lmoFreeformService = LMOFreeformService(dmi)
        val realUIService = LMOFreeformUIService(systemContext, dmi, lmoFreeformService)
        val wrapperService = InjectedFreeformService(realUIService)

        try {
            val smClass = ServiceManager::class.java
            smClass.getMethod("addService", String::class.java, IBinder::class.java)
                .invoke(null, SERVICE_NAME, wrapperService)
            val cache = ArrayMap<String, IBinder>()
            cache[SERVICE_NAME] = wrapperService
            smClass.getMethod("initServiceCache", Map::class.java).invoke(null, cache)
            Slog.i(TAG, SERVICE_NAME + " injected")
        } catch (e: Exception) {
            Slog.e(TAG, "Failed to register " + SERVICE_NAME, e)
        }
    }

    companion object {
        private const val TAG = "LMOFreeform/SystemServerHook"
        private const val SERVICE_NAME = "lmo_freeform"
    }
}
