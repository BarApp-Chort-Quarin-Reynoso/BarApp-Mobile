package com.barapp.data.retrofit

import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UserApiService {
  @GET("/api/usuarios")
  fun getAllUsers(@QueryMap options: Map<String, String>?): Call<List<Usuario>>

  @GET("/api/usuarios/{id}")
  fun getUser(@Path("id") id: String): Call<Usuario>

  @POST("/api/usuarios")
  fun createUser(@Body usuario: Usuario): Call<Usuario>

  @PUT("/api/usuarios/{id}")
  fun updateUser(@Path("id") id: String, @Body usuario: Usuario): Call<Usuario>

  @PATCH("/api/usuarios/{id}/restaurantes-favoritos")
  fun updateFavoriteRestaurants(@Path("id") id: String, @Body restauranteId: String): Call<Usuario>

  @PATCH("/api/usuarios/{id}/busquedas-recientes")
  fun updateRecentSearches(@Path("id") id: String, @Body busqueda: String): Call<Usuario>

  @PATCH("/api/usuarios/{id}/foto")
  fun updatePhoto(@Path("id") id: String, @Body foto: String): Call<Void>

  @GET("/api/usuarios/{id}/favoritos")
    fun getFavoriteRestaurants(@Path("id") id: String): Call<List<Restaurante>>
}