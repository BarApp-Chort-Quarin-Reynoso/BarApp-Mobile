package com.barapp.data.entities

import java.util.UUID

class OpinionEntity(
  var idOpinion: String,
  var comentario: String,
  var nota: Double
) {
  constructor() : this(UUID.randomUUID().toString(), "", 1.0)

  override fun toString(): String {
    return "OpinionEntity(id='$idOpinion', comentario='$comentario', nota=$nota)"
  }
}
