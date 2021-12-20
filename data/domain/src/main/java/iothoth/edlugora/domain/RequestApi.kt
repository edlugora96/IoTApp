package iothoth.edlugora.domain

data class RequestApi(
    val data : String? = null,
    val feed : RequestApiFeed? = null
)

data class RequestApiFeed(
    val ssid: String,
    val pass: String
)