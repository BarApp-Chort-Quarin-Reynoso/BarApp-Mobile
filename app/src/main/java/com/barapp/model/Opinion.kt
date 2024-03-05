package com.barapp.model

import java.util.UUID

class Opinion(id: String, var comentario: String, var nota: Double, var usuario: Usuario) :
  BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), "undefined", -1.0, Usuario())

  constructor(
    comentario: String,
    nota: Double,
    usuario: Usuario,
  ) : this(UUID.randomUUID().toString(), comentario, nota, usuario)

  override fun toString(): String {
    return "Opinion{" +
      "id='" +
      id +
      '\'' +
      ", comentario='" +
      comentario +
      '\'' +
      ", nota=" +
      nota +
      ", usuario=" +
      usuario +
      '}'
  }
}
