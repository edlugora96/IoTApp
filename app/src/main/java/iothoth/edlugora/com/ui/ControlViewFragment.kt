package iothoth.edlugora.com.ui

import android.content.res.Configuration
import android.net.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.domain.User
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

    private val _gadgetId: MutableLiveData<Int> = MutableLiveData()

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

    private val _gadget: MutableLiveData<Gadget> = MutableLiveData()
    val gadget: LiveData<Gadget> = _gadget

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
    private val getGadget by lazy {
        GetGadget(gadgetRepository)
    }
    private val viewModel: ControlViewModel by lazy {
        ControlViewModel(
            triggerGadgetAction,
            testGadgetConnection,
            getUserInfo,
            getGadget
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        viewModel.checkFirstStep(requireActivity())
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        fillUserAndGadget()
    }

    //region Methods
    private fun fillUserAndGadget() {
        viewModel.getUser(requireActivity()).observe(viewLifecycleOwner) {
            _user.value = it

            _gadgetId.value = if (navigationArg.gadgetId > 0) {
                navigationArg.gadgetId
            } else {
                it?.lastGadgetAdded ?: 0
            }
        }

        _gadgetId.observe(viewLifecycleOwner) { id ->
            run {
                viewModel.gadget(id).observe(viewLifecycleOwner) {
                    _gadget.value = it
                }
            }
        }
    }

    fun action(act: Char) {

        gadget.value.let {
            if (it != null) {
                viewModel.gadgetDoAction(it.ipAddress, "/action", RequestApi(data = act.toString()))
            }
        }
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
            Configuration.UI_MODE_NIGHT_NO -> {
                requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.white)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                requireActivity().window.statusBarColor =
                    getColor(requireActivity(), R.color.gray_background)
            }
        }

    }

    fun goToProfileView() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToProfileViewFragment(
            _gadgetId.value ?: 0
        )
        findNavController().navigate(action)
    }

    private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is ShowErrorSnackBar -> reaction.run {
                    requireActivity().showLongSnackBar(
                        R.id.root_activity,
                        this.message,
                        getColor(requireContext(), R.color.error)
                    )
                }
                is ShowSuccessSnackBar -> reaction.run {
                    requireActivity().showLongSnackBar(
                        R.id.root_activity,
                        this.message,
                        getColor(requireContext(), R.color.success)
                    )
                }
                is ShowWarningSnackBar -> reaction.run {
                    requireActivity().showLongSnackBar(
                        R.id.root_activity,
                        this.message,
                        getColor(requireContext(), R.color.warning)
                    )
                }
                else -> goToProfileView()
            }

        }

    }
    //endregion
}