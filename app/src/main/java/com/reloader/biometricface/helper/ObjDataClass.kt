package com.reloader.biometricface.helper

import android.content.Context
import com.reloader.biometricface.R

object ObjDataClass {

    fun getDataPhotos(context: Context): List<ListUrlPhotos> {

        val ssd_imagenes: String = context.getString(R.string.ssd_imagenes)
        val dataList: MutableList<ListUrlPhotos> = arrayListOf()

        dataList.add(ListUrlPhotos(ssd_imagenes + "img_01.jpg", "Actual"))
        dataList.add(ListUrlPhotos(ssd_imagenes + "img_02.jpg", "Anterior1"))
        dataList.add(ListUrlPhotos(ssd_imagenes + "img_03.jpg", "Anterior2"))
        dataList.add(ListUrlPhotos(ssd_imagenes + "img_04.jpg", "Anterior3"))
        return dataList.toList()
    }
}