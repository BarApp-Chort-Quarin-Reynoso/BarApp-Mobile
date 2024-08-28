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
    Timber.d("Buscando favoritos del usuario con id: $idUsuario")
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

  fun guardar(entidad: RestauranteUsuario, idDetalleUsuario: String, callback: FirestoreCallback<List<String>>) {
    Timber.d("Guardando restaurante favorito: " + entidad.idRestauranteUsuario + " del usuario con idDetalleUsuario: $idDetalleUsuario")
    restaurantAPI.addFavoriteRestaurant(entidad.idRestauranteUsuario, entidad, idDetalleUsuario).enqueue(object : Callback<List<String>> {
      override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurante favorito guardado con éxito, lista actualizada de restaurantes favoritos del usuario: $data")
          callback.onSuccess(data!!)
        } else {
          val errorBodyString = response.errorBody()?.string()
          Timber.e("Error guardando restaurante favorito: $errorBodyString")
          callback.onError(Throwable("Error guardando favorito"))
        }
      }

      override fun onFailure(call: Call<List<String>>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  fun borrar(idRestaurante: String, idUsuario: String, idDetalleUsuario: String, callback: FirestoreCallback<List<String>>) {
    Timber.d("Borrando restaurante favorito: $idRestaurante del idUsuario: $idUsuario con idDetalleUsuario: $idDetalleUsuario")
    restaurantAPI.deleteFavoriteRestaurant(idRestaurante, idUsuario, idDetalleUsuario).enqueue(object : Callback<List<String>> {
      override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Restaurante favorito eliminado con éxito, lista actualizada de restaurantes favoritos del usuario: $data")
          callback.onSuccess(data!!)
        } else {
          val errorBodyString = response.errorBody()?.string()
          Timber.e("Error eliminando restaurante favorito: $errorBodyString")
          callback.onError(Throwable("Error eliminando favorito"))
        }
      }

      override fun onFailure(call: Call<List<String>>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  companion object {
    @JvmStatic val instance = RestauranteFavoritoRepository()
  }
}
