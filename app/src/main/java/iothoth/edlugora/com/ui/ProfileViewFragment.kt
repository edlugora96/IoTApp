package iothoth.edlugora.com.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentProfileViewBinding
import iothoth.edlugora.com.utils.showLongToast
import iothoth.edlugora.com.viewModel.ProfileViewModel
import iothoth.edlugora.com.viewModel.ProfileViewModel.UiReactions
import iothoth.edlugora.com.viewModel.ProfileViewModel.UiReactions.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource
import kotlinx.coroutines.launch


class ProfileViewFragment : Fragment() {

    private lateinit var binding: FragmentProfileViewBinding
    private val navigationArg: ProfileViewFragmentArgs by navArgs()
    private var _gadgetId = MutableLiveData<String>()
    val gadgetId: LiveData<String> = _gadgetId

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
    private val setUserInfo = SetUserInfo(userInfoRepository)

    private val insertGadget by lazy {
        InsertGadget(gadgetRepository)
    }
    private val updateGadget by lazy {
        UpdateGadget(gadgetRepository)
    }
    private val updateUserInfo by lazy {
        UpdateUserInfo(userInfoRepository)
    }
    private val getGadget by lazy {
        GetGadget(gadgetRepository)
    }

    private val viewModel: ProfileViewModel by lazy {
        ProfileViewModel(
            setUserInfo,
            insertGadget,
            updateGadget,
            updateUserInfo,
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile_view, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        _gadgetId.value = navigationArg.gadgatId.toString()
        viewModel.getUser(requireActivity()).observe(viewLifecycleOwner){
            binding.temporalTexView.text = it?.lastGadgetAdded.toString() ?: "0"
        }
        bind()
    }

    //region Methods
    private fun bind() {
        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this@ProfileViewFragment
            it.lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun changeColorStatusBar() {
        requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.blue)
    }

    private fun getInputsValueForUser(): iothoth.edlugora.domain.User =
        iothoth.edlugora.domain.User(
            name = binding.userNameInput.text.toString(),
            firstStep = false,
        )

    private fun getInputsValueForGadget(): iothoth.edlugora.domain.Gadget =
        iothoth.edlugora.domain.Gadget(
            id = navigationArg.gadgatId,
            name = binding.gadgetNameInput.text.toString(),
            ipAddress = binding.ipAddressInput.text.toString(),
            ssid = "",
            ssidPassword = "",
            wifiOwnership = "Tukum"
        )

    fun sendForm() {
        lifecycleScope.launch {
            viewModel.sendForm(
                requireActivity(),
                getInputsValueForUser(),
                getInputsValueForGadget()
            )
        }
    }


    fun goControlView() {
        val id = if (navigationArg.gadgatId > 0) {
            navigationArg.gadgatId
        } else {
            _gadgetId.value?.toInt() ?: 0
        }
        val action =
            ProfileViewFragmentDirections.actionProfileViewFragmentToControlViewFragment(id)
        findNavController().navigate(action)
    }

    private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is ShowToast -> reaction.run {
                    requireContext().showLongToast(this.message)
                }
                is IdInsertedGadget -> reaction.run {
                    _gadgetId.value = this.id.toString()
                    viewModel.updateUserInfo(requireActivity(), UpdateUser(
                        lastGadgetAdded = this.id.toInt()
                    ))
                }
                else -> goControlView()
            }

        }

    }
    //endregion
}


