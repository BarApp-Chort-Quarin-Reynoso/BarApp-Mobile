package com.barapp.ui.pantallas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentPantallaMiCuentaBinding
import com.barapp.util.interfaces.LogOutListener
import com.barapp.util.Interpolator
import com.barapp.util.notifications.NotificacionReservaManager
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaMiCuentaViewModel
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber

class PantallaMiCuenta : Fragment() {

  private lateinit var imageUri: Uri
  private var cambioFotoUsuario = false
  private lateinit var nombreArchivoFoto: String

  private lateinit var logOutListener: LogOutListener

  private lateinit var binding: FragmentPantallaMiCuentaBinding

  private val viewModel: PantallaMiCuentaViewModel by viewModels()

  private val activityViewModel: MainActivityViewModel by activityViewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition = MaterialFadeThrough().setInterpolator(Interpolator.emphasizedInterpolator())
    exitTransition = MaterialFadeThrough().setInterpolator(Interpolator.emphasizedInterpolator())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {

    binding = FragmentPantallaMiCuentaBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    activityViewModel.usuario.observe(viewLifecycleOwner) {
      nombreArchivoFoto = "profile_picture_" + it.id.toString()

      // Setear datos del usuario al fragmento
      Glide.with(requireContext())
        .load(activityViewModel.usuario.value!!.foto)
        .into(binding.imageViewFotoUsuario)

      prepararTextField(binding.textFieldNombre, it.nombre)
      prepararTextField(binding.textFieldApellido, it.apellido)
      prepararTextField(binding.textFieldMail, it.detalleUsuario!!.mail)
      prepararTextField(binding.textFieldTelefono, it.detalleUsuario!!.telefono)
    }

    logOutListener = requireActivity() as LogOutListener

    binding.botonEditarFoto.setOnClickListener { cargarFotoUsuario() }

    binding.botonCerrarSesion.setOnClickListener {
      MaterialAlertDialogBuilder(this.requireContext())
        .setTitle(R.string.pantalla_mi_cuenta_dialog_titulo)
        .setMessage(R.string.pantalla_mi_cuenta_dialog_texto)
        .setNegativeButton(R.string.boton_cancelar) { _, _ -> }
        .setPositiveButton(R.string.boton_confirmar) { _, _ -> cerrarSesion() }
        .show()
    }

    binding.botonGuardarCambios.setOnClickListener {
      if (cambioFotoUsuario) guardarFotoEnBaseDeDatos()
    }
  }

  private fun mostrarToastCambiosGuardados() {

    Toast.makeText(this.context, "¡Cambios guardados con éxito!", Toast.LENGTH_SHORT).show()
  }

  private fun cargarFotoUsuario() {
    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT
    launchSomeActivity.launch(intent)
  }

  /* atributo que se utiliza para buscar datos de otra aplicacion.
   * Se hace asi para no usar el startActivityForResult() que esta deprecated
   */
  private val launchSomeActivity: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result: ActivityResult ->
      if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data

        if (data != null && data.data != null) {
          imageUri = data.data!!

          Glide.with(requireContext()).load(imageUri).into(binding.imageViewFotoUsuario)

          cambioFotoUsuario = true
        }
      }
    }

  private fun guardarFotoEnBaseDeDatos() {

    // Se setea la ubicacion donde se guardara

    val storageRef =
      FirebaseStorage.getInstance().getReference("images/usuarios/").child(nombreArchivoFoto)

    storageRef
      .putFile(imageUri)
      .addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { urlFoto ->
          mostrarToastCambiosGuardados()
          activityViewModel.usuario.value!!.foto = urlFoto.toString()
          viewModel.actualizarFoto(activityViewModel.usuario.value!!)
        }
      }
      .addOnFailureListener { Timber.e(it) }
  }

  private fun cerrarSesion() {

    val prefs =
      requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), Context.MODE_PRIVATE)
    val persistentPref = requireActivity().getSharedPreferences(getString(R.string.persistent_pref_file), Context.MODE_PRIVATE)

    if (prefs != null) {
      val fcmtoken = persistentPref.getString(getString(R.string.prefkey_fcmtoken), null)
      val editor = prefs.edit()

      activityViewModel.usuario.value?.let {
        val manager = NotificacionReservaManager(it.id)
        manager.eliminarAlarmas(requireContext())
      }
      activityViewModel.eliminarFcmToken(fcmtoken)

      editor.clear()
      editor.apply()
      FirebaseAuth.getInstance().signOut()

      logOutListener.onLogOut()
    }
  }

  private fun prepararTextField(textField: TextInputLayout, texto: String) {

    textField.editText!!.setText(texto)

    // Validaciones para que el campo no pueda ser modificado
    textField.editText!!.isActivated = false
    textField.editText!!.isClickable = false
    textField.editText!!.isFocusable = false
  }
}
