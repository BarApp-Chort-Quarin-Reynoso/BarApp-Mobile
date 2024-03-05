package com.barapp.viewModels

import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.R
import com.barapp.model.Usuario
import com.barapp.data.repositories.DetalleRestauranteRepository
import com.barapp.data.repositories.UsuarioRepository

class PantallaMiCuentaViewModel : ViewModel() {

  // lateinit var usuario : Usuario

  private var _codigoErrorNombre: MutableLiveData<Int> = MutableLiveData()
  val codigoErrorNombre: LiveData<Int> = _codigoErrorNombre

  private var _codigoErrorApellido: MutableLiveData<Int> = MutableLiveData()
  val codigoErrorApellido: LiveData<Int> = _codigoErrorApellido

  private var _codigoErrorMail: MutableLiveData<Int> = MutableLiveData()
  val codigoErrorMail: LiveData<Int> = _codigoErrorMail

  private var _codigoErrorTelefono: MutableLiveData<Int> = MutableLiveData()
  val codigoErrorTelefono: LiveData<Int> = _codigoErrorTelefono

  private var _nombreUsuario: MutableLiveData<String> = MutableLiveData()
  val nombreUsuario: LiveData<String> = _nombreUsuario

  private var _apellidoUsuario: MutableLiveData<String> = MutableLiveData()
  val apellidoUsuario: LiveData<String> = _apellidoUsuario

  private var _mailUsuario: MutableLiveData<String> = MutableLiveData()
  val mailUsuario: LiveData<String> = _mailUsuario

  private var _telefonoUsuario: MutableLiveData<String> = MutableLiveData()
  val telefonoUsuario: LiveData<String> = _telefonoUsuario

  fun actualizarFoto(usuario: Usuario) {
    UsuarioRepository.instance.actualizarFoto(usuario)
    DetalleRestauranteRepository.instance.actualizarFotoUsuario(usuario)
  }

  fun validarNombre(nombre: Editable?): Boolean {

    var campoValido = true

    if (TextUtils.isEmpty(nombre)) {
      _codigoErrorNombre.value = R.string.error_campo_obligatorio
      campoValido = false
    } else if (nombre.toString().length < 3 || nombre.toString().length > 20) {
      _codigoErrorNombre.value = R.string.error_campo_fuera_de_rango
      campoValido = false
    }

    return campoValido
  }

  fun validarApellido(apellido: Editable?): Boolean {

    var campoValido = true

    if (TextUtils.isEmpty(apellido)) {
      _codigoErrorApellido.value = R.string.error_campo_obligatorio
      campoValido = false
    } else if (apellido.toString().length < 3 || apellido.toString().length > 20) {
      _codigoErrorApellido.value = R.string.error_campo_fuera_de_rango
      campoValido = false
    }

    return campoValido
  }

  fun validarMail(email: Editable?): Boolean {

    var campoValido = true

    if (TextUtils.isEmpty(email)) {
      _codigoErrorMail.value = R.string.error_campo_obligatorio
      campoValido = false
    } else if (email.toString().length < 3 || email.toString().length > 45) {
      _codigoErrorMail.value = R.string.error_campo_fuera_de_rango
      campoValido = false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()) {
      _codigoErrorMail.value = R.string.error_mail_no_valido
      campoValido = false
    }

    return campoValido
  }

  fun validarTelefono(telefono: Editable?): Boolean {

    var campoValido = true

    if (TextUtils.isEmpty(telefono)) {
      _codigoErrorTelefono.value = R.string.error_campo_obligatorio
      campoValido = false
    } else if (telefono.toString().length < 6 || telefono.toString().length > 13) {
      _codigoErrorTelefono.value = R.string.error_campo_fuera_de_rango
      campoValido = false
    }

    return campoValido
  }
}
