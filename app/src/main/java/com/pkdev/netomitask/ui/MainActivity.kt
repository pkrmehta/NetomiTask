package com.pkdev.netomitask.ui

import android.Manifest
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pkdev.netomitask.ui.Adapters.ChatListAdapter
import com.pkdev.netomitask.ViewModel.ChatViewModel
import com.pkdev.netomitask.Model.ListConnection
import com.pkdev.netomitask.R
import com.pkdev.netomitask.databinding.ActivityMainBinding
import dagger.hilt.android.HiltAndroidApp
import kotlin.collections.get

@HiltAndroidApp
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var connectivityManager: ConnectivityManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if(isNetworkAvailable()) {
            viewModel.loadConnections()
        }
        else{
            binding.apply {
                retryBtn.visibility = View.VISIBLE
                noChat.text = getString(R.string.no_internet_connection)
                chatRv.visibility = View.GONE
                noChat.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        binding.retryBtn.setOnClickListener {
            if(isNetworkAvailable()) {
                binding.apply {
                    retryBtn.visibility = View.GONE
                    noChat.visibility = View.GONE
                }
                viewModel.loadConnections()
            }
            else{
                Snackbar.make(binding.root, getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show()
            }
        }

        addObservers()

    }

    override fun onResume() {
        super.onResume()
        if(isNetworkAvailable()) {
            viewModel.loadConnections()
        }
        else{
            Snackbar.make(binding.root, getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show()
        }
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
                binding.noChat.text = getString(R.string.no_chat_found)
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

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }


    interface OnChatClickListener {
        fun onChannelClicked(channel: String, latMessage: String)
    }
}