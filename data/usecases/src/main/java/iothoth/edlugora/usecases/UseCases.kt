package iothoth.edlugora.usecases

import android.app.Activity
import iothoth.edlugora.domain.*
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
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
    fun invoke(activity: Activity, data: User?): Boolean =
        userInfoRepository.setUserInfo(activity, data)
}

class UpdateUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity, data: UpdateUser?): Boolean =
        userInfoRepository.updateUserInfo(activity, data)
}
//endregion

//region Gadgets
class GetGadget(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(id: Int): Flow<Gadget> = gadgetRepository.getGadget(id)
}

class GetAllGadgets(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(): Flow<List<Gadget>> = gadgetRepository.getAllGadgets()
}

class CountAllGadgets(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(): Flow<Int> = gadgetRepository.countAllGadgets()
}
class IsGadgetAdded(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(uid:String): Flow<Int> = gadgetRepository.isGadgetAdded(uid)
}

class GetOneGadget(
    private val gadgetRepository: GadgetRepository
){
    fun invoke(): Flow<Gadget> = gadgetRepository.getOneGadget()
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
    suspend fun invoke(gadget: Gadget) : Long = gadgetRepository.insertGadget(gadget)
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