package com.barapp.data.utils

interface FirestoreCallback<T> {
  fun onSuccess(result: T)

  fun onError(exception: Throwable)
}
