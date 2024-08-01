package com.barapp.data.retrofit

import android.annotation.SuppressLint
import android.content.Context
import com.barapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
object RetrofitInstance {
  private lateinit var context: Context
  private var baseUrl: String = BuildConfig.BASE_URL

  fun initialize(context: Context) {
    RetrofitInstance.context = context
  }

  private val client = OkHttpClient.Builder()
    .addInterceptor(Interceptor { chain ->
      val original = chain.request()

      val sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
      val jsessionId = sharedPref.getString("JSESSIONID", "")

      Timber.d("JSESSIONID: $jsessionId")

      val request = original.newBuilder()
        .header("Cookie", "JSESSIONID=$jsessionId")
        .method(original.method(), original.body())
        .build()

      chain.proceed(request)
    })
    .build()

  private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(client)
      .addConverterFactory(StringConverterFactory())
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  fun <T> createService(serviceClass: Class<T>): T {
    return retrofit.create(serviceClass)
  }
}