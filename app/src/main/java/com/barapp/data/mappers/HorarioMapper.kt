package com.barapp.data.mappers

import com.barapp.data.entities.HorarioEntity
import com.barapp.model.Horario
import com.barapp.model.TipoComida
import java.time.LocalTime
import java.util.stream.Collectors

object HorarioMapper {
  @JvmStatic
  fun toEntity(horario: Horario): HorarioEntity {
    return HorarioEntity(horario.id, horario.horario, horario.tipoComida.toString())
  }

  @JvmStatic
  fun fromEntity(horarioEntity: HorarioEntity): Horario {
    return Horario(
      horarioEntity.idHorario,
      horarioEntity.horario,
      TipoComida.valueOf(horarioEntity.tipoComida),
    )
  }

  @JvmStatic
  fun fromEntityToList(listaHorarioEntity: List<HorarioEntity>): List<Horario> {
    return listaHorarioEntity.stream().map { obj -> fromEntity(obj) }.collect(Collectors.toList())
  }
}
