package iothoth.edlugora.com.ui

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.adapters.ControlGadgetAdapter
import iothoth.edlugora.com.databinding.FragmentControlViewBinding
import iothoth.edlugora.com.utils.*
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

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            val permissionsGranted = isGranted.all { it.value == true }
            if (!permissionsGranted) {
                binding.messageBarText.text = getString(R.string.ask_permission_wifi)
                binding.messageBarAction.visibility = View.GONE
                binding.messageBarAction.text = getString(R.string.grant_permission)
                binding.messageBar.setBackgroundColor(getColor(requireContext(), R.color.error))
            }
        }

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

    @RequiresApi(Build.VERSION_CODES.R)
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

        val adapter = ControlGadgetAdapter { action ->
            action(action.value, action.url)

        }

        binding.recycleControlGadget.adapter = adapter
        binding.recycleControlGadget.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.gadget(gadgetId).observe(viewLifecycleOwner, Observer { gadget ->
            gadget.let {
                ifPermissionsAreGranted()
                statusCheck()
                if (it != null) {
                    binding.navBar.gadgetName.text = it.name
                    _gadgetObservable.value = it
                    adapter.submitList(it.actions)
                }
            }
        })

        binding.messageBarText.text = getString(R.string.loading)
        binding.messageBarAction.visibility = View.GONE
        binding.messageBar.setBackgroundColor(
            getColor(
                requireContext(),
                R.color.blue
            )
        )
        var flag = true
        _gadgetObservable.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.status == "1") {
                    binding.messageBarText.text = getString(R.string.another_web)
                    binding.messageBarAction.visibility = View.VISIBLE
                    binding.messageBarAction.text = getString(R.string.sync_wifi)
                    binding.messageBar.setBackgroundColor(
                        getColor(
                            requireContext(),
                            R.color.error
                        )
                    )
                    binding.messageBar.setOnClickListener {
                        goToSyncWifiView()
                    }
                }
                if (flag) {
                    flag = false
                    viewModel.startConnection("http://${it.ipAddress}", "/")
                        .observe(viewLifecycleOwner, Observer { response ->
                            if (it.status != "1") {
                                when (response.code.toInt()) {
                                    in 200..299 -> {
                                        binding.messageBarText.text = getString(R.string.online)
                                        binding.messageBarAction.visibility = View.GONE
                                        binding.messageBar.setBackgroundColor(
                                            getColor(
                                                requireContext(),
                                                R.color.blue
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.messageBarText.text = getString(R.string.offline)
                                        binding.messageBarAction.visibility = View.GONE
                                        binding.messageBar.setBackgroundColor(
                                            getColor(
                                                requireContext(),
                                                R.color.warning
                                            )
                                        )
                                    }
                                }
                            }
                        })
                }
            }
        })
        /*wifiManager.actualSsidLiveData().observe(viewLifecycleOwner, Observer { ssidOb ->
            run {
                if (_gadgetObservable.value?.status == "1") {
                    binding.messageBarText.text = getString(R.string.another_web)
                    binding.messageBarAction.visibility = View.VISIBLE
                    binding.messageBarAction.text = getString(R.string.sync_wifi)
                    binding.messageBar.setBackgroundColor(
                        getColor(
                            requireContext(),
                            R.color.error
                        )
                    )
                    binding.messageBar.setOnClickListener {
                        goToSyncWifiView()
                    }
                }

                val wifiOwnership = _gadgetObservable.value?.wifiOwnership ?: String()
                if (ssidOb == wifiOwnership) {
                    binding.messageBarText.text = getString(R.string.same_web)
                    binding.messageBarAction.visibility = View.GONE
                    binding.messageBar.setBackgroundColor(
                        getColor(
                            requireContext(),
                            R.color.blue
                        )
                    )
                } else {
                }
            }

        })*/
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

    private fun statusCheck() {
        val manager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //manager.
            gpsEnable(requireActivity(), requireContext())
            //buildAlertMessageNoGps()
        }
    }
    //region Methods

    fun action(act: String, url: String) {
        _gadgetObservable.value.let {
            if (it != null) {
                viewModel.gadgetDoAction("http://${it.ipAddress}", url, RequestApi(data = act))
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
            val unlockGadget = dialogGadgetProfile.findViewById<LinearLayout>(R.id.unlock_gadget)

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
                lifecycleScope.launch {
                    viewModel.updateGadget(
                        getInputsValueForGadget(gadgetNameInput?.text.toString()).copy(
                            showing = 0
                        )
                    )
                        .join()
                    dialogGadgetProfile.onBackPressed()
                    goBackAfterDelete()

                }
            }

            delete?.setOnClickListener {
                requireContext().showConfirmDialog(
                    getString(
                        R.string.delete_gadget_message,
                        _gadgetObservable.value?.name.toString()
                    ),
                    getString(R.string.attention),
                    ::acceptDeleteGadget,
                    fun() { dialogGadgetProfile.onBackPressed() }
                )
            }
            syncWifi?.setOnClickListener {
                dialogGadgetProfile.onBackPressed()
                goToSyncWifiView()
            }
            unlockGadget?.setOnClickListener {
                dialogGadgetProfile.onBackPressed()
                _gadgetObservable.value?.unitId?.let { it1 ->
                    action(it1, "/unlock")
                }
            }
            dialogGadgetProfile.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun ifPermissionsAreGranted() {
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions.launch(PERMISSIONS)
            binding.messageBarText.text = getString(R.string.ask_permission_wifi)
            binding.messageBarAction.visibility = View.VISIBLE
            binding.messageBarAction.text = getString(R.string.grant_permission)
            binding.messageBar.setBackgroundColor(getColor(requireContext(), R.color.error))
            binding.messageBar.setOnClickListener { requestPermissions.launch(PERMISSIONS) }
        }
    }

    private fun getInputsValueForGadget(name: String): Gadget =
        _gadgetObservable.value?.copy(id = _gadgetObservable.value!!.id, name = name)
            ?: emptyGadget()

    private fun goToListGadget() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToGadgetsListFragment()
        findNavController().navigate(action)
    }

    private fun goBackAfterDelete() {
        findNavController().popBackStack()
    }

    private fun goToSyncWifiView() {
        val action = ControlViewFragmentDirections.actionControlViewFragmentToDetectNetworkFragment(
            gadgetId
        )
        findNavController().navigate(action)
    }

    private fun goToInsertGadget() {
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


    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
        )
    }
}


