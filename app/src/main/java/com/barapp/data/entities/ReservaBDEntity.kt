package com.barapp.data.entities

import com.barapp.model.EstadoReserva
import com.google.firebase.Timestamp
import java.util.UUID

class ReservaBDEntity(
  var idReserva: String,
  var estado: EstadoReserva,
  var cantidadPersonas: Int,
  var fecha: String,
  var idUsuario: String,
  var idRestaurante: String,
  var nombre: String,
  var puntuacion: Double,
  var foto: String,
  var logo: String,
  var idDetalleRestaurante: String,
  var idUbicacion: String,
  var calle: String,
  var numero: Int,
  var idHorario: String,
  var hora: String,
  var tipoComida: String,
  var timestamp: Timestamp?,
) {
  constructor() :
    this(
      UUID.randomUUID().toString(),
      EstadoReserva.PENDIENTE,
      -1,
      "",
      "",
      "",
      "",
      -1.0,
      "",
      "",
      "",
      "",
      "",
      -1,
      "",
      "",
      "",
      null,
    )

  override fun toString(): String {
    return "ReservaBDEntity(id='$idReserva', estado=$estado, cantidadPersonas=$cantidadPersonas, fecha='$fecha', idUsuario='$idUsuario', idRestaurante='$idRestaurante', nombre='$nombre', puntuacion=$puntuacion, foto='$foto', logo='$logo', idDetalleRestaurante='$idDetalleRestaurante', idUbicacion='$idUbicacion', calle='$calle', numero=$numero, idHorario='$idHorario', hora='$hora', tipoComida='$tipoComida', timestamp=$timestamp)"
  }
}
