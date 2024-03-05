package com.barapp.data.entities

import java.util.UUID

class HorarioEntity(
  var idHorario: String,
  var hora: String,
  var tipoComida: String
) {
  constructor() : this(UUID.randomUUID().toString(), "", "")

  override fun toString(): String {
    return "HorarioEntity(id='$idHorario', hora='$hora', tipoComida='$tipoComida')"
  }
}

