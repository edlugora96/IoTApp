package iothoth.edlugora.domain

data class Gadget(
    val id: Int,
    val unitId: String?,
    val name: String,
    val icon: String?,
    val actions: List<Action>?,
    val values: List<Value>?,
    val ipAddress: String,
    val ssid: String,
    val ssidPassword: String,
    val wifiOwnership: String,
    val location: String?,
    val coordinates: String?,
    val setupInfo: String?,
    val type: String?,
    val status: String?,
    val showing: Int?
)

data class Value(
    val icon: String,
    val name: String,
    val type: String,
    val value: String,
    val url: String
)

data class Action(
    val icon: String,
    val name: String,
    val type: String,
    val value: String,
    val url: String
)


