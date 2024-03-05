package com.barapp.model

import com.barapp.model.BaseClass
import java.util.LinkedList
import java.util.UUID

class DetalleUsuario(
  id: String,
  var mail: String,
  var telefono: String,
  var busquedasRecientes: LinkedList<String>,
  var idsRestaurantesFavoritos: HashSet<String>,
) : BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), "", "", LinkedList(), HashSet())

  constructor(
    mail: String,
    telefono: String,
  ) : this(UUID.randomUUID().toString(), mail, telefono, LinkedList(), HashSet())

  fun agregarFavorito(idRestaurante: String) {
    idsRestaurantesFavoritos.add(idRestaurante)
  }

  fun agregarBusquedaReciente(textoBusqueda: String) {
    if (busquedasRecientes.contains(textoBusqueda)) {
      busquedasRecientes.remove(textoBusqueda)
      busquedasRecientes.addFirst(textoBusqueda)
    } else {
      if (busquedasRecientes.size < 4) {
        busquedasRecientes.addFirst(textoBusqueda)
      } else {
        busquedasRecientes.addFirst(textoBusqueda)
        busquedasRecientes.removeLast()
      }
    }
  }

  fun eliminarBusquedasRecientes() {
    busquedasRecientes = LinkedList()
  }

  override fun toString(): String {
    return "DetalleUsuario{" +
      "id='" +
      id +
      '\'' +
      ", mail='" +
      mail +
      '\'' +
      ", telefono='" +
      telefono +
      '\'' +
      ", busquedasRecientes=" +
      busquedasRecientes +
      ", idsRestaurantesFavoritos=" +
      idsRestaurantesFavoritos +
      '}'
  }
}
