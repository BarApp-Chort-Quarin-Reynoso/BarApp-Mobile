package com.barapp.data.entities

import java.util.UUID

class DetalleRestauranteEntity(
  var idDetalleRestaurante: String,
  var descripcion: String,
  var menu: String,
  var capacidadPorHorario: Int,
  var listaHorarioEntities: List<HorarioEntity>,
  var listaOpinionEntities: List<OpinionUsuarioEntity>,
) {
  constructor() : this(UUID.randomUUID().toString(), "", "", -1, ArrayList(), ArrayList())

  override fun toString(): String {
    return "DetalleRestauranteEntity(id='$idDetalleRestaurante', descripcion='$descripcion', menu='$menu', capacidadPorHorario=$capacidadPorHorario, listaHorarioEntities=$listaHorarioEntities, listaOpinionEntities=$listaOpinionEntities)"
  }
}
