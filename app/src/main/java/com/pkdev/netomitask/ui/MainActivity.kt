package com.pkdev.netomitask.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkdev.netomitask.ui.Adapters.ChatListAdapter
import com.pkdev.netomitask.ViewModel.ChatViewModel
import com.pkdev.netomitask.Model.ListConnection
import com.pkdev.netomitask.databinding.ActivityMainBinding
import dagger.hilt.android.HiltAndroidApp
import kotlin.collections.get

@HiltAndroidApp
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        viewModel.loadConnections()

        addObservers()

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadConnections()
    }

    private fun addObservers() {
        viewModel.connection.observe(this) { connectionList ->
            binding.progressBar.visibility = View.GONE
            if (connectionList.isNullOrEmpty().not()) {
                binding.noChat.visibility = View.GONE
                binding.chatRv.visibility = View.VISIBLE
                val chatAdapter = ChatListAdapter(onChatClickListener)
                binding.chatRv.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = chatAdapter
                    chatAdapter.submit(getLatestMessageList(connectionList))
                }
            } else {
                binding.chatRv.visibility = View.GONE
                binding.noChat.visibility = View.VISIBLE
            }
        }
        viewModel.loading.observe(this) { loading ->
            loading?.let {
                if (loading) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun getLatestMessageList(connectionList: List<ListConnection>): List<ListConnection> {
        val map = mutableMapOf<String, ListConnection>()

        for (item in connectionList) {
            val existing = map[item.channelName]
            if (existing == null || item.timestamp!! > existing.timestamp!!) {
                map[item.channelName!!] = item
            }
        }

        return map.values.toList()
    }

    val onChatClickListener = object : OnChatClickListener {
        override fun onChannelClicked(channel: String, latMessage: String) {
            startActivity(Intent(this@MainActivity, ChatDetailsActivity::class.java).apply {
                putExtra("channel", channel)
                putExtra("latMessage", latMessage)
            })
        }
    }


    interface OnChatClickListener {
        fun onChannelClicked(channel: String, latMessage: String)
    }
}