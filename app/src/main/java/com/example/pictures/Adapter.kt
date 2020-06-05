package com.example.pictures


import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.scale
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_pic.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class Adapter(val data:PicModel, val context: Context): RecyclerView.Adapter<Adapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val image:ImageView = itemView.pic_holder
        val pic_name:TextView = itemView.pic_text

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.item_pic, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.hits.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val temp:String = "Favorite : " + data.hits.get(position).favorites.toString()
        holder.pic_name.text = temp
        Picasso
            .get() // give it the context
            .load(data.hits.get(position).webformatURL)
            .into(holder.image)

        var wallpaperUrl:String? = data.hits.get(position).fullHDURL
            if(data.hits.get(position).fullHDURL==null)
            {
                wallpaperUrl = data.hits.get(position).largeImageURL

            }
        else
        {
            wallpaperUrl = data.hits.get(position).imageURL
        }




        fun  storeImage(image:Bitmap, id:String) {
            val externalStorageState = Environment.getExternalStorageState()
            if(externalStorageState.equals(Environment.MEDIA_MOUNTED))
            {
                val storageDirectory = Environment.getExternalStorageDirectory().toString()
                val file = File(storageDirectory, id)
                try {
                    val stream: OutputStream = FileOutputStream(file)
                    image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                    stream.close()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else
            {

            }
        }


        holder.image.setOnClickListener{
            val progresDialog:ProgressDialog = ProgressDialog(context)
            progresDialog.setTitle("Downloading Image")
            progresDialog.show()

            try {
                Picasso.get()
                    .load(wallpaperUrl)
                    .into(object : Target{
                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                        }

                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                            progresDialog.dismiss()

                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {


                            progresDialog.dismiss()

                            val wm:WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            val display:Display = wm.defaultDisplay


                            AlertDialog.Builder(context)
                                .setTitle("Confirm Wallpaper")
                                .setPositiveButton("Yes"){
                                        dialog: DialogInterface?, which: Int ->
                                    try {



                                        val wallpaperManager:WallpaperManager = WallpaperManager.getInstance(context)

                                            wallpaperManager.setBitmap(bitmap, null, true)


                                        wallpaperManager.suggestDesiredDimensions(display.width , display.height);


                                        if (bitmap != null) {
                                            storeImage(bitmap, data.hits.get(position).id.toString())
                                        }


                                    } catch (e: java.lang.Exception){

                                    }
                                }
                                .setNegativeButton("No", null)
                                .show()




                        }

                    })
            }
            catch (e:java.lang.Exception)
            {
                progresDialog.dismiss()
            }


        }
        
    }
}