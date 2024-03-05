package com.barapp.ui.pantallas

import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentPantallaBusquedaBinding
import com.barapp.model.Usuario
import com.barapp.util.Interpolator.Companion.emphasizedInterpolator
import com.barapp.viewModels.MainActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis

class PantallaBusqueda : Fragment() {
  // Binding
  private lateinit var binding: FragmentPantallaBusquedaBinding
  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    sharedElementEnterTransition =
      MaterialContainerTransform()
        .setDuration(
          resources.getInteger(R.integer.fragment_transition_enter_long_duration).toLong()
        )
        .setInterpolator(emphasizedInterpolator())
    sharedElementReturnTransition =
      MaterialContainerTransform()
        .setDuration(
          resources.getInteger(R.integer.fragment_transition_exit_long_duration).toLong()
        )
        .setInterpolator(emphasizedInterpolator())
    exitTransition =
      MaterialSharedAxis(MaterialSharedAxis.X, true).setInterpolator(emphasizedInterpolator())
    reenterTransition =
      MaterialSharedAxis(MaterialSharedAxis.X, false).setInterpolator(emphasizedInterpolator())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentPantallaBusquedaBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.textViewBuscar.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent?
      ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        guardarBusqueda(v.text)
        realizarBusqueda(v.text.toString())
        return@setOnEditorActionListener true
      }
      false
    }

    mainActivityViewModel.usuario.observe(viewLifecycleOwner) { usuario: Usuario ->
      val layoutBusquedasRecientes = binding.layoutBusquedasRecientes

      if (usuario.detalleUsuario!!.busquedasRecientes.isEmpty()) {
        binding.botonEliminarHistorial.visibility = View.INVISIBLE
        binding.textViewBusquedasRecientesVacia.visibility = View.VISIBLE
      } else {
        binding.botonEliminarHistorial.visibility = View.VISIBLE
        binding.textViewBusquedasRecientesVacia.visibility = View.INVISIBLE
      }

      for (busquedaReciente in usuario.detalleUsuario!!.busquedasRecientes) {

        agregarTextView(busquedaReciente, layoutBusquedasRecientes)
      }
    }
  }

  private fun realizarBusqueda(texto: String) {
    val bundle = Bundle()
    bundle.putString("textoBusqueda", texto)
    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaBusqueda_to_pantallaResultadosBusqueda, bundle)
  }

  private fun guardarBusqueda(texto: CharSequence) {
    if (!TextUtils.isEmpty(texto)) {
      mainActivityViewModel.guardarBusquedaReciente(texto.toString())
    }
  }

  private fun agregarTextView(busquedaReciente: String, layoutBusquedasRecientes: LinearLayout) {
    val textView = TextView(this.context)
    textView.text = busquedaReciente

    val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    params.setMargins(
      TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics).toInt()
    )

    textView.layoutParams = params
    textView.setOnClickListener() {
      guardarBusqueda(textView.text)
      realizarBusqueda(textView.text.toString())
    }

    binding.botonEliminarHistorial.setOnClickListener() {
      MaterialAlertDialogBuilder(this.requireContext())
        .setTitle(R.string.pantalla_busqueda_dialog_titulo)
        .setMessage(R.string.pantalla_busqueda_dialog_texto)
        .setNegativeButton(R.string.boton_cancelar) { _, _ -> }
        .setPositiveButton(R.string.boton_confirmar) { _, _ ->
          mainActivityViewModel.eliminarBusquedasRecientes()
          layoutBusquedasRecientes.removeAllViews()
          binding.botonEliminarHistorial.visibility = View.INVISIBLE
          binding.textViewBusquedasRecientesVacia.visibility = View.VISIBLE
        }
        .show()
    }

    val viewDivider = View(this.context)
    viewDivider.setBackgroundColor(Color.rgb(224, 224, 224))
    val dividerHeight = (resources.displayMetrics.density * 1).toInt() // 1dp to pixels
    viewDivider.layoutParams =
      RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight)

    layoutBusquedasRecientes.addView(textView)
    layoutBusquedasRecientes.addView(viewDivider)
  }
}
