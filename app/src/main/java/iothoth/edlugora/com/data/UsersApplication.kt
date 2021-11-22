package iothoth.edlugora.com.data

import android.app.Application

class UsersApplication: Application() {
    val database: UsersRoomDatabase by lazy { UsersRoomDatabase.getDatabase(this) }
}