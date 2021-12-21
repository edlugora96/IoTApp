package iothoth.edlugora.repository

import android.app.Activity
import android.util.Log
import iothoth.edlugora.domain.*
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.domain.repository.RemoteGadgetDataSource
import iothoth.edlugora.domain.repository.SharedUserInfoDataSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class GadgetRepository(
    private val localGadgetDataSource: LocalGadgetDataSource,
    private val remoteGadgetDataSource: RemoteGadgetDataSource
) {
    suspend fun testGadgetConnection(baseUrl:String, url: String): ResponseApi =
        remoteGadgetDataSource.testGadgetConnection(baseUrl, url)

    suspend fun triggerGadgetAction(baseUrl:String, url: String, data: RequestApi): ResponseApi =
        remoteGadgetDataSource.triggerGadgetAction(baseUrl, url, data)

    suspend fun insertGadget(gadget: Gadget) : Long = localGadgetDataSource.insertGadget(gadget)
    suspend fun updateGadget(gadget: Gadget) = localGadgetDataSource.updateGadget(gadget)
    suspend fun deleteGadget(gadget: Gadget) = localGadgetDataSource.deleteGadget(gadget)
    fun getGadget(id: Int): Flow<Gadget> = localGadgetDataSource.getGadget(id)
    fun getAllGadgets(): Flow<List<Gadget>> = localGadgetDataSource.getAllGadgets()
    fun countAllGadgets(): Flow<Int> = localGadgetDataSource.countAllGadgets()
    fun getOneGadget(): Flow<Gadget> = localGadgetDataSource.getOneGadget()
}

class UserInfoRepository(
    private val userInfoDataSource: SharedUserInfoDataSource
) {
    fun setUserInfo(activity: Activity, data: User?): Boolean =
        userInfoDataSource.setUserInfo(activity, data)

    fun updateUserInfo(activity: Activity, data: UpdateUser?): Boolean =
        userInfoDataSource.updateUserInfo(activity, data)

    fun getUserInfo(activity: Activity): User = userInfoDataSource.getUserInfo(activity)
}