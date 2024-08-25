package com.barapp.data.repositories

import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.model.Usuario
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.data.retrofit.UserApiService
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class UsuarioRepository private constructor() : IGenericRepository<Usuario> {
  private val api = RetrofitInstance.createService(UserApiService::class.java)

  override fun buscarPorId(id: String, callback: FirestoreCallback<Usuario>) {
    Timber.d("Buscando usuario con id: $id")
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
  }

  override fun buscarTodos(callback: FirestoreCallback<List<Usuario>>) {}

  override fun guardar(entidad: Usuario) {
    Timber.d("Guardando usuario: $entidad")
    api.createUser(entidad.id, entidad).enqueue(object : Callback<String> {
      override fun onResponse(call: Call<String>, response: Response<String>) {
        if (response.isSuccessful) {
          Timber.d("Usuario creado exitosamente")
        } else {
          Timber.e("Error creando usuario: ${response.errorBody()}")
        }
      }

      override fun onFailure(call: Call<String>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  fun actualizarFoto(user: Usuario) {
    Timber.d("Actualizando foto de usuario")
    api.updatePhoto(user.id, user.foto).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
          if (response.isSuccessful) {
            Timber.d("Foto de usuario actualizada!")
          } else {
            Timber.e("Error al actualizar foto de usuario")
          }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
          Timber.e(t)
        }
      })
  }

  override fun actualizar(entidad: Usuario) {
    Timber.d("Actualizando usuario: $entidad")
    api.updateUser(entidad.id, entidad).enqueue(object : Callback<Usuario> {
      override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
        if (response.isSuccessful) {
          Timber.d("Usuario actualizado exitosamente")
        } else {
          Timber.e("Error actualizado usuario: ${response.errorBody()}")
        }
      }

      override fun onFailure(call: Call<Usuario>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  override fun borrar(entidad: Usuario) {}

  fun crearUsuarioEnFirebaseAuth(
    email: String,
    constrasenia: String,
    callback: FirestoreCallback<String>,
  ) {
    Timber.d("Creando usuario en Firebase Auth: $email - $constrasenia")
    api.registerUser(email, constrasenia).enqueue(object : Callback<String> {
      override fun onResponse(call: Call<String>, response: Response<String>) {
        if (response.isSuccessful) {
          Timber.d("Usuario creado exitosamente")
          callback.onSuccess(response.body()!!)
        } else {
          Timber.e("Error creando usuario: ${response.errorBody()}")
          callback.onError(Throwable("Error creando usuario"))
        }
      }

      override fun onFailure(call: Call<String>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun signInUsuarioEnFirebase(email: String, contrasenia: String, callback: FirestoreCallback<String>) {
    Timber.d("Iniciando sesión en Firebase Auth: $email - $contrasenia")
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
