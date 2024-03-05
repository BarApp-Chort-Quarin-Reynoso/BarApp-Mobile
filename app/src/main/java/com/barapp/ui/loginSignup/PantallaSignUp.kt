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

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentSignupBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Componentes
    nombre = binding.txtViewNombre
    apellido = binding.txtViewApellido
    email = binding.txtViewEmail
    telefono = binding.txtViewTelefono
    contrasenia = binding.txtViewContrasenia
    confirmarContrasenia = binding.txtViewConfirmeContrasenia
    botonCrearCuenta = binding.botonCrearCuentaSignup

    setearTextChangedListeners()
    botonCrearCuenta.setOnClickListener {
      if (validarDatos()) {
        crearUsuarioEnFirebase()
      }
    }
  }

  private fun crearUsuarioEnFirebase() {
    val detalleUsuario =
      DetalleUsuario(email.editText!!.text.toString(), telefono.editText!!.text.toString())
    val usuario = Usuario()
    usuario.nombre = nombre.editText!!.text.toString()
    usuario.apellido = apellido.editText!!.text.toString()
    usuario.foto = "https://firebasestorage.googleapis.com/v0/b/barapp-b1bc0.appspot.com/o/images%2Fusuarios%2Fdefault-400x400.jpg?alt=media&token=1f93f14d-c435-496d-97b1-06457913ed1d"
    usuario.idDetalleUsuario = detalleUsuario.id
    usuario.detalleUsuario = detalleUsuario

    viewModel.crearUsuarioEnFirebaseAuth(
      email.editText!!.text.toString(),
      contrasenia.editText!!.text.toString(),
      usuario)

    viewModel.idUsuario.observe(viewLifecycleOwner) {
      (activity as AuthActivity?)!!.onLoginExitoso(it)
    }

    viewModel.error.observe(viewLifecycleOwner) {
      (activity as AuthActivity?)!!.errorAutenticacion()
      Timber.e(it)
    }
  }

  private fun validarDatos(): Boolean {
    var camposCorrectos = true
    val editableNombre = nombre.editText!!.text
    val editableApellido = apellido.editText!!.text
    val editableEmail = email.editText!!.text
    val editableTelefono = telefono.editText!!.text
    val editableContrasenia = contrasenia.editText!!.text
    val editableConfirmarContrasenia = confirmarContrasenia.editText!!.text

    // Nombre
    if (TextUtils.isEmpty(editableNombre)) {
      nombre.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (editableNombre.toString().length < 3 || editableNombre.toString().length > 20) {
      nombre.error = getString(R.string.error_campo_fuera_de_rango, 3, 20)
      camposCorrectos = false
    }

    // Apellido
    if (TextUtils.isEmpty(editableApellido)) {
      apellido.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (editableApellido.toString().length < 3 || editableApellido.toString().length > 20) {
      apellido.error = getString(R.string.error_campo_fuera_de_rango, 3, 20)
      camposCorrectos = false
    }

    // Email
    if (TextUtils.isEmpty(editableEmail)) {
      email.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (editableEmail.toString().length < 3 || editableEmail.toString().length > 45) {
      email.error = getString(R.string.error_campo_fuera_de_rango, 3, 45)
      camposCorrectos = false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(editableEmail.toString()).matches()) {
      email.error = getString(R.string.error_mail_no_valido)
      camposCorrectos = false
    }

    // Telefono
    if (TextUtils.isEmpty(editableTelefono)) {
      telefono.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (editableTelefono.toString().length < 6 || editableTelefono.toString().length > 13) {
      telefono.error = getString(R.string.error_campo_fuera_de_rango, 7, 12)
      camposCorrectos = false
    }

    // Contraseña
    if (TextUtils.isEmpty(editableContrasenia)) {
      contrasenia.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (editableContrasenia.toString().length < 5) {
      contrasenia.error = getString(R.string.error_campo_por_debajo_valor, 6)
      camposCorrectos = false
    } else if (!editableContrasenia.toString().matches(".*\\d.*".toRegex())) {
      contrasenia.error = getString(R.string.error_campo_con_al_menos_un_digito)
      camposCorrectos = false
    }

    // Repetir contraseña
    if (TextUtils.isEmpty(editableConfirmarContrasenia)) {
      confirmarContrasenia.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (
      !TextUtils.isEmpty(editableContrasenia) &&
        editableConfirmarContrasenia.toString() != editableContrasenia.toString()
    ) {
      confirmarContrasenia.error = getString(R.string.error_contrasenia_no_coincide)
      camposCorrectos = false
    }
    return camposCorrectos
  }

  private fun setearTextChangedListeners() {
    nombre.editText!!.addTextChangedListener(textChangeListener(nombre))
    apellido.editText!!.addTextChangedListener(textChangeListener(apellido))
    email.editText!!.addTextChangedListener(textChangeListener(email))
    telefono.editText!!.addTextChangedListener(textChangeListener(telefono))
    contrasenia.editText!!.addTextChangedListener(textChangeListener(contrasenia))
    confirmarContrasenia
      .editText!!
      .addTextChangedListener(textChangeListener(confirmarContrasenia))
  }

  private fun textChangeListener(textInputLayout: TextInputLayout?): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // noop
      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        textInputLayout?.error = null
      }

      override fun afterTextChanged(s: Editable) {
        // noop
      }
    }
  }
}
