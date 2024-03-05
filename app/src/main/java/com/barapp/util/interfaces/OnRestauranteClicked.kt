package com.barapp.util.interfaces

import android.view.View
import com.barapp.model.Restaurante

interface OnRestauranteClicked {
  fun onRestauranteClicked(restaurante: Restaurante, transitionView: View, distancia: Int?)
}
