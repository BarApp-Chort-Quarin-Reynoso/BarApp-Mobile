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
class NotificacionReservaManager(private val idUsuario: String) {
  fun sincronizarAlarmas(context: Context) {
    Timber.i("Sincronizar Alarmas - En ejecucion")
    recuperarReservasANotificar(object : FirestoreCallback<List<Reserva>> {
      override fun onSuccess(result: List<Reserva>) {
        for (r in result) {
          crearAlarma(context, r)
        }
      }

      override fun onError(exception: Throwable) {}
    })
  }

  fun eliminarAlarmas(context: Context) {
    Timber.i("Eliminar Alarmas - En ejecucion")
    recuperarReservasANotificar(object : FirestoreCallback<List<Reserva>> {
      override fun onSuccess(result: List<Reserva>) {
        for (r in result) {
          cancelarAlarma(context, r)
        }
      }

      override fun onError(exception: Throwable) {}
    })
  }

  private fun recuperarReservasANotificar(callback: FirestoreCallback<List<Reserva>>) {
    instance.buscarReservasANotificar(idUsuario, TIEMPO_ALARMA_MINUTOS, callback)
  }

  companion object {
    const val CHANNEL_ID = "General"
    const val TIEMPO_ALARMA_MINUTOS = 60

    /**
     * Crea una alarma para una hora antes de la [Reserva] dada, que lanza una notificacion
     * de recordatorio de que se tiene dicha reserva.
     *
     * @param context Contexto donde se genera la alarma
     * @param reserva Reserva acerca de la que se establece el recordatorio
     */
    fun crearAlarma(context: Context, reserva: Reserva) {
      val dateTime = LocalDateTime.of(reserva.getFechaAsLocalDate(), reserva.horario.getHorarioAsLocalTime()).minusMinutes(
        TIEMPO_ALARMA_MINUTOS.toLong()
      )
      if (dateTime.isBefore(LocalDateTime.now())) return
      val timeInMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
      val logo = convertBitmapToByteArray(
        (Objects.requireNonNull(
          AppCompatResources.getDrawable(
            context,
            R.drawable.ic_launcher_barrapp
          )
        ) as BitmapDrawable).bitmap
      )
      Glide.with(context)
        .asBitmap()
        .load(reserva.restaurante.logo)
        .into(object : CustomTarget<Bitmap>() {
          override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val logo = convertBitmapToByteArray(resource)
            val intent = Intent(context, NotificacionReservaReceiver::class.java)
            intent.putExtra("message", "Single Shot Alarm")
            intent.putExtra("restaurante", reserva.restaurante.nombre)
            intent.putExtra("logo", logo)
            val pi = PendingIntent.getBroadcast(
              context,
              reserva.id.hashCode(),
              intent,
              PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am[AlarmManager.RTC_WAKEUP, timeInMillis] = pi
            Timber.e("Alarma seteada - $dateTime")
          }

          override fun onLoadCleared(placeholder: Drawable?) {
            val intent = Intent(context, NotificacionReservaReceiver::class.java)
            intent.putExtra("message", "Single Shot Alarm")
            intent.putExtra("restaurante", reserva.restaurante.nombre)
            intent.putExtra("logo", logo)
            val pi = PendingIntent.getBroadcast(
              context,
              reserva.id.hashCode(),
              intent,
              PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am[AlarmManager.RTC_WAKEUP, timeInMillis] = pi
            Timber.e("Alarma seteada - $dateTime")
          }
        })
    }

    fun cancelarAlarma(context: Context, reserva: Reserva) {
      val intent = Intent(context, NotificacionReservaReceiver::class.java)
      intent.putExtra("message", "Single Shot Alarm")
      val pi = PendingIntent.getBroadcast(
        context,
        reserva.id.hashCode(),
        intent,
        PendingIntent.FLAG_IMMUTABLE
      )
      val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      am.cancel(pi)
    }

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
      val name = context.getString(R.string.notificacion_reserva_channel_name)
      val description = context.getString(R.string.notificacion_reserva_channel_description)
      val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
      channel.description = description
      context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
  }
}
