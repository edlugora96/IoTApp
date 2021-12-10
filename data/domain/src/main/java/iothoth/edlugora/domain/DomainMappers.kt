package iothoth.edlugora.domain

import androidx.lifecycle.MutableLiveData

fun User.toUpdateUser() = UpdateUser(
    name, icon, firstStep, startScreen, pinedGadget, pinedLocation, lastGadgetAdded
)

fun UpdateUser.toUser() = User(
    name = name ?: "",
    icon = icon ?: "",
    firstStep = firstStep ?: true,
    startScreen = startScreen ?: "",
    pinedGadget = pinedGadget ?: "",
    pinedLocation = pinedLocation ?: "",
    lastGadgetAdded = lastGadgetAdded ?: 0
)


fun <T> MutableLiveData<T>.emptyGadget(): Gadget {
    return Gadget(
        id = 0,
        unitId = null,
        name = "",
        icon = "",
        ipAddress = "",
        ssid = "",
        ssidPassword = "",
        wifiOwnership = "",
        location = "",
        coordinates = "",
        setupInfo = "",
        type = "",
        actions = emptyList(),
        values = emptyList(),
        status = ""
    )
}