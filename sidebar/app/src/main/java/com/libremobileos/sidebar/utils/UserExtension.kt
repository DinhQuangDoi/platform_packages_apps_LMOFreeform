package com.libremobileos.sidebar.utils

import android.content.pm.UserInfo
import android.os.UserHandle
import android.os.UserManager
import com.libremobileos.sidebar.bean.SidebarUserInfo

@Suppress("UNCHECKED_CAST")
fun UserManager.getSidebarFilteredUsers(): List<SidebarUserInfo> {
    val myUserId = UserHandle.myUserId()
    return (UserManager::class.java.getDeclaredMethod("getUsers")
        .invoke(this) as List<UserInfo>)
        .filter { isSidebarUserAllowed(it) }
        .map { userInfo ->
            SidebarUserInfo(
                userInfo.id,
                userInfo.userHandle,
                if (userInfo.id != myUserId) {
                    " (${userInfo.name})"
                } else {
                    ""
                }
            )
        }
}

fun UserManager.isSidebarUserAllowed(userInfo: UserInfo): Boolean {
    val myUserId = UserHandle.myUserId()
    return userInfo.id == myUserId ||
        userInfo.parallelParentId == myUserId ||
        (userInfo.profileGroupId == myUserId &&
            !(UserManager::class.java.getDeclaredMethod(
                "isQuietModeEnabled", UserHandle::class.java
            ).invoke(this, userInfo.userHandle) as Boolean))
}

fun UserManager.isSidebarUserAllowed(userId: Int): Boolean =
    isSidebarUserAllowed(
        UserManager::class.java.getDeclaredMethod(
            "getUserInfo", Integer.TYPE
        ).invoke(this, userId) as UserInfo
    )
