package com.libremobileos.freeform.server.ui

import android.util.AtomicFile
import android.util.Slog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

/**
 * Stores per-package freeform window config as a JSON file owned by system_server.
 *
 * The file lives under [CONFIG_DIR] so it can also be edited by hand (adb root) and is
 * readable by the LMOFreeform app (android.uid.system) through the AIDL service.
 */
object WindowConfigStore {
    private const val TAG = "LMOFreeform/WindowConfigStore"

    private const val CONFIG_DIR = "/data/system/lmo_freeform"
    private const val CONFIG_FILE = "$CONFIG_DIR/window_config.json"

    private val gson = Gson()
    private val type = object : TypeToken<HashMap<String, WindowConfigEntry>>() {}.type

    @Volatile
    private var cache: HashMap<String, WindowConfigEntry>? = null

    private val lock = Any()

    @JvmStatic
    fun get(packageName: String): WindowConfigEntry? {
        if (packageName.isNullOrEmpty()) return null
        synchronized(lock) {
            return loadLocked()[packageName]
        }
    }

    @JvmStatic
    fun all(): Map<String, WindowConfigEntry> {
        synchronized(lock) {
            return HashMap(loadLocked())
        }
    }

    @JvmStatic
    fun allJson(): String {
        synchronized(lock) {
            return gson.toJson(loadLocked())
        }
    }

    @JvmStatic
    fun put(packageName: String, entry: WindowConfigEntry) {
        if (packageName.isNullOrEmpty()) return
        synchronized(lock) {
            val map = loadLocked()
            map[packageName] = entry
            saveLocked(map)
        }
    }

    @JvmStatic
    fun update(packageName: String, transform: (WindowConfigEntry) -> WindowConfigEntry) {
        if (packageName.isNullOrEmpty()) return
        synchronized(lock) {
            val map = loadLocked()
            val current = map[packageName] ?: return
            map[packageName] = transform(current)
            saveLocked(map)
        }
    }

    @JvmStatic
    fun remove(packageName: String) {
        if (packageName.isNullOrEmpty()) return
        synchronized(lock) {
            val map = loadLocked()
            if (map.remove(packageName) != null) {
                saveLocked(map)
            }
        }
    }

    @JvmStatic
    fun isForceResizeable(packageName: String): Boolean =
        get(packageName)?.forceResizeable ?: false

    private fun loadLocked(): HashMap<String, WindowConfigEntry> {
        cache?.let { return it }
        val map = try {
            val file = File(CONFIG_FILE)
            if (file.exists()) {
                gson.fromJson<HashMap<String, WindowConfigEntry>>(
                    file.readText(),
                    type
                ) ?: HashMap()
            } else {
                HashMap()
            }
        } catch (e: Exception) {
            Slog.e(TAG, "failed to load config", e)
            HashMap()
        }
        cache = map
        return map
    }

    private fun saveLocked(map: HashMap<String, WindowConfigEntry>) {
        try {
            val dir = File(CONFIG_DIR)
            if (!dir.exists() && !dir.mkdirs()) {
                Slog.e(TAG, "failed to create config dir $CONFIG_DIR")
            }
            val atomicFile = AtomicFile(File(CONFIG_FILE))
            var out: FileOutputStream? = null
            try {
                out = atomicFile.startWrite()
                out.write(gson.toJson(map).toByteArray(Charsets.UTF_8))
                out.flush()
                atomicFile.finishWrite(out)
            } catch (e: Exception) {
                Slog.e(TAG, "failed to write config", e)
                atomicFile.failWrite(out)
            }
            cache = map
        } catch (e: Exception) {
            Slog.e(TAG, "failed to save config", e)
        }
    }
}
