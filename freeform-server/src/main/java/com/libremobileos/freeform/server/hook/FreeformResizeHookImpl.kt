package com.libremobileos.freeform.server.hook

import android.content.ComponentName
import android.util.Slog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedHelpers
import java.io.File

object FreeformResizeHookImpl {
    private const val TAG = "LMOFreeform/FreeformResizeHookImpl"
    private const val TASK_CLASS = "com.android.server.wm.Task"
    private const val CONFIG_FILE = "/data/system/lmo_freeform/window_config.json"
    private const val TTL_MS = 3000L

    private val gson = Gson()
    private val configType = object : TypeToken<HashMap<String, Map<String, Any?>>>() {}.type

    @Volatile
    private var configCache: HashMap<String, Boolean>? = null
    @Volatile
    private var lastLoadTime = 0L
    private val configLock = Any()

    @JvmStatic
    fun install() {
        installHook(
            "setResizeMode",
            Int::class.javaPrimitiveType!!,
            callback = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (isForceResizeableTask(param.thisObject)) {
                        param.args[0] = 1
                    }
                }
            }
        )
        installHook(
            "getResizeMode",
            callback = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (isForceResizeableTask(param.thisObject)) {
                        param.result = 1
                    }
                }
            }
        )
    }

    private fun installHook(methodName: String, vararg params: Any, callback: XC_MethodHook) {
        try {
            val args = params.toMutableList().apply { add(callback) }.toTypedArray()
            val loader = FreeformResizeHookImpl::class.java.classLoader
            XposedHelpers.findAndHookMethod(TASK_CLASS, loader, methodName, *args)
            Slog.i(TAG, "hooked Task#$methodName")
        } catch (t: Throwable) {
            Slog.w(TAG, "failed to hook Task#$methodName: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    private fun isForceResizeableTask(task: Any?): Boolean {
        if (task == null) return false
        val packageName = getTaskPackageName(task) ?: return false
        return isForceResizeable(packageName)
    }

    private fun getTaskPackageName(task: Any): String? {
        val top = getTopActivity(task) ?: return null

        (XposedHelpers.getObjectField(top, "mActivityComponent") as? ComponentName)?.packageName?.let { return it }

        (XposedHelpers.getObjectField(top, "packageName") as? String)?.let { return it }

        return null
    }

    private fun getTopActivity(task: Any): Any? {
        val methods = listOf("topActivity", "getTopMostActivity", "getRootActivity")
        for (method in methods) {
            try {
                val result = XposedHelpers.callMethod(task, method)
                if (result != null) return result
            } catch (_: Throwable) {}
        }
        return null
    }

    private fun isForceResizeable(packageName: String): Boolean {
        return loadForceResizeableCache()[packageName] ?: false
    }

    private fun loadForceResizeableCache(): HashMap<String, Boolean> {
        val now = System.currentTimeMillis()
        val cached = configCache
        if (cached != null && now - lastLoadTime < TTL_MS) {
            return cached
        }
        synchronized(configLock) {
            if (configCache != null && now - lastLoadTime < TTL_MS) {
                return configCache!!
            }
            val result = HashMap<String, Boolean>()
            try {
                val file = File(CONFIG_FILE)
                if (file.exists()) {
                    val raw: HashMap<String, Map<String, Any?>> =
                        gson.fromJson(file.readText(), configType) ?: return result
                    raw.forEach { (pkg, entry) ->
                        val force = entry["forceResizeable"]
                        if (force is Boolean && force) {
                            result[pkg] = true
                        } else if (force is Number && force.toInt() == 1) {
                            result[pkg] = true
                        }
                    }
                }
            } catch (_: Throwable) {}
            configCache = result
            lastLoadTime = now
            return result
        }
    }
}
