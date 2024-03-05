package com.barapp.util

import android.content.Context
import android.content.DialogInterface.OnClickListener
import com.barapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Dialogs {
  companion object {
    fun rationalPermissionRequestDialog(
      ctx: Context,
      positiveListener: OnClickListener,
      negativeListener: OnClickListener,
    ) {
      MaterialAlertDialogBuilder(ctx)
        .setTitle(ctx.resources.getString(R.string.titulo_pedir_permisos_dialog))
        .setMessage(ctx.resources.getString(R.string.texto_pedir_permisos_dialog))
        .setNegativeButton(ctx.resources.getString(R.string.boton_no_permitir), negativeListener)
        .setPositiveButton(ctx.resources.getString(R.string.boton_permitir), positiveListener)
        .show()
    }
  }
}
