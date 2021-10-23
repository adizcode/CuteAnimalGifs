package com.github.adizcode.cuteanimalgifs.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.github.adizcode.cuteanimalgifs.adapter.CuteAnimalGifsAdapter
import com.github.adizcode.cuteanimalgifs.databinding.ActivityMainBinding
import com.github.adizcode.cuteanimalgifs.model.CuteAnimalGif
import com.github.adizcode.cuteanimalgifs.network.CuteAnimalGifsApiService
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val baseUrl = "https://g.tenor.com/"
private const val apiKey = "KSYBKKTS489O"

class MainActivity : AppCompatActivity() {
    private val list = mutableListOf<CuteAnimalGif>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CuteAnimalGifsAdapter
    private lateinit var cuteAnimalGifsApiService: CuteAnimalGifsApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Set Up Recycler View */

        adapter = CuteAnimalGifsAdapter(list, Glide.with(this))
        val layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager


        /* Instantiate the API Service */

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        cuteAnimalGifsApiService = retrofit.create(CuteAnimalGifsApiService::class.java)


        /* Fetch initial data */

        enqueueRequest(pos = 0)
    }

    /* Network Call */

    private fun enqueueRequest(pos: Int) {
        val call = cuteAnimalGifsApiService.getJsonObjectResponse(key = apiKey, pos = pos)

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                if (!response.isSuccessful) {
                    Log.e("API Call Unsuccessful", response.code().toString())
                    return
                }

                val newList = response.body()?.getAsJsonArray("results")?.map {

                    CuteAnimalGif(
                        it.asJsonObject.getAsJsonArray("media")[0].asJsonObject.getAsJsonObject(
                            "gif"
                        ).getAsJsonPrimitive("url").asString
                    )
                }!!

                list.addAll(newList.shuffled())
                adapter.notifyItemRangeInserted(0, newList.size)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("API Call Failed", t.message.toString())
            }
        })
    }
}