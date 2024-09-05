package com.barapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.barapp.model.Restaurante
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleOnSubscribe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.roundToInt
import timber.log.Timber

class Maps {
  companion object {
    private const val distanciaMaximaAMostrar = 1000
    private const val factorRedondeo = 50

    /**
     * Chequea que se cuente con los permisos de ubicación
     * [Manifest.permission.ACCESS_FINE_LOCATION] y [Manifest.permission.ACCESS_COARSE_LOCATION]
     *
     * @return true si se cuenta con los permisos, false en caso contrario
     * @author Federico Quarin
     */
    fun tienePermisosLocalizacion(ctx: Context): Boolean {
      return (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED)
    }

    @JvmStatic
    fun calcularDistanciasABares(
      restaurantes: List<Restaurante>,
      ubicacionUsuario: Location,
      callback: (HashMap<String, Int?>) -> Unit,
    ): Disposable {
      return Single.create(
          SingleOnSubscribe<HashMap<String, Int?>> { emitter ->
            val distancias: HashMap<String, Int?> =
              calcularDistanciasABares(restaurantes, ubicacionUsuario)

            distancias.replaceAll { _, v: Int? -> adaptarDistancia(v) }

            emitter.onSuccess(distancias)
          }
        )
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(callback)
    }

    /**
     * Este metodo calcula la distancia desde el usuario a todos los bares de la lista
     * *restaurantes*.
     *
     * @param restaurantes lista con los restaurantes cuya distancia se debe calcular
     * @param ubicacionUsuario ubicacion del usuario a utilizar
     * @return [HashMap] con los pares (id del restaurante, distancia al restaurante)
     * @author Federico Quarin
     */
    @JvmStatic
    fun calcularDistanciasABares(
      restaurantes: List<Restaurante>,
      ubicacionUsuario: Location,
    ): HashMap<String, Int?> {
      val distancias: HashMap<String, Int?> = LinkedHashMap()
      for (r in restaurantes) {
        val ubicacionRestaurante = Location("")
        ubicacionRestaurante.latitude = r.ubicacion.latitud
        ubicacionRestaurante.longitude = r.ubicacion.longitud

        distancias[r.id] = ubicacionUsuario.distanceTo(ubicacionRestaurante).roundToInt()
      }

      return distancias
    }

    /**
     * Esta funcion adapta una distancia a un valor apropiado para se mostrado en la interfaz de
     * usuario. Dicho valor es:
     * - *factorRedondeo*, si la distancia es menor a dicho valor
     * - null, si la distancia es mayor a *distanciaMaximaAMostrar*, lo que quiere decir que no se
     *   deberia mostrarlo por pantalla
     * - el multiplo inferior de *factorRedondeo*
     *
     * @param distancia distancia a adaptar
     * @author Federico Quarin
     */
    @JvmStatic
    fun adaptarDistancia(distancia: Int?): Int? {
      distancia?.run {
        if (distancia <= factorRedondeo) return factorRedondeo
        if (distancia >= distanciaMaximaAMostrar) return null

        return distancia.div(factorRedondeo).times(factorRedondeo)
      } ?: return null
    }
  }
}
