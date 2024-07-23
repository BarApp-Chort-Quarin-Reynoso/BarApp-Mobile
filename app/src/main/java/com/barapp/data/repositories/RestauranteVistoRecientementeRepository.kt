package com.barapp.data.repositories

import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.data.retrofit.RestaurantApiService
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.data.retrofit.UserApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.LinkedList
import timber.log.Timber

class RestauranteVistoRecientementeRepository private constructor() {
  private val userAPI = RetrofitInstance.createService(UserApiService::class.java)
  private val restaurantAPI = RetrofitInstance.createService(RestaurantApiService::class.java)

  fun buscarVistosRecientementeDelUsuario(
    idUsuario: String,
    callback: FirestoreCallback<LinkedList<Restaurante>>,
  ) {
    println("Buscando vistos recientemente del usuario con id: $idUsuario")
    userAPI.getRecentlySeenRestaurants(idUsuario).enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurantes vistos recientemente: $data")
          callback.onSuccess(LinkedList(data!!))
        } else {
          Timber.e("Error restaurantes vistos recientemente: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando vistos recientemente"))
        }
      }

      override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun guardar(entidad: Restaurante, idUsuario: String) {
    val restauranteVistoRecientementeEntity = toRestauranteUsuario(entidad)
    restauranteVistoRecientementeEntity.idUsuario = idUsuario
    println("Guardando restaurante visto recientemente: " + restauranteVistoRecientementeEntity.idRestauranteUsuario + " idUsuario: " + idUsuario)
    restaurantAPI.addSeenRecentlyRestaurant(restauranteVistoRecientementeEntity.idRestauranteUsuario, restauranteVistoRecientementeEntity).enqueue(object : Callback<Restaurante> {
      override fun onResponse(call: Call<Restaurante>, response: Response<Restaurante>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurante visto recientemente guardado: $data")
        } else {
          Timber.e("Error guardando restaurante visto recientemente: ${response.errorBody()}")
        }
      }

      override fun onFailure(call: Call<Restaurante>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  companion object {
    @JvmStatic val instance = RestauranteVistoRecientementeRepository()
  }
}
