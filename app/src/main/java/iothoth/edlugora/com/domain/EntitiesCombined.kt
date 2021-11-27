package iothoth.edlugora.com.domain

import iothoth.edlugora.com.domain.Gadget
import iothoth.edlugora.com.domain.User

data class EntitiesCombined(
    val user : User,
    val gadget : Gadget
)
