package com.barapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.barapp.R

class MarkerUtils {
  /**
   * Toma el [Bitmap] logo y lo coloca en la posicion adecuada, sobre el del marcador.
   *
   * @param logo el bitmap del logo. Debe ser de tamaño 128x128
   * @param markerSize el tamaño deseado del marcador
   * @return un bitmap con el marcador y el logo en su interior
   */
  companion object {

    @JvmStatic
    fun construirMarcador(logo: Bitmap, context: Context): Bitmap {
      val markerSize = context.resources.getInteger(R.integer.marker_size)
      val logoSize = 78 * markerSize / 128

      val marker =
        ContextCompat.getDrawable(context, R.drawable.icon_color_marker)!!
          .toBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)

      val bordeHorizontal = (markerSize - logoSize) / 2 + 2

      val bmOverlay = Bitmap.createBitmap(marker.width, marker.height, marker.config)
      val canvas = Canvas(bmOverlay)

      canvas.drawBitmap(
        logo,
        null,
        Rect(
          bordeHorizontal,
          9 * markerSize / 128,
          bordeHorizontal + logoSize,
          9 * markerSize / 128 + logoSize,
        ),
        null,
      )

      val paint = Paint()
      paint.colorFilter =
        PorterDuffColorFilter(
          context.resources.getColor(R.color.md_theme_light_primaryContainer, null),
          PorterDuff.Mode.SRC_IN,
        )
      canvas.drawBitmap(marker, Matrix(), paint)

//      marker.recycle()
//      logo.recycle()

      return bmOverlay
    }
  }
}