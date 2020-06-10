package com.example.pictures

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.solver.widgets.WidgetContainer
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.pictures.models.SearchResponse
import com.example.pictures.models.UnsplashPhoto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.w3c.dom.Text
import java.io.IOException
import java.lang.reflect.Type


external fun decodeURIComponent(encodedURI: String): String

class MainActivity : AppCompatActivity() {
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    private val okClient by lazy { OkHttpClient() }


    lateinit var URL: String
    var mainPicList: MutableList<UnsplashPhoto>? = ArrayList()

    var query: String = ""
    var page: Int = 1

    var lastVisibleItem: Int? = null

    lateinit var access_key: String

    lateinit var adapter: Adapter

    fun getQuery(query: String): String {
        if (query.equals("")) {
            URL =
                "https://api.unsplash.com/photos/?" + "page=" + page + "&client_id=" + access_key;
        } else {
            URL =
                "https://api.unsplash.com/search/photos?query=" + query + "&?page=" + page + "&client_id=" + access_key;
        }
        return URL
    }

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


        URL = getQuery("")

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

                    if (lastVisiblePositions[1] + visibleThreshold + 2 >= totalItemCount) {
                        page++
                        URL = getQuery(query)

                        if (query.equals("")) {
                            CoroutineScope(IO).launch {

                                myfunc(URL)

                            }
                        } else {
                            CoroutineScope(IO).launch {

                                myfuncSearch(URL)

                            }
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
                        URL = getQuery(query1)
                        adapter.data?.clear()
                    }




                    CoroutineScope(Main).launch {
                        myfuncSearch(URL)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId)
        {
            R.id.home -> {
                if(!query.equals(""))
                {
                    query = ""
                    CoroutineScope(Main).launch {
                        adapter.data?.clear()
                        myfunc(getQuery(""))
                    }

                }



                return  true
            }
            else -> return true
        }
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
                            Toast.makeText(this@MainActivity, "No Result", Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val body = response?.body?.string()

                            if(response==null || body.equals("Rate Limit Exceeded"))
                            {
                                runOnUiThread(
                                    object : Runnable {
                                        override fun run() {
                                            Toast.makeText(this@MainActivity, "No Result", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                            }
                            else
                            {

                                val gson = Gson()


                                val listType: Type =
                                    object : TypeToken<ArrayList<UnsplashPhoto?>?>() {}.type


                                val obj: List<UnsplashPhoto>
                                obj =
                                    gson.fromJson(body, listType)
                                if (obj != null) {
                                    for (i in obj) {
                                        adapter.data?.add(i)
                                    }
                                }
                                else
                                {
                                    Toast.makeText(this@MainActivity, "No Result", Toast.LENGTH_LONG).show()
                                }

                                runOnUiThread(
                                    object : Runnable {
                                        override fun run() {
                                            adapter.notifyDataSetChanged()

                                        }
                                    }
                                )
                            }

                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

    }

    suspend fun myfuncSearch(URL: String) {
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

                            if(response==null || body.equals("Rate Limit Exceeded"))
                            {
                                runOnUiThread(
                                    object : Runnable {
                                        override fun run() {
                                            Toast.makeText(this@MainActivity, "No Result", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                            }
                            else
                            {
                                val gson = Gson()
                                val obj: SearchResponse
                                obj =
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

                            }


                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }
}
