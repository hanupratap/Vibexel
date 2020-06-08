package com.example.pictures

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pictures.models.UnsplashPhoto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_pic.view.*


class Adapter(var data: MutableList<UnsplashPhoto>?, val context: Context) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {


    var page: Int = 1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.pic_holder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_pic, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (data != null) {
            return data!!.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val temp: String =  data?.get(position)?.likes.toString() + " Likes"
//        holder.pic_name.text = temp
        if (data != null) {
            Picasso
                .get() // give it the context
                .load(data!![position].urls.small)
                .into(holder.image)
        }

        val wallpaper: UnsplashPhoto? = data?.get(position)




        holder.image.setOnClickListener {

            val intent = Intent(context, ImagePreview::class.java)
            intent.putExtra("wallpaper", wallpaper)
            context.startActivity(intent)
        }
    }

}