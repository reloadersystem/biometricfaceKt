package com.reloader.biometricface

import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
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
import com.bumptech.glide.Glide
import com.reloader.biometricface.domain.HelperWs
import com.reloader.biometricface.domain.MethodWs
import com.reloader.biometricface.helper.ShareDataRead
import com.reloader.biometricface.ui.DetectionActivity
import com.reloader.biometricface.ui.GroupingActivity
import com.reloader.biometricface.ui.VerificationMenuActivity
import kotlinx.android.synthetic.main.activity_face_verification.*
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

    private var mMyTask: AsyncTask<*, *, *>? = null

    private lateinit var resValues: MutableList<URL>

    lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Descargandow foto del servidor...")

        obtenerImagenes()

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


    private fun obtenerImagenes() {

        resValues = ArrayList()

        val methodWs = HelperWs.getConfiguration(applicationContext).create(MethodWs::class.java)
        val responseBodyCall = methodWs.getRecursos()
        responseBodyCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val informacion = response.body()
                    try {
                        val respuesta = informacion!!.string()
                        val resObject = JSONObject(respuesta)
                        val version = resObject.getInt("version_resources")
                        val fechamodif = resObject.getString("fechamodif")
                        val resArray = resObject.getJSONArray("listarecursos")

                        val sharpref = applicationContext?.getSharedPreferences(
                            "biometricpref",
                            Context.MODE_PRIVATE
                        )!!

                        if (sharpref.contains("version_resources")) {
                            val data =
                                ShareDataRead.obtenerValor(applicationContext, "version_resources")
                            Log.v("valorBiom", data)
                            val numversion = Integer.parseInt(data)

                            if (version > numversion) {

                                ShareDataRead.guardarValor(
                                    applicationContext,
                                    "version_resources",
                                    version.toString()
                                )
                                for (idx in 0 until resArray.length()) {
                                    resValues.add(URL(resArray.getJSONObject(idx).getString("url")))
                                }
                                iniciarDescarga(resValues as ArrayList<URL>)
                            }
                        } else {
                            ShareDataRead.guardarValor(
                                applicationContext,
                                "version_resources",
                                version.toString()
                            )

                            for (idx in 0 until resArray.length()) {
                                resValues.add(URL(resArray.getJSONObject(idx).getString("url")))
                            }

                            iniciarDescarga(resValues as ArrayList<URL>)

                            Log.v("photosrc", resArray.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("infoResponseFalse", t.message)
            }
        })
    }

    private fun iniciarDescarga(urlList: ArrayList<URL>) {

        mMyTask = DownloadTask().execute(urlList)

    }

    private inner class DownloadTask : AsyncTask<List<URL>, Int, List<Bitmap>>() {

        override fun onPreExecute() {
            mProgressDialog.show()
            mProgressDialog.setProgress(0)
        }


        override fun doInBackground(vararg urls: List<URL>): List<Bitmap> {
            val count = urls[0].size
            var connection: HttpURLConnection? = null
            val bitmaps = ArrayList<Bitmap>()

            for (i in 0 until count) {

                val currentURL = urls[0][i]

                try {
                    connection = currentURL.openConnection() as HttpURLConnection
                    connection.connect()
                    val inputStream = connection.inputStream
                    val bufferInputStream = BufferedInputStream(inputStream)
                    val bmp = BitmapFactory.decodeStream(bufferInputStream)
                    bitmaps.add(bmp)

                    publishProgress(((i + 1) / count.toFloat() * 100).toInt())

                    if (isCancelled()) {
                        break
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    connection!!.disconnect()
                }
            }
            return bitmaps
        }

        override fun onProgressUpdate(vararg progress: Int?) {
            super.onProgressUpdate(*progress)
            mProgressDialog.progress = progress[0]!!
        }

        override fun onPostExecute(result: List<Bitmap>?) {
            super.onPostExecute(result)
            mProgressDialog.dismiss()

            for (i in result!!.indices) {
                val bitmap = result[i]
                val archivo = resValues[i].toString()
                val parts =
                    archivo.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val part5 = parts[6] // 123

                val imageInternalUri = saveImageToInternalStorage(bitmap, i, part5)

            }
            handlerMessage(applicationContext, "Completado gracias, proceda marcar...")
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap, index: Int, nombre: String): Uri {

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, nombre)
        try {
            var stream: OutputStream? = null
            stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val saveImageUri = Uri.parse(file.absolutePath)
        return saveImageUri
    }


    private fun handlerMessage(context: Context, mensaje: String) {

        val handler = Handler(context.mainLooper)
        handler.post {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

        }
    }
}


