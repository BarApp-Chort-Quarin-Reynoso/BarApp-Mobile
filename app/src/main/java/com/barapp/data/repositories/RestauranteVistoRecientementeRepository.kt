package com.barapp.data.repositories

import com.barapp.data.entities.RestauranteEntity
import com.barapp.model.Restaurante
import com.barapp.model.Ubicacion
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.mappers.RestauranteMapper.fromEntity
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuarioEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.LinkedList
import timber.log.Timber

class RestauranteVistoRecientementeRepository private constructor() {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_RESTAURANTES_VISTOS_RECIENTEMENTE = "restaurantesVistosRecientemente"

  /**
   * El metodo busca un restaurante en la coleccion de restaurantes vistos recientemente segun el id
   * compuesto
   *
   * @param idCompuesto se compone de el restaurante.getId()+idUsuario
   * @param callback
   */
  fun buscarPorId(idCompuesto: String, callback: FirestoreCallback<Restaurante>) {
    db
      .collection(COLECCION_RESTAURANTES_VISTOS_RECIENTEMENTE)
      .document(idCompuesto)
      .get()
      .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
        val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
        val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
        callback.onSuccess(fromEntity(restauranteEntity!!, ubicacionRestaurante, null))
      }
      .addOnFailureListener { e: Exception? -> callback.onError(e!!) }
  }

  fun buscarVistosRecientementeDelUsuario(
    idUsuario: String,
    callback: FirestoreCallback<LinkedList<Restaurante>>,
  ) {
    val listaRestaurantesVistosRecientemente = LinkedList<Restaurante>()
    db
      .collection(COLECCION_RESTAURANTES_VISTOS_RECIENTEMENTE)
      .whereEqualTo("idUsuario", idUsuario)
      .orderBy("fechaGuardado", Query.Direction.DESCENDING)
      .get()
      .addOnCompleteListener { task: Task<QuerySnapshot> ->
        if (task.isSuccessful) {
          for (documentSnapshot in task.result) {
            val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
            val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
            listaRestaurantesVistosRecientemente.add(
              fromEntity(restauranteEntity, ubicacionRestaurante, null)
            )
          }
          callback.onSuccess(listaRestaurantesVistosRecientemente)
        } else {
          Timber.e("Error recuperando restaurantes vistos recientemente")
          Timber.e(task.exception)
          callback.onError(task.exception!!)
        }
      }
  }

  fun guardar(entidad: Restaurante, idUsuario: String) {
    val restauranteVistoRecientementeEntity = toRestauranteUsuarioEntity(entidad, idUsuario)
    db
      .collection(COLECCION_RESTAURANTES_VISTOS_RECIENTEMENTE)
      .document(restauranteVistoRecientementeEntity.idRestauranteUsuario)
      .set(restauranteVistoRecientementeEntity)
      .addOnSuccessListener { Timber.d("Restaurante visto recientemente guardado con exito") }
      .addOnFailureListener { e: Exception? ->
        Timber.e(e, "Error guardado restaurante visto recientemente")
      }
  }

  fun borrar(entidad: Restaurante, idUsuario: String) {
    db
      .collection(COLECCION_RESTAURANTES_VISTOS_RECIENTEMENTE)
      .document(entidad.id + idUsuario)
      .delete()
      .addOnCompleteListener { task: Task<Void?> ->
        if (task.isSuccessful) {
          Timber.d("Restaurante visto recientemente eliminado con éxito")
        } else {
          Timber.e(task.exception)
        }
      }
  }

  companion object {
    @JvmStatic val instance = RestauranteVistoRecientementeRepository()
  }
}
