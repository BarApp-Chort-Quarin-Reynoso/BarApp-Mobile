package com.barapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

object BitmapConverter {
  @JvmStatic
  fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    var baos: ByteArrayOutputStream? = null
    return try {
      baos = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
      baos.toByteArray()
    } finally {
      if (baos != null) {
        try {
          baos.close()
        } catch (e: IOException) {
          Timber.e("ByteArrayOutputStream was not closed")
        }
      }
    }
  }

  @JvmStatic
  fun convertCompressedByteArrayToBitmap(src: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(src, 0, src.size)
  }
}
