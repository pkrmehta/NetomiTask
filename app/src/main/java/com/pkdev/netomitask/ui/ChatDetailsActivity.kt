package com.pkdev.netomitask.ui

import android.Manifest
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.piesocket.channels.Channel
import com.piesocket.channels.PieSocket
import com.piesocket.channels.misc.PieSocketEvent
import com.piesocket.channels.misc.PieSocketEventListener
import com.piesocket.channels.misc.PieSocketOptions
import com.pkdev.netomitask.ui.Adapters.ChatAdapter
import com.pkdev.netomitask.Model.ChatMessage
import com.pkdev.netomitask.databinding.ActivityChatDetailsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatDetailsBinding
    private lateinit var channelName: String
    private lateinit var latMessage: String

    private lateinit var piesocket: PieSocket
    private lateinit var channel: Channel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var connectivityManager: ConnectivityManager
    val queueItems = mutableListOf<ChatMessage>()

    val messageList = mutableListOf<ChatMessage>()
    val clientEvent = PieSocketEvent("system:message")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatDetailsBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        channelName = intent.getStringExtra("channel") ?: ""
        latMessage = intent.getStringExtra("latMessage") ?: ""

        messageList.add(ChatMessage(latMessage, false))

        chatAdapter = ChatAdapter(messageList)

        if (channelName.isNotEmpty()) {
            binding.toolbar.text = channelName
            binding.chatRv.apply {
                layoutManager = LinearLayoutManager(context).apply {
                    stackFromEnd = true
                }
                itemAnimator = null
                this.adapter = chatAdapter
            }
        }

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)


        binding.btnSend.setOnClickListener {
            val message = binding.message.text.toString()
            binding.message.text.clear()
            hideKeyboard()
            if (message.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    messageList.add(ChatMessage(message, true))
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    binding.chatRv.scrollToPosition(messageList.size - 1)

                    clientEvent.setData(message)
                    channel.publish(clientEvent)
                } else {
                    queueItems.add(ChatMessage(message, isSentByMe = true))
                    messageList.add(
                        ChatMessage(
                            message,
                            isSentByMe = true,
                            isNetworkConnected = false
                        )
                    )
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    binding.chatRv.scrollToPosition(messageList.size - 1)
                }
            }
        }

    }

    val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            Handler(Looper.getMainLooper()).post {
                connectChannel(channelName)
                messageList.removeIf { it.isNetworkConnected.not() }
                chatAdapter.notifyDataSetChanged()
                binding.chatRv.scrollToPosition(messageList.size - 1)

                lifecycleScope.launch {
                    val iterator = queueItems.iterator()

                    while (iterator.hasNext()) {
                        val item = iterator.next()

                        messageList.add(item)
                        chatAdapter.notifyItemInserted(messageList.size - 1)
                        binding.chatRv.scrollToPosition(messageList.size - 1)

                        clientEvent.setData(item.message)
                        channel.publish(clientEvent)

                        iterator.remove()

                        delay(1000)
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            // Internet lost
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

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.message.windowToken, 0)

    }

    private fun connectChannel(channelName: String) {

        val pieSocketOptions = PieSocketOptions()
        pieSocketOptions.clusterId = "s15618.blr1"
        pieSocketOptions.apiKey = "g9a6EyKv7khUU1rttVQoU1LlFmkyqX6Wiu1cT2Cj"


        piesocket = PieSocket(pieSocketOptions)
        channel = piesocket.join(channelName)


        channel.listen("system:connected", object : PieSocketEventListener() {
            override fun handleEvent(event: PieSocketEvent?) {
                Log.d("PIESOCKET-SDK", "Channel connected")
                Handler(Looper.getMainLooper()).post {
                    val snackbar =
                        Snackbar.make(binding.root, "Channel Connected", Snackbar.LENGTH_SHORT)

                    val view = snackbar.view
                    val params = view.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.CENTER_VERTICAL
                    view.layoutParams = params

                    snackbar.show()

                }
            }
        })

        channel.listen("system:message", object : PieSocketEventListener() {
            override fun handleEvent(event: PieSocketEvent) {
                Log.d("PIESOCKET-SDK", "New message received: " + event.getData() + event)
                if (event.data.isNotEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        messageList.add(ChatMessage(event.data, false))
                        chatAdapter.notifyItemInserted(messageList.size - 1)
                        binding.chatRv.scrollToPosition(messageList.size - 1)
                    }

                }
            }
        })
    }


}