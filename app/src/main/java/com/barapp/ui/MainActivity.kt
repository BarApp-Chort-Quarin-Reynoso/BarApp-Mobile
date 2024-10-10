package com.barapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.MainActivityBinding
import com.barapp.ui.pantallas.OnReservaClicked
import com.barapp.ui.pantallas.PantallaPrincipal
import com.barapp.util.interfaces.LogOutListener
import com.barapp.util.notifications.NotificacionReservaManager
import com.barapp.data.retrofit.RetrofitInstance
import com.barapp.viewModels.MainActivityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import timber.log.Timber

class MainActivity :
  AppCompatActivity(), LogOutListener {

  companion object {
    const val DESDE_BOTON_PRINCIPAL = 1
    const val NAVEGACION_DESDE_NOTIFICACION_RESERVA = 1
    const val NAVEGACION_DESDE_CONFIRMACION_RESERVA = 2
    const val NAVEGACION_DESDE_NOTIFICACION_OPINAR = 3
    const val NAVEGACION_DESDE_NOTIFICACION_RESERVA_CANCELADA = 4
  }

  private lateinit var binding: MainActivityBinding

  private val mainActivityViewModel: MainActivityViewModel by viewModels {
    MainActivityViewModel.Factory(intent.extras?.getString("origen"))
  }

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private val cancellationTokenSource: CancellationTokenSource = CancellationTokenSource()

  private val startAuthActivity =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      if (it.resultCode != AuthActivity.RESULTADO_USUARIO_LOGEADO) {
        this.finish()
      } else {
        val idUsuario = it.data?.extras?.getString("idUsuario")
        Timber.e("Id que llego del intent: %s", idUsuario)

        idUsuario?.let { id ->
          // Guardado de datos de sesion
          val prefs =
            getSharedPreferences(getString(R.string.shared_pref_file), Context.MODE_PRIVATE)
          prefs.edit().putString("idUsuario", id).apply()

          val fcmtoken =
            getSharedPreferences(getString(R.string.persistent_pref_file), Context.MODE_PRIVATE)
              .getString("fcmtoken", "")!!

          onLogedIn(id, fcmtoken)
        }
      }
    }

  private val requestLocationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        obtenerUbicacion()
      }
    }

  private val requestNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        initNotifications()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    RetrofitInstance.initialize(this)

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_NO -> comprobarSesionActiva()
    }

    binding = MainActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }

  // Comprobar si tenemos guardado un id/una sesion activa
  private fun comprobarSesionActiva() {
    val sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_file), MODE_PRIVATE)
    val idUsuario = sharedPreferences.getString("idUsuario", null)
    val fcmtoken =
      getSharedPreferences(getString(R.string.persistent_pref_file), Context.MODE_PRIVATE)
        .getString(getString(R.string.prefkey_fcmtoken), null)

    if (idUsuario == null) {
      iniciarAutenticacion()
    } else {
      onLogedIn(idUsuario, fcmtoken)
    }
  }

  private fun iniciarAutenticacion() {
    val intent = Intent(this, AuthActivity::class.java)

    startAuthActivity.launch(intent)
  }

  private fun onLogedIn(idUsuario: String, fcmtoken: String?) {
    askNotificationPermission()

    mainActivityViewModel.buscarUsuarioPorId(idUsuario)
    mainActivityViewModel.usuario.observe(this) {
      mainActivityViewModel.guardarFcmToken(it, fcmtoken)
    }

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    obtenerUbicacion()
  }

  override fun onLogOut() {
    val prefs =
      getSharedPreferences(getString(R.string.shared_pref_file), Context.MODE_PRIVATE)

    iniciarAutenticacion()
  }

  private fun askNotificationPermission() {
    // This is only necessary for API level >= 33 (TIRAMISU)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
      ) {
        initNotifications()
      } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
        // TODO ask rational permission
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        // Directly ask for the permission
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  private fun initNotifications() {
    NotificacionReservaManager.crearCanalNotificacion(this)
  }

  private fun obtenerUbicacion() {
    if (
      ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
      PackageManager.PERMISSION_GRANTED &&
      ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
      PackageManager.PERMISSION_GRANTED
    ) {
      if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
        return
      }

      pedirPermisos()
      return
    }

    fusedLocationClient
      .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
      .addOnSuccessListener {
        it?.let {
          mainActivityViewModel.setearUbicacion(it)
        }
          ?: run {
            // TODO ver como pedir activar localizacion
          }
      }
  }

  private fun pedirPermisos() {
    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
  }

  fun setLoading(loading: Boolean) {
    if (loading) {
      binding.loadingOverlay.visibility = View.VISIBLE
      binding.loadingProgressBar.visibility = View.VISIBLE
    }
    else {
      binding.loadingOverlay.visibility = View.GONE
      binding.loadingProgressBar.visibility = View.GONE
    }
  }
}
