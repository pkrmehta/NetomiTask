package com.pkdev.netomitask.Repository

import com.pkdev.netomitask.Model.ListConnection
import com.pkdev.netomitask.Network.Retrofit

class ChatRepository {
    suspend fun fetchUsers(): List<ListConnection> {
        return Retrofit.api.getConnectionList()
    }
}