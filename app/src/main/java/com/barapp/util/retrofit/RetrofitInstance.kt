package com.barapp.util.retrofit

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
  private const val BASE_URL = "http://172.28.175.28:8080/"

  private lateinit var context: Context

  fun initialize(context: Context) {
    this.context = context
  }

  private val client = OkHttpClient.Builder()
    .addInterceptor(Interceptor { chain ->
      val original = chain.request()

      val sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
      val jsessionId = sharedPref.getString("JSESSIONID", "")

      println("JSESSIONID: $jsessionId")

      val request = original.newBuilder()
        .header("Cookie", "JSESSIONID=$jsessionId")
        .method(original.method(), original.body())
        .build()

      chain.proceed(request)
    })
    .build()

  private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  fun <T> createService(serviceClass: Class<T>): T {
    return retrofit.create(serviceClass)
  }
}