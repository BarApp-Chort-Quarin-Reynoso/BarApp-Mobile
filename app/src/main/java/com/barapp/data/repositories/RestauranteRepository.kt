package com.barapp.data.repositories

import com.barapp.data.entities.RestauranteEntity
import com.barapp.model.Restaurante
import com.barapp.model.Ubicacion
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.mappers.RestauranteMapper.fromEntity
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUbicacion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class RestauranteRepository private constructor() : IGenericRepository<Restaurante> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_RESTAURANTES = "restaurantes"

  override fun buscarPorId(id: String, callback: FirestoreCallback<Restaurante>) {
    db
      .collection(COLECCION_RESTAURANTES)
      .document(id)
      .get()
      .addOnSuccessListener { documentSnapshot ->
        val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
        val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
        callback.onSuccess(fromEntity(restauranteEntity!!, ubicacionRestaurante, null))
      }
      .addOnFailureListener { e -> callback.onError(e) }
  }

  override fun buscarTodos(callback: FirestoreCallback<List<Restaurante>>) {
    val listaRestaurantes: MutableList<Restaurante> = ArrayList()
    db.collection(COLECCION_RESTAURANTES).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        for (documentSnapshot in task.result) {
          val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
          val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
          listaRestaurantes.add(fromEntity(restauranteEntity, ubicacionRestaurante, null))
        }
        callback.onSuccess(listaRestaurantes)
      } else {
        Timber.w("Error recuperando Restaurantes")
        callback.onError(task.exception!!)
      }
    }
  }

  fun buscarDestacados(callback: FirestoreCallback<List<Restaurante>>) {
    val listaRestaurantes: MutableList<Restaurante> = ArrayList()
    db
      .collection(COLECCION_RESTAURANTES)
      .orderBy("puntuacion", Query.Direction.DESCENDING)
      .limit(6)
      .get()
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          for (documentSnapshot in task.result) {
            val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
            val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
            listaRestaurantes.add(fromEntity(restauranteEntity, ubicacionRestaurante, null))
          }
          callback.onSuccess(listaRestaurantes)
        } else {
          Timber.w("Error recuperando Restaurantes")
          callback.onError(task.exception!!)
        }
      }
  }

  override fun guardar(entidad: Restaurante) {
    val restauranteUbicacionEntity = toRestauranteUbicacion(entidad)
    db
      .collection(COLECCION_RESTAURANTES)
      .document(entidad.id)
      .set(restauranteUbicacionEntity)
      .addOnSuccessListener { Timber.d("Restaurante successfully written!") }
      .addOnFailureListener { e -> Timber.w(e, "Error writing restaurante") }
  }

  override fun actualizar(entidad: Restaurante) {}

  override fun borrar(entidad: Restaurante) {}

  companion object {
    @JvmStatic val instance = RestauranteRepository()
  }
}
