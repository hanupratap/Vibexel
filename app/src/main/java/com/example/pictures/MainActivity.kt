package com.example.pictures

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView

import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

external fun decodeURIComponent(encodedURI: String): String

class MainActivity : AppCompatActivity() {
    private val okClient by lazy { OkHttpClient() }

    val SEARCH_SERVICE:String = "My Service"
    val api_key: String = "16763655-155c7893c0bd18e6e492c8171";



    private var request: Request? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.elevation = 0F;

        val layouManager:StaggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layouManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layouManager
        val query = ""
        val URL = "https://pixabay.com/api/?key=" + api_key + "&q=" + query;
        CoroutineScope(Main).launch {
            myfunc(URL)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.my_menu, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query1: String?): Boolean {
                    query1?.replace(' ', '+');
                    val URL:String = "https://pixabay.com/api/?key=" + api_key + "&q=" + query1;
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



    suspend fun myfunc(URL:String) {
        withContext(Main) {

            request = URL?.let {
                Request.Builder()
                    .url(it)
                    .build()
            };
            recyclerView.adapter = null

            request?.let {
                okClient.newCall(it).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response?.body?.string()
                        Log.d("THIS --- ",  body)

                        val gson = Gson()
                        val obj:PicModel = gson.fromJson(body, PicModel::class.java)

                        val adapter:Adapter = Adapter(obj, this@MainActivity)

                        runOnUiThread(
                            object : Runnable{
                                override fun run() {
                                    recyclerView.adapter = adapter

                                }
                            }
                        )

                        Log.d("TJOS", "onResponse: ");
                    }
                })
            }

        }


    }


}
