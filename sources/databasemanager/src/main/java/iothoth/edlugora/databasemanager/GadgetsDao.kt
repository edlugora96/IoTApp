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

    @Query("SELECT * FROM gadgets WHERE showing != 0")
    fun getAllGadgets(): Flow<List<GadgetsEntity>>

    @Query("SELECT count(*) FROM gadgets WHERE showing != 0")
    fun countAllGadgets(): Flow<Int>

    @Query("SELECT count(*) FROM gadgets WHERE u_id = :uid AND showing != 0")
    fun isGadgetAdded(uid:String): Flow<Int>

    @Query("SELECT * FROM gadgets WHERE showing != 0 LIMIT 1")
    fun getOneGadget(): Flow<GadgetsEntity>
}