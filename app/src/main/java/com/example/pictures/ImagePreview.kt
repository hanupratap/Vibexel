package com.example.pictures

import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_image_preview.*
import java.io.File


class ImagePreview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.pictures.R.layout.activity_image_preview)

        val progresDialog: ProgressDialog = ProgressDialog(this)
        progresDialog.setTitle("Downloading Image")
        progresDialog.show()

        val wallpaperUrl:String = intent.getStringExtra("image")

        supportActionBar?.hide()
        try {
            Picasso.get()
                .load(wallpaperUrl)
                .into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        progresDialog.dismiss()

                    }

                    @RequiresApi(Build.VERSION_CODES.Q)
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {


                        progresDialog.dismiss()

                        val wm: WindowManager =
                            getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        val display: Display = wm.defaultDisplay


                        val imageView:PhotoView = image_prev

                        imageView.setImageBitmap(bitmap)


                        fab.setOnClickListener{
                            try {
                                val wallpaperManager: WallpaperManager =
                                    WallpaperManager.getInstance(this@ImagePreview)

                                val metrics:DisplayMetrics = DisplayMetrics()
                                windowManager.defaultDisplay.getMetrics(metrics)
                                val height = metrics.heightPixels
                                val width = metrics.widthPixels
                                if(bitmap!=null)
                                {
                                    val bitmap1:Bitmap = Bitmap.createScaledBitmap(bitmap,2*width, height, true )
                                    wallpaperManager.setBitmap(bitmap, null, true)

                                }
 
                                wallpaperManager.suggestDesiredDimensions(
                                    height,
                                    width
                                )
                            }
                            catch (e:java.lang.Exception)
                            {
                                e.printStackTrace()
                            }

                        }


//                            AlertDialog.Builder(context)
//                                .setTitle("Confirm Wallpaper")
//                                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
//                                    try {
//
//
//                                        val wallpaperManager: WallpaperManager =
//                                            WallpaperManager.getInstance(context)
//
//                                        wallpaperManager.setBitmap(bitmap, null, true)
//
//
//                                        wallpaperManager.suggestDesiredDimensions(
//                                            display.width,
//                                            display.height
//                                        );
//
//
//                                        if (bitmap != null) {
//                                            storeImage(
//                                                bitmap,
//                                                data.hits.get(position).id.toString()
//                                            )
//                                        }
//
//
//                                    } catch (e: java.lang.Exception) {
//
//                                    }
//                                }
//                                .setNegativeButton("No", null)
//                                .show()


                    }

                })
        } catch (e: java.lang.Exception) {
            progresDialog.dismiss()
        }
    }


}