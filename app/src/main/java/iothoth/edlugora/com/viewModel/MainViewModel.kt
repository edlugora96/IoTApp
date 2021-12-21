package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import iothoth.edlugora.domain.User
import iothoth.edlugora.usecases.CountAllGadgets
import iothoth.edlugora.usecases.GetUserInfo

class MainViewModel(
    private val getUserInfo: GetUserInfo,
    private val countAllGadgets: CountAllGadgets,
) : ViewModel() {
    fun getUser(activity: Activity): LiveData<User?> = MutableLiveData(getUserInfo.invoke(activity))
    val countOfAllGadgets = countAllGadgets.invoke().asLiveData()
}