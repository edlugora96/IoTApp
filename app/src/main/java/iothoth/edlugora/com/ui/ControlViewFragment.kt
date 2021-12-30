package iothoth.edlugora.com.ui

import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.internal.bi
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.adapters.ControlGadgetAdapter
import iothoth.edlugora.com.databinding.FragmentControlViewBinding
import iothoth.edlugora.com.utils.actualSsid
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
import kotlin.concurrent.schedule

class ControlViewFragment : Fragment() {
    private lateinit var binding: FragmentControlViewBinding
    private val navigationArg: ControlViewFragmentArgs by navArgs()

    private var gadgetId: Int = 0

    /*private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user*/

    private val _gadgetObservable: MutableLiveData<Gadget> = MutableLiveData()
    //val gadgetObservable: LiveData<Gadget> = _gadgetObservable

    //private var gadgetObserver: LiveData<Gadget> = MutableLiveData()
    private var _userObserver: LiveData<User> = MutableLiveData()


    private lateinit var wifiManager: WifiManager

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
    private val countAllGadgets by lazy {
        CountAllGadgets(gadgetRepository)
    }
    private val viewModel: ControlViewModel by lazy {
        ControlViewModel(
            triggerGadgetAction,
            testGadgetConnection,
            getUserInfo,
            getGadget,
            updateGadget,
            deleteGadget,
            countAllGadgets
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
        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        viewModel.checkFirstStep(requireActivity())
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        gadgetId = navigationArg.gadgetId

        _userObserver = viewModel.getUser(requireActivity())
        //gadgetObserver = viewModel.gadget(gadgetId)


        viewModel.countOfAllGadgets.observe(viewLifecycleOwner, Observer(this::isOneGadget))

        val adapter = ControlGadgetAdapter {
            action(it.value, it.url)
        }
        binding.recycleControlGadget.adapter = adapter
        binding.recycleControlGadget.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.gadget(gadgetId).observe(viewLifecycleOwner, Observer { gadget ->
            gadget.let {
                if (it != null) {
                    binding.navBar.gadgetName.text = it.name
                    _gadgetObservable.value = it
                    adapter.submitList(it.actions)

                    if (wifiManager.actualSsid() == it.wifiOwnership){
                        binding.messageBarText.text = getString(R.string.online)
                        binding.messageBarAction.visibility = View.GONE
                        binding.messageBar.setBackgroundColor(getColor(requireContext(), R.color.blue))
                    } else {
                        binding.messageBarText.text = getString(R.string.offline)
                        binding.messageBarAction.visibility = View.VISIBLE
                        binding.messageBarAction.text = getString(R.string.ask_wifi_sync)
                        binding.messageBar.setBackgroundColor(getColor(requireContext(), R.color.error))
                        binding.messageBar.setOnClickListener { goToSyncWifiView() }
                    }

                }
            }
        })
        bind()
        //fillUserAndGadget()
    }

    private fun isOneGadget(count: Int) {
        if (count < 2) {
            binding.navBar.cancel.text = getString(R.string.plus)
            binding.navBar.cancel.setOnClickListener { goToInsertGadget() }

        } else {
            binding.navBar.cancel.setOnClickListener { goToListGadget() }
        }
    }


    //region Methods

    fun action(act: String, url: String) {
        _gadgetObservable.value.let {
            if (it != null) {
                viewModel.gadgetDoAction(it.ipAddress, url, RequestApi(data = act))
            }
        }
    }

    private fun bind() {
        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this@ControlViewFragment
            it.lifecycleOwner = viewLifecycleOwner
        }


        //binding.navBar.gadgetName.text = _gadgetObserver.value?.name ?: ""
        /*_gadgetObserver.observe(viewLifecycleOwner) {
            binding.navBar.gadgetName.text = it.name
            if (it != null) {
                _gadgetObserver.removeObservers(viewLifecycleOwner)
            }
        }*/

        /*_userObserver.observe(viewLifecycleOwner) {
            binding.navBar.profileName.text = it?.name ?: ""
        }*/
        /*gadgetObserver.observe(viewLifecycleOwner) {
            binding.gadgetName.text = it.name
        }*/
//        gadgetObserver.observe(viewLifecycleOwner, Observer{
//            binding.navBar.gadgetName.text = it?.name
//        })


        /*binding.gadgetName.text = gadgetObserver.value?.name
        lifecycleScope.launch {
            Timer().schedule(500) {
                lifecycleScope.launch {
                    binding.gadgetName.text = gadgetObserver.value?.name
                }
            }
        }*/
        //binding.gadgetName.text = gadgetObserver.distinctUntilChanged().value?.name

        /*binding.navBar.gadgetName.setOnClickListener { goToProfileView() }
        binding.navBar.profileName.setOnClickListener { goToProfileView() }
        binding.navBar.profilePhotoCard.setOnClickListener { goToProfileView() }*/

        binding.navBar.cogMenu.setOnClickListener {
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
            val syncWifi = dialogGadgetProfile.findViewById<LinearLayout>(R.id.wifi_gadget)

            gadgetNameInput?.setText(_gadgetObservable.value?.name ?: "")
            wifi?.text = _gadgetObservable.value?.wifiOwnership
            save?.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.updateGadget(getInputsValueForGadget(gadgetNameInput?.text.toString()))
                        .join()
                    dialogGadgetProfile.onBackPressed()

                }
            }

            fun acceptDeleteGadget() {
                //_gadgetObserver.removeObservers(viewLifecycleOwner)
                //_gadgetObserver.value = MutableLiveData<Gadget>().emptyGadget()
                lifecycleScope.launch {
                    viewModel.deleteGadget(
                        MutableLiveData<Gadget>().emptyGadget().copy(
                            id = gadgetId
                        )
                    )
                    goToListGadget()
                    //findNavController().navigateUp()
                    dialogGadgetProfile.onBackPressed()
                }
            }

            delete?.setOnClickListener {
                requireContext().showConfirmDialog(
                    getString(
                        R.string.delete_gadget_message,
                        _gadgetObservable.value?.name.toString()
                    ),
                    getString(R.string.attention),
                    ::acceptDeleteGadget
                )
            }
            syncWifi?.setOnClickListener {
                dialogGadgetProfile.onBackPressed()
                goToSyncWifiView()
            }
            dialogGadgetProfile.show()
        }
    }


    private fun getInputsValueForGadget(name: String): Gadget =
        _gadgetObservable.value?.copy(id = _gadgetObservable.value!!.id, name = name)
            ?: MutableLiveData<Gadget>().emptyGadget()

    private fun goToListGadget() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToGadgetsListFragment()
        findNavController().navigate(action)
    }

    private fun goToSyncWifiView() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToDetectNetworkFragment(
            gadgetId
        )
        findNavController().navigate(action)
    }

    fun goToInsertGadget() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToInsertGadgetFragment()
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
                else -> {}
            }

        }

    }
    //endregion
}


