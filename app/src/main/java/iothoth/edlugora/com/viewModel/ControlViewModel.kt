package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions.*
import iothoth.edlugora.domain.*
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class ControlViewModel(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
    private val getUserInfo: GetUserInfo,
    private val getGadget: GetGadget,
    private val updateGadget: UpdateGadget,
    private val deleteGadget: DeleteGadget,
    private val countAllGadgets: CountAllGadgets,

    ) : ViewModel() {
    //region Utils declarations
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events
    private val _gadget = MutableLiveData<Gadget>()

    sealed class UiReactions {
        object NavToProfile : UiReactions()
        data class ShowSuccessSnackBar(val message: String) : UiReactions()
        data class ShowErrorSnackBar(val message: String) : UiReactions()
        data class ShowWarningSnackBar(val message: String) : UiReactions()
        data class ShowSuccessConnection(val message: String) : UiReactions()
        data class ShowErrorConnection(val message: String) : UiReactions()
        data class ShowWarningConnection(val message: String) : UiReactions()
    }
    //endregion

    //region Get gadget and user information
    fun gadget(id: Int): LiveData<Gadget> = try {
        getGadget.invoke(id).asLiveData()
    } catch (ex: NullPointerException) {
        MutableLiveData(emptyGadget())
    }

    fun getUser(activity: Activity): LiveData<User> = MutableLiveData(getUserInfo.invoke(activity))

    fun deleteGadget(gadget: Gadget) {
        viewModelScope.launch { deleteGadget.invoke(gadget) }
    }
    //endregion


    //region Control handler
    val countOfAllGadgets = countAllGadgets.invoke().asLiveData()

    private suspend fun testConnection(baseUrl: String, url: String) {
        viewModelScope.launch {

            val res = testGadgetConnection.invoke(baseUrl, url)


            when (res.code.toInt()) {
                in 200..299 -> _events.value = Event(ShowSuccessConnection(res.data.toString()))
                in 400..499 -> _events.value = Event(ShowErrorConnection(res.data.toString()))
                else -> _events.value = Event(ShowWarningConnection(res.error.toString()))
            }

        }.join()

    }

    fun startConnection(baseUrl: String, url: String) : LiveData<ResponseApi> {
        return flow {
            while (true) {
                val res = testGadgetConnection.invoke(baseUrl, url)
                emit(res)
                delay(5000)
            }
        }.asLiveData()
    }

    suspend fun updateGadget(gadget: Gadget): Job {
        return viewModelScope.launch { updateGadget.invoke(gadget) }
    }

    fun gadgetDoAction(
        baseUrl: String,
        url: String,
        data: RequestApi
    ) {
        _loading.value = true
        viewModelScope.launch {
            viewModelScope.launch {

                val res = triggerGadgetAction.invoke(baseUrl, url, data)


                when (res.code.toInt()) {
                    in 200..299 -> _events.value = Event(ShowSuccessSnackBar(res.data.toString()))
                    in 400..499 -> _events.value = Event(ShowWarningSnackBar(res.data.toString()))
                    else -> _events.value = Event(ShowErrorSnackBar(res.error.toString()))
                }

            }.join()
            Timer().schedule(2000) {
                viewModelScope.launch {
                    _loading.value = false
                }
            }
        }

    }
    //endregion

    //region Other methods
    fun checkFirstStep(activity: Activity) {
        if (getUser(activity).value?.firstStep == true) {
            _events.value = Event(NavToProfile)
        }
    }
    //endregion
}