package iothoth.edlugora.databasemanager

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GadgetsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGadget(gadget: GadgetsEntity) : Long

    @Update
    suspend fun updateGadget(gadget: GadgetsEntity)

    @Delete
    suspend fun deleteGadget(gadget: GadgetsEntity)

    @Query("SELECT * FROM gadgets WHERE id = :id")
    fun getGadget(id: Int): Flow<GadgetsEntity>

    @Query("SELECT * FROM gadgets")
    fun getAllGadgets(): Flow<List<GadgetsEntity>>

    @Query("SELECT count(*) FROM gadgets")
    fun countAllGadgets(): Flow<Int>

    @Query("SELECT count(*) FROM gadgets WHERE u_id = :uid")
    fun isGadgetAdded(uid:String): Flow<Int>

    @Query("SELECT * FROM gadgets LIMIT 1")
    fun getOneGadget(): Flow<GadgetsEntity>
}