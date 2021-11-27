package iothoth.edlugora.com

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import iothoth.edlugora.com.database.GadgetsRoomDatabase

@HiltAndroidApp
class IoThothApplication: Application() {
    val database: GadgetsRoomDatabase by lazy { GadgetsRoomDatabase.getDatabase(this) }
}