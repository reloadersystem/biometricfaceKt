package com.reloader.biometricface.ui

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.projectoxford.face.contract.Face
import com.microsoft.projectoxford.face.contract.GroupResult
import com.reloader.biometricface.R
import com.reloader.biometricface.helper.ImageHelper
import com.reloader.biometricface.helper.LogHelper
import com.reloader.biometricface.helper.SampleApp
import kotlinx.android.synthetic.main.activity_grouping.*
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GroupingActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: ProgressDialog
    var mBitmap: Bitmap? = null

    lateinit var mFaceListAdapter: FaceListAdapter


    inner class GroupingTask : AsyncTask<UUID, String, GroupResult>() {
        override fun doInBackground(vararg params: UUID): GroupResult? {
            val faceServiceClient = SampleApp.getFaceServiceClient()
            addLog("Request: Grouping" + params.size + "faces(s)")

            try {
                publishProgress("Agrupando....")

                return faceServiceClient.group(params)
            } catch (e: Exception) {
                addLog(e.message)
                publishProgress(e.message)
                return null
            }
        }

        override fun onPreExecute() {
            mProgressDialog.show()
        }

        override fun onProgressUpdate(vararg values: String) {
            setUiDuringBackgroundTask(values[0])
        }

        override fun onPostExecute(result: GroupResult?) {
            if (result != null) {
                addLog("Response: Exitoso. Grouped into " + result.groups.size + " face group(s).")
            }
            setUiAfterGrouping(result)
        }
    }

    inner class DetectionTask : AsyncTask<InputStream, String, Array<Face>>() {

        private var mSucceed = true

        override fun doInBackground(vararg params: InputStream?): Array<Face>? {

            val faceServiceClient = SampleApp.getFaceServiceClient()
            try {

                return faceServiceClient.detect(
                    params[0],
                    true,
                    false,
                    null
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
        }

        override fun onProgressUpdate(vararg values: String?) {
            setUiDuringBackgroundTask(values[0])
        }

        override fun onPostExecute(result: Array<Face>?) {
            if (result != null) {

                addLog("Response: Succes. Grouped into " + result.size + "face group(s)")
            }

            setUiAfterDetection(result)
        }


    }


    fun setUiDuringBackgroundTask(progress: String?) {
        mProgressDialog.setMessage(progress)
        setInfo(progress)

    }

    private fun setUiAfterDetection(result: Array<Face>?) {
        mProgressDialog.dismiss()

        setAllButtonsEnabledStatus(true)

        if (result != null) {
            setInfo("Detection is done")

            mFaceListAdapter.addFaces(result)
        }

    }

    private fun setUiAfterGrouping(result: GroupResult?) {

        mProgressDialog.dismiss()
        setAllButtonsEnabledStatus(true)

    }

    private fun setAllButtonsEnabledStatus(isEnabled: Boolean) {

        group.isEnabled = isEnabled

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grouping)


    }

    fun addLog(log: String?) {
        LogHelper.addGroupingLog(log)
    }



    private fun setInfo(informacion: String?) {

        info.text = informacion

    }

    inner class FaceListAdapter : BaseAdapter() {

        var faces: MutableList<Face>
        var faceThumbnails: MutableList<Bitmap>
        var faceIdThumbnailMap: MutableMap<UUID, Bitmap>


        init {
            faces = ArrayList()
            faceThumbnails = ArrayList()
            faceIdThumbnailMap = HashMap()
        }

        fun addFaces(detectionResult: Array<Face>?) {

            if (detectionResult != null) {

                val detectedFaces = Arrays.asList(*detectionResult)
                for (face in detectedFaces) {
                    faces.add(face)
                    try {

                        val faceThumbnail = ImageHelper.generateFaceThumbnail(
                            mBitmap, face.faceRectangle
                        )
                        faceThumbnails.add(faceThumbnail)
                        faceIdThumbnailMap[face.faceId] = faceThumbnail

                    } catch (e: IOException) {

                        info.text = e.message
                    }
                }
            }


        }

        override fun getCount(): Int {
            return faces.size
        }

        override fun getItem(position: Int): Any {
            return faces[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = layoutInflater.inflate(R.layout.item_face, parent, false)
            }
            convertView!!.id = position

            // Show the face thumbnail.
            (convertView.findViewById(R.id.image_face) as ImageView).setImageBitmap(faceThumbnails[position])

            return convertView
        }


    }
}
