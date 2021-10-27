package com.github.adizcode.cuteanimalgifs.activity

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
private const val spanCountPortrait = 2
private const val spanCountLandscape = 4

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CuteAnimalGifsAdapter
    private lateinit var cuteAnimalGifsApiService: CuteAnimalGifsApiService

    private val list = mutableListOf<CuteAnimalGif>()

    private var spanCount = spanCountPortrait
    private var itemsLoaded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spanCount =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) spanCountPortrait else spanCountLandscape

        /* Set Up Recycler View */

        adapter = CuteAnimalGifsAdapter(list, Glide.with(this))
        val layoutManager =
            StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager


        /* Instantiate the API Service */

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        cuteAnimalGifsApiService = retrofit.create(CuteAnimalGifsApiService::class.java)


        /* Fetch initial data */

        enqueueRequest()


        /* Pagination: Load more GIFs  */

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // RecyclerView cannot be scrolled down anymore - EOL reached
                if (!recyclerView.canScrollVertically(1)) {

                    // Show loader
                    binding.loader.visibility = View.VISIBLE

                    // Fetch next chunk of GIFs
                    enqueueRequest()
                }
            }
        })
    }

    /* Network Call */

    private fun enqueueRequest() {

        // Call with updated query parameters
        val call = cuteAnimalGifsApiService.getJsonObjectResponse(key = apiKey, pos = itemsLoaded)

        // Asynchronous HTTP request
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                if (!response.isSuccessful) {
                    Log.e("API Call Unsuccessful", response.code().toString())
                    return
                }

                // Parse response
                val newList = response.body()?.getAsJsonArray("results")?.map {

                    CuteAnimalGif(
                        it.asJsonObject.getAsJsonArray("media")[0].asJsonObject.getAsJsonObject(
                            "gif"
                        ).getAsJsonPrimitive("url").asString
                    )
                }!!

                // Randomize order of GIFs within this chunk, and append to the data set
                list.addAll(newList.shuffled())

                // Update UI
                adapter.notifyItemRangeInserted(itemsLoaded, newList.size)

                // Update the position query parameter
                itemsLoaded += newList.size

                // Hide loader
                binding.loader.visibility = View.GONE
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("API Call Failed", t.message.toString())
            }
        })
    }
}