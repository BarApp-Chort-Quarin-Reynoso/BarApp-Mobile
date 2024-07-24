package com.barapp.data.repositories

import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.retrofit.RestaurantApiService
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.data.retrofit.UserApiService
import com.barapp.model.RestauranteUsuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class RestauranteFavoritoRepository private constructor() {

  private val userAPI = RetrofitInstance.createService(UserApiService::class.java)
  private val restaurantAPI = RetrofitInstance.createService(RestaurantApiService::class.java)

  fun buscarFavoritosDelUsuario(idUsuario: String, callback: FirestoreCallback<List<Restaurante>>) {
    println("Buscando favoritos del usuario con id: $idUsuario")
    userAPI.getFavoriteRestaurants(idUsuario).enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurantes favoritos: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error restaurantes favoritos: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando favoritos"))
        }
      }

      override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun guardar(entidad: RestauranteUsuario, idUsuario: String) {
    println("Guardando restaurante favorito: " + entidad.idRestauranteUsuario + " idUsuario: " + idUsuario)
    restaurantAPI.addFavoriteRestaurant(entidad.idRestauranteUsuario, entidad).enqueue(object : Callback<Restaurante> {
      override fun onResponse(call: Call<Restaurante>, response: Response<Restaurante>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurante favorito guardado: $data")
        } else {
          val errorBodyString = response.errorBody()?.string()
          Timber.e("Error guardando restaurante favorito: $errorBodyString")
        }
      }

      override fun onFailure(call: Call<Restaurante>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  fun borrar(entidad: Restaurante) {
    println("Borrando restaurante favorito: " + entidad.id)
    restaurantAPI.deleteFavoriteRestaurant(entidad.id).enqueue(object : Callback<Void> {
      override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
          Timber.d("Restaurante favorito eliminado con éxito")
        } else {
          Timber.e(response.errorBody().toString())
        }
      }

      override fun onFailure(call: Call<Void>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  companion object {
    @JvmStatic val instance = RestauranteFavoritoRepository()
  }
}
