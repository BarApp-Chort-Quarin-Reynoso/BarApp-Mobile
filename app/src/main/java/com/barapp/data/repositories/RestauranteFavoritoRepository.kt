package com.barapp.data.repositories

import com.barapp.data.entities.RestauranteEntity
import com.barapp.model.Restaurante
import com.barapp.model.Ubicacion
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.mappers.RestauranteMapper.fromEntity
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuarioEntity
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class RestauranteFavoritoRepository private constructor() {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_RESTAURANTES_FAVORITOS = "restaurantesFavoritos"

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

  fun buscarFavoritosDelUsuario(idUsuario: String, callback: FirestoreCallback<List<Restaurante>>) {
    val listaRestaurantesFavoritos: MutableList<Restaurante> = ArrayList()
    db
      .collection(COLECCION_RESTAURANTES_FAVORITOS)
      .whereEqualTo("idUsuario", idUsuario)
      .get()
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          for (documentSnapshot in task.result) {
            val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
            val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
            listaRestaurantesFavoritos.add(
              fromEntity(restauranteEntity, ubicacionRestaurante, null)
            )
          }
          callback.onSuccess(listaRestaurantesFavoritos)
        } else {
          Timber.w("Error recuperando Restaurantes favoritos")
          callback.onError(task.exception!!)
        }
      }
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
