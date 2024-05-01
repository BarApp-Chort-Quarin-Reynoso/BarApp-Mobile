package com.barapp.util.retrofit

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginService {
  @FormUrlEncoded
  @POST("/login")
  fun login(
    @Field("username") username: String,
    @Field("password") password: String,
    @Field("_csrf") csrf: String
  ): Call<Void>
}