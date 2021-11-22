package iothoth.edlugora.com.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import iothoth.edlugora.com.data.Dao.UsersDao
import iothoth.edlugora.com.data.model.Users

@Database(entities = [Users::class], version = 1, exportSchema = false)
abstract class UsersRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UsersDao
    companion object {
        @Volatile
        private var INSTANCE: UsersRoomDatabase? = null

        fun getDatabase(
            context: Context
        ): UsersRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UsersRoomDatabase::class.java,
                    "app_database"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

