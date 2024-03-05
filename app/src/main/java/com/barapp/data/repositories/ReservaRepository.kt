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
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDateTime
import java.time.ZoneId
import timber.log.Timber

class ReservaRepository private constructor() : IGenericRepository<Reserva> {
  // Base de datos
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  // Coleccion
  private val COLECCION_RESERVAS = "reservas"

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
    db
      .collection(COLECCION_RESERVAS)
      .document(id)
      .get()
      .addOnSuccessListener { documentSnapshot ->
        val reservaEntity = documentSnapshot.toObject(ReservaEntity::class.java)
        val restauranteEntity = documentSnapshot.toObject(RestauranteEntity::class.java)
        val horarioEntity = documentSnapshot.toObject(HorarioEntity::class.java)
        // Tiene solo idUbicacion, calle y numero, el resto de campos son nulls
        val ubicacion = documentSnapshot.toObject(Ubicacion::class.java)
        val reserva =
          fromEntity(
            reservaEntity!!,
            fromEntity(restauranteEntity!!, ubicacion, null),
            fromEntity(horarioEntity!!),
            null,
          )
        callback.onSuccess(reserva)
      }
      .addOnFailureListener { e ->
        Timber.w("Error recuperando reserva")
        callback.onError(e)
      }
  }

  fun buscarTodosAsociadosAUsuario(idUsuario: String, callback: FirestoreCallback<List<Reserva>>) {
    val listaReservas: MutableList<Reserva> = ArrayList()
    db
      .collection(COLECCION_RESERVAS)
      .whereEqualTo("idUsuario", idUsuario)
      .orderBy("timestamp", Query.Direction.ASCENDING)
      .get()
      .addOnCompleteListener { task ->
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

  fun buscarReservasANotificar(
    idUsuario: String,
    minutosMinimos: Int,
    callback: FirestoreCallback<List<Reserva>>,
  ) {
    val listaReservas: MutableList<Reserva> = ArrayList()
    db
      .collection(COLECCION_RESERVAS)
      .whereEqualTo("idUsuario", idUsuario)
      .whereGreaterThan(
        "timestamp",
        Timestamp(
          LocalDateTime.now()
            .plusMinutes(minutosMinimos.toLong())
            .atZone(ZoneId.of("America/Buenos_Aires"))
            .toInstant()
            .epochSecond,
          0,
        ),
      )
      .get()
      .addOnCompleteListener { task: Task<QuerySnapshot> ->
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

  override fun guardar(entidad: Reserva) {
    db
      .collection(COLECCION_RESERVAS)
      .document(entidad.id)
      .set(toReservaBDEntity(entidad))
      .addOnSuccessListener { Timber.d("Reserva successfully written!") }
      .addOnFailureListener { e: Exception? -> Timber.w(e, "Error writing reserva") }
  }

  override fun actualizar(entidad: Reserva) {}

  override fun borrar(entidad: Reserva) {}

  companion object {
    @JvmStatic val instance = ReservaRepository()
  }
}
