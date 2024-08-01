package com.barapp.data.retrofit

import com.barapp.model.DetalleRestaurante
import com.barapp.model.Horario
import com.barapp.model.Restaurante
import com.barapp.model.RestauranteUsuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface RestaurantApiService {
  @GET("/api/restaurantes")
  fun getAllRestaurants(@QueryMap options: Map<String, String>?): Call<List<Restaurante>>

  @GET("/api/restaurantes/{id}")
  fun getRestaurantById(@Path("id") id: String): Call<Restaurante>

  @GET("/api/restaurantes/detalle/{id}")
  fun getRestaurantDetailById(@Path("id") id: String): Call<DetalleRestaurante>

  @GET("/api/restaurantes/{correo}/horarios")
  fun getRestaurantHours(@Path("correo") correo: String, @Query("mesAnio") mesAnio: String): Call<Map<String, List<Horario>>>

  @POST("/api/restaurantes/{id}/favoritos")
  fun addFavoriteRestaurant(@Path("id") id: String, @Body restaurante: RestauranteUsuario): Call<Restaurante>

  @DELETE("/api/restaurantes/{id}/favoritos")
  fun deleteFavoriteRestaurant(@Path("id") id: String): Call<Void>

  @POST("/api/restaurantes/{id}/vistos-recientemente")
  fun addSeenRecentlyRestaurant(@Path("id") id: String, @Body restaurante: RestauranteUsuario): Call<Restaurante>
}