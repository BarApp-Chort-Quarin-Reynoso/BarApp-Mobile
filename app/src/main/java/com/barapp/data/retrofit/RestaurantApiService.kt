package com.barapp.data.retrofit

import com.barapp.model.DetalleRestaurante
import com.barapp.model.HorarioConCapacidadDisponible
import com.barapp.model.Opinion
import com.barapp.model.Restaurante
import com.barapp.model.RestauranteUsuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApiService {
  @GET("/api/restaurantes")
  fun getAllRestaurants(): Call<List<Restaurante>>

  @GET("/api/restaurantes/destacados")
  fun getFeaturedRestaurants(): Call<List<Restaurante>>

  @GET("/api/restaurantes/{id}")
  fun getRestaurantById(@Path("id") id: String): Call<Restaurante>

  @GET("/api/restaurantes/{id}/detalle")
  fun getRestaurantDetailById(@Path("id") id: String): Call<DetalleRestaurante>

  @GET("/api/restaurantes/{correo}/horarios")
  fun getRestaurantHours(@Path("correo") correo: String, @Query("mesAnio") mesAnio: String, @Query("cantMeses") cantMeses: Int): Call<Map<String, Map<String, HorarioConCapacidadDisponible>>>

  @POST("/api/restaurantes/{id}/favoritos")
  fun addFavoriteRestaurant(@Path("id") id: String, @Body restaurante: RestauranteUsuario, @Query("idDetalleUsuario") idDetalleUsuario: String): Call<List<String>>

  @DELETE("/api/restaurantes/{id}/favoritos")
  fun deleteFavoriteRestaurant(@Path("id") id: String, @Query("idUsuario") idUsuario: String, @Query("idDetalleUsuario") idDetalleUsuario: String): Call<List<String>>

  @POST("/api/restaurantes/{id}/vistos-recientemente")
  fun addSeenRecentlyRestaurant(@Path("id") id: String, @Body restaurante: RestauranteUsuario): Call<Restaurante>

  @GET("/api/restaurantes/cercanos")
  fun getRestaurantsByArea(@Query("neLat") neLat: String, @Query("neLon") neLon: String, @Query("swLat") swLat: String, @Query("swLon") swLon: String, ): Call<List<Restaurante>>

  @GET("/api/restaurantes/{id}/opiniones")
    fun getReviewsByRestaurant(@Path("id") id: String): Call<List<Opinion>>
}