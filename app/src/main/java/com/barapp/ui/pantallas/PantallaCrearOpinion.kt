package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentPantallaCrearOpinionBinding

class PantallaCrearOpinion : Fragment() {

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

        binding.botonEnviarOpinion.isEnabled = false

        binding.botonEnviarOpinion.setOnClickListener {
            enviarOpinion()
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
    private fun enviarOpinion() {

    }

}