package com.barapp.data.utils

interface IGenericRepository<T> {
  fun buscarPorId(id: String, callback: FirestoreCallback<T>)

  fun buscarTodos(callback: FirestoreCallback<List<T>>)

  fun guardar(entidad: T)

  fun actualizar(entidad: T)

  fun borrar(entidad: T)
}
