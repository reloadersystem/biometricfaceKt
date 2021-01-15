package com.reloader.biometricface

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.reloader.biometricface.ui.DetectionActivity
import com.reloader.biometricface.ui.GroupingActivity
import com.reloader.biometricface.ui.VerificationMenuActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private var MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (getString(R.string.subscription_key).startsWith("Please")) {

            AlertDialog.Builder(this)
                .setTitle(R.string.add_subscription_key_tip_title)
                .setMessage(R.string.add_subscription_key_tip)
                .setCancelable(false)
                .show()

        }

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



        btn_detection.setOnClickListener(clickListener)
        btn_verificacion.setOnClickListener(clickListener)
        btn_agrupando.setOnClickListener(clickListener)
        btn_rostrosimilares.setOnClickListener(clickListener)
        btn_identificacion.setOnClickListener(clickListener)
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
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialogInterface, i -> }
            .show()
    }


    private val clickListener: View.OnClickListener = View.OnClickListener { view ->

        when (view.id) {

            R.id.btn_detection -> {

                val intent = Intent(this, DetectionActivity::class.java)
                startActivity(intent)
            }

            R.id.btn_verificacion -> {
                val intent = Intent(this, VerificationMenuActivity::class.java)
                startActivity(intent)
            }

            R.id.btn_agrupando -> {

                val intent = Intent(this, GroupingActivity::class.java)
                startActivity(intent)

            }

            R.id.btn_rostrosimilares -> {

            }

            R.id.btn_identificacion -> {

            }
        }
    }
}


