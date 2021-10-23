package com.github.adizcode.cuteanimalgifs.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CuteAnimalGifsApiService {
    @GET("v1/search?q=cute+animals&contentfilter=high&media_filer=minimal&limit=20")
    fun getJsonObjectResponse(@Query("key") key: String, @Query("pos") pos: Int): Call<JsonObject>
}