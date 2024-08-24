package com.barapp.data.entities

import com.barapp.model.EstadoReserva
import java.util.Date
import java.util.UUID

class ReservaEntity(
  var idReserva: String,
  var estado: EstadoReserva,
  var cantidadPersonas: Int,
  var fecha: String,
  var idUsuario: String,
  var timestamp: Date? = null,
) {
  constructor() : this(UUID.randomUUID().toString(), EstadoReserva.PENDIENTE, -1, "", "", null)

  override fun toString(): String {
    return "ReservaEntity(idReserva='$idReserva', estado=$estado, cantidadPersonas=$cantidadPersonas, fecha='$fecha', idUsuario='$idUsuario', timestamp=$timestamp)"
  }
}
