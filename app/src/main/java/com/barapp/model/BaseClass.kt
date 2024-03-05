package com.barapp.model

abstract class BaseClass(var id: String) {

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BaseClass) return false

    return id == other.id
  }
}
