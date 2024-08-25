package com.barapp.util.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.barapp.ui.MainActivity
import com.barapp.R
import com.barapp.util.BitmapConverter.convertCompressedByteArrayToBitmap
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Clase que genera una notificacion de recordatorio de reserva al recibir un intent destinado a
 * tal fin.
 *
 * @author Federico Quarin
 */
class NotificacionReservaReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Timber.e("Notificacion emitidad - " + LocalDateTime.now())
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      return
    }
    val restaurante = intent.getStringExtra("restaurante")
    val logo = convertCompressedByteArrayToBitmap(
      intent.getByteArrayExtra("logo")!!
    )

    if (restaurante == null) return

    val destino = Intent(context, MainActivity::class.java)
    destino.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    val bundle = Bundle()
    bundle.putInt("origen", MainActivity.DESDE_NOTIFICACION)

    val channelId = context.getString(R.string.notificacion_channel_id_reserva)

    val pendingIntent = NavDeepLinkBuilder(context)
      .setGraph(R.navigation.navegacion)
      .setDestination(R.id.pantallaNavegacionPrincipal)
      .setComponentName(MainActivity::class.java)
      .setArguments(bundle)
      .createPendingIntent()

    val notification = NotificationCompat.Builder(context, channelId)
      .setContentTitle(context.getString(R.string.notificacion_reserva_title, restaurante))
      .setContentText(context.getString(R.string.notificacion_reserva_text, restaurante))
      .setSmallIcon(R.mipmap.ic_launcher_barrapp)
      .setLargeIcon(logo)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .build()
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(NOTIFICATION_ID, notification)
  }

  companion object {
    private const val NOTIFICATION_ID = 1
  }
}
