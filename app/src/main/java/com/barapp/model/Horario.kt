package com.barapp.model

import java.time.LocalTime
import java.util.UUID

class Horario(id: String, var horario: String, var tipoComida: TipoComida) : BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), LocalTime.MAX.toString(), TipoComida.NINGUNO)

  constructor(
    horario: String,
    tipoComida: TipoComida,
  ) : this(UUID.randomUUID().toString(), horario, tipoComida)

  override fun toString(): String {
    return "Horario{id='$id', horario=$horario, tipoComida=$tipoComida}"
  }

  fun getHorarioAsLocalTime(): LocalTime {
    return LocalTime.parse(horario)
  }
}
