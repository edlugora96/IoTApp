package iothoth.edlugora.com.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.launch

class DetectNetworkViewModel(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events

    sealed class UiReactions {
        data class ShowSuccessSnackBar(val message: String) : UiReactions()
        data class ShowSuccessTestSnackBar(val message: String) : UiReactions()
        data class ShowErrorSnackBar(val message: String) : UiReactions()
        data class ShowWarningSnackBar(val message: String) : UiReactions()
    }

    fun testConnection(baseUrl: String, url: String) {
        viewModelScope.launch {
            val res = testGadgetConnection.invoke(baseUrl, url)

            when (res.code.toInt()) {
                in 200..299 -> _events.value =
                    Event(UiReactions.ShowSuccessTestSnackBar(res.data.toString()))
                in 400..499 -> _events.value =
                    Event(UiReactions.ShowWarningSnackBar(res.data.toString()))
                else -> _events.value = Event(UiReactions.ShowErrorSnackBar(res.error.toString()))
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