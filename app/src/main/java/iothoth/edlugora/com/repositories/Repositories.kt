package iothoth.edlugora.com.repositories

import android.app.Activity
import iothoth.edlugora.com.domain.Gadget
import iothoth.edlugora.com.domain.RequestApi
import iothoth.edlugora.com.domain.ResponseApi
import iothoth.edlugora.com.domain.User
import kotlinx.coroutines.flow.Flow

class GadgetRepository(
    private val localGadgetDataSource: LocalGadgetDataSource,
    private val remoteGadgetDataSource: RemoteGadgetDataSource
) {
    suspend fun testGadgetConnection(baseUrl:String, url: String): ResponseApi =
        remoteGadgetDataSource.testGadgetConnection(baseUrl, url)

    suspend fun triggerGadgetAction(baseUrl:String, url: String, data: RequestApi): ResponseApi =
        remoteGadgetDataSource.triggerGadgetAction(baseUrl, url, data)

    suspend fun insertGadget(gadget: Gadget) = localGadgetDataSource.insertGadget(gadget)
    suspend fun updateGadget(gadget: Gadget) = localGadgetDataSource.updateGadget(gadget)
    suspend fun deleteGadget(gadget: Gadget) = localGadgetDataSource.deleteGadget(gadget)
    fun getGadget(id: String): Flow<Gadget> = localGadgetDataSource.getGadget(id)
    fun getAllGadgets(): Flow<List<Gadget>> = localGadgetDataSource.getAllGadgets()
}

class UserInfoRepository(
    private val userInfoDataSource: SharedUserInfoDataSource
) {
    fun setUserInfo(activity: Activity?, data: User?): Boolean =
        userInfoDataSource.setUserInfo(activity, data)

    fun updateUserInfo(activity: Activity?, data: User?): Boolean =
        userInfoDataSource.updateUserInfo(activity, data)

    fun getUserInfo(activity: Activity?): User? = userInfoDataSource.getUserInfo(activity)
}