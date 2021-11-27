package iothoth.edlugora.com.database

import androidx.room.*
import iothoth.edlugora.com.domain.Gadget
import kotlinx.coroutines.flow.Flow

@Dao
interface GadgetsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGadget(gadget: GadgetsEntity)

    @Update
    suspend fun updateGadget(gadget: GadgetsEntity)

    @Delete
    suspend fun deleteGadget(gadget: GadgetsEntity)

    @Query("SELECT * from gadgets WHERE id = :id")
    fun getGadget(id: String): Flow<GadgetsEntity>

    @Query("SELECT * from gadgets")
    fun getAllGadgets(): Flow<List<GadgetsEntity>>
}