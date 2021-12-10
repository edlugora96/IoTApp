package iothoth.edlugora.com.adapters

import android.content.ClipData
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.ItemGadgetViewBinding
import iothoth.edlugora.domain.Gadget

class GadgetAdapter(private val onItemClicked: (Gadget) -> Unit) :
    ListAdapter<Gadget, GadgetAdapter.GadgetViewHolder>(DiffCallback){

    //private val

    class GadgetViewHolder(private var binding: ItemGadgetViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(gadget: Gadget) {
            binding.apply {
                nameGadget.text = gadget.name
                //iconGadget.text = gadget.icon

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GadgetViewHolder {
        return GadgetViewHolder(
            ItemGadgetViewBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: GadgetViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Gadget>() {
            override fun areItemsTheSame(oldItem: Gadget, newItem: Gadget): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Gadget, newItem: Gadget): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}