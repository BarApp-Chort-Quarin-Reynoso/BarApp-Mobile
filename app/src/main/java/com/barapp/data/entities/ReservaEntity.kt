package com.barapp.data.entities

import java.util.Date
import java.util.UUID

class ReservaEntity(
  var idReserva: String,
  var cancelada: Boolean,
  var cantidadPersonas: Int,
  var fecha: String,
  var idUsuario: String,
  var timestamp: Date? = null,
) {
  constructor() : this(UUID.randomUUID().toString(), false, -1, "", "", null)

  override fun toString(): String {
    return "ReservaEntity(idReserva='$idReserva', cancelada=$cancelada, cantidadPersonas=$cantidadPersonas, fecha='$fecha', idUsuario='$idUsuario', timestamp=$timestamp)"
  }
}
