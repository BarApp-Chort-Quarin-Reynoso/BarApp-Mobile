package com.barapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.barapp.databinding.ActivityAutentificacionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AuthActivity : AppCompatActivity() {
  private lateinit var binding: ActivityAutentificacionBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAutentificacionBinding.inflate(layoutInflater)
    val view: View = binding.root
    setContentView(view)
  }

  fun onLoginExitoso(idUsuario: String?) {
    val intent = Intent()
    intent.putExtra("idUsuario", idUsuario)
    setResult(RESULTADO_USUARIO_LOGEADO, intent)
    finish()
  }

  fun errorAutenticacion() {
    MaterialAlertDialogBuilder(this)
      .setTitle("Error")
      .setMessage("Se ha producido un error autenticando al usuario")
      .setPositiveButton("Aceptar", null)
      .show()
  }

  companion object {
    const val RESULTADO_USUARIO_LOGEADO = 100
  }
}
