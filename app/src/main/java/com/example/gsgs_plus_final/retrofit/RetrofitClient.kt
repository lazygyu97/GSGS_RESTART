package com.example.tmaptest.retrofit

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var instance: Retrofit? = null
    private val gson = GsonBuilder().setLenient().create() //gson가져오기 밑에서 자동 converting 세팅 할 때 씀
    private const val BASE_URL = "https://apis.openapi.sk.com" // 기본 URL은 무조건 "/"전까지만 쓰기


    fun getInstance(): Retrofit {

        //Logger 만들기
        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }


        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL) // 기본 URL 세팅
                .client(client) //Logger 세팅
                .addConverterFactory(GsonConverterFactory.create(gson)) //Json을 자동으로 data class로 convert하는 부분
                .build()
        }

        return instance!!
    }

}