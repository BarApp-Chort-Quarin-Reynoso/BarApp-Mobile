package com.barapp.util.diffCallbacks

import androidx.recyclerview.widget.DiffUtil
import com.barapp.model.Restaurante

class RestauranteDiffCallback(
  private val oldRestauranteList: List<Restaurante>,
  private val newRestauranteList: List<Restaurante>,
) : DiffUtil.Callback() {

  override fun getOldListSize(): Int {
    return oldRestauranteList.size
  }

  override fun getNewListSize(): Int {
    return newRestauranteList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldRestauranteList[oldItemPosition].id == newRestauranteList[newItemPosition].id
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldRestauranteList[oldItemPosition] == newRestauranteList[newItemPosition]
  }
}
