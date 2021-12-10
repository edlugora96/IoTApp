package iothoth.edlugora.com.viewModel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions.*
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.domain.User
import iothoth.edlugora.domain.emptyGadget
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import java.util.concurrent.ScheduledFuture
import kotlin.concurrent.schedule

class ControlViewModel(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
    private val getUserInfo: GetUserInfo,
    private val getGadget: GetGadget,
    private val updateGadget: UpdateGadget,
    private val deleteGadget: DeleteGadget
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
        data class GetUserR(val data: User) : UiReactions()
        data class GetGadgetR(val data: Gadget) : UiReactions()
    }
    //endregion

    //region Get gadget and user information
    fun gadget(id: Int): LiveData<Gadget> {
        return if (id > 0) {
            viewModelScope.launch {
                getGadget.invoke(id).collect {
                    _gadget.value = it
                }
            }
            _gadget
        } else {
            MutableLiveData(_gadget.emptyGadget())
        }

    }

    fun getUser(activity: Activity): LiveData<User?> = MutableLiveData(getUserInfo.invoke(activity))

    fun deleteGadget(gadget: Gadget) {
        viewModelScope.launch { deleteGadget.invoke(gadget) }
    }
    //endregion


    //region Control handler
    suspend fun testConnection(baseUrl: String, url: String) {
        testGadgetConnection.invoke(baseUrl, url)

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