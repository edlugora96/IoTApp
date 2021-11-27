package iothoth.edlugora.com.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GadgetsEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GadgetsRoomDatabase : RoomDatabase() {
    abstract fun gadgetDao(): GadgetsDao
    companion object {
        @Volatile
        private var INSTANCE: GadgetsRoomDatabase? = null


        fun getDatabase(
            context: Context
        ): GadgetsRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GadgetsRoomDatabase::class.java,
                    "gadgets_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

