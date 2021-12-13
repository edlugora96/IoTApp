package iothoth.edlugora.com.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import iothoth.edlugora.com.R
import iothoth.edlugora.com.adapters.WifiScannerAdapter
import iothoth.edlugora.com.databinding.FragmentDetectNetworkBinding
import iothoth.edlugora.com.utils.showConfirmDialog
import android.net.wifi.WifiConfiguration
import com.google.android.play.core.internal.t
import java.lang.Exception
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


class DetectNetworkFragment : Fragment() {
    private lateinit var binding: FragmentDetectNetworkBinding

    //region Wifi Scanner
    private lateinit var wifiManager: WifiManager
    private val wifiScan = MutableLiveData<List<ScanResult>>()
    private val intentFilter = IntentFilter()
    private fun scanSuccess() {
        wifiScan.value = wifiManager.scanResults
    }

    private fun scanFailure() {
        wifiScan.value = wifiManager.scanResults
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            val permissionsGranted = isGranted.all { it.value == true }
            if (permissionsGranted) {
                startScanWifi()
            }
        }

    private val wifiScanReceiver =
        object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onReceive(context: Context, intent: Intent) {
                /*Log.d("SomeNet", "connection: ${intent.action}")
                if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    Log.e("SomeNet", "error connection: ${intent.action}")

                }*/

                when (intent.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN
                )) {
                    WifiManager.WIFI_STATE_DISABLED, WifiManager.WIFI_STATE_DISABLING -> askForTurnOnWifi()
                    else -> {
                        val success =
                            intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                        if (success) {
                            scanSuccess()
                        } else {
                            scanFailure()
                        }
                    }
                }
            }
        }
    //endregion

    //region Wifi Connect
    private lateinit var connectivityManager: ConnectivityManager
    private val netWorkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.i("SomeNet", "The default network is now: $network")
        }

        override fun onLost(network: Network) {
            Log.i(
                "SomeNet",
                "The application no longer has a default network. The last default network was $network"
            )
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {

            Log.e("SomeNet", "The default network changed capabilities: $networkCapabilities")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.i("SomeNet", "The default network changed link properties: $linkProperties")
        }
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
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            requireContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        checkPermissionAndStartDetection()

        val adapter = WifiScannerAdapter(this::actualSsid) { wifi ->
            run {
                //Log.i("SomeNet", "$wifi")

                val dialogSetWifiPassword = BottomSheetDialog(requireContext())

                dialogSetWifiPassword.setContentView(R.layout.dialog_set_wifi_password)

                val ssid = dialogSetWifiPassword.findViewById<TextView>(R.id.wifi_ssid)
                val passwordLayout =
                    dialogSetWifiPassword.findViewById<TextInputLayout>(R.id.wifi_password_name)
                val password =
                    dialogSetWifiPassword.findViewById<TextInputEditText>(R.id.wifi_password_name_input)
                val save = dialogSetWifiPassword.findViewById<LinearLayout>(R.id.save_wifi_password)

                ssid?.text = wifi.SSID

                passwordLayout?.setEndIconOnClickListener {
                    if (password?.inputType == InputType.TYPE_CLASS_TEXT) {
                        password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                        password.transformationMethod = PasswordTransformationMethod.getInstance()
                        passwordLayout.setEndIconDrawable(R.drawable.ic_visibility)
                    } else {
                        password?.inputType = InputType.TYPE_CLASS_TEXT
                        password?.transformationMethod = null
                        passwordLayout.setEndIconDrawable(R.drawable.ic_visibility_off)
                    }
                }

                save?.setOnClickListener {

                    val networkSSID = wifi.SSID
                    val networkPass = password?.text.toString()

                    val suggestion2 = WifiNetworkSuggestion.Builder()
                        .setSsid(networkSSID)
                        .setWpa2Passphrase(networkPass)
                        .build()

                    val suggestion3 = WifiNetworkSuggestion.Builder()
                        .setSsid(networkSSID)
                        .setWpa3Passphrase(networkPass)
                        .build()

                    val suggestionsList = listOf(suggestion2, suggestion3)

                    wifiManager.addNetworkSuggestions(suggestionsList)

                    //connectToANewWifiNetwork(networkSSID, networkPass)

                    dialogSetWifiPassword.onBackPressed()
                }

                dialogSetWifiPassword.show()
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
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAndStartDetection() {
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions.launch(PERMISSIONS)
        } else {
            startScanWifi()
        }
    }


    private fun askForTurnOnWifi() {
        requireContext().showConfirmDialog(
            getString(R.string.ask_for_turn_on_wifi),
            getString(R.string.attention),
        ) { }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startScanWifi() {
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)
        requireContext().registerReceiver(wifiScanReceiver, intentFilter)
    }

    private fun actualSsid() = wifiManager.connectionInfo.ssid


    private fun connectToANewWifiNetwork(ssid : String, pass : String) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", ssid)
        wifiConfig.preSharedKey = String.format("\"%s\"", pass)
        val netId = wifiManager.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()
    }

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )
    }

}