package iothoth.edlugora.com.repositories

import android.app.Activity
import iothoth.edlugora.com.domain.Gadget
import iothoth.edlugora.com.domain.RequestApi
import iothoth.edlugora.com.domain.ResponseApi
import iothoth.edlugora.com.domain.User
import kotlinx.coroutines.flow.Flow

interface RemoteGadgetDataSource {
    suspend fun testGadgetConnection(baseUrl: String, url: String): ResponseApi
    suspend fun triggerGadgetAction(baseUrl: String, url: String, data: RequestApi): ResponseApi
}

interface SharedUserInfoDataSource {
    fun setUserInfo(activity: Activity?, data: User?): Boolean
    fun updateUserInfo(activity: Activity?, data: User?): Boolean
    fun getUserInfo(activity: Activity?): User?
}

interface LocalGadgetDataSource {
    suspend fun insertGadget(gadget: Gadget)
    suspend fun updateGadget(gadget: Gadget)
    suspend fun deleteGadget(gadget: Gadget)
    fun getGadget(id: String): Flow<Gadget>
    fun getAllGadgets(): Flow<List<Gadget>>
}