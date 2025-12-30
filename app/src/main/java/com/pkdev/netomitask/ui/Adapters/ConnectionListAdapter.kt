package com.pkdev.netomitask.ui.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.pkdev.netomitask.Model.ListConnection
import com.pkdev.netomitask.R
import com.pkdev.netomitask.ui.MainActivity

class ChatListAdapter(val onChatClickListener: MainActivity.OnChatClickListener) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val list = mutableListOf<ListConnection>()

    fun submit(newList: List<ListConnection>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    class ChatViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: ChatViewHolder, p: Int) {
        val item = list[p]
        holder.view.findViewById<TextView>(R.id.chat_room_name).text = item.channelName
        holder.view.findViewById<TextView>(R.id.chat_room_desc).text = item.channelLastMessage
        holder.view.findViewById<ConstraintLayout>(R.id.rootChat).setOnClickListener {
            onChatClickListener.onChannelClicked(item.channelName!!, item.channelLastMessage!!)
        }
    }

    override fun getItemCount() = list.size
    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        ChatViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_chat_list, p, false))
}
