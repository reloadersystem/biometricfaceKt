package com.reloader.biometricface.ui

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.projectoxford.face.contract.Face
import com.microsoft.projectoxford.face.contract.VerifyResult
import com.reloader.biometricface.R
import com.reloader.biometricface.helper.ImageHelper
import com.reloader.biometricface.helper.LogHelper
import com.reloader.biometricface.helper.SampleApp
import com.reloader.biometricface.log.VerificationLogActivity
import kotlinx.android.synthetic.main.activity_face_verification.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*

class FaceVerificationActivity : AppCompatActivity() {

    lateinit var mProgressDialog: ProgressDialog

    private var mFaceId0: UUID? = null
    private var mFaceId1: UUID? = null


    private var mBitmap0: Bitmap? = null
    private var mBitmap1: Bitmap? = null

    private var bitmap: Bitmap? = null


    //todo servicio  que compara fotos
    private inner class VerificationTask constructor(
        private val mFaceId0: UUID,
        private val mFaceId1: UUID
    ) : AsyncTask<Void, String, VerifyResult>() {

        override fun doInBackground(vararg p0: Void?): VerifyResult? {

            val faceServiceClient = SampleApp.getFaceServiceClient()

            try {
                publishProgress("Verificando")

                return faceServiceClient.verify(
                    mFaceId0,
                    mFaceId1
                )
            } catch (e: Exception) {
                publishProgress(e.message)
                addLog(e.message)
                return null
            }
        }

        override fun onPreExecute() {
            mProgressDialog.show()
            addLog("Request: Verifying face $mFaceId0  and face $mFaceId1")
        }

        override fun onProgressUpdate(vararg progress: String?) {
            mProgressDialog.setMessage(progress[0])
            setInfo(progress[0])
        }

        override fun onPostExecute(result: VerifyResult?) {

            if (result != null) {
//                addLog(
//                    "Response: Success. Face $mFaceId0 and face $mFaceId1 + " + (if (result.isIdentical) " " else " don't ")
//                            + "belong to the same person"
//                )

                mProgressDialog.dismiss()

                val formatter = DecimalFormat("#0.00")

                val verificationResult =
                    ((if (result.isIdentical) "La misma persona" else "Personas diferentes")
                            + ". La Similitud es " + formatter.format(result.confidence)
                            )

                Log.v("Resultado", verificationResult)

                info.text = verificationResult

                setAllButtonEnabledStatus(true)
            }
            // setUiAfterVerification(result)
        }
    }

