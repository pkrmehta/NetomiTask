package com.pkdev.netomitask.ui.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pkdev.netomitask.Model.ChatMessage
import com.pkdev.netomitask.databinding.ItemChatRecieveBinding
import com.pkdev.netomitask.databinding.ItemChatSendBinding

private const val VIEW_TYPE_SEND = 1
private const val VIEW_TYPE_RECEIVE = 2
class ChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByMe) {
            VIEW_TYPE_SEND
        } else {
            VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_SEND) {
            val binding = ItemChatSendBinding.inflate(
                inflater,
                parent,
                false
            )
            SendViewHolder(binding)
        } else {
            val binding = ItemChatRecieveBinding.inflate(
                inflater,
                parent,
                false
            )
            ReceiveViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val chat = messages[position]

        when (holder) {
            is SendViewHolder -> holder.bind(chat)
            is ReceiveViewHolder -> holder.bind(chat)
        }
    }

    override fun getItemCount(): Int = messages.size


    class SendViewHolder(
        private val binding: ItemChatSendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.message.text = chat.message
            if(chat.isNetworkConnected){
                binding.warning.visibility = View.GONE
            }
            else{
                binding.warning.visibility = View.VISIBLE
            }
        }
    }

    class ReceiveViewHolder(
        private val binding: ItemChatRecieveBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.message.text = chat.message
        }
    }
}
