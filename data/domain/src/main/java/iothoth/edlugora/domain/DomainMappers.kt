package iothoth.edlugora.domain

fun User.toUpdateUser() = UpdateUser(
    name, icon, firstStep, startScreen, pinedGadget, pinedLocation, lastGadgetAdded
)

fun UpdateUser.toUser() = User(
    name ?: "",
    icon ?: "",
    firstStep ?: true,
    startScreen ?: "",
    pinedGadget ?: "",
    pinedLocation ?: "",
    lastGadgetAdded ?: 0
)

fun Gadget.emptyGadget() = Gadget(
    id = 0,
    name = "",
    icon = "",
    actions = null,
    values = null,
    ipAddress = "",
    ssid = "",
    ssidPassword = "",
    wifiOwnership = "",
    location = "",
    coordinates = "",
    setupInfo = "",
    type = ""
)