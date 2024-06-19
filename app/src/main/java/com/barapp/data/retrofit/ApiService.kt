package com.barapp.data.retrofit

import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ApiService {
  @GET("/api/usuarios")
  fun getAllUsers(@QueryMap options: Map<String, String>): Call<List<Usuario>>

  @GET("/api/usuarios/{id}")
  fun getUser(@Path("id") id: Int): Call<Usuario>

  @POST("/api/usuarios")
  fun createUser(@Body usuario: Usuario): Call<Usuario>

  @DELETE("/api/usuarios/{id}")
  fun deleteUser(@Path("id") id: Int): Call<Void>

  @GET("/api/restaurantes")
  fun getAllRestaurants(@QueryMap options: Map<String, String>): Call<List<Restaurante>>

  @GET("/api/restaurantes/{id}")
  fun getRestaurant(@Path("id") id: Int): Call<Restaurante>

  @POST("/api/restaurantes")
  fun createRestaurant(@Body restaurante: Restaurante): Call<Restaurante>

  @DELETE("/api/restaurantes/{id}")
  fun deleteRestaurant(@Path("id") id: Int): Call<Void>

}