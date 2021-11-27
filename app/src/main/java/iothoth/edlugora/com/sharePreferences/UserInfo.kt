package iothoth.edlugora.com.sharePreferences

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import iothoth.edlugora.com.R
import iothoth.edlugora.com.domain.User
import iothoth.edlugora.com.sharePreferences.SharePreferencesConstants.FIRST_STEP
import iothoth.edlugora.com.sharePreferences.SharePreferencesConstants.ICON
import iothoth.edlugora.com.sharePreferences.SharePreferencesConstants.NAME
import iothoth.edlugora.com.sharePreferences.SharePreferencesConstants.PINED_GADGET
import iothoth.edlugora.com.sharePreferences.SharePreferencesConstants.PINED_LOCATION
import iothoth.edlugora.com.sharePreferences.SharePreferencesConstants.START_SCREEN

class UserInfo {

    fun setUserInfo(activity: Activity, data: User?): Boolean {
        if (data == null) return false

        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(FIRST_STEP, data.firstStep)
            putString(ICON, data.icon)
            putString(NAME, data.name)
            putString(PINED_GADGET, data.pinedGadget)
            putString(PINED_LOCATION, data.pinedLocation)
            putString(START_SCREEN, data.startScreen)
            commit()
        }
        return true
    }

    fun updateUserInfo(activity: Activity, data: User?): Boolean {
        if (data == null) return false

        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(NAME, data.name)
            putBoolean(FIRST_STEP, data.firstStep)
            if (data.icon != null) putString(ICON, data.icon)
            if (data.pinedGadget != null) putString(PINED_GADGET, data.pinedGadget)
            if (data.pinedLocation != null) putString(PINED_LOCATION, data.pinedLocation)
            if (data.startScreen != null) putString(START_SCREEN, data.startScreen)
            commit()
        }
        return true
    }

    fun getUserInfo(activity: Activity): User {
        fun sharedPref(entity: String) = activity.getSharedPreferences(entity, Context.MODE_PRIVATE)
            .toString()

        return User(
            name = sharedPref(NAME),
            icon = sharedPref(ICON),
            firstStep = sharedPref(FIRST_STEP) == "true",
            pinedGadget = sharedPref(PINED_GADGET),
            pinedLocation = sharedPref(PINED_LOCATION),
            startScreen = sharedPref(START_SCREEN)
        )
    }

}