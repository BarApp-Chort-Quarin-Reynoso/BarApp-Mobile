package com.barapp.data.repositories

import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.retrofit.RestaurantApiService
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.model.HorarioConCapacidadDisponible
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class RestauranteRepository private constructor() : IGenericRepository<Restaurante> {
  private val api = RetrofitInstance.createService(RestaurantApiService::class.java)

  override fun buscarPorId(id: String, callback: FirestoreCallback<Restaurante>) {
    Timber.d("Buscando restaurante con id: $id")
    api.getRestaurantById(id).enqueue(object : Callback<Restaurante> {
      override fun onResponse(call: Call<Restaurante>, response: Response<Restaurante>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurante recibido: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurante"))
        }
      }

      override fun onFailure(call: Call<Restaurante>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  override fun buscarTodos(callback: FirestoreCallback<List<Restaurante>>) {
    Timber.d("Buscando todos los restaurantes")
    api.getAllRestaurants().enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(
        call: Call<List<Restaurante>>,
        response: Response<List<Restaurante>>
      ) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurantes recibidos: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurantes"))
        }
      }

      override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun buscarHorariosPorCorreo(correo: String, mesAnio: String, cantMesas: Int, callback: FirestoreCallback<Map<String, Map<String, HorarioConCapacidadDisponible>>>) {
    Timber.d("Buscando horarios para restaurante con correo: $correo")
    api.getRestaurantHours(correo, mesAnio, cantMesas).enqueue(object : Callback<Map<String, Map<String, HorarioConCapacidadDisponible>>> {
        override fun onResponse(call: Call<Map<String, Map<String, HorarioConCapacidadDisponible>>>, response: Response<Map<String, Map<String, HorarioConCapacidadDisponible>>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Horarios restaurante: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error horarios restaurante: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Horarios"))
        }
      }

      override fun onFailure(call: Call<Map<String, Map<String, HorarioConCapacidadDisponible>>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun buscarDestacados(callback: FirestoreCallback<List<Restaurante>>) {
    api.getFeaturedRestaurants().enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(
        call: Call<List<Restaurante>>,
        response: Response<List<Restaurante>>
      ) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurantes destacados: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error restaurantes destacados: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurantes"))
        }
      }

      override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  override fun guardar(entidad: Restaurante) {}

  override fun actualizar(entidad: Restaurante) {}

  override fun borrar(entidad: Restaurante) {}

  fun getRestaurantesPorArea(
    northeast: LatLng,
    southwest: LatLng,
    callback: FirestoreCallback<List<Restaurante>>
  ) {
    api.getRestaurantsByArea(
      northeast.latitude.toString(),
      northeast.longitude.toString(),
      southwest.latitude.toString(),
      southwest.longitude.toString()
    ).enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(
        call: Call<List<Restaurante>>,
        response: Response<List<Restaurante>>
      ) {
        if (response.isSuccessful) {
          callback.onSuccess(response.body() ?: ArrayList())
        } else {
          Timber.e("Error buscando restaurantes por area: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurantes"))
        }
      }

      override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  companion object {
    @JvmStatic
    val instance = RestauranteRepository()
  }
}
