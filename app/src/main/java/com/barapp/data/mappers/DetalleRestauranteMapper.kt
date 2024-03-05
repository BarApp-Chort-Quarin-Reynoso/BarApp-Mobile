package com.barapp.data.mappers

import com.barapp.data.entities.DetalleRestauranteEntity
import com.barapp.data.entities.HorarioEntity
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Horario
import com.barapp.model.Opinion
import java.util.stream.Collectors

object DetalleRestauranteMapper {
  @JvmStatic
  fun toEntity(detalleRestaurante: DetalleRestaurante): DetalleRestauranteEntity {
    return DetalleRestauranteEntity(
      detalleRestaurante.id,
      detalleRestaurante.descripcion,
      detalleRestaurante.menu,
      detalleRestaurante.capacidadPorHorario,
      detalleRestaurante.horarios
        .stream()
        .map { obj -> HorarioMapper.toEntity(obj) }
        .collect(Collectors.toList<HorarioEntity>()),
      detalleRestaurante.opiniones
        .stream()
        .map { obj -> OpinionMapper.toOpinionUsuarioEntity(obj) }
        .collect(Collectors.toList()),
    )
  }

  @JvmStatic
  fun fromEntity(
    detalleRestauranteEntity: DetalleRestauranteEntity,
    horarios: List<Horario>,
    opiniones: List<Opinion>,
  ): DetalleRestaurante {
    return DetalleRestaurante(
      detalleRestauranteEntity.idDetalleRestaurante,
      detalleRestauranteEntity.descripcion,
      detalleRestauranteEntity.menu,
      detalleRestauranteEntity.capacidadPorHorario,
      ArrayList(horarios),
      ArrayList(opiniones),
    )
  }
}
