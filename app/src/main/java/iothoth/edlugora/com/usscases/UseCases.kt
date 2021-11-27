package iothoth.edlugora.com.usscases

import android.app.Activity
import iothoth.edlugora.com.domain.Gadget
import iothoth.edlugora.com.domain.RequestApi
import iothoth.edlugora.com.domain.ResponseApi
import iothoth.edlugora.com.domain.User
import iothoth.edlugora.com.repositories.GadgetRepository
import iothoth.edlugora.com.repositories.UserInfoRepository
import kotlinx.coroutines.flow.Flow

//region User Indo
class GetUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity): User? = userInfoRepository.getUserInfo(activity)
}

class SetUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity?, data: User?): Boolean =
        userInfoRepository.setUserInfo(activity, data)
}

class UpdateUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity?, data: User?): Boolean =
        userInfoRepository.updateUserInfo(activity, data)
}
//endregion

//region Gadgets
class GetGadget(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(id: String): Flow<Gadget> = gadgetRepository.getGadget(id)
}

class GetAllGadgets(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(): Flow<List<Gadget>> = gadgetRepository.getAllGadgets()
}

class TriggerGadgetAction(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(baseUrl: String, url: String, data: RequestApi): ResponseApi =
        gadgetRepository.triggerGadgetAction(baseUrl, url, data)
}

class TestGadgetConnection(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(baseUrl: String, url: String): ResponseApi =
        gadgetRepository.testGadgetConnection(baseUrl, url)
}

class InsertGadget(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(gadget: Gadget) = gadgetRepository.insertGadget(gadget)
}

class UpdateGadget(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(gadget: Gadget) = gadgetRepository.updateGadget(gadget)
}

class DeleteGadget(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(gadget: Gadget) = gadgetRepository.deleteGadget(gadget)
}
//endregion