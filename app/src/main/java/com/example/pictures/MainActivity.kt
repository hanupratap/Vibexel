package com.example.pictures

import android.app.SearchManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.pictures.models.SearchResponse
import com.example.pictures.models.UnsplashPhoto
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image_preview.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

import kotlin.Exception

external fun decodeURIComponent(encodedURI: String): String

class MainActivity : AppCompatActivity() {
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    private val okClient by lazy { OkHttpClient() }


    lateinit var URL: String
    var mainPicList: MutableList<UnsplashPhoto>? = ArrayList()

    lateinit var query: String
    var page: Int = 1

    var lastVisibleItem: Int? = null

    lateinit var access_key: String

    lateinit var adapter: Adapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        adapter = Adapter(mainPicList, this@MainActivity)


        access_key = resources.getString(R.string.unsplash_access_key)
        supportActionBar?.elevation = 0F;


        val layouManager: StaggeredGridLayoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        layouManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layouManager
        recyclerView.adapter = adapter


        query = "random"
        URL =
            "https://api.unsplash.com/search/photos?query=" + query + "&?page=" + page + "&client_id=" + access_key;
        CoroutineScope(Main).launch {
            myfunc(URL)
        }



        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleItemCount = layouManager.childCount
                    val totalItemCount: Int = layouManager.itemCount

                    val lastVisiblePositions =
                        layouManager.findLastCompletelyVisibleItemPositions(null)
                    lastVisibleItem = getLastVisibleItem(lastVisiblePositions)
                    val visibleThreshold = layouManager.spanCount

                    if (visibleItemCount + visibleThreshold >= totalItemCount) {
                        page++
                        URL =
                            "https://api.unsplash.com/search/photos?query=" + query + "&client_id=" + access_key + "&page=" + page

                        CoroutineScope(IO).launch {

                            myfunc(URL)

                        }
                    }

                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })


    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.my_menu, menu)


        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query1: String?): Boolean {
                    query1?.replace(' ', '+')
                    if (query1 != null) {
                        page = 1
                        query = query1
                        URL =
                            "https://api.unsplash.com/search/photos?query=" + query + "&client_id=" + access_key + "&page=" + page
                        adapter.data?.clear()
                    }




                    CoroutineScope(Main).launch {
                        myfunc(URL)

                    }

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {

                    return false
                }

            }
        )
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onCreateOptionsMenu(menu)


    }


    suspend fun myfunc(URL: String) {
        withContext(Main) {


            var request: Request = URL?.let {
                Request.Builder()
                    .url(it)
                    .build()
            }

            try {
                request?.let {
                    okClient.newCall(it).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val body = response?.body?.string()
                            Log.d("THIS --- ", body)

                            val gson = Gson()

                            val obj: SearchResponse? =
                                gson.fromJson(body, SearchResponse::class.java)



                            if (obj?.results != null) {
                                for (i in obj?.results) {
                                    adapter.data?.add(i)
                                }

                            }




                            runOnUiThread(
                                object : Runnable {
                                    override fun run() {
                                        adapter.notifyDataSetChanged()

                                    }
                                }
                            )






                            Log.d("TJOS", "onResponse: ");
                        }
                    })
                }
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }




        }


    }


}
