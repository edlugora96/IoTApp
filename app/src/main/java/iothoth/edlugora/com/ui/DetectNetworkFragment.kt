package iothoth.edlugora.com.ui

import android.Manifest
import android.content.*
import android.net.*
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.adapters.WifiScannerAdapter
import iothoth.edlugora.com.databinding.FragmentDetectNetworkBinding
import iothoth.edlugora.com.dialogs.dialogSetWifiPassword
import iothoth.edlugora.com.utils.PermissionHandler
import iothoth.edlugora.com.utils.showConfirmDialog
import iothoth.edlugora.com.utils.showLongSnackBar
import iothoth.edlugora.com.utils.showLongToast
import iothoth.edlugora.com.viewModel.DetectNetworkViewModel
import iothoth.edlugora.com.viewModel.DetectNetworkViewModel.UiReactions
import iothoth.edlugora.com.viewModel.DetectNetworkViewModel.UiReactions.*
import iothoth.edlugora.com.viewModel.GadgetsListViewModel
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.domain.RequestApiFeed
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.myapplication.WifiHandler
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule


class DetectNetworkFragment : Fragment() {
    private lateinit var binding: FragmentDetectNetworkBinding
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiHandler: WifiHandler
    private lateinit var wifiManager: WifiManager

    private val wifiScan = MutableLiveData<List<ScanResult>>()
    private var _gadgetObserver: LiveData<Gadget> = MutableLiveData()
    private val _gadget: MutableLiveData<Gadget> = MutableLiveData()
    val gadget: LiveData<Gadget> = _gadget
    private var ip = "http://192.168.4.1"
    private var ssidGadget = ""
    private var _syncState = MutableLiveData(0)
    private var syncState: LiveData<Int> = _syncState
    private val navigationArg: DetectNetworkFragmentArgs by navArgs()


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermissions(callback: () -> Unit) =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            val permissionsGranted = isGranted.all { it.value == true }
            if (permissionsGranted) {
                callback()
            }
        }.launch(PERMISSIONS)


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
    private val viewModel: DetectNetworkViewModel by lazy {
        DetectNetworkViewModel(
            triggerGadgetAction,
            testGadgetConnection,
            getGadget,
            updateGadget
        )
    }
    //endregion


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detect_network, container, false
        )
        _gadgetObserver = viewModel.gadget(navigationArg.gadgetId)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            requireContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager



        wifiHandler = WifiHandler(
            context = requireContext(),
            connectivityManager = connectivityManager,
            intentFilter = IntentFilter(),
            scanSuccess = this::scanResult
        )

        permissionHandler = PermissionHandler(
            context = requireContext(),
            requestPermissions = this::requestPermissions,
            PERMISSIONS = PERMISSIONS
        )


        //connectivityManager.registerDefaultNetworkCallback(wifiHandler.netWorkCallback)
        val adapter = WifiScannerAdapter(this::actualSsid) { wifi ->
            dialogSetWifiPassword(requireContext(), wifi) { networkSSID, networkPass ->
                run {
                    _syncState.value = 2
                    lifecycleScope.launch {
                        viewModel.gadgetDoAction(
                            ip, "/feed", RequestApi(
                                feed = RequestApiFeed(
                                    ssid = networkSSID,
                                    pass = networkPass
                                )
                            )
                        )
                        ssidGadget = networkSSID
                        Timer().schedule(60000) {
                            viewModel.testConnection(ip, "/")
                        }
                    }


                }
            }
        }

        binding.navBar.menuIcon.setOnClickListener {
            _gadgetObserver.value?.ssid.let {
                if (it != null) {
                    if (!actualSsid().matches(it.toRegex())) {
                        startGadgetConnection()
                    } else {
                        viewModel.testConnection(ip, "/")
                    }
                }
            }
        }


        binding.recycleWifiScanner.adapter = adapter
        wifiScan.observe(viewLifecycleOwner) { scan ->
            scan.let {
                if (it != null) {
                    adapter.submitList(it)
                }
            }
        }




        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        _gadgetObserver.observe(viewLifecycleOwner) {
            if (it != null) {
                syncState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        0 -> run {
                            wifiScan.value = emptyList()
                            startGadgetConnection()
                        }
                        1 -> wifiScan.value =
                            wifiManager.scanResults.filter { it.SSID != _gadgetObserver.value?.ssid ?: "" }
                        2 -> wifiScan.value = emptyList()
                    }
                }
                permissionHandler.ifPermissionsAreGranted { wifiHandler.startScanWifi() }
                if (actualSsid().matches(it.ssid.toRegex())) {
                    _syncState.value = 1
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiHandler.stopScanWifi()
        wifiHandler.disconnectToANewWifiNetwork()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startGadgetConnection() {
        permissionHandler.ifPermissionsAreGranted {
            _gadgetObserver.value.let {
                if (it != null) {
                    wifiHandler.connectToANewWifiNetwork(it.ssid, it.ssidPassword) { link ->
                        run {
                            ip = "http:/${link.dnsServers[0]}"
                            lifecycleScope.launch { _syncState.value = 1 }
                        }
                    }
                }
            }
        }
    }

    private fun askForTurnOnWifi() {
        requireContext().showConfirmDialog(
            getString(R.string.ask_for_turn_on_wifi),
            getString(R.string.attention),
        )
    }

    private fun actualSsid() = wifiManager.connectionInfo.ssid.replace("\"", "")

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun scanResult(intent: Intent) {
        Log.i(
            "someNetScan",
            "${
                intent.action
            }"
        )
        when (intent.getIntExtra(
            WifiManager.EXTRA_WIFI_STATE,
            WifiManager.WIFI_STATE_UNKNOWN
        )) {
            WifiManager.WIFI_STATE_DISABLED, WifiManager.WIFI_STATE_DISABLING -> askForTurnOnWifi()
        }
    }

    private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is ShowErrorSnackBar -> reaction.run { requireContext().showLongToast(this.message) }
                is ShowSuccessSnackBar -> reaction.run { requireContext().showLongToast(this.message) }
                is ShowSuccessTestSnackBar -> reaction.run {
                    if (syncState.value!! > 1) {
                        val res =
                            this.message.matches("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|\$)){4}\\b".toRegex())

                        if (res) {
                            _syncState.value = 3
                            //findNavController().navigateUp()


                            _gadgetObserver.observe(viewLifecycleOwner) {
                                if (it != null) {
                                    lifecycleScope.launch {
                                        viewModel.updateGadget(
                                            it.copy(
                                                ipAddress = ip,
                                                wifiOwnership = ssidGadget
                                            )
                                        ).join()
                                        findNavController().navigateUp()
                                    }
                                }
                            }
                        } else {
                            _syncState.value = 1
                            requireActivity().showLongSnackBar(
                                R.id.root_activity,
                                getString(R.string.wifi_error_password),
                                ContextCompat.getColor(requireContext(), R.color.error)
                            )
                        }
                    }


                }
                is ShowWarningSnackBar -> reaction.run { requireContext().showLongToast(this.message) }
                else -> {}
            }

        }

    }

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

