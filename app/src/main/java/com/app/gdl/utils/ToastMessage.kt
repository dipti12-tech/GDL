package com.app.gdl.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.app.gdl.R

fun ToastMessage(context: Context, message: String) {
    val layout = LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
    layout.findViewById<TextView>(R.id.toast_text).text = message

    Toast(context.applicationContext).apply {
        view = layout
        duration = Toast.LENGTH_SHORT
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
        show()
    }
}


