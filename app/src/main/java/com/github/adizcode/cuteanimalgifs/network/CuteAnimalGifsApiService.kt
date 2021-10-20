package com.github.adizcode.cuteanimalgifs.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET

interface CuteAnimalGifsApiService {
    @GET("v1/search?key=KSYBKKTS489O&q=cute+animals&limit=50")
    fun getJsonObjectResponse(): Call<JsonObject>
}