package iothoth.edlugora.com.ui

import android.content.res.Configuration
import android.net.*
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.DialogGadgetProfileBinding
import iothoth.edlugora.com.databinding.FragmentControlViewBinding
import iothoth.edlugora.com.utils.changeColorStatusBar
import iothoth.edlugora.com.utils.showConfirmDialog
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
import iothoth.edlugora.domain.emptyGadget
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource
import kotlinx.coroutines.launch
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
    private val updateGadget by lazy {
        UpdateGadget(gadgetRepository)
    }
    private val deleteGadget by lazy {
        DeleteGadget(gadgetRepository)
    }
    private val viewModel: ControlViewModel by lazy {
        ControlViewModel(
            triggerGadgetAction,
            testGadgetConnection,
            getUserInfo,
            getGadget,
            updateGadget,
            deleteGadget
        )
    }
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext().changeColorStatusBar(
            requireActivity(),
            R.color.white,
            R.color.gray_background
        )
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_control_view, container, false)
        return binding.root
    }

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

        gadget.observe(viewLifecycleOwner) {
            binding.navBar.gadgetName.text = it?.name ?: ""
        }
        user.observe(viewLifecycleOwner) {
            binding.navBar.profileName.text = it?.name ?: ""
        }
        binding.navBar.gadgetName.setOnClickListener { goToProfileView() }
        binding.navBar.profileName.setOnClickListener { goToProfileView() }
        binding.navBar.profilePhotoCard.setOnClickListener { goToProfileView() }
        binding.navBar.menuIcon.setOnClickListener {
            it.isClickable = false
            val dialogGadgetProfile = BottomSheetDialog(requireContext())

            dialogGadgetProfile.setContentView(R.layout.dialog_gadget_profile)

            dialogGadgetProfile.setOnDismissListener { _ ->
                it.isClickable = true
            }

            val gadgetNameInput =
                dialogGadgetProfile.findViewById<TextInputEditText>(R.id.gadget_name_input)
            val save = dialogGadgetProfile.findViewById<LinearLayout>(R.id.save_gadget)
            val wifi = dialogGadgetProfile.findViewById<TextView>(R.id.wifi_ssid)
            val delete = dialogGadgetProfile.findViewById<LinearLayout>(R.id.delete_gadget)

            gadgetNameInput?.setText(gadget.value?.name.toString())
            wifi?.text = gadget.value?.wifiOwnership
            save?.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.updateGadget(getInputsValueForGadget(gadgetNameInput?.text.toString()))
                        .join()
                    dialogGadgetProfile.onBackPressed()

                }
            }

            fun acceptDeleteGadget() {
                lifecycleScope.launch {
                    if (_gadgetId.value != null) {
                        viewModel.deleteGadget(MutableLiveData<Gadget>().emptyGadget().copy(
                            id = _gadgetId.value!!.toInt()
                        ))
                        dialogGadgetProfile.onBackPressed()
                        findNavController().navigateUp()
                    }
                }
            }

            delete?.setOnClickListener {
                requireContext().showConfirmDialog(
                    getString(R.string.delete_gadget_message, gadget.value?.name.toString()),
                    getString(R.string.attention),
                    ::acceptDeleteGadget
                )
            }


            dialogGadgetProfile.show()
        }
    }


    private fun getInputsValueForGadget(name: String): Gadget =
        gadget.value!!.copy(id = gadget.value!!.id, name = name)

    private fun goToProfileView() {
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

