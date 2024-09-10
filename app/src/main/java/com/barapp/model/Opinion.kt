package com.barapp.model

import java.util.UUID

class Opinion(id: String, var comentario: String, var nota: Double, var usuario: Usuario, var horario: Horario, var fecha: String, var cantidadPersonas: Int) :
  BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), "", -1.0, Usuario(), Horario(), "", -1)

  constructor(
    comentario: String,
    nota: Double,
    usuario: Usuario,
    horario: Horario,
    fecha: String,
    cantidadPersonas: Int
  ) : this(UUID.randomUUID().toString(), comentario, nota, usuario, horario, fecha, cantidadPersonas)

  override fun toString(): String {
    return "Opinion{" +
      "id='" +
      id +
      ", comentario='" +
      comentario +
      ", nota=" +
      nota +
      ", usuario=" +
      usuario +
      ", horario=" +
      horario +
      ", fecha='" +
      fecha +
      ", cantidadPersonas=" +
      cantidadPersonas +
      '}'
  }
}
