package iothoth.edlugora.com.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentControlViewBinding
import iothoth.edlugora.com.utils.showLongSnackBar
import iothoth.edlugora.com.viewModel.ControlViewModel
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource
import java.util.*

class ControlViewFragment : Fragment() {
    private lateinit var binding: FragmentControlViewBinding
    private val navigationArg: ControlViewFragmentArgs by navArgs()
    private val _gadget : MutableLiveData<iothoth.edlugora.domain.Gadget> = MutableLiveData()
    //val gadget : LiveData<iothoth.edlugora.domain.Gadget> = _gadget
    //private var isSent = false

    //region ViewModel Declaration
    private val database: GadgetsRoomDatabase by lazy {
        (activity?.application as IoThothApplication).database
    }
    private val localGadgetDataSource: LocalGadgetDataSource by lazy {
        GadgetRoomDataSource(database)
    }
    private val gadgetRequest = GadgetRequest()
    private val remoteGadgetDataSource = GadgetApiDataSource(gadgetRequest)
    private val gadgetRepository by lazy {
        GadgetRepository(localGadgetDataSource, remoteGadgetDataSource)
    }
    private val userInfo = UserInfo()
    private val shareUserInfoDataSource =
        UserInfoDataSource(userInfo)

    private val userInfoRepository = UserInfoRepository(shareUserInfoDataSource)

    private val getUserInfo = GetUserInfo(userInfoRepository)
    private val triggerGadgetAction by lazy {
        TriggerGadgetAction(gadgetRepository)
    }
    private val testGadgetConnection by lazy {
        TestGadgetConnection(gadgetRepository)
    }
    private val viewModel: ControlViewModel by lazy {
        ControlViewModel(
            triggerGadgetAction,
            testGadgetConnection,
            getUserInfo
        )
    }
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        changeColorStatusBar()
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_control_view, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        viewModel.checkFirstStep(requireActivity())
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
    }

    //region Methods
    /*fun reload() {
        binding.progressBar.visibility = View.VISIBLE
        Timer().schedule(2000) {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.GONE
            }
        }
    }*/

    /*private fun preventReSend(it: GadgetsEntity, action: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            if (!isSent) {
                binding.powerIcon.isClickable = false
                binding.carIcon.isClickable = false
                binding.peopleIcon.isClickable = false
                binding.personIcon.isClickable = false
//                handlerSnackBar(viewModel.sendActionToApi(it, action))
                isSent = true
                Timer().schedule(2000) {
                    lifecycleScope.launch {
                        binding.progressBar.visibility = View.GONE
                        isSent = false
                        binding.powerIcon.isClickable = true
                        binding.carIcon.isClickable = true
                        binding.peopleIcon.isClickable = true
                        binding.personIcon.isClickable = true
                    }
                }
            }
        }
    }*/


    fun doActionCar() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "A")
        }*/
    }

    fun doActionPerson() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "P")

        }*/
    }

    fun doActionPeople() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "M")

        }*/
    }

    fun doActionCommon() {
        requireActivity().showLongSnackBar(R.id.root_activity, "Hello", getColor(requireContext(), R.color.success))
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "C")
        }*/
    }

    /*private fun handlerSnackBar(res: ResponseApi) {
        if (!res.data.isNullOrEmpty()) {
            showSnackBarSuccess(res.data)
            if (res.data == "Busy") {
                showSnackBarWarning(res.data)
            }
        } else {
            showSnackBarError(res.error ?: "Net Error")
        }
    }*/

    private fun showSnackBarSuccess(text: String) {
        showSnackBar(text).setBackgroundTint(getColor(requireContext(), R.color.success)).show()
    }

    private fun showSnackBarWarning(text: String) {
        showSnackBar(text).setBackgroundTint( getColor(requireContext(), R.color.warning)).show()
    }

    private fun showSnackBarError(text: String) {
        showSnackBar(text).setBackgroundTint(getColor(requireContext(), R.color.error)).show()
    }

    private fun showSnackBar(text: String): Snackbar {
        return Snackbar.make(
            requireActivity().findViewById(R.id.root_activity),
            text, Snackbar.LENGTH_LONG
        )
    }

    private fun bind() {
        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this@ControlViewFragment
            it.lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun changeColorStatusBar() {

        when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.white)}
            Configuration.UI_MODE_NIGHT_YES -> {requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.gray_background)}
        }

    }

    fun goToProfileView() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToProfileViewFragment(_gadget.value?.id ?: 0)
        findNavController().navigate(action)
    }

    private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is ShowErrorSnackBar -> reaction.run {
                    showSnackBarError(this.message)
                }
                is ShowSuccessSnackBar -> reaction.run {
                    showSnackBarSuccess(this.message)
                }
                is ShowWarningSnackBar -> reaction.run {
                    showSnackBarWarning(this.message)
                }
                else -> {}
            }

        }

    }
    //endregion
}