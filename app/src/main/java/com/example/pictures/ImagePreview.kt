package com.example.pictures

import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.WallpaperManager

import android.content.Context
import android.content.DialogInterface

import android.graphics.Bitmap

import android.graphics.Point

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore

import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pictures.models.UnsplashPhoto

import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_image_preview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImagePreview : AppCompatActivity(), Target {
    lateinit var progresDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.pictures.R.layout.activity_image_preview)


        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Please wait")
        progresDialog.setMessage("Loading image")
        progresDialog.show()

        val wallpaper: UnsplashPhoto? = intent.extras?.getParcelable<UnsplashPhoto>("wallpaper")

        val wallpaperUrl: String? = wallpaper?.urls?.full


        if (wallpaper != null) {
            val temp: String = "by " + wallpaper.user.name
            photo_owner.text = temp
        }


//        try
//        {
//            val myColor = Color.parseColor(wallpaper?.color)
//            if(myColor == Color.BLACK || myColor == Color.TRANSPARENT)
//            {
//                fab.supportBackgroundTintList = ColorStateList.valueOf(Color.WHITE)
//                fab.foregroundTintList = ColorStateList.valueOf(Color.BLACK)
//            }
//            else{
//                fab.supportBackgroundTintList = ColorStateList.valueOf(myColor)
//
//            }
//
//        }
//        catch (e:Exception)
//        {
//            e.printStackTrace()
//        }


        supportActionBar?.hide()


        val wm: WindowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        wm.defaultDisplay.getSize(size)

        if (wallpaper != null) {
            Picasso.get().load(wallpaperUrl).resize(wallpaper.width, size.y).centerInside()
                .into(this)
        }


    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        Toast.makeText(this, "Preparing image", Toast.LENGTH_LONG).show()
    }

    override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
        Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()

        progresDialog.dismiss()
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

        progresDialog.dismiss()

        val wm: WindowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager


        val imageView = image_prev

        imageView.setImageBitmap(bitmap)

        fab1.setOnClickListener {
            showSnackbar("Downloading Image")

            MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                intent.getStringExtra("title"),
                intent.getStringExtra("description")
            )

        }

        fab.setOnClickListener {


            AlertDialog.Builder(this)
                .setTitle("Select Options")
                .setPositiveButton("Home") { _: DialogInterface?, _: Int ->
                    showSnackbar("Wallpaper will be applied shortly, please wait...")
                    CoroutineScope(IO).launch {


                        val wallpaperManager: WallpaperManager =
                            WallpaperManager.getInstance(this@ImagePreview)

                        withContext(IO)
                        {
                            wallpaperManager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_SYSTEM
                            )
                        }


                    }
                }
                .setNegativeButton("Lock") { dialog: DialogInterface?, which: Int ->
                    showSnackbar("Wallpaper will be applied shortly, please wait...")
                    CoroutineScope(IO).launch {


                        val wallpaperManager: WallpaperManager =
                            WallpaperManager.getInstance(this@ImagePreview)



                        withContext(IO)
                        {
                            wallpaperManager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_LOCK
                            )
                        }


                    }
                }
                .setNeutralButton("Cancel", null)
                .create()
                .show()


        }


    }


    fun showSnackbar(message: String) {
        runOnUiThread {
            Snackbar.make(
                relativeLayout,
                message,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }


}


