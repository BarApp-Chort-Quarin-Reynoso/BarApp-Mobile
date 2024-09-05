package com.barapp.data.retrofit

import com.barapp.barapp.model.Reserva
import com.barapp.model.EstadoReserva
import com.barapp.model.Opinion
import com.barapp.model.Restaurante
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ReservationApiService {
  @GET("/api/reservas")
  fun getAllReservations(@QueryMap options: Map<String, String>?): Call<List<Reserva>>

  @GET("/api/reservas/{id}")
  fun getReservation(@Path("id") id: String): Call<Reserva>

  @POST("/api/reservas/{id}/opinar")
  fun sendReview(@Path("id") id: String, @Body opinion: Opinion): Call<Opinion>

  @GET("/api/reservas/usuario/{id}")
  fun getReservationsByUser(@Path("id") id: String): Call<List<Reserva>>

  @GET("/api/reservas/ultimas")
  fun getLastReservationsByUserRestaurant(
    @Query("idRestaurante") idRestaurante: String,
    @Query("idUsuario") idUsuario: String,
    @Query("cantidad") cantidad: Int
  ): Call<List<Reserva>>

  @GET("/api/reservas/{idReserva}/concretar")
  fun concretarReserva(@Path("idReserva") idReserva: String, @Query("idUsuario") idUsuario: String, @Query("idRestaurante") idRestaurante: String): Call<Reserva>

  @POST("/api/reservas/{id}")
  fun createReservation(@Path("id") id: String, @Body reserva: Reserva): Call<String>

  @PATCH("/api/reservas/{id}/estado")
  fun updateReservation(
    @Path("id") id: String,
    @Query("estado") estado: EstadoReserva
  ): Call<Reserva>
}