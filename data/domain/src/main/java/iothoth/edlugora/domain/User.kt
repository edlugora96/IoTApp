package iothoth.edlugora.domain

data class User(
    val name: String,
    val icon: String? = null,
    val firstStep: Boolean = true,
    val startScreen: String? = null,
    val pinedGadget: String? = null,
    val pinedLocation: String? = null,
    val lastGadgetAdded: Int = 0
)

data class UpdateUser(
    val name: String? = null,
    val icon: String? = null,
    val firstStep: Boolean? = null,
    val startScreen: String? = null,
    val pinedGadget: String? = null,
    val pinedLocation: String? = null,
    val lastGadgetAdded: Int? = null
)

// Mappers
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