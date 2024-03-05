package com.barapp.model

import java.time.LocalTime
import java.util.UUID

class Horario(id: String, var hora: LocalTime, var tipoComida: TipoComida) : BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), LocalTime.MAX, TipoComida.NINGUNO)

  constructor(
    hora: LocalTime,
    tipoComida: TipoComida,
  ) : this(UUID.randomUUID().toString(), hora, tipoComida)

  override fun toString(): String {
    return "Horario{id='$id', hora=$hora, tipoComida=$tipoComida}"
  }
}
