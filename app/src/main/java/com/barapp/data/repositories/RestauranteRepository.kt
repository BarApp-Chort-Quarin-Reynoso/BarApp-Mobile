package com.barapp.data.repositories

import com.barapp.data.entities.RestauranteEntity
import com.barapp.model.Restaurante
import com.barapp.model.Ubicacion
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.mappers.RestauranteMapper.fromEntity
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUbicacion
import com.barapp.util.retrofit.RestaurantApiService
import com.barapp.util.retrofit.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class RestauranteRepository private constructor() : IGenericRepository<Restaurante> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_RESTAURANTES = "restaurantes"

  private val api = RetrofitInstance.createService(RestaurantApiService::class.java)

  override fun buscarPorId(id: String, callback: FirestoreCallback<Restaurante>) {
    println("Buscando restaurante con id: $id")
    api.getRestaurantById(id).enqueue(object : Callback<Restaurante> {
      override fun onResponse(call: Call<Restaurante>, response: Response<Restaurante>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurante"))
        }
      }

      override fun onFailure(call: Call<Restaurante>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
        }
      })
//    db
//      .collection(COLECCION_RESTAURANTES)
//      .document(id)
//      .get()
//      .addOnSuccessListener { documentSnapshot ->
//        val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
//        val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
//        callback.onSuccess(fromEntity(restauranteEntity!!, ubicacionRestaurante, null))
//      }
//      .addOnFailureListener { e -> callback.onError(e) }
  }

  override fun buscarTodos(callback: FirestoreCallback<List<Restaurante>>) {
    api.getAllRestaurants(emptyMap()).enqueue(object : Callback<List<Restaurante>> {
      override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Restaurantes"))
        }
      }

      override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
//    db.collection(COLECCION_RESTAURANTES).get().addOnCompleteListener { task ->
//      if (task.isSuccessful) {
//        for (documentSnapshot in task.result) {
//          val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
//          val ubicacionRestaurante = documentSnapshot.toObject(Ubicacion::class.java)
//          listaRestaurantes.add(fromEntity(restauranteEntity, ubicacionRestaurante, null))
//        }
//        callback.onSuccess(listaRestaurantes)
//      } else {
//        Timber.w("Error recuperando Restaurantes")
//        callback.onError(task.exception!!)
//      }
//    }
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