    //todo Verifica si es una foto  Usuario 1 / 2
    private inner class DetectionTask internal constructor(
        private val mIndex: Int
    ) : AsyncTask<InputStream, String, Array<Face>>() {

        private var mSucceed = true

        override fun doInBackground(vararg params: InputStream?): Array<Face>? {

            val faceServiceClien = SampleApp.getFaceServiceClient()
            try {
                publishProgress("Detectando...")

                return faceServiceClien.detect(
                    params[0],
                    true,
                    false, null
                )
            } catch (e: Exception) {
                mSucceed = false
                publishProgress(e.message)
                addLog(e.message)
                return null
            }

        }

        override fun onPreExecute() {
            mProgressDialog.show()
            addLog("Request: Detecting in image$mIndex")
        }

        override fun onProgressUpdate(vararg progress: String?) {
            mProgressDialog.setMessage(progress[0])
            setInfo(progress[0])
        }

        override fun onPostExecute(result: Array<Face>) {
            mProgressDialog.hide()
            setUiAfterDetection(result, mIndex, mSucceed)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_verification)

//        initializeFaceList(0)
//        initializeFaceList(1)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Espere por favor")

        verify.setOnClickListener(clickListener)
        select_image_0.setOnClickListener(clickListener)
        select_image_1.setOnClickListener(clickListener)
        view_log.setOnClickListener(clickListener)


        clearDetectFaces(0)
        clearDetectFaces(1)

        setVerifyButtonEnabledStatus(false)
        LogHelper.clearVerificationLog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val index: Int
        if (requestCode == REQUEST_SELECT_IMAGE_0) {
            index = 0
        } else if (requestCode == REQUEST_SELECT_IMAGE_1) {
            index = 1
        } else {
            return
        }

        if (resultCode == RESULT_OK) {

            bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                data?.data, contentResolver
            )

            if (bitmap != null) {
                setVerifyButtonEnabledStatus(false)
                clearDetectFaces(index)


                if (index == 0) {
                    mBitmap0 = bitmap
                    mFaceId0 = null
                } else {
                    mBitmap1 = bitmap
                    mFaceId1 = null
                }

                addLog("Image" + index + ": " + data?.data + "resize to " + bitmap!!.width + "x" + bitmap!!.height)

                detect(bitmap!!, index)
            }
        }
    }

    private fun clearDetectFaces(index: Int) {

        val imageView = findViewById(if (index == 0) R.id.image_0 else R.id.image_1) as ImageView
        imageView.setImageResource(android.R.color.transparent)

    }


    private fun setSelectImageButtonEnabledStatus(isEnabled: Boolean, index: Int) {

        val button: Button

        if (index == 0) {
            button = findViewById(R.id.select_image_0) as Button
        } else {
            button = findViewById(R.id.select_image_1) as Button
        }

        button.isEnabled = isEnabled

        view_log.isEnabled = isEnabled

    }

    private fun setVerifyButtonEnabledStatus(isEnabled: Boolean) {
        verify.isEnabled = isEnabled
    }

    private fun setAllButtonEnabledStatus(isEnabled: Boolean) {

        select_image_0.isEnabled = isEnabled
        select_image_1.isEnabled = isEnabled
        verify.isEnabled = isEnabled
        view_log.isEnabled = isEnabled

    }

    private fun setUiAfterDetection(result: Array<Face>?, index: Int, succeed: Boolean) {


        setSelectImageButtonEnabledStatus(true, index)

        if (succeed) {

            addLog(
                "Response: Success. Detected "
                        + result!!.size + " face(s) in image" + index
            )

            setInfo(result.size.toString() + " face" + (if (result.size != 1) "s" else "") + " detected")

            if (index == 0) {
                mFaceId0 = result[0].faceId
                image_0.setImageBitmap(bitmap)
            } else {
                mFaceId1 = result[0].faceId
                image_1.setImageBitmap(bitmap)
            }
        }

        if (result != null && result.size == 0) {
            setInfo("Rostro no detectado!")
        }

        if (index == 0 && mBitmap1 == null || index == 1 && mBitmap0 == null || index == 2) {
            mProgressDialog.dismiss()
        }

        if (mFaceId0 != null && mFaceId1 != null) {
            setVerifyButtonEnabledStatus(true)
        }


    }


    private fun detect(bitmap: Bitmap, index: Int) {

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        val inputStream = ByteArrayInputStream(output.toByteArray())

        DetectionTask(index).execute(inputStream)

        setSelectImageButtonEnabledStatus(false, index)

        setInfo("Detectando... ")

    }

    private fun setInfo(inf: String?) {
        info.text = inf
    }

    private fun addLog(log: String?) {
        LogHelper.addVerificationLog(log)
    }

    companion object {
        private val REQUEST_SELECT_IMAGE_0 = 0
        private val REQUEST_SELECT_IMAGE_1 = 1
    }


    private val clickListener: View.OnClickListener = View.OnClickListener { view ->

        when (view.id) {

            R.id.verify -> {
                setAllButtonEnabledStatus(false)
                VerificationTask(this!!.mFaceId0!!, this!!.mFaceId1!!).execute()
            }

            R.id.select_image_0 -> {
                selectImage(0)
            }

            R.id.select_image_1 -> {
                selectImage(1)
            }

            R.id.view_log -> {
                val intent = Intent(this, VerificationLogActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun selectImage(index: Int) {
        val intent = Intent(this, SelectImageActivity::class.java)
        startActivityForResult(
            intent,
            if (index == 0) REQUEST_SELECT_IMAGE_0 else REQUEST_SELECT_IMAGE_1
        )
    }
}
