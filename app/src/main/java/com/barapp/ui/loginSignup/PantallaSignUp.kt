package com.barapp.ui.loginSignup

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentSignupBinding
import com.barapp.model.DetalleUsuario
import com.barapp.model.Usuario
import com.barapp.ui.AuthActivity
import com.barapp.viewModels.authViewModels.PantallaSignUpViewModel
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

class PantallaSignUp : Fragment() {
  private lateinit var binding: FragmentSignupBinding
  private val viewModel: PantallaSignUpViewModel by viewModels()

  // Componentes
  private lateinit var nombre: TextInputLayout
  private lateinit var apellido: TextInputLayout
  private lateinit var email: TextInputLayout
  private lateinit var telefono: TextInputLayout
  private lateinit var contrasenia: TextInputLayout
  private lateinit var confirmarContrasenia: TextInputLayout
  private lateinit var botonCrearCuenta: Button

  private lateinit var prefixTelefono: String

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentSignupBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    nombre = binding.txtViewNombre
    apellido = binding.txtViewApellido
    email = binding.txtViewEmail
    telefono = binding.txtViewTelefono
    contrasenia = binding.txtViewContrasenia
    confirmarContrasenia = binding.txtViewConfirmeContrasenia
    botonCrearCuenta = binding.botonCrearCuentaSignup

    prefixTelefono = telefono.prefixText.toString()

    setearTextChangedListeners()
    botonCrearCuenta.setOnClickListener {
      if (validarDatos()) {
        crearUsuarioEnFirebase()
      }
    }

    viewModel.idUsuario.observe(viewLifecycleOwner) {
      (activity as AuthActivity?)!!.onLoginExitoso(it)
    }

    viewModel.error.observe(viewLifecycleOwner) {
      (activity as AuthActivity?)!!.errorAutenticacion()
      Timber.e(it)
    }
  }

  private fun crearUsuarioEnFirebase() {
    val telefonoUsuario = prefixTelefono + telefono.editText!!.text.toString()
    val detalleUsuario =
      DetalleUsuario(email.editText!!.text.toString(), telefonoUsuario)
    val usuario = Usuario()
    usuario.nombre = nombre.editText!!.text.toString()
    usuario.apellido = apellido.editText!!.text.toString()
    usuario.foto = "https://firebasestorage.googleapis.com/v0/b/barapp-b1bc0.appspot.com/o/images%2Fusuarios%2Fprofile_pecture_default.jpg?alt=media&token=11784857-f2c7-4b60-b332-e5580678c3d3"
    usuario.idDetalleUsuario = detalleUsuario.id
    usuario.detalleUsuario = detalleUsuario

    viewModel.crearUsuarioEnFirebaseAuth(
      email.editText!!.text.toString(),
      contrasenia.editText!!.text.toString(),
      usuario)
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun validarDatos(): Boolean {
    return validarNombre() && validarApellido() && validarEmail() && validarTelefono() &&
      validarContrasenia() && validarConfirmarContrasenia()
  }

  private fun validarNombre(): Boolean {
    val editableNombre = nombre.editText?.text
    return if (TextUtils.isEmpty(editableNombre)) {
      nombre.error = getString(R.string.error_campo_obligatorio)
      false
    } else if (editableNombre.toString().length !in 3..20) {
      nombre.error = getString(R.string.error_campo_fuera_de_rango, 3, 20)
      false
    } else true
  }

  private fun validarApellido(): Boolean {
    val editableApellido = apellido.editText?.text
    return if (TextUtils.isEmpty(editableApellido)) {
      apellido.error = getString(R.string.error_campo_obligatorio)
      false
    } else if (editableApellido.toString().length !in 3..20) {
      apellido.error = getString(R.string.error_campo_fuera_de_rango, 3, 20)
      false
    } else true
  }

  private fun validarEmail(): Boolean {
    val editableEmail = email.editText?.text
    return if (TextUtils.isEmpty(editableEmail)) {
      email.error = getString(R.string.error_campo_obligatorio)
      false
    } else if (editableEmail.toString().length !in 3..45) {
      email.error = getString(R.string.error_campo_fuera_de_rango, 3, 45)
      false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(editableEmail.toString()).matches()) {
      email.error = getString(R.string.error_mail_no_valido)
      false
    } else true
  }

  private fun validarTelefono(): Boolean {
    val editableTelefono = telefono.editText?.text
    return if (TextUtils.isEmpty(editableTelefono)) {
      telefono.error = getString(R.string.error_campo_obligatorio)
      false
    } else if (editableTelefono.toString().length !in 6..13) {
      telefono.error = getString(R.string.error_campo_fuera_de_rango, 7, 12)
      false
    } else true
  }

  private fun validarContrasenia(): Boolean {
    val editableContrasenia = contrasenia.editText?.text
    return if (TextUtils.isEmpty(editableContrasenia)) {
      contrasenia.error = getString(R.string.error_campo_obligatorio)
      false
    } else if (editableContrasenia.toString().length < 6) {
      contrasenia.error = getString(R.string.error_campo_por_debajo_valor, 6)
      false
    } else if (!editableContrasenia.toString().matches(".*\\d.*".toRegex())) {
      contrasenia.error = getString(R.string.error_campo_con_al_menos_un_digito)
      false
    } else true
  }

  private fun validarConfirmarContrasenia(): Boolean {
    val editableConfirmarContrasenia = confirmarContrasenia.editText?.text
    return if (TextUtils.isEmpty(editableConfirmarContrasenia)) {
      confirmarContrasenia.error = getString(R.string.error_campo_obligatorio)
      false
    } else if (editableConfirmarContrasenia.toString() != contrasenia.editText?.text.toString()) {
      confirmarContrasenia.error = getString(R.string.error_contrasenia_no_coincide)
      false
    } else true
  }

  private fun setearTextChangedListeners() {
    val textInputLayouts = listOf(nombre, apellido, email, telefono, contrasenia, confirmarContrasenia)
    textInputLayouts.forEach { it.editText?.addTextChangedListener(textChangeListener(it)) }
  }

  private fun textChangeListener(textInputLayout: TextInputLayout): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // noop
      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        textInputLayout.error = null
      }

      override fun afterTextChanged(s: Editable) {
        // noop
      }
    }
  }
}
