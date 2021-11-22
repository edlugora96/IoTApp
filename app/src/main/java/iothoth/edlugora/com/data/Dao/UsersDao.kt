package iothoth.edlugora.com.data.Dao

import androidx.room.*
import iothoth.edlugora.com.data.model.Users
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(users: Users)

    @Update()
    suspend fun update(users: Users)

    @Query("SELECT * from users WHERE type = 'user' LIMIT 1")
    fun getUser(): Flow<Users>

    @Query("SELECT * from users WHERE type = 'gadget' LIMIT 1")
    fun getGadget(): Flow<Users>

    @Query("SELECT * from users WHERE type = 'user'")
    fun getUsers(): Flow<List<Users>>

    @Query("SELECT * from users WHERE type = 'gadget'")
    fun getGadgets(): Flow<List<Users>>
}