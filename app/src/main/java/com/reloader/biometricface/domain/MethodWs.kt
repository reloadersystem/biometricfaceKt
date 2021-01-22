package com.reloader.biometricface.domain

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Reloader on 10/7/19.
 */


interface MethodWs {
    @GET("{dni}/photos.json")
    fun getRecursos(@Path("dni") imagen: String): Call<ResponseBody>
}

