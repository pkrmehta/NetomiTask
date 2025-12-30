package com.pkdev.netomitask.Network

import com.pkdev.netomitask.Model.ListConnection
import retrofit2.http.GET
import retrofit2.http.Headers

interface ApiService {

    @GET("/api/logs")
    @Headers(
        "key: g9a6EyKv7khUU1rttVQoU1LlFmkyqX6Wiu1cT2Cj",
        "secret: 8SuokAgdW3HwhtNf5vf3NUNr8pbKCFvn"
    )
    suspend fun getConnectionList(): List<ListConnection>
}