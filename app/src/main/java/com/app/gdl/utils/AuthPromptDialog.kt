package com.app.gdl.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.app.gdl.databinding.DialogAuthPromptBinding

class AuthPromptDialog(
    private val activity: Context,
    private val txtString: String,
    private val onRegisterClicked: () -> Unit,
    private val onSignInClicked: () -> Unit

) {
    fun show() {
        val binding = DialogAuthPromptBinding.inflate(LayoutInflater.from(activity))

        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        binding.tvTitle.text = txtString
        binding.btnRegister.setOnClickListener {
            onRegisterClicked()
            dialog.dismiss()
        }

        binding.btnSignIn.setOnClickListener {
            onSignInClicked()
            dialog.dismiss()
        }

        dialog.show()
    }
}
