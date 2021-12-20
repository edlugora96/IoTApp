package iothoth.edlugora.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi

class WifiHandler(
    private val context: Context,
    private val connectivityManager: ConnectivityManager,
    private val intentFilter: IntentFilter,
    private val scanSuccess: (intent: Intent) -> Unit
) {

    private lateinit var networkCallback : ConnectivityManager.NetworkCallback

    private val wifiScanReceiver =
        object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onReceive(context: Context, intent: Intent) {
                scanSuccess(intent)
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startScanWifi() {
        intentFilter.apply {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.ACTION_WIFI_SCAN_AVAILABILITY_CHANGED)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
        //addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
    }

    fun stopScanWifi() {
        context.unregisterReceiver(wifiScanReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun connectToANewWifiNetwork(
        ssid: String,
        pass: String,
        callbackOnAvailable: ((network: LinkProperties) -> Unit)? = null
    ) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()


        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network);
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                if (callbackOnAvailable != null) callbackOnAvailable(linkProperties)
            }

            override fun onUnavailable() {
                connectToANewWifiNetwork(ssid, pass, callbackOnAvailable)
            }

        }
        connectivityManager.requestNetwork(request, networkCallback)
    }

    fun disconnectToANewWifiNetwork(){
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /*val netWorkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d("someNet", "The default network is now: $network")
        }

        override fun onLost(network: Network) {

        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {

        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.i(
                "someNet",
                "The default network changed link properties: ${linkProperties.dnsServers}"
            )
            Log.i(
                "someNet",
                "The default network changed link properties: ${linkProperties.linkAddresses}"
            )
        }
    }*/

}