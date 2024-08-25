package com.barapp.util.notifications

import android.content.Context
import com.barapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class NotificationNotificationService : FirebaseMessagingService() {
  override fun onNewToken(token: String) {
    val persistentPref = getSharedPreferences(getString(R.string.persistent_pref_file), Context.MODE_PRIVATE)
    persistentPref.edit().putString(getString(R.string.prefkey_fcmtoken), token).apply()

    Timber.d("Refreshed token: $token")
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)

    Timber.d(message.messageId)
  }
}