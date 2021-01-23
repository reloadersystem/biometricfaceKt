package com.reloader.biometricface.ui.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reloader.biometricface.R
import com.reloader.biometricface.helper.ListUrlPhotos
import com.reloader.biometricface.ui.OnPhotosListener

class PhotosAdapterRecycler(val photos: List<ListUrlPhotos>) :
    RecyclerView.Adapter<PhotosAdapterRecycler.MyViewHolderPhotos>() {

    private var onPhotosListener: OnPhotosListener? = null

    fun setOnPhotosListener(onLibrosListener: OnPhotosListener?) {
        this.onPhotosListener = onLibrosListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolderPhotos {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photos, parent, false)
        return MyViewHolderPhotos(view, onPhotosListener)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: MyViewHolderPhotos, position: Int) {

        holder.txtdescripcion.setText(photos[position].descripcion)
        Glide.with(holder.itemView.getContext())
            .load(photos[position].iconModel)
            .into(holder.imgPhotoImage)
    }

    class MyViewHolderPhotos(view: View, onPhotosListener: OnPhotosListener?) :
        RecyclerView.ViewHolder(view) {

        val imgPhotoImage: ImageView
        val cardview_ini: CardView

        val txtdescripcion: TextView = itemView.findViewById(R.id.txt_descripcion)

        init {
            imgPhotoImage = itemView.findViewById(R.id.img_photoitem)
            cardview_ini = itemView.findViewById(R.id.cardview_ini)

            cardview_ini.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    onPhotosListener?.onSelectedImage(position)
                }
            }
        }

    }
}