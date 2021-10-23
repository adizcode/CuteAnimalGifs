package com.github.adizcode.cuteanimalgifs.activity

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
private const val spanCount = 2

class MainActivity : AppCompatActivity() {
    private val list = mutableListOf<CuteAnimalGif>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CuteAnimalGifsAdapter
    private lateinit var cuteAnimalGifsApiService: CuteAnimalGifsApiService
    private var isScrolling = false
    private var itemsLoaded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Set Up Recycler View */

        adapter = CuteAnimalGifsAdapter(list, Glide.with(this))
        val layoutManager = StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL)

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

                // The user is trying to scroll
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Get positions of the last completely visible items
                val lastCompletelyVisibleItems =
                    layoutManager.findLastCompletelyVisibleItemPositions(null)

                // The user just made an attempt to scroll
                if (isScrolling) {

                    // The last or second last item is completely visible
                    if (lastCompletelyVisibleItems.contains(itemsLoaded - 2) || lastCompletelyVisibleItems.contains(
                            itemsLoaded - 1
                        )
                    ) {

                        // Show loader
                        binding.loader.visibility = View.VISIBLE

                        // Fetch next chunk of GIFs
                        enqueueRequest()
                    }
                }

                // Scrolling has stopped
                isScrolling = false
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