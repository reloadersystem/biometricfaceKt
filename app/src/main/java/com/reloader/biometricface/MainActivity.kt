package com.reloader.biometricface

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.reloader.biometricface.domain.HelperWs
import com.reloader.biometricface.domain.MethodWs
import com.reloader.biometricface.helper.ShareDataRead
import com.reloader.biometricface.ui.DetectionActivity
import com.reloader.biometricface.ui.GroupingActivity
import com.reloader.biometricface.ui.VerificationMenuActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {




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

        btn_detection.setOnClickListener(clickListener)
        btn_verificacion.setOnClickListener(clickListener)
        btn_agrupando.setOnClickListener(clickListener)
        btn_rostrosimilares.setOnClickListener(clickListener)
        btn_identificacion.setOnClickListener(clickListener)
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


