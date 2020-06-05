package com.example.pictures

class PicModel (val total:Int, val totalHits:Int, val hits:List<Pic>){

}

class Pic(
  val id:Long,val pageUrl:String,val type:String, val tags:String, val previewURL:String, val previewWidth:Int,
  val previewHeight: Int,val webformatURL:String, val largeImageURL:String,val fullHDURL:String, val imageURL:String,
  val imageWidth:Int, val imageHeight:Int, imageSize:Long, val views:Long, downloads: Long, val favorites: Long, val likes:Long,
  val comments:Long, val user_id:Long, val user:String, val userImageURL:String
){}