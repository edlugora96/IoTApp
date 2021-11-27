package iothoth.edlugora.com.database

import iothoth.edlugora.com.domain.Gadget
import iothoth.edlugora.com.repositories.LocalGadgetDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GadgetRoomDataSource(
    database: GadgetsRoomDatabase
) : LocalGadgetDataSource {
    private val gadgetsDao by lazy { database.gadgetDao()}

    override suspend fun insertGadget(gadget: Gadget) = gadgetsDao.insertGadget(gadget.toGadgetEntity())
    override suspend fun updateGadget(gadget: Gadget) = gadgetsDao.updateGadget(gadget.toGadgetEntity())
    override suspend fun deleteGadget(gadget: Gadget) = gadgetsDao.deleteGadget(gadget.toGadgetEntity())
    override fun getGadget(id: String): kotlinx.coroutines.flow.Flow<Gadget> = gadgetsDao.getGadget(id).map(GadgetsEntity::toGadgetDomain)
    override fun getAllGadgets(): Flow<List<Gadget>> = gadgetsDao.getAllGadgets().map(List<GadgetsEntity>::toGadgetDomainList)
}