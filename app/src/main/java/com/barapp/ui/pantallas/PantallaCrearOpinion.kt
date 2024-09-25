package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentPantallaCrearOpinionBinding
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaCrearOpinionViewModel

class PantallaCrearOpinion : Fragment() {

    private val activitySharedViewModel: MainActivityViewModel by activityViewModels()

    private val viewModel: PantallaCrearOpinionViewModel by viewModels{
        PantallaCrearOpinionViewModel.Factory(activitySharedViewModel.reserva)
    }

    private lateinit var binding: FragmentPantallaCrearOpinionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPantallaCrearOpinionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { volverAtras() }

        binding.labelNombreRestaurante.text = activitySharedViewModel.reserva.restaurante.nombre

        binding.botonEnviarOpinion.isEnabled = false

        binding.botonEnviarOpinion.setOnClickListener {
            enviarOpinionYVolverAtras()
        }

        val contenedorCalificadores = binding.contenedorCalificadores

        viewModel.detalleRestaurante.observe(viewLifecycleOwner) {
            cargarCaracteristicas(contenedorCalificadores)
        }

        binding.ratingBarPuntuacionGeneral.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            binding.botonEnviarOpinion.isEnabled = rating > 0
        }
    }

    private fun cargarCaracteristicas(contenedorCalificadores: ViewGroup) {
        val caractersiticas = viewModel.detalleRestaurante.value?.caracteristicas

        if (caractersiticas != null) {

            binding.textViewValoracionesOpcionales.visibility = View.VISIBLE

            for (caracteristica in caractersiticas) {
                val textView = TextView(context)
                textView.text = caracteristica.key
                contenedorCalificadores.addView(textView,0)

                val ratingBar = RatingBar(context)
                ratingBar.numStars = 5
                ratingBar.stepSize = 1f
                ratingBar.progressTintList = this.resources.getColorStateList(R.color.orange_primary)
                ratingBar.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                ratingBar.scaleX = 0.9f
                ratingBar.scaleY = 0.9f
                contenedorCalificadores.addView(ratingBar, 1)
            }
        }
    }

    /**
     * Vuelve a la [PantallaMisReservas] anterior.
     *
     * @author Julio Chort
     */
    private fun volverAtras() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    /**
     * Se navega a la [PantallaMisReservas] anterior con un mensaje de éxito.
     *
     * @author Julio Chort
     */
    private fun enviarOpinionYVolverAtras() {
        val caracteristicasValoradas = mutableMapOf<String, Float>()
        val contenedorCalificadores = binding.contenedorCalificadores

        for (i in 0 until contenedorCalificadores.childCount) {
            val view = contenedorCalificadores.getChildAt(i)
            if (view is RatingBar) {
                val textView = contenedorCalificadores.getChildAt(i - 1) as TextView
                caracteristicasValoradas[textView.text.toString()] = view.rating
            }
        }

        viewModel.crearYEnviarOpinion(
            binding.comentarioOpinion.text.toString(),
            binding.ratingBarPuntuacionGeneral.rating.toDouble(),
            activitySharedViewModel.usuario.value!!,
            caracteristicasValoradas.filterKeys { it != "Calificación general" }
        )
        Toast.makeText(context, getString(R.string.pantalla_crear_opinion_snackbar_texto), Toast.LENGTH_SHORT).show()
        volverAtras()
    }

}