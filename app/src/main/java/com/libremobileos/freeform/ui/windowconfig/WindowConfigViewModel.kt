package com.libremobileos.freeform.ui.windowconfig

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.libremobileos.freeform.LMOFreeformServiceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WindowConfig(
    val width: Int = 0,
    val height: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
    val forceResizeable: Boolean = false
)

data class AppItem(val packageName: String, val label: String)

class WindowConfigViewModel(private val app: Application) : AndroidViewModel(app) {
    private val gson = Gson()
    private val type = object : TypeToken<Map<String, WindowConfig>>() {}.type

    private val _configs = MutableStateFlow<Map<String, WindowConfig>>(emptyMap())
    val configs = _configs.asStateFlow()

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps = _apps.asStateFlow()

    init {
        loadConfigs()
        loadApps()
    }

    fun loadConfigs() {
        viewModelScope.launch(Dispatchers.IO) {
            val raw = LMOFreeformServiceManager.getWindowConfigs()
            val map = runCatching {
                gson.fromJson<Map<String, WindowConfig>>(raw, type)
            }.getOrNull() ?: emptyMap()
            _configs.value = map
        }
    }

    fun saveConfig(packageName: String, config: WindowConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            LMOFreeformServiceManager.setWindowConfig(
                packageName,
                config.width,
                config.height,
                config.x,
                config.y,
                config.forceResizeable
            )
            loadConfigs()
        }
    }

    fun removeConfig(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            LMOFreeformServiceManager.removeWindowConfig(packageName)
            loadConfigs()
        }
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = app.packageManager
            val list = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.enabled }
                .mapNotNull { info ->
                    runCatching {
                        val launch = pm.getLaunchIntentForPackage(info.packageName)
                        if (launch != null && launch.component != null) {
                            AppItem(
                                info.packageName,
                                pm.getApplicationLabel(info).toString()
                            )
                        } else null
                    }.getOrNull()
                }
                .distinctBy { it.packageName }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            _apps.value = list
        }
    }
}
