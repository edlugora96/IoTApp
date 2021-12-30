package iothoth.edlugora.com.viewModel

import androidx.lifecycle.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetectNetworkViewModel(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
    private val getGadget: GetGadget,
    private val updateGadget: UpdateGadget,
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events

    sealed class UiReactions {
        data class ShowSuccessSnackBar(val message: String) : UiReactions()
        data class ShowSuccessTest(val message: String, val wifi: String) : UiReactions()
        data class ShowErrorSnackBar(val message: String) : UiReactions()
        data class ShowErrorTest(val message: String) : UiReactions()
        data class ShowWarningSnackBar(val message: String) : UiReactions()
        data class ShowWarningTest(val message: String) : UiReactions()
    }

    fun gadget(id: Int): LiveData<Gadget> = getGadget.invoke(id).asLiveData()

    fun updateGadget(gadget: Gadget): Job {
        return viewModelScope.launch { updateGadget.invoke(gadget) }

    }


    fun testConnection(baseUrl: String, url: String) {
        viewModelScope.launch {
            val res = testGadgetConnection.invoke(baseUrl, url)

            when (res.code.toInt()) {
                in 200..299 -> _events.value =
                    Event(UiReactions.ShowSuccessTest(res.data.toString(), res.wifi.toString()))
                in 400..499 -> _events.value =
                    Event(UiReactions.ShowWarningTest(res.data.toString()))
                else -> _events.value = Event(UiReactions.ShowErrorTest(res.error.toString()))
            }
        }
    }

    fun gadgetDoAction(
        baseUrl: String,
        url: String,
        data: RequestApi
    ) {
        viewModelScope.launch {

            val res = triggerGadgetAction.invoke(baseUrl, url, data)

            when (res.code.toInt()) {
                in 200..299 -> _events.value =
                    Event(UiReactions.ShowSuccessSnackBar(res.data.toString()))
                in 400..499 -> _events.value =
                    Event(UiReactions.ShowWarningSnackBar(res.data.toString()))
                else -> _events.value = Event(UiReactions.ShowErrorSnackBar(res.error.toString()))
            }

        }


    }

}