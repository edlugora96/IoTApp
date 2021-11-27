package iothoth.edlugora.com.domain

data class User(
    val name: String,
    val icon : String? = null,
    val firstStep : Boolean = true,
    val startScreen : String? = null,
    val pinedGadget : String? = null,
    val pinedLocation : String? = null
)