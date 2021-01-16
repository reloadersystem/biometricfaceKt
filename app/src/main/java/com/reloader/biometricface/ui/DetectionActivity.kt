package com.reloader.biometricface.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.projectoxford.face.FaceServiceClient
import com.microsoft.projectoxford.face.contract.*
import com.reloader.biometricface.R
import com.reloader.biometricface.helper.ImageHelper
import com.reloader.biometricface.helper.LogHelper
import com.reloader.biometricface.helper.SampleApp
import kotlinx.android.synthetic.main.activity_detection.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class DetectionActivity : AppCompatActivity() {


    private var mImageUri: Uri? = null
    private var mBitmap: Bitmap? = null
    lateinit var mProgressDialog: ProgressDialog

    companion object {
        val REQUEST_SELECT_IMAGE = 0
    }

    private inner class DetectionTask : AsyncTask<InputStream, String, Array<Face>>() {

        private var mSucceed = true

        override fun doInBackground(vararg params: InputStream?): Array<Face>? {

            val faceServiceClient = SampleApp.getFaceServiceClient()

            try {

                publishProgress("Detectando")
                return faceServiceClient?.detect(
                    params[0],
                    true,
                    true,
                    arrayOf(
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Smile,
                        FaceServiceClient.FaceAttributeType.Glasses,
                        FaceServiceClient.FaceAttributeType.FacialHair,
                        FaceServiceClient.FaceAttributeType.Emotion,
                        FaceServiceClient.FaceAttributeType.HeadPose,
                        FaceServiceClient.FaceAttributeType.Accessories,
                        FaceServiceClient.FaceAttributeType.Blur,
                        FaceServiceClient.FaceAttributeType.Exposure,
                        FaceServiceClient.FaceAttributeType.Hair,
                        FaceServiceClient.FaceAttributeType.Makeup,
                        FaceServiceClient.FaceAttributeType.Noise,
                        FaceServiceClient.FaceAttributeType.Occlusion
                    )
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
            addLog("Respuesta:  Detectando imagen " + mImageUri!!)
        }

        override fun onProgressUpdate(vararg progress: String?) {
            mProgressDialog.setMessage(progress[0])
            setInfo(progress[0])
        }

        override fun onPostExecute(result: Array<Face>?) {

            if (mSucceed) {

                mProgressDialog.hide()
                addLog(
                    "Respuesta: Deteccion Exitosa " + (result?.size
                        ?: 0) + "rostro(s) en " + mImageUri
                )
                val hair = result?.get(0)?.faceAttributes?.hair?.let { getHair(it) }
                val facialhair = result?.get(0)?.faceAttributes?.facialHair?.let {
                    getFacialHair(it)
                }
                val makeup = result?.get(0)?.faceAttributes?.makeup?.let { getMakeup(it) }


                val face_description = String.format(
                    "Age: %s  Gender: %s\nHair: %s  FacialHair: %s\nMakeup: %s  %s\nForeheadOccluded: %s  Blur: %s\nEyeOccluded: %s  %s\n" + "MouthOccluded: %s  Noise: %s\nGlassesType: %s\nHeadPose: %s\nAccessories: %s",
                    result?.get(0)?.faceAttributes?.age.toString(),
                    result?.get(0)?.faceAttributes?.gender.toString(),
                    hair.toString(),
                    facialhair,
                    makeup,
                    result?.get(0)?.faceAttributes?.emotion?.let { getEmotion(it) },
                    result?.get(0)?.faceAttributes?.occlusion?.foreheadOccluded,
                    result?.get(0)?.faceAttributes?.blur?.blurLevel,
                    result?.get(0)?.faceAttributes?.occlusion?.eyeOccluded,
                    result?.get(0)?.faceAttributes?.exposure?.exposureLevel,
                    result?.get(0)?.faceAttributes?.occlusion?.mouthOccluded,
                    result?.get(0)?.faceAttributes?.noise?.noiseLevel,
                    result?.get(0)?.faceAttributes?.glasses,
                    result?.get(0)?.faceAttributes?.headPose?.let { getHeadPose(it) },
                    result?.get(0)?.faceAttributes?.accessories?.let { getAccesories(it) }
                )
                txt_info.text = face_description
                setAllButtonsEnabledStatus(true)

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title))

        setDetectButtonEnableStatus(false)
        LogHelper.clearDetectionLog()

        select_image.setOnClickListener(clickListener)
        detect.setOnClickListener(clickListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("ImageUri", mImageUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mImageUri = savedInstanceState.getParcelable("ImageUri")
        if (mImageUri != null) {
            mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                mImageUri, contentResolver
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            REQUEST_SELECT_IMAGE ->

                if (resultCode == Activity.RESULT_OK) {
                    mImageUri = data?.data
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                        mImageUri, contentResolver
                    )

                    if (mBitmap != null) {
                        image.setImageBitmap(mBitmap)
                        addLog(
                            "Imagen: " + mImageUri + "resize to " + mBitmap?.width
                                    + "x" + mBitmap?.height
                        )
                    }
                    setDetectButtonEnableStatus(true)
                }
            else -> {
            }
        }
    }

    private fun setDetectButtonEnableStatus(isEnabled: Boolean) {
        detect.isEnabled = isEnabled
    }

    private fun setAllButtonsEnabledStatus(isEnabled: Boolean) {
        select_image.isEnabled = isEnabled
        detect.isEnabled = isEnabled
        view_log.isEnabled = isEnabled
    }


    private fun setInfo(info: String?) {
        txt_info.text = info
    }

    fun addLog(log: String?) {

        LogHelper.addDetectionLog(log)
    }


    val clickListener: View.OnClickListener = View.OnClickListener { view ->

        when (view.id) {

            R.id.select_image -> {
                val intent = Intent(this, SelectImageActivity::class.java)
                startActivityForResult(intent, REQUEST_SELECT_IMAGE)
            }

            R.id.detect -> {

                val output = ByteArrayOutputStream()
                mBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, output)
                val inputStream = ByteArrayInputStream(output.toByteArray())
                DetectionTask().execute(inputStream)
                setAllButtonsEnabledStatus(false)


            }


        }
    }


    fun getHair(hair: Hair): String {
        if (hair.hairColor.size == 0) {
            return if (hair.invisible)
                "Invisible" else
                "Bald"
        } else {

            var maxConfidenceIndex = 0
            var maxConfidence = 0.0

            for (i in hair.hairColor.indices) {

                if (hair.hairColor[i].confidence > maxConfidence) {
                    maxConfidence = hair.hairColor[i].confidence
                    maxConfidenceIndex = i
                }
            }

            return hair.hairColor[maxConfidenceIndex].color.toString()
        }
    }

    fun getMakeup(makeup: Makeup): String {
        return if (makeup.eyeMakeup || makeup.lipMakeup) "Yes" else "No"
    }

    fun getAccesories(accesories: Array<Accessory>): String {

        if (accesories.size == 0) {
            return "NoAccesories"
        } else {
            val accesoriesList = arrayOfNulls<String>(accesories.size)

            for (i in accesories.indices) {

                accesoriesList[i] = accesories[i].type.toString()
            }

            return TextUtils.join(",", accesoriesList)
        }

    }

    fun getFacialHair(facialHair: FacialHair): String {

        return if (facialHair.moustache + facialHair.beard + facialHair.sideburns > 0) "Yes" else "No"
    }

    fun getEmotion(emotion: Emotion): String {
        var emotionType = ""
        var emotionValue = 0.0

        if (emotion.anger > emotionValue) {
            emotionValue = emotion.anger
            emotionType = "Anger"
        }

        if (emotion.contempt > emotionValue) {
            emotionValue = emotion.anger
            emotionType = "Contempt"
        }

        if (emotion.disgust > emotionValue) {
            emotionValue = emotion.disgust
            emotionType = "Disgust"
        }

        if (emotion.fear > emotionValue) {
            emotionValue = emotion.fear
            emotionType = "Fear"
        }

        if (emotion.happiness > emotionValue) {
            emotionValue = emotion.happiness
            emotionType = "Happiness"
        }
        if (emotion.neutral > emotionValue) {
            emotionValue = emotion.neutral
            emotionType = "Neutral"
        }

        if (emotion.sadness > emotionValue) {
            emotionValue = emotion.sadness
            emotionType = "Sadness"
        }

        if (emotion.surprise > emotionValue) {
            emotionValue = emotion.surprise
            emotionType = "Surprise"
        }

        return String.format("%s:  %f", emotionType, emotionValue)

    }

    fun getHeadPose(headPose: HeadPose): String {
        return String.format(
            "Pitch: %s, Roll: %s, Yaw: %s",
            headPose.pitch,
            headPose.roll,
            headPose.yaw
        )

    }


}



