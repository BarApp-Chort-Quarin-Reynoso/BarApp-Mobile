package com.barapp.model

import java.util.UUID

class DetalleRestaurante(
  id: String,
  var descripcion: String,
  var menu: String,
  var capacidadPorHorario: Int,
  var horarios: MutableList<Horario>,
  var opiniones: MutableList<Opinion>,
) : BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), "", "", -1, ArrayList(), ArrayList())

  constructor(
    descripcion: String,
    menu: String,
    capacidadPorHorario: Int,
  ) : this(
    UUID.randomUUID().toString(),
    descripcion,
    menu,
    capacidadPorHorario,
    ArrayList(),
    ArrayList(),
  )

  fun agregarHorario(horario: Horario) {
    horarios.add(horario)
  }

  fun agregarOpinion(opinion: Opinion) {
    opiniones.add(opinion)
  }

  override fun toString(): String {
    return "DetalleRestaurante{" +
      "id='" +
      id +
      '\'' +
      ", descripcion='" +
      descripcion +
      '\'' +
      ", menu='" +
      menu +
      '\'' +
      ", capacidadPorHorario='" +
      capacidadPorHorario +
      '\'' +
      ", horarios=" +
      horarios +
      ", opiniones=" +
      opiniones +
      '}'
  }
}
