package com.barapp.ui.loginSignup

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentLoginBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.AuthActivity
import com.barapp.util.retrofit.LoginService
import com.barapp.util.retrofit.RetrofitInstance
import com.barapp.viewModels.authViewModels.PantallaLoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PantallaLogin : Fragment() {
  private lateinit var binding: FragmentLoginBinding
  private val viewModel: PantallaLoginViewModel by viewModels()

  // Componentes
  private lateinit var email: TextInputLayout
  private lateinit var contrasenia: TextInputLayout
  private lateinit var botonLoguearse: Button
  private lateinit var botonRegistrarse: Button
  private lateinit var botonGoogle: ImageButton
  private lateinit var botonFacebook: ImageButton

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentLoginBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Componentes
    botonLoguearse = binding.botonIngresar
    botonRegistrarse = binding.botonRegistrarse
    email = binding.txtViewEmail
    contrasenia = binding.txtViewContrasenia
    botonGoogle = binding.botonGoogle
    botonFacebook = binding.botonFacebook

    viewModel.idUsuario.observe(viewLifecycleOwner) { id ->
      (activity as AuthActivity).onLoginExitoso(id)
    }
    viewModel.error.observe(viewLifecycleOwner) {
      (activity as AuthActivity).errorAutenticacion()
      Timber.e(it)
    }

    setearTextChangedListeners()
    botonLoguearse.setOnClickListener {
      if (validarDatos()) {
//        val call = RetrofitInstance.loginService.login(email.editText!!.text.toString(), contrasenia.editText!!.text.toString(), "cCwWAQ1vuYRRVkT2jZgkyqpYsm5xWq4XOr9PQrIJV7gQ5i8KER11LDVZ2LN8ZXOSv6lFqZtt1A1C")
        val call = RetrofitInstance.createService(LoginService::class.java).login("bar@gmail.com", "12345678", "3m_P0F67R-L78poGNvRnC7aj3-AA7x_W-Gocn_GZQ2GC4RWru1__4jvaftTWwvk0D9lTOtTF8tkx3ir7ml9-_Mf_dVXn2Saa")
        call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
          val headers = response.headers()
          println("Headers: $headers")
          val jsessionIdFull = headers.get("Set-Cookie")
          val jsessionId = jsessionIdFull?.split("=")?.get(1)?.split(";")?.get(0)

          // Save the JSESSIONID in SharedPreferences
          val sharedPref = requireContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
          with(sharedPref.edit()) {
            putString("JSESSIONID", jsessionId)
            apply()
          }

          viewModel.signInUsuarioEnFirebase(
            email.editText!!.text.toString(),
            contrasenia.editText!!.text.toString()
          )
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
          (activity as AuthActivity).errorAutenticacion()
          Timber.e(t)
        }

      })
      }
    }
    botonRegistrarse.setOnClickListener {
      NavHostFragment.findNavController(this).navigate(R.id.action_pantallaLogin_to_pantallaSignUp)
    }
//      val message = getString(R.string.login_with_google_not_implemented)
//      Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    botonGoogle.setOnClickListener {
//      RetrofitInstance.userApiService.getAllUsers(emptyMap()).enqueue(object : Callback<List<Usuario>> {
//        override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
//          if (response.isSuccessful) {
//            val data = response.body()
//            // Use your data here
//            // For example, you can print it to the log:
//            Timber.d("Data received: $data")
//          } else {
//            // Handle the error
//            Timber.e("Error: ${response.errorBody()}")
//          }
//        }
//
//        override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
//          // Handle the failure
//          Timber.e(t, "Failure")
//        }
//      })
    }
    botonFacebook.setOnClickListener {
//      RetrofitInstance.restaurantApiService.getAllRestaurants(emptyMap()).enqueue(object : Callback<List<Restaurante>> {
//        override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
//          if (response.isSuccessful) {
//            val data = response.body()
//            // Use your data here
//            // For example, you can print it to the log:
//            Timber.d("Data received: $data")
//          } else {
//            // Handle the error
//            Timber.e("Error: ${response.errorBody()}")
//          }
//        }
//
//        override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
//          // Handle the failure
//          Timber.e(t, "Failure")
//        }
//      })
    }
  }

  private fun setearTextChangedListeners() {
    email.editText!!.addTextChangedListener(textChangeListener(email))
    contrasenia.editText!!.addTextChangedListener(textChangeListener(contrasenia))
  }

  private fun validarDatos(): Boolean {
    var camposCorrectos = true
    val editableEmail = email.editText!!.text
    val editableContrasenia = contrasenia.editText!!.text

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

    // Contraseña
    if (TextUtils.isEmpty(editableContrasenia)) {
      contrasenia.error = getString(R.string.error_campo_obligatorio)
      camposCorrectos = false
    } else if (editableContrasenia.toString().length < 5) {
      contrasenia.error = getString(R.string.error_campo_por_debajo_valor, 5)
      camposCorrectos = false
    } else if (!editableContrasenia.toString().matches(".*\\d.*".toRegex())) {
      contrasenia.error = getString(R.string.error_campo_con_al_menos_un_digito)
      camposCorrectos = false
    }
    return camposCorrectos
  }



  private fun textChangeListener(textInputLayout: TextInputLayout?): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // noop
      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        textInputLayout!!.error = null
      }

      override fun afterTextChanged(s: Editable) {
        // noop
      }
    }
  }
}
