package iothoth.edlugora.com.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class Users(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "first_conf")
    val firstConf : Int,
    @ColumnInfo(name = "host_ip")
    val host : String,
    @NonNull
    val type : String
)
