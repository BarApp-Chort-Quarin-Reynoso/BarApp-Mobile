package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.barapp.R
import com.barapp.databinding.FragmentPantallaOpinionesBinding
import com.barapp.viewModels.MainActivityViewModel
import com.google.android.material.transition.MaterialSharedAxis
import com.barapp.ui.recyclerViewAdapters.OpinionesRecyclerAdapter

class PantallaOpiniones : Fragment(), OpinionesRecyclerAdapter.Callbacks {

    private lateinit var binding: FragmentPantallaOpinionesBinding

//    private val pantallaOpinionesViewModel: PantallaOpinionesViewModel by viewModels()

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var adapter: OpinionesRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPantallaOpinionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listaVacia.textListaVacia.text = getString(R.string.lista_vacia_opiniones)
        binding.listaVacia.textListaVacia.visibility = View.GONE
        binding.listaVacia.logoBarApp.visibility = View.GONE

        postponeEnterTransition()
        OneShotPreDrawListener.add(view, this::startPostponedEnterTransition)

        binding.toolbar.setNavigationOnClickListener { volverAtras() }

        val recyclerView = binding.recyclerViewOpiniones

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        adapter = OpinionesRecyclerAdapter(mutableListOf())

        recyclerView.adapter = adapter
        actualizarCantidadOpiniones(adapter!!.itemCount)
    }

    private fun volverAtras() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    override fun actualizarCantidadOpiniones(cantOpiniones: Int) {
        binding.txtViewCantidadOpiniones.text = cantOpiniones.toString()
        if (cantOpiniones == 0) {
            binding.listaVacia.textListaVacia.visibility = View.VISIBLE
            binding.listaVacia.logoBarApp.visibility = View.VISIBLE
        } else {
            binding.listaVacia.textListaVacia.visibility = View.GONE
            binding.listaVacia.logoBarApp.visibility = View.GONE
        }
    }
}