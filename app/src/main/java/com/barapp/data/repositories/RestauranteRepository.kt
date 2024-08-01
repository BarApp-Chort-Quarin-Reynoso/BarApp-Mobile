package com.barapp.data.repositories

import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.retrofit.RestaurantApiService
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.model.Horario
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
          Timber.d("Data received: $data")
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
    api.getAllRestaurants(emptyMap()).enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
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

  fun buscarHorariosPorCorreo(correo: String, mesAnio: String, callback: FirestoreCallback<Map<String, List<Horario>>>) {
    Timber.d("Buscando horarios para restaurante con correo: $correo")
    api.getRestaurantHours(correo, mesAnio).enqueue(object : Callback<Map<String, List<Horario>>> {
      override fun onResponse(call: Call<Map<String, List<Horario>>>, response: Response<Map<String, List<Horario>>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Horarios restaurante: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error horarios restaurante: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Horarios"))
        }
      }

      override fun onFailure(call: Call<Map<String, List<Horario>>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun buscarDestacados(callback: FirestoreCallback<List<Restaurante>>) {
    val queryParams = mapOf("puntuacion" to "desc", "limit" to "6")
    api.getAllRestaurants(queryParams).enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
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

  override fun guardar(entidad: Restaurante) {}

  override fun actualizar(entidad: Restaurante) {}

  override fun borrar(entidad: Restaurante) {}

  companion object {
    @JvmStatic val instance = RestauranteRepository()
  }
}
