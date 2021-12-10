package iothoth.edlugora.databasemanager

import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GadgetRoomDataSource(
    database: GadgetsRoomDatabase
) : LocalGadgetDataSource {
    private val gadgetsDao by lazy { database.gadgetDao() }

    override suspend fun insertGadget(gadget: Gadget): Long =
        gadgetsDao.insertGadget(gadget.toGadgetEntity())

    override suspend fun updateGadget(gadget: Gadget) =
        gadgetsDao.updateGadget(gadget.toGadgetEntity())

    override suspend fun deleteGadget(gadget: Gadget) =
        gadgetsDao.deleteGadget(gadget.toGadgetEntity())

    override fun getGadget(id: Int): Flow<Gadget> =
        gadgetsDao.getGadget(id).map(GadgetsEntity::toGadgetDomain)

    override fun getAllGadgets(): Flow<List<Gadget>> =
        gadgetsDao.getAllGadgets().map(List<GadgetsEntity>::toGadgetDomainList)
}

