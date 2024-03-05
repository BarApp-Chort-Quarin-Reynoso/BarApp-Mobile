package com.barapp.data.repositories

import com.barapp.data.entities.DetalleUsuarioEntity
import com.barapp.model.DetalleUsuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.mappers.DetalleUsuarioMapper.fromEntity
import com.barapp.data.mappers.DetalleUsuarioMapper.toEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class DetalleUsuarioRepository private constructor() : IGenericRepository<DetalleUsuario> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_DETALLES_USUARIOS = "detallesUsuarios"

  override fun buscarPorId(id: String, callback: FirestoreCallback<DetalleUsuario>) {
    db
      .collection(COLECCION_DETALLES_USUARIOS)
      .document(id)
      .get()
      .addOnSuccessListener { documentSnapshot ->
        val detalleUsuarioEntity = documentSnapshot.toObject(DetalleUsuarioEntity::class.java)
        Timber.d(detalleUsuarioEntity.toString())
        callback.onSuccess(fromEntity(detalleUsuarioEntity!!))
      }
      .addOnFailureListener { e ->
        Timber.w("Error recuperando ciudad")
        callback.onError(e)
      }
  }

  override fun buscarTodos(callback: FirestoreCallback<List<DetalleUsuario>>) {}

  override fun guardar(entidad: DetalleUsuario) {
    db
      .collection(COLECCION_DETALLES_USUARIOS)
      .document(entidad.id)
      .set(toEntity(entidad))
      .addOnSuccessListener { Timber.d("DetalleUsuario successfully written!") }
      .addOnFailureListener { e -> Timber.w(e, "Error writing detalleUsuario") }
  }

  override fun actualizar(entidad: DetalleUsuario) {
    guardar(entidad)
  }

  fun actualizarFavoritos(entidad: DetalleUsuario) {
    db
      .collection(COLECCION_DETALLES_USUARIOS)
      .document(entidad.id)
      .update("idRestaurantesFavoritos", ArrayList(entidad.idsRestaurantesFavoritos))
      .addOnCompleteListener { task: Task<Void?> ->
        if (task.isSuccessful) {
          Timber.d("Se ha actualizado la lista favoritos")
        } else {
          Timber.d("Hubo un error actualizando la lista favoritos")
        }
      }
  }

  fun actualizarBusquedasRecientes(entidad: DetalleUsuario) {
    db
      .collection(COLECCION_DETALLES_USUARIOS)
      .document(entidad.id)
      .update("busquedasRecientes", ArrayList(entidad.busquedasRecientes))
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          Timber.d("Se ha actualizado la lista de busquedas recientes")
        } else {
          Timber.d("Hubo un error actualizando la lista de busquedas recientes")
        }
      }
  }

  override fun borrar(entidad: DetalleUsuario) {}

  companion object {
    @JvmStatic val instance = DetalleUsuarioRepository()
  }
}
