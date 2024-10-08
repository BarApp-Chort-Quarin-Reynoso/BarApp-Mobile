package com.barapp.data.mappers

import com.barapp.data.entities.RestauranteEntity
import com.barapp.data.entities.RestauranteUbicacionEntity
import com.barapp.data.entities.RestauranteUsuarioEntity
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Restaurante
import com.barapp.model.RestauranteUsuario
import com.barapp.model.Ubicacion

object RestauranteMapper {
  @JvmStatic
  fun toEntity(restaurante: Restaurante): RestauranteEntity {
    return RestauranteEntity(
      restaurante.nombre,
      restaurante.estado,
      restaurante.puntuacion,
      restaurante.cantidadOpiniones,
      restaurante.portada,
      restaurante.correo,
      restaurante.logo,
      restaurante.idDetalleRestaurante,
    )
  }

  @JvmStatic
  fun fromEntity(
    restauranteEntity: RestauranteEntity,
    ubicacion: Ubicacion?,
    detalleRestaurante: DetalleRestaurante?,
  ): Restaurante {
    return Restaurante(
      "",
      restauranteEntity.nombre,
      restauranteEntity.estado,
      restauranteEntity.puntuacion,
      restauranteEntity.cantidadOpiniones,
      restauranteEntity.portada,
      restauranteEntity.correo,
      restauranteEntity.logo,
      ubicacion!!,
      restauranteEntity.idDetalleRestaurante,
      detalleRestaurante,
    )
  }

  @JvmStatic
  fun toRestauranteUsuario(restaurante: Restaurante): RestauranteUsuario {
    val idAReemplazar = restaurante.idRestaurante.ifEmpty { restaurante.id }

    return RestauranteUsuario(
      idAReemplazar,
      restaurante.nombre,
      restaurante.estado,
      restaurante.puntuacion,
      restaurante.cantidadOpiniones,
      restaurante.portada,
      restaurante.correo,
      restaurante.logo,
      restaurante.ubicacion,
      restaurante.idDetalleRestaurante,
      restaurante.detalleRestaurante,
      idAReemplazar,
    )
  }

  @JvmStatic
  fun toRestauranteUbicacion(restaurante: Restaurante): RestauranteUbicacionEntity {
    return RestauranteUbicacionEntity(
      restaurante.id,
      restaurante.nombre,
      restaurante.estado,
      restaurante.puntuacion,
      restaurante.portada,
      restaurante.correo,
      restaurante.logo,
      restaurante.idDetalleRestaurante,
      restaurante.ubicacion.id,
      restaurante.ubicacion.calle,
      restaurante.ubicacion.numero,
      restaurante.ubicacion.latitud,
      restaurante.ubicacion.longitud,
      restaurante.ubicacion.nombreCiudad,
      restaurante.ubicacion.nombreProvincia,
      restaurante.ubicacion.nombrePais,
    )
  }

  @JvmStatic
  fun toRestauranteUsuarioEntity(
    restaurante: Restaurante,
    idUsuario: String,
  ): RestauranteUsuarioEntity {
    return RestauranteUsuarioEntity(
      restaurante.id,
      restaurante.nombre,
      restaurante.estado,
      restaurante.puntuacion,
      restaurante.portada,
      restaurante.correo,
      restaurante.logo,
      restaurante.idDetalleRestaurante,
      restaurante.ubicacion.id,
      restaurante.ubicacion.calle,
      restaurante.ubicacion.numero,
      restaurante.ubicacion.latitud,
      restaurante.ubicacion.longitud,
      restaurante.ubicacion.nombreCiudad,
      restaurante.ubicacion.nombreProvincia,
      restaurante.ubicacion.nombrePais,
      idUsuario,
    )
  }
}
