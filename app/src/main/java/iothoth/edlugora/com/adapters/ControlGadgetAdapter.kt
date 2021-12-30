package iothoth.edlugora.com.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import iothoth.edlugora.com.databinding.ItemControlGadgetBinding
import iothoth.edlugora.com.databinding.ItemGadgetViewBinding
import iothoth.edlugora.com.utils.getStringWithIdentifier
import iothoth.edlugora.domain.Action

class ControlGadgetAdapter(private val onItemClicked: (Action) -> Unit) :
    ListAdapter<Action, ControlGadgetAdapter.ControlGadgetViewHolder>(DiffCallback){

    class ControlGadgetViewHolder(private var binding: ItemControlGadgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(action: Action) {
            binding.apply {
                actionDisplayName = action.name
                icon =  binding.root.context.getStringWithIdentifier(action.icon)

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ControlGadgetViewHolder {
        return ControlGadgetViewHolder(
            ItemControlGadgetBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ControlGadgetViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Action>() {
            override fun areItemsTheSame(oldItem: Action, newItem: Action): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Action, newItem: Action): Boolean {
                return oldItem.name == newItem.name && oldItem.icon == newItem.icon
            }
        }
    }

}