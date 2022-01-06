package iothoth.edlugora.databasemanager

import androidx.room.ColumnInfo
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import iothoth.edlugora.domain.Action
import iothoth.edlugora.domain.Value

@Entity(tableName = "gadgets")
data class GadgetsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "u_id")
    val unitId: String?,
    val name: String,
    val icon: String?,
    val actions: List<Action>?,
    val values: List<Value>?,
    @ColumnInfo(name = "ip_address")
    val ipAddress: String,
    val ssid: String,
    @ColumnInfo(name = "ssid_password")
    val ssidPassword: String,
    @ColumnInfo(name = "wifi_ownership")
    val wifiOwnership: String,
    val location: String?,
    val coordinates: String?,
    @ColumnInfo(name = "setup_info")
    val setupInfo: String?,
    val type: String?,
    val status: String?,
    val showing: Int? = 1
)
