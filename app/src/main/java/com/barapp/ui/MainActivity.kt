package com.barapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.MainActivityBinding
import com.barapp.ui.pantallas.OnReservaClicked
import com.barapp.ui.pantallas.PantallaPrincipal
import com.barapp.util.interfaces.LogOutListener
import com.barapp.util.notifications.NotificacionReservaManager
import com.barapp.util.retrofit.RetrofitInstance
import com.barapp.viewModels.MainActivityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import timber.log.Timber

class MainActivity :
  AppCompatActivity(), PantallaPrincipal.OnFabBuscarClicked, LogOutListener, OnReservaClicked {

  companion object {
    const val DESDE_BOTON_PRINCIPAL = 0
    const val DESDE_NOTIFICACION = 1
    const val DESDE_CONFIRMACION_RESERVA = 2
  }

  private lateinit var binding: MainActivityBinding

  private val mainActivityViewModel: MainActivityViewModel by viewModels()

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private val cancellationTokenSource: CancellationTokenSource = CancellationTokenSource()

  private val startAuthActivity =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      if (it.resultCode != AuthActivity.RESULTADO_USUARIO_LOGEADO) {
        this.finish()
      } else {
        val idUsuario = it.data?.extras?.getString("idUsuario")

        idUsuario?.let { id ->
          // Guardado de datos de sesion
          val prefs =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
          prefs.putString("idUsuario", id)
          prefs.apply()
        }
        Timber.e("Id que llego del intent: %s", idUsuario)

        if (idUsuario != null) {
          onLogedIn(idUsuario)
        }
      }
    }

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        obtenerUbicacion()
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
    val sharedPreferences = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
    val idUsuario = sharedPreferences.getString("idUsuario", null)

    if (idUsuario == null) {
      iniciarAutenticacion()
    } else {
      onLogedIn(idUsuario)
    }
  }

  private fun iniciarAutenticacion() {
    val intent = Intent(this, AuthActivity::class.java)

    startAuthActivity.launch(intent)
  }

  private fun onLogedIn(idUsuario: String) {
    NotificacionReservaManager.crearCanalNotificacion(this)
    NotificacionReservaManager(idUsuario).sincronizarAlarmas(this)

    mainActivityViewModel.buscarYGuardarUsuarioPorId(idUsuario)

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    obtenerUbicacion()
  }

  override fun onLogOut() {
    iniciarAutenticacion()
  }

  override fun onFabBuscarClicked(fabBuscar: View) {
    val extras = FragmentNavigatorExtras(fabBuscar to "transition_pantalla_buscar")

    NavHostFragment.findNavController(binding.fragmentContainerView.getFragment())
      .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaBusqueda, null, null, extras)
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
          Timber.e("Localizacion encontrada")
        }
          ?: run {
            // TODO ver como pedir activar localizacion
          }
      }
  }

  private fun pedirPermisos() {
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
  }

  override fun onReservaClicked() {

    NavHostFragment.findNavController(binding.fragmentContainerView.getFragment())
      .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaResumenReserva, null, null)
  }
}
