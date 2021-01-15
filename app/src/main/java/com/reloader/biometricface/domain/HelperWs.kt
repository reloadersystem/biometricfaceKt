package com.reloader.biometricface.domain

import android.content.Context

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Reloader on 11/11/19.
 */

object HelperWs {

    fun getConfiguration(context: Context): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(39, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://192.169.218.177/PVT/Recursos/Biometric/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    }
}
