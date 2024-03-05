package com.barapp.data.utils

class EntityNotFoundException(msg: String) : Exception(msg) {
  constructor() : this("The entity was not found")
}