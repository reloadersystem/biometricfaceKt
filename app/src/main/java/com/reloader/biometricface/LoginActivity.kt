package com.reloader.biometricface

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.reloader.biometricface.ui.DetectionActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val verificarPermisoWrite =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (verificarPermisoWrite != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                solicitarPermiso()
            } else
            {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_STORAGE)
            }
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun solicitarPermiso() {

        AlertDialog.Builder(this)
            .setTitle("Autorización")
            .setMessage("Necesito permiso para Almacenar Archivos")
            .setPositiveButton("Aceptar") { dialogInterface, i ->
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                )
                Log.v("permisionOk", "permiso aceptado")
                btnLogin.isEnabled
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialogInterface, i -> }
            .show()
    }
}
