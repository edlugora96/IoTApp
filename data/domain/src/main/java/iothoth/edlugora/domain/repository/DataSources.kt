package iothoth.edlugora.domain.repository

import android.app.Activity
import iothoth.edlugora.domain.*
import kotlinx.coroutines.flow.Flow

interface RemoteGadgetDataSource {
    suspend fun testGadgetConnection(baseUrl: String, url: String): ResponseApi
    suspend fun triggerGadgetAction(baseUrl: String, url: String, data: RequestApi): ResponseApi
}

interface SharedUserInfoDataSource {
    fun setUserInfo(activity: Activity, data: User?): Boolean
    fun updateUserInfo(activity: Activity, data: UpdateUser?): Boolean
    fun getUserInfo(activity: Activity): User
}

interface LocalGadgetDataSource {
    suspend fun insertGadget(gadget: Gadget): Long
    suspend fun updateGadget(gadget: Gadget)
    suspend fun deleteGadget(gadget: Gadget)
    fun getGadget(id: Int): Flow<Gadget>
    fun getAllGadgets(): Flow<List<Gadget>>
}