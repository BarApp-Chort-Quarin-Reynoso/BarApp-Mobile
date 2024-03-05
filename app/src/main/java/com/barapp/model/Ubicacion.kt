package com.barapp.model

import com.barapp.model.BaseClass
import java.util.UUID

class Ubicacion(
  id: String,
  var calle: String,
  var numero: Int,
  var latitud: Double,
  var longitud: Double,
  var nombreCiudad: String,
  var nombreProvincia: String,
  var nombrePais: String,
) : BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), "", -1, -1.0, -1.0, "", "", "")

  constructor(
    calle: String,
    numero: Int,
    latitud: Double,
    longitud: Double,
    nombreCiudad: String,
    nombreProvincia: String,
    nombrePais: String,
  ) : this(
    UUID.randomUUID().toString(),
    calle,
    numero,
    latitud,
    longitud,
    nombreCiudad,
    nombreProvincia,
    nombrePais,
  )

  override fun toString(): String {
    return "Ubicacion{" +
      "idUbicacion='" +
      id +
      '\'' +
      ", calle='" +
      calle +
      '\'' +
      ", numero=" +
      numero +
      ", latitud=" +
      latitud +
      ", longitud=" +
      longitud +
      ", nombreCiudad='" +
      nombreCiudad +
      '\'' +
      ", nombreProvincia='" +
      nombreProvincia +
      '\'' +
      ", nombrePais='" +
      nombrePais +
      '\'' +
      '}'
  }
}
