package com.barapp.data.repositories

import com.barapp.data.entities.RestauranteUsuarioEntity
import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.mappers.RestauranteMapper.fromEntity
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuarioEntity
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.data.retrofit.UserApiService
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class RestauranteFavoritoRepository private constructor() {

  private val api = RetrofitInstance.createService(UserApiService::class.java)

  /**
   * El metodo busca un restaurante en la coleccion de restaurantes favoritos segun el id compuesto
   *
   * @param idCompuesto se compone de el restaurante.getId()+idUsuario
   * @param callback
   */
  fun buscarPorId(idCompuesto: String, callback: FirestoreCallback<Restaurante>) {
    db
      .collection(COLECCION_RESTAURANTES_FAVORITOS)
      .document(idCompuesto)
      .get()
      .addOnSuccessListener { documentSnapshot ->
        val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
        val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
        callback.onSuccess(fromEntity(restauranteEntity!!, ubicacionRestaurante, null))
      }
      .addOnFailureListener { e -> callback.onError(e) }
  }
  private val userAPI = RetrofitInstance.createService(UserApiService::class.java)

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
//    val listaRestaurantesFavoritos = mutableListOf<Restaurante>()
//    db
//      .collection(COLECCION_RESTAURANTES_FAVORITOS)
//      .whereEqualTo("idUsuario", idUsuario)
//      .get()
//      .addOnCompleteListener { task ->
//        if (task.isSuccessful) {
//          for (documentSnapshot in task.result) {
//            val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
//            val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
//            listaRestaurantesFavoritos.add(
//              fromEntity(restauranteEntity, ubicacionRestaurante, null)
//            )
//          }
//          callback.onSuccess(listaRestaurantesFavoritos)
//        } else {
//          Timber.w("Error recuperando Restaurantes favoritos")
//          callback.onError(task.exception!!)
//        }
//      }
  }

  fun guardar(entidad: Restaurante, idUsuario: String) {
    val restauranteFavoritoEntity = toRestauranteUsuarioEntity(entidad, idUsuario)
    db
      .collection(COLECCION_RESTAURANTES_FAVORITOS)
      .document(restauranteFavoritoEntity.idRestauranteUsuario)
      .set(restauranteFavoritoEntity)
      .addOnSuccessListener { Timber.d("Restaurante favorito guardado con exito") }
      .addOnFailureListener { e -> Timber.e(e, "Error guardado restaurante favorito") }
  }

  fun actualizar(entidad: Restaurante) {}

  fun borrar(entidad: Restaurante, idUsuario: String) {
    db
      .collection(COLECCION_RESTAURANTES_FAVORITOS)
      .document(entidad.id + idUsuario)
      .delete()
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          Timber.d("Restaurante favorito eliminado con éxito")
        } else {
          Timber.e(task.exception)
        }
      }
  }

  companion object {
    @JvmStatic val instance = RestauranteFavoritoRepository()
  }
}
