package com.barapp.data.retrofit

import com.barapp.model.DetalleUsuario
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.util.LinkedList

interface UserApiService {
  @GET("/api/usuarios")
  fun getAllUsers(@QueryMap options: Map<String, String>?): Call<List<Usuario>>

  @GET("/api/usuarios/{id}")
  fun getUser(@Path("id") id: String): Call<Usuario>

  @GET("/api/usuarios/detalle/{id}")
  fun getUserDetailById(@Path("id") id: String): Call<DetalleUsuario>

  @POST("/api/usuarios")
  fun createUser(@Body usuario: Usuario): Call<Usuario>

  @PUT("/api/usuarios/{id}")
  fun updateUser(@Path("id") id: String, @Body usuario: Usuario): Call<Usuario>

  @PATCH("/api/usuarios/detalle/{id}/restaurantes-favoritos")
  fun updateFavoriteRestaurants(@Path("id") id: String, @Body idRestaurantesFavoritos: HashSet<String>): Call<DetalleUsuario>

  @PATCH("/api/usuarios/detalle/{id}/busquedas-recientes")
  fun updateRecentSearches(@Path("id") id: String, @Body busquedasRecientes: LinkedList<String>): Call<Void>

  @PATCH("/api/usuarios/{id}/foto")
  fun updatePhoto(@Path("id") id: String, @Body foto: String): Call<Void>

  @GET("/api/usuarios/{id}/favoritos")
  fun getFavoriteRestaurants(@Path("id") id: String): Call<List<Restaurante>>

  @GET("/api/usuarios/{id}/vistos-recientemente")
  fun getRecentlySeenRestaurants(@Path("id") id: String): Call<List<Restaurante>>
}