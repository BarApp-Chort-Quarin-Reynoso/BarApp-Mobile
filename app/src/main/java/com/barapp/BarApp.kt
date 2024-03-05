package com.barapp

import android.app.Application
import timber.log.Timber

class BarApp : Application() {
  // Called when the application is starting, before any other application objects have been
  // created.
  // Overriding this method is totally optional!
  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
  }
}
