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
import retrofit2.http.Query
import java.util.LinkedList

interface UserApiService {
  @GET("/api/usuarios/{id}")
  fun getUser(@Path("id") id: String): Call<Usuario>

  @GET("/api/usuarios/detalle/{id}")
  fun getUserDetailById(@Path("id") id: String): Call<DetalleUsuario>

  @POST("/api/usuarios/{id}")
  fun createUser(@Path("id") id: String, @Body usuario: Usuario): Call<String>

  @POST("/api/usuarios/detalle/{id}")
  fun createUserDetail(@Path("id") id: String, @Body detalleUsuario: DetalleUsuario): Call<String>

  @POST("/api/usuarios/registrar")
  fun registerUser(@Query("mail") mail: String, @Query("contrasenia") contrasenia: String): Call<String>

  @PUT("/api/usuarios/{id}")
  fun updateUser(@Path("id") id: String, @Body usuario: Usuario): Call<Usuario>

  @PATCH("/api/usuarios/detalle/{id}/busquedas-recientes")
  fun updateRecentSearches(@Path("id") id: String, @Body busquedasRecientes: LinkedList<String>): Call<Void>

  @PATCH("/api/usuarios/{id}/foto")
  fun updatePhoto(@Path("id") id: String, @Body foto: String): Call<Void>

  @GET("/api/usuarios/{id}/favoritos")
  fun getFavoriteRestaurants(@Path("id") id: String): Call<List<Restaurante>>

  @GET("/api/usuarios/{id}/vistos-recientemente")
  fun getRecentlySeenRestaurants(@Path("id") id: String): Call<List<Restaurante>>
}