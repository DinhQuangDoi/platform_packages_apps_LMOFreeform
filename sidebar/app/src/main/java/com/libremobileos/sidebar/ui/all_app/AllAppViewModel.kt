package com.libremobileos.sidebar.ui.all_app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.libremobileos.sidebar.bean.AppInfo
import com.libremobileos.sidebar.utils.Logger
import com.libremobileos.sidebar.utils.getBadgedIcon
import com.libremobileos.sidebar.utils.getSidebarFilteredUsers
import com.libremobileos.sidebar.utils.getInfo
import com.libremobileos.sidebar.utils.isResizeableActivity
import com.libremobileos.sidebar.utils.isSidebarUserAllowed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Collections
import java.util.Locale

/**
 * @author KindBrave
 * @since 2023/10/25
 */
class AllAppViewModel(private val application: Application): AndroidViewModel(application) {
    private val logger = Logger("AllAppViewModel")
    private val allAppList = ArrayList<AppInfo>()
    val appListFlow: StateFlow<List<AppInfo>>
        get() = _appList.asStateFlow()
    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    private val appComparator = AppComparator()

    private val appContext = application.applicationContext
    private val userManager = application.getSystemService(Context.USER_SERVICE) as UserManager
    private val launcherApps = application.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val userProfileReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            logger.d("userProfileReceiver received ${intent.action}")
            allAppList.clear()
            initAllAppList()
        }
    }

    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) {
            logger.d("onPackageRemoved: $packageName")
            viewModelScope.launch(Dispatchers.IO) {
                allAppList.remove(allAppList.getInfo(packageName, user))
                _appList.value = allAppList.toList()
            }
        }

        override fun onPackageAdded(packageName: String, user: UserHandle) {
            logger.d("onPackageAdded: $packageName")
            viewModelScope.launch(Dispatchers.IO) {
                allAppList.remove(allAppList.getInfo(packageName, user))
            }

            runCatching {
                val info = application.packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
                val launchIntent = application.packageManager.getLaunchIntentForPackage(packageName)
                val userId = getUserIdentifier(user)
                if (!userManager.isSidebarUserAllowed(userId)) {
                    logger.d("onPackageAdded: $packageName userId=$userId not allowed")
                    return
                }
                if (launchIntent != null && launchIntent.component != null) {
                    if (!application.isResizeableActivity(launchIntent.component!!)) {
                        logger.d("onPackageAdded: activity not resizeable, but adding anyway: ${launchIntent.component}")
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        allAppList.add(
                            AppInfo(
                                info.loadLabel(application.packageManager).toString(),
                                application.getBadgedIcon(info, user),
                                info.packageName,
                                launchIntent.component!!.className,
                                userId
                            )
                        )
                        Collections.sort(allAppList, appComparator)
                        _appList.value = allAppList.toList()
                    }
                }
            }
                .onFailure { logger.e("onPackageAdded: error in $packageName", it) }
        }

        override fun onPackageChanged(packageName: String, user: UserHandle) {
            logger.d("onPackageChanged: $packageName")
            val appInfo = application.packageManager.getApplicationInfo(packageName, 0)
            if (!appInfo.enabled) {
                onPackageRemoved(packageName, user)
            }
        }

        override fun onPackagesAvailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean
        ) {

        }

        override fun onPackagesUnavailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean
        ) {

        }
    }

    init {
        logger.d("init")
        initAllAppList()
        launcherApps.registerCallback(launcherAppsCallback)
        registerReceiverAsUser(
            appContext,
            userProfileReceiver,
            currentUserHandle,
            IntentFilter().apply {
                addAction(ACTION_PROFILE_AVAILABLE)
                addAction(ACTION_PROFILE_UNAVAILABLE)
            },
        )
    }

    override fun onCleared() {
        logger.d("onCleared")
        launcherApps.unregisterCallback(launcherAppsCallback)
        appContext.unregisterReceiver(userProfileReceiver)
    }

    private fun initAllAppList() {
        viewModelScope.launch(Dispatchers.IO) {
            userManager.getSidebarFilteredUsers().forEach { userInfo ->
                val list = launcherApps.getActivityList(null, userInfo.userHandle)
                list.forEach { info ->
                    val component = info.componentName
                    if (!application.isResizeableActivity(component)) {
                        logger.d("activity not resizeable, but adding anyway: $component")
                    }
                    allAppList.add(
                        AppInfo(
                            info.label.toString(),
                            application.packageManager.getUserBadgedIcon(
                                info.getIcon(0),
                                userInfo.userHandle
                            ),
                            component.packageName,
                            component.className,
                            userInfo.userId
                        )
                    )
                }
            }
            Collections.sort(allAppList, appComparator)
            _appList.value = allAppList.toList()
        }
    }

    companion object {
        private const val ACTION_PROFILE_AVAILABLE = "android.intent.action.PROFILE_AVAILABLE"
        private const val ACTION_PROFILE_UNAVAILABLE = "android.intent.action.PROFILE_UNAVAILABLE"

        val Factory = viewModelFactory {
            initializer {
                AllAppViewModel(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                )
            }
        }

        val currentUserHandle: UserHandle by lazy {
            UserHandle::class.java.getDeclaredField("CURRENT").get(null) as UserHandle
        }

        fun getUserIdentifier(user: UserHandle): Int {
            return UserHandle::class.java.getDeclaredMethod("getIdentifier")
                .invoke(user) as Int
        }

        fun registerReceiverAsUser(
            context: Context,
            receiver: BroadcastReceiver,
            user: UserHandle,
            filter: IntentFilter,
        ): Intent? {
            return Context::class.java.getDeclaredMethod(
                "registerReceiverAsUser",
                BroadcastReceiver::class.java,
                UserHandle::class.java,
                IntentFilter::class.java,
                String::class.java,
                android.os.Handler::class.java,
            ).invoke(context, receiver, user, filter, null, null) as Intent?
        }
    }

    private inner class AppComparator : Comparator<AppInfo> {
        override fun compare(p0: AppInfo, p1: AppInfo): Int {
            return Collator.getInstance().compare(p0.label, p1.label)
        }
    }
}
