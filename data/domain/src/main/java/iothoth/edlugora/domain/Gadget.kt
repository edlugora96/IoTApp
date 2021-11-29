package iothoth.edlugora.domain

data class Gadget(
    val id: Int = 0,
    val name: String,
    val icon: String? = null,
    val actions: List<Action>? = null,
    val values: List<Value>? = null,
    val ipAddress: String,
    val ssid: String,
    val ssidPassword: String,
    val wifiOwnership: String,
    val location: String? = null,
    val coordinates: String? = null,
    val setupInfo: String? = null,
    val type: String? = null
)

data class Value(
    val name: String,
    val value: String
)

data class Action(
    val icon: String,
    val name: String,
    val type: String,
    val value: String,
    val url: String
)