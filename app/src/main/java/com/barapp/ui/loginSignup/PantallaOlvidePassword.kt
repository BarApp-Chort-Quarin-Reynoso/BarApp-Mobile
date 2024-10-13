package com.barapp.ui.loginSignup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.barapp.R
import com.barapp.databinding.FragmentPantallaOlvidePasswordBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class PantallaOlvidePassword : Fragment() {
    private lateinit var binding: FragmentPantallaOlvidePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPantallaOlvidePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.botonEnviarMail.setOnClickListener {
            Firebase.auth.sendPasswordResetEmail(binding.txtViewEmail.editText?.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("Se ha enviado el mail de reseteo de password")
                        Toast.makeText(context, "Se ha enviado un mail para resetear tu contraseña", Toast.LENGTH_LONG).show()
                    } else {
                        Timber.d("No se ha enviado el mail de reseteo de password")
                        Toast.makeText(context, "El mail no se encuentra registrado en el sistema", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

}