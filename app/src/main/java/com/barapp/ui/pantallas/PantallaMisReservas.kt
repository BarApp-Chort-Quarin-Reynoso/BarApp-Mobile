package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.barapp.R
import com.barapp.barapp.model.Reserva
import com.barapp.databinding.FragmentPantallaMisReservasBinding
import com.barapp.ui.MainActivity
import com.barapp.ui.recyclerViewAdapters.ReservasPasadasRecyclerAdapter
import com.barapp.ui.recyclerViewAdapters.ReservasPendientesRecyclerAdapter
import com.barapp.util.Interpolator.Companion.emphasizedInterpolator
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaMisReservasViewModel
import com.google.android.material.transition.MaterialFadeThrough

interface OnReservaClicked {
  fun onReservaClicked(transitionView: View)
  fun onOpinarButtonClicked(transitionView: View)
}

class PantallaMisReservas : Fragment(), ReservasPendientesRecyclerAdapter.OnItemClickListener, ReservasPasadasRecyclerAdapter.OnOpinarButtonClickListener {

  private lateinit var onReservaClicked: OnReservaClicked

  private lateinit var binding: FragmentPantallaMisReservasBinding
  private val viewModelMisReservas: PantallaMisReservasViewModel by viewModels()
  private val activityViewModel: MainActivityViewModel by activityViewModels()

  private val reservasPasadasAdapter = ReservasPasadasRecyclerAdapter(ArrayList(), this)
  private val reservasPendientesAdapter = ReservasPendientesRecyclerAdapter(ArrayList(), this)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaMisReservasBinding.inflate(inflater, container, false)

    enterTransition = MaterialFadeThrough().setInterpolator(emphasizedInterpolator())
    exitTransition = MaterialFadeThrough().setInterpolator(emphasizedInterpolator())

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onReservaClicked =
      (parentFragment as NavHostFragment).parentFragment as OnReservaClicked

    binding.listaVacia.textListaVacia.text = getString(R.string.lista_vacia_mis_reservas)

    // Se asigna el valor del idUsuario al viewModel y luego se buscan las reservas
    activityViewModel.usuario.observe(viewLifecycleOwner) {
      viewModelMisReservas.idUsuario = it.id
      viewModelMisReservas.buscarReservas()
    }

    // Se setea layoutManager de los recycler
    binding.recyclerViewReservasPendientes.layoutManager =
      LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    binding.recyclerViewReservasPasadas.layoutManager = LinearLayoutManager(view.context)

    binding.recyclerViewReservasPendientes.adapter = reservasPendientesAdapter
    binding.recyclerViewReservasPasadas.adapter = reservasPasadasAdapter

    // Se setea observer de reservas pendientes
    viewModelMisReservas.reservasPendientes.observe(viewLifecycleOwner) { reservas ->
      if (reservas.isEmpty()) {
        binding.labelPendientes.visibility = View.GONE
      } else {
        binding.labelPendientes.visibility = View.VISIBLE
      }
      reservasPendientesAdapter.setData(reservas)
    }

    // Se setea observer de reservas pasadas
    viewModelMisReservas.reservasPasadas.observe(viewLifecycleOwner) { reservas ->
      if (reservas.isEmpty()) {
        binding.labelPasadas.visibility = View.GONE
      } else {
        binding.labelPasadas.visibility = View.VISIBLE
      }
      reservasPasadasAdapter.setData(reservas)
    }

    // Se setea observer de reservas pendientes
    viewModelMisReservas.habilitarListaVacia.observe(viewLifecycleOwner) { banderaHabilitar ->
      if (banderaHabilitar) {
        binding.listaVacia.textListaVacia.visibility = View.VISIBLE
        binding.listaVacia.logoBarApp.visibility = View.VISIBLE
      } else {
        binding.listaVacia.textListaVacia.visibility = View.GONE
        binding.listaVacia.logoBarApp.visibility = View.GONE
      }
    }

    viewModelMisReservas.loading.observe(viewLifecycleOwner) { loading ->
      (activity as MainActivity).setLoading(loading)
      if (loading) {
        binding.labelPasadas.visibility = View.GONE
        binding.labelPendientes.visibility = View.GONE
        binding.recyclerViewReservasPendientes.visibility = View.GONE
        binding.recyclerViewReservasPasadas.visibility = View.GONE
      } else {
        binding.recyclerViewReservasPendientes.visibility = View.VISIBLE
        binding.recyclerViewReservasPasadas.visibility = View.VISIBLE
      }
    }
  }

  override fun onClick(transitionView: View, reserva: Reserva) {
    activityViewModel.setReservaSync(reserva)

    onReservaClicked.onReservaClicked(transitionView)
  }

  override fun onOpinarButtonClick(transitionView: View, reserva: Reserva) {
    activityViewModel.setReservaSync(reserva)
    onReservaClicked.onOpinarButtonClicked(transitionView)
  }
}
