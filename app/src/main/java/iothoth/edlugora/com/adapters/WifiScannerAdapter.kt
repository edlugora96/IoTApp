package iothoth.edlugora.com.adapters

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import iothoth.edlugora.com.databinding.ItemWifiScannerBinding
import iothoth.edlugora.com.utils.isWifiLocked

class WifiScannerAdapter(private val actualSsid: ()->String, private val onItemClicked: (ScanResult) -> Unit) :
    ListAdapter<ScanResult, WifiScannerAdapter.WifiScannerHolder>(WifiScannerAdapter.DiffCallback) {

    class WifiScannerHolder(private var binding: ItemWifiScannerBinding, private val actualSsid: ()->String) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scanned: ScanResult) {
            binding.apply {
                wifiName.text = scanned.SSID
                wifiCapability.text = scanned.BSSID
                wifiIsLock.visibility = if (isWifiLocked(scanned)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                if (actualSsid() == scanned.SSID){
                    wifiIsLock.visibility =View.GONE
                    wifiIsHome.visibility= View.VISIBLE
                } else {
                    wifiIsHome.visibility= View.GONE
                }

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WifiScannerHolder {
        return WifiScannerHolder(
            ItemWifiScannerBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            ),
            actualSsid
        )
    }

    override fun onBindViewHolder(holder: WifiScannerHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ScanResult>() {
            override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem.BSSID == newItem.BSSID
            }
        }
    }
}