package com.libremobileos.freeform.server.hook

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.util.Slog
import com.libremobileos.freeform.server.ui.WindowConfigStore
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * Hooks window manager Task methods so that packages marked with
 * [WindowConfigStore][com.libremobileos.freeform.server.ui.WindowConfigStore] as
 * `forceResizeable` are treated as resizeable, preventing letterboxing and activity
 * reload caused by manifest aspect/orientation constraints.
 *
 * This class references the Xposed API and must only be loaded when LSPosed is present
 * (see [FreeformResizeHook]).
 */
object FreeformResizeHookImpl {
    private const val TAG = "LMOFreeform/FreeformResizeHookImpl"
    private const val TASK_CLASS = "com.android.server.wm.Task"

    @JvmStatic
    fun install() {
        installHook("setResizeMode", Int::class.javaPrimitiveType) { param ->
            val task = param.thisObject
            if (isForceResizeableTask(task)) {
                param.args[0] = ActivityInfo.RESIZE_MODE_RESIZEABLE
            }
        }
        installHook("getResizeMode") { param ->
            if (isForceResizeableTask(param.thisObject)) {
                param.result = ActivityInfo.RESIZE_MODE_RESIZEABLE
            }
        }
    }

    private fun installHook(methodName: String, vararg params: Any, callback: XC_MethodHook) {
        try {
            val args = params.toMutableList().apply { add(callback) }.toTypedArray()
            XposedHelpers.findAndHookMethod(TASK_CLASS, methodName, *args)
            Slog.i(TAG, "hooked Task#$methodName")
        } catch (t: Throwable) {
            Slog.w(TAG, "failed to hook Task#$methodName: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    private fun isForceResizeableTask(task: Any?): Boolean {
        if (task == null) return false
        val packageName = runCatching {
            val top = XposedHelpers.callMethod(task, "topActivity") ?: return false
            val component = XposedHelpers.getObjectField(top, "mActivityComponent") as? ComponentName
            component?.packageName
        }.getOrNull() ?: return false
        return WindowConfigStore.isForceResizeable(packageName)
    }
}
