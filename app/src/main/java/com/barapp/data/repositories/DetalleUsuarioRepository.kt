package com.barapp.data.repositories

import com.barapp.model.DetalleUsuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.mappers.DetalleUsuarioMapper.toEntity
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.data.retrofit.UserApiService
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalleUsuarioRepository private constructor() : IGenericRepository<DetalleUsuario> {
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  private val COLECCION_DETALLES_USUARIOS = "detallesUsuarios"

  private val api = RetrofitInstance.createService(UserApiService::class.java)

  override fun buscarPorId(id: String, callback: FirestoreCallback<DetalleUsuario>) {
    Timber.d("Buscando detalle usuario con id: $id")

    api.getUserDetailById(id).enqueue(object : Callback<DetalleUsuario> {
      override fun onResponse(call: Call<DetalleUsuario>, response: Response<DetalleUsuario>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando DetalleUsuario"))
        }
      }

      override fun onFailure(call: Call<DetalleUsuario>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
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
    Timber.d("Actualizando favoritos del usuario con id: ${entidad.id}" + " con favoritos: ${entidad.idsRestaurantesFavoritos}")
    api.updateFavoriteRestaurants(entidad.id, entidad.idsRestaurantesFavoritos).enqueue(object : Callback<DetalleUsuario> {
      override fun onResponse(call: Call<DetalleUsuario>, response: Response<DetalleUsuario>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
        } else {
          Timber.e("Error: ${response.errorBody()}")
        }
      }

      override fun onFailure(call: Call<DetalleUsuario>, t: Throwable) {
        Timber.e(t)
      }
    })
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
