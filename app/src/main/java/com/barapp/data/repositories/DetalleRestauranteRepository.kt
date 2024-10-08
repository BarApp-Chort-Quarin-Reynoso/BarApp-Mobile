package com.barapp.data.repositories

import com.barapp.data.entities.DetalleRestauranteEntity
import com.barapp.data.entities.OpinionEntity
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.retrofit.RestaurantApiService
import com.barapp.data.retrofit.RetrofitInstance
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class DetalleRestauranteRepository private constructor() : IGenericRepository<DetalleRestaurante> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_DETALLES_RESTAURANTES = "detallesRestaurantes"

  private val api = RetrofitInstance.createService(RestaurantApiService::class.java)

  override fun buscarPorId(id: String, callback: FirestoreCallback<DetalleRestaurante>) {
    Timber.d("Buscando detalle restaurante del restaurante con id: $id")
    api.getRestaurantDetailById(id).enqueue(object : Callback<DetalleRestaurante> {
      override fun onResponse(call: Call<DetalleRestaurante>, response: Response<DetalleRestaurante>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurante"))
        }
      }

      override fun onFailure(call: Call<DetalleRestaurante>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  override fun buscarTodos(callback: FirestoreCallback<List<DetalleRestaurante>>) {}

  override fun guardar(entidad: DetalleRestaurante) {}

  fun actualizarFotoUsuario(usuario: Usuario) {
    db.collection(COLECCION_DETALLES_RESTAURANTES).get().addOnCompleteListener {
      task: Task<QuerySnapshot> ->
      if (task.isSuccessful) {
        for (documentSnapshot in task.result) {
          val detalleRestauranteEntity =
            documentSnapshot.toObject(DetalleRestauranteEntity::class.java)
          val listaOpinion = detalleRestauranteEntity.listaOpinionEntities
          for (opinionEntity in listaOpinion) {
            if (opinionEntity.idUsuario == usuario.id) {
              opinionEntity.foto = usuario.foto
              actualizarListaOpinion(detalleRestauranteEntity, listaOpinion)
            }
          }
        }
      } else {
        Timber.e(task.exception)
      }
    }
  }

  fun actualizarListaOpinion(
    dr: DetalleRestauranteEntity?,
    opinionUsuarioEntityList: List<OpinionEntity>?,
  ) {
    db
      .collection(COLECCION_DETALLES_RESTAURANTES)
      .document(dr!!.idDetalleRestaurante)
      .update("listaOpinionEntities", opinionUsuarioEntityList)
      .addOnSuccessListener { aVoid: Void? -> Timber.d("Lista opinion succesfully written!") }
      .addOnFailureListener { t: Exception? -> Timber.e(t) }
  }

  override fun actualizar(entidad: DetalleRestaurante) {}

  override fun borrar(entidad: DetalleRestaurante) {}

  companion object {
    @JvmStatic val instance = DetalleRestauranteRepository()
  }
}
