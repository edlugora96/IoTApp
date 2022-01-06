package iothoth.edlugora.com.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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
import iothoth.edlugora.com.adapters.WifiScannerAdapter
import iothoth.edlugora.com.databinding.FragmentDetectNetworkBinding
import iothoth.edlugora.com.dialogs.dialogSetWifiPassword
import iothoth.edlugora.com.utils.*
import iothoth.edlugora.com.viewModel.DetectNetworkViewModel
import iothoth.edlugora.com.viewModel.DetectNetworkViewModel.UiReactions
import iothoth.edlugora.com.viewModel.DetectNetworkViewModel.UiReactions.*
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
import iothoth.edlugora.usecases.GetGadget
import iothoth.edlugora.usecases.TestGadgetConnection
import iothoth.edlugora.usecases.TriggerGadgetAction
import iothoth.edlugora.usecases.UpdateGadget
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule


class DetectNetworkFragment : Fragment() {
    private lateinit var binding: FragmentDetectNetworkBinding

    //private lateinit var permissionHandler: PermissionHandler
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiHandler: WifiHandler
    private lateinit var wifiManager: WifiManager

    private val wifiScan = MutableLiveData<List<ScanResult>>()

    //private var _gadgetObserver: LiveData<Gadget> = MutableLiveData()
    private val _gadgetObservable: MutableLiveData<Gadget> = MutableLiveData()
    private val _gadget: MutableLiveData<Gadget> = MutableLiveData()
    val gadget: LiveData<Gadget> = _gadget
    private var ip = ""
    private var ssidGadget = ""
    private var _syncState = MutableLiveData(-1)
    var syncState: LiveData<Int> = _syncState
    private val navigationArg: DetectNetworkFragmentArgs by navArgs()


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
        /*permissionHandler = PermissionHandler(
            context = requireContext(),
            requestPermissions = this::requestPermissions,
            PERMISSIONS = PERMISSIONS
        )*/

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
                        Timer().schedule(15000) {
                            viewModel.testConnection(ip, "/")
                        }
                    }


                }
            }
        }
        binding.navBar.cogMenu.visibility = View.GONE
        binding.navBar.cancel.setOnClickListener { findNavController().navigateUp() }
        binding.navBar.gadgetName.text = getString(R.string.sync_wifi)
        binding.startSearch.setOnClickListener {
            startGadgetConnection()
        }

        binding.recycleWifiScanner.adapter = adapter
        wifiScan.observe(viewLifecycleOwner) { scan ->
            scan.let {
                if (it != null) {
                    adapter.submitList(it)
                }
            }
        }

        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this
            it.lifecycleOwner = viewLifecycleOwner
        }


        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        wifiHandler.startScanWifi()
        viewModel.gadget(navigationArg.gadgetId)
            .observe(viewLifecycleOwner, { gadget ->
                _gadgetObservable.value = gadget
                if (gadget != null) {
                    _syncState.value = 0
                    _syncState.observe(viewLifecycleOwner) { state ->
                        run {
                            if (wifiManager.actualSsid() != gadget.ssid && state != 0){
                                _syncState.value = 0
                            }
                            when (state) {
                                -1 -> viewModel.testConnection(ip, "/")
                                0 -> startGadgetConnection()
                                1 -> wifiScan.value =
                                    wifiManager.scanResults.filter { it.SSID != gadget.ssid }
                                //else ->
                            }
                        }

                    }
                } else {
                    _syncState.value = -2
                }

            })


        /*_gadgetObserver.observe(viewLifecycleOwner) {
            if (it != null) {
                _syncState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        -2 -> ifPermissionsAreGranted()
                        -1 -> viewModel.testConnection(it.ipAddress, "/")
                        0 -> run {
                            wifiScan.value = emptyList()
                            startGadgetConnection()
                        }
                        1 -> wifiScan.value =
                            wifiManager.scanResults.filter { it.SSID != _gadgetObserver.value?.ssid ?: "" }
                        //else -> wifiScan.value = emptyList()
                    }
                }
                if (actualSsid().matches(it.ssid.toRegex())) {
                    _syncState.value = 1
                }
            }
        }*/
    }

    /*override fun onDestroy() {
        super.onDestroy()
        wifiHandler.stopScanWifi()
        wifiHandler.disconnectToANewWifiNetwork()
    }*/

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startGadgetConnection() {
        _gadgetObservable.value.let {
            if (it != null) {
                wifiHandler.connectToANewWifiNetwork(it.ssid, it.ssidPassword) { link ->
                    run {
                        ip = "http:/${link.dnsServers[0]}"
                        lifecycleScope.launch { _syncState.value = -1 }
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


    private fun saveDeviceConfiguration(setIp: String? = null, setSsid: String? = null) {
        //_gadgetObservable.observe(viewLifecycleOwner) {
        //if (it != null) {
        wifiHandler.stopScanWifi()
        wifiHandler.disconnectToANewWifiNetwork()
        lifecycleScope.launch {
            _gadgetObservable.value?.let {
                viewModel.updateGadget(
                    it.copy(
                        ipAddress = setIp ?: ip,
                        wifiOwnership = setSsid ?: ssidGadget
                    )
                ).join()
            }
            requireActivity().showLongSnackBar(
                R.id.root_activity,
                getString(R.string.wifi_success),
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            )

            findNavController().navigateUp()
        }
        //}
        //}
    }

    private fun actualSsid() = wifiManager.actualSsid()

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

        if (_syncState.value == 1) {
            wifiScan.value =
                wifiManager.scanResults
        }
    }

    /*private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    ContextCompat.startActivity(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        )
                    )
                })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert: AlertDialog = builder.create()
        alert.show()
    }*/

    private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is ShowErrorSnackBar -> reaction.run { requireContext().showLongToast(this.message) }
                is ShowSuccessSnackBar -> reaction.run { requireContext().showLongToast(this.message) }
                is ShowSuccessTest -> reaction.run {
                    val res =
                        this.message.matches("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|\$)){4}\\b".toRegex())
                    if (syncState.value == -1) {
                        if (res && this.wifi != null) {
                            requireContext().showConfirmDialog(
                                title = this.wifi,
                                message = "El dispositivo ya se encuentra asociado a una red, ¿Desea volver a configurar el dispositivo?",
                                acceptName = "Volver a configurar",
                                declineName = "Mantener la configuracion",
                                accept = fun() {
                                    _syncState.value = 1
                                },
                                decline = {
                                    _syncState.value = -2
                                    saveDeviceConfiguration(this.message, this.wifi)
                                }
                            )
                        } else {
                            _syncState.value = 1
                        }
                    }

                    if (syncState.value!! > 1) {
                        if (res) {
                            _syncState.value = 3
                            saveDeviceConfiguration(this.message, this.wifi)

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
                is ShowWarningTest -> reaction.run {
                    val res =
                        this.message.matches("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|\$)){4}\\b".toRegex())
                    if (!res && syncState.value!! == -1) {
                        _syncState.value = 1
                    }
                }
                is ShowErrorTest -> {
                    _syncState.value = 1
                }
                else -> {}
            }

        }

    }


}

