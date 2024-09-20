package com.barapp.data.repositories

import com.barapp.data.entities.HorarioEntity
import com.barapp.data.entities.ReservaEntity
import com.barapp.data.entities.RestauranteEntity
import com.barapp.barapp.model.Reserva
import com.barapp.model.Ubicacion
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.utils.IGenericRepository
import com.barapp.data.mappers.HorarioMapper.fromEntity
import com.barapp.data.mappers.ReservaMapper.fromEntity
import com.barapp.data.mappers.ReservaMapper.toReservaBDEntity
import com.barapp.data.mappers.RestauranteMapper.fromEntity
import com.barapp.data.retrofit.ReservationApiService
import com.barapp.data.retrofit.RestaurantApiService
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.model.EstadoReserva
import com.barapp.model.Restaurante
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneId
import timber.log.Timber

class ReservaRepository private constructor() : IGenericRepository<Reserva> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_RESERVAS = "reservas"

  private val api = RetrofitInstance.createService(ReservationApiService::class.java)

  override fun buscarTodos(callback: FirestoreCallback<List<Reserva>>) {
    val listaReservas: MutableList<Reserva> = ArrayList()
    db.collection(COLECCION_RESERVAS).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        for (document in task.result) {
          val reservaEntity = document.toObject(ReservaEntity::class.java)
          val restauranteEntity = document.toObject(RestauranteEntity::class.java)
          val horarioEntity = document.toObject(HorarioEntity::class.java)
          // Tiene solo idUbicacion, calle y numero, el resto de campos son nulls
          val ubicacion = document.toObject(Ubicacion::class.java)
          val reserva =
            fromEntity(
              reservaEntity,
              fromEntity(restauranteEntity, ubicacion, null),
              fromEntity(horarioEntity),
              null,
            )
          listaReservas.add(reserva)
        }
        callback.onSuccess(listaReservas)
      } else {
        Timber.d("Falló la busqueda de reservas")
        callback.onError(task.exception!!)
      }
    }
  }

  override fun buscarPorId(id: String, callback: FirestoreCallback<Reserva>) {
    Timber.d("Buscando reserva con id: $id")
    api.getReservation(id).enqueue(object : Callback<Reserva> {
      override fun onResponse(call: Call<Reserva>, response: Response<Reserva>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Reserva recibido: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Reserva"))
        }
      }

      override fun onFailure(call: Call<Reserva>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  fun buscarTodosAsociadosAUsuario(idUsuario: String, callback: FirestoreCallback<List<Reserva>>) {
    Timber.d("Buscando reservas con id: $idUsuario")
    api.getReservationsByUser(idUsuario).enqueue(object : Callback<List<Reserva>> {
      override fun onResponse(call: Call<List<Reserva>>, response: Response<List<Reserva>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Data received: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Reservas"))
        }
      }

      override fun onFailure(call: Call<List<Reserva>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  override fun guardar(entidad: Reserva) {
    Timber.d("Guardando reserva: $entidad")
    api.createReservation(entidad.id, entidad).enqueue(object : Callback<String> {
      override fun onResponse(call: Call<String>, response: Response<String>) {
        if (response.isSuccessful) {
          Timber.d("Reserva creada exitosamente")
        } else {
          Timber.e("Error creando reserva: ${response.errorBody()}")
        }
      }

      override fun onFailure(call: Call<String>, t: Throwable) {
        Timber.e(t)
      }
    })
  }

  fun cancelarReserva(reserva: Reserva) {
    Timber.d("Cancelando reserva por parte del usuario: $reserva")
    api.updateReservation(reserva.id, EstadoReserva.CANCELADA_USUARIO)
      .enqueue(object : Callback<Reserva> {
        override fun onResponse(call: Call<Reserva>, response: Response<Reserva>) {
          if (response.isSuccessful) {
            Timber.d("Reserva cancelada exitosamente")
          } else {
            Timber.e("Error: ${response.errorBody()}")
          }
        }

        override fun onFailure(call: Call<Reserva>, t: Throwable) {
          Timber.e(t)
        }
      })
  }

  fun buscarUltimasReservasPendientes(
    idRestaurante: String,
    idUsuario: String,
    maxCantidad: Int,
    callback: FirestoreCallback<List<Reserva>>
  ) {
    Timber.d(
      "Buscando ultimas $maxCantidad reservas " +
          "con idRestaurante $idRestaurante y idUsuario $idUsuario")
    api.getLastReservationsByUserRestaurant(idRestaurante, idUsuario, maxCantidad).enqueue(object : Callback<List<Reserva>> {
      override fun onResponse(call: Call<List<Reserva>>, response: Response<List<Reserva>>) {
        if (response.isSuccessful) {
          val data = response.body()
          Timber.d("Reservas recibidas: $data")
          callback.onSuccess(data!!)
        } else {
          Timber.e("Error: ${response.errorBody()}")
          callback.onError(Throwable("Error recuperando Reserva"))
        }
      }

      override fun onFailure(call: Call<List<Reserva>>, t: Throwable) {
        Timber.e(t)
        callback.onError(t)
      }
    })
  }

  override fun actualizar(entidad: Reserva) {}

  override fun borrar(entidad: Reserva) {}

  companion object {
    @JvmStatic
    val instance = ReservaRepository()
  }
}
