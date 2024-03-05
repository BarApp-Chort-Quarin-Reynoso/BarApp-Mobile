package com.barapp.util.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.barapp.R
import timber.log.Timber

class BootCompletedReciver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.e("BootCompletedReciver iniciado")


    }
}