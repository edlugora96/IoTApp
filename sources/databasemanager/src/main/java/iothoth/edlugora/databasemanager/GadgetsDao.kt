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

    @Query("SELECT * from gadgets WHERE id = :id")
    fun getGadget(id: Int): Flow<GadgetsEntity>

    @Query("SELECT * from gadgets")
    fun getAllGadgets(): Flow<List<GadgetsEntity>>
}