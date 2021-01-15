package com.reloader.biometricface.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.reloader.biometricface.R
import kotlinx.android.synthetic.main.activity_verification_menu.*

class VerificationMenuActivity : AppCompatActivity() {

    private var MY_PERMISSIONS_REQUEST_CAMERA = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_menu)


        select_face_face_verification.setOnClickListener(clickListener)
        select_face_person_verification.setOnClickListener(clickListener)

        val verificarPermisoCamera =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (verificarPermisoCamera != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale((Manifest.permission.CAMERA))) {
                solicitarPermisoCamara()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
            }

        }

    }

    val clickListener: View.OnClickListener = View.OnClickListener { view ->

        when (view.id) {

            R.id.select_face_face_verification -> {

                val intent = Intent(this, FaceVerificationActivity::class.java)
                startActivity(intent)
            }

            R.id.select_face_person_verification -> {

                val intent = Intent(this, PersonVerificationActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun solicitarPermisoCamara() {
        AlertDialog.Builder(this)
            .setTitle("Autorización")
            .setMessage("Se necesita permiso para tomar Fotos por favor acepte")
            .setPositiveButton("Aceptar") { dialogInterface, i ->
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
                Log.v("permisionOk", "permiso aceptado")
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialogInterface, i -> }
            .show()
    }
}
