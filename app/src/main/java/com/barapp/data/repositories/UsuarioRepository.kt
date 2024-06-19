package com.barapp.data.repositories

import com.barapp.data.entities.UsuarioEntity
import com.barapp.data.mappers.UsuarioMapper.fromEntity
import com.barapp.data.mappers.UsuarioMapper.toEntity
import com.barapp.data.utils.EntityNotFoundException
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.model.DetalleUsuario
import com.barapp.model.Usuario
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.data.retrofit.UserApiService
import com.barapp.model.Restaurante
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class UsuarioRepository private constructor() : IGenericRepository<Usuario> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Repositories
  private val detalleUsuarioRepository: DetalleUsuarioRepository = DetalleUsuarioRepository.instance

  // Coleccion
  private val COLECCION_USUARIOS = "usuarios"

  private val api = RetrofitInstance.createService(UserApiService::class.java)

  override fun buscarPorId(id: String, callback: FirestoreCallback<Usuario>) {
    println("Buscando usuario con id: $id")
    api.getUser(id).enqueue(object : Callback<Usuario> {
      override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Usuario"))
        }
      }

      override fun onFailure(call: Call<Usuario>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
//    db
//      .collection(COLECCION_USUARIOS)
//      .document(id)
//      .get()
//      .addOnSuccessListener { document ->
//        val usuarioEntity = document.toObject(UsuarioEntity::class.java)
//        usuarioEntity?.let { usuario ->
//          detalleUsuarioRepository.buscarPorId(
//            usuario.idDetalleUsuario,
//            object : FirestoreCallback<DetalleUsuario> {
//              override fun onSuccess(result: DetalleUsuario) {
//                callback.onSuccess(fromEntity(usuario, result))
//              }
//
//              override fun onError(exception: Throwable) {
//                callback.onError(exception)
//              }
//            },
//          )
//        } ?: callback.onError(EntityNotFoundException("The user $id was not found in Firebase"))
//      }
//      .addOnFailureListener { e ->
//        Timber.w("Error recuperando usuario")
//        callback.onError(e)
//      }
  }

  fun buscarPorIdSinDetalle(id: String, callback: FirestoreCallback<Usuario?>) {
    db
      .collection(COLECCION_USUARIOS)
      .document(id)
      .get()
      .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
        val usuarioEntity = documentSnapshot.toObject(UsuarioEntity::class.java)
        usuarioEntity?.let { usuario ->
          callback.onSuccess(fromEntity(usuario, null))
        } ?: callback.onError(EntityNotFoundException("El usuario $id no fue hallado"))
      }
      .addOnFailureListener { e: Exception ->
        Timber.w("Error recuperando usuario")
        callback.onError(e)
      }
  }

  override fun buscarTodos(callback: FirestoreCallback<List<Usuario>>) {
    val listaUsuarios: MutableList<Usuario> = ArrayList()
    db.collection(COLECCION_USUARIOS).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        for (documentSnapshot in task.result) {
          val usuarioEntity = documentSnapshot.toObject(UsuarioEntity::class.java)
          listaUsuarios.add(fromEntity(usuarioEntity, null))
        }
        callback.onSuccess(listaUsuarios)
      } else {
        callback.onError(task.exception!!)
      }
    }
  }

  override fun guardar(entidad: Usuario) {
    db
      .collection(COLECCION_USUARIOS)
      .document(entidad.id)
      .set(toEntity(entidad))
      .addOnSuccessListener { aVoid: Void? -> Timber.d("DocumentSnapshot successfully written!") }
      .addOnFailureListener { e: Exception? -> Timber.w(e, "Error writing document") }
  }

  fun actualizarFoto(user: Usuario) {

    api.updatePhoto(user.id, user.foto).enqueue(object : retrofit2.Callback<Void> {
        override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
          if (response.isSuccessful) {
            Timber.d("Foto de usuario actualizada!")
          } else {
            Timber.e("Error al actualizar foto de usuario")
          }
        }

        override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
          Timber.e(t)
        }
      })
//    db
//      .collection(COLECCION_USUARIOS)
//      .document(entidad.id)
//      .update("foto", entidad.foto)
//      .addOnSuccessListener { aVoid: Void? -> Timber.d("DocumentSnapshot successfully written!") }
//      .addOnFailureListener { e: Exception? -> Timber.w(e, "Error writing document") }
  }

  override fun actualizar(entidad: Usuario) {}

  override fun borrar(entidad: Usuario) {}

  fun crearUsuarioEnFirebaseAuth(
    email: String,
    constrasenia: String,
    callback: FirestoreCallback<String>,
  ) {
    FirebaseAuth.getInstance()
      .createUserWithEmailAndPassword(email, constrasenia)
      .addOnCompleteListener { task: Task<AuthResult> ->
        if (task.isSuccessful) {
          callback.onSuccess(task.result.user!!.uid)
        } else {
          callback.onError(task.exception!!)
        }
      }
  }

  fun signInUsuarioEnFirebase(email: String, contrasenia: String, callback: FirestoreCallback<String>) {
    FirebaseAuth.getInstance()
      .signInWithEmailAndPassword(email, contrasenia)
      .addOnSuccessListener { result ->
        callback.onSuccess(result.user!!.uid)
      }
      .addOnFailureListener { exc ->
        callback.onError(exc)
      }
  }

  companion object {
    @JvmStatic val instance = UsuarioRepository()
  }
}
