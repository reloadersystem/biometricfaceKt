package com.reloader.biometricface.domain

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by Reloader on 10/7/19.
 */


interface MethodWs {
    @GET("photos.json")
    fun getRecursos(): Call<ResponseBody>
}

