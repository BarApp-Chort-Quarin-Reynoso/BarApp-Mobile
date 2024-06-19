package com.barapp.data.retrofit

import com.barapp.model.DetalleRestaurante
import com.barapp.model.Opinion
import com.barapp.model.Restaurante
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface RestaurantApiService {
  @GET("/api/restaurantes")
  fun getAllRestaurants(@QueryMap options: Map<String, String>?): Call<List<Restaurante>>

  @GET("/api/restaurantes/{id}")
  fun getRestaurantById(@Path("id") id: String): Call<Restaurante>

  @GET("/api/restaurantes/detalle/{id}")
  fun getRestaurantDetailById(@Path("id") id: String): Call<DetalleRestaurante>

//  @POST("/api/restaurantes/{id}/opinar")
//  fun reviewRestaurant(@Path("id") id: String, @Body opinion: Opinion): Call<Restaurante>

}