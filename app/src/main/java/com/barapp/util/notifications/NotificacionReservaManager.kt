package com.barapp.util.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.barapp.R
import com.barapp.barapp.model.Reserva
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.ReservaRepository.Companion.instance
import com.barapp.util.BitmapConverter.convertBitmapToByteArray
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Objects

/**
 * Esta clase tiene las funciones necesarias para programar notificaciones que sirvan de recordatorio
 * para reservas pendientes
 *
 * @author Federico Quarin
 */
class NotificacionReservaManager {

  companion object {
    /**
     *
     *
     * Crea el canal sobre el que se va a emitir la notificacion de recordatorio de reserva.
     *
     *
     *
     * Es obligatorio haber creado el canal antes de que se lance la notificacion, por lo
     * que esta funcion se debe llamar lo antes posible dentro de la aplicacion.
     *
     *
     * @param context Contexto donde se debe crear el canal
     */
    fun crearCanalNotificacion(context: Context) {
      val channelId = context.getString(R.string.notificacion_channel_id_reserva)
      val name = context.getString(R.string.notificacion_channel_name_reserva)
      val description = context.getString(R.string.notificacion_channel_description_reserva)
      val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
      channel.description = description
      context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
  }
}
