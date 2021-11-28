package iothoth.edlugora.userpreferencesmanager

import android.app.Activity
import android.content.Context
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.domain.User
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.FIRST_STEP
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.ICON
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.LAST_GADGET_ADDED
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.NAME
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.PINED_GADGET
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.PINED_LOCATION
import iothoth.edlugora.userpreferencesmanager.SharePreferencesConstants.START_SCREEN

class UserInfo {
    private fun stringSharedPref(activity:Activity, entity: String) : String =
        activity.getPreferences(Context.MODE_PRIVATE)
            .getString(entity, "") ?: ""
    private fun booleanSharedPref(activity:Activity, entity: String) : Boolean =
        activity.getPreferences(Context.MODE_PRIVATE)
            .getBoolean(entity, true)
    private fun intSharedPref(activity:Activity, entity: String) : Int =
        activity.getPreferences(Context.MODE_PRIVATE)
            .getInt(entity.toString(), 0)

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
            putInt(LAST_GADGET_ADDED, data.lastGadgetAdded)
            commit()
        }
        return true
    }

    fun updateUserInfo(activity: Activity, data: UpdateUser?): Boolean {
        if (data == null) return false

        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            if (data.name != null) putString(NAME, data.name)
            if (data.icon != null) putString(ICON, data.icon)
            if (data.pinedGadget != null) putString(PINED_GADGET, data.pinedGadget)
            if (data.pinedLocation != null) putString(PINED_LOCATION, data.pinedLocation)
            if (data.startScreen != null) putString(START_SCREEN, data.startScreen)
            if (data.firstStep != null) putBoolean(FIRST_STEP, data.firstStep!!)
            if (data.lastGadgetAdded != null) putInt(LAST_GADGET_ADDED, data.lastGadgetAdded!!)
            commit()
        }
        return true
    }

    fun getUserInfo(activity: Activity): User =
        User(
            name = stringSharedPref(activity, NAME),
            icon = stringSharedPref(activity, ICON),
            firstStep = booleanSharedPref(activity, FIRST_STEP),
            pinedGadget = stringSharedPref(activity, PINED_GADGET),
            pinedLocation = stringSharedPref(activity, PINED_LOCATION),
            startScreen = stringSharedPref(activity, START_SCREEN),
            lastGadgetAdded = intSharedPref(activity, LAST_GADGET_ADDED)
        )


}