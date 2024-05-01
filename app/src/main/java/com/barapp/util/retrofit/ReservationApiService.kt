package com.barapp.util.retrofit

import com.barapp.barapp.model.Reserva
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ReservationApiService {
    @GET("/api/reservas")
    fun getAllReservations(@QueryMap options: Map<String, String>?): Call<List<Reserva>>

    @GET("/api/reservas/{id}")
    fun getReservation(@Path("id") id: String): Call<Reserva>
}