package iothoth.edlugora.userpreferencesmanager

import android.app.Activity
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.domain.User
import iothoth.edlugora.domain.repository.SharedUserInfoDataSource

class UserInfoDataSource(
    private val userInfo: UserInfo
) : SharedUserInfoDataSource {
    override fun setUserInfo(activity: Activity, data: User?): Boolean =
        userInfo.setUserInfo(activity, data)

    override fun updateUserInfo(activity: Activity, data: UpdateUser?): Boolean =
        userInfo.updateUserInfo(activity, data)

    override fun getUserInfo(activity: Activity): User =
        userInfo.getUserInfo(activity)

}