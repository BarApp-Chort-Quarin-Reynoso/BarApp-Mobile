package com.barapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.barapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Esta función muestra un [androidx.appcompat.app.AlertDialog] donde se le pide al usuario que vaya
 * a las configuraciones de la app a cambiar los permisos que se necesitan
 *
 * @param registry
 * @param context el [Context] donde se debe mostrar el Dialog
 * @param callback
 * @author Federico Quarin
 */
class CambiarPermisosEnConfiguracion(
  private val registry: ActivityResultRegistry,
  private val context: Context,
  private val callback: ActivityResultCallback<ActivityResult>,
) {
  private val launcher: ActivityResultLauncher<Intent> =
    registry.register("key", ActivityResultContracts.StartActivityForResult(), callback)

  fun showDialog() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri

    MaterialAlertDialogBuilder(context)
      .setTitle(context.resources.getString(R.string.titulo_pedir_permisos_dialog))
      .setMessage(context.resources.getString(R.string.texto_cambiar_permisos_dialog))
      .setPositiveButton(context.resources.getString(R.string.boton_ajustes)) { _, _ ->
        launcher.launch(intent)
      }
      .setNegativeButton(context.resources.getString(R.string.boton_ahora_no)) { _, _ -> }
      .show()
  }
}
