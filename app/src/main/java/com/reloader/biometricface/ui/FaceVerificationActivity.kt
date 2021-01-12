package com.reloader.biometricface.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*

class FaceVerificationActivity : AppCompatActivity() {

    lateinit var mProgressDialog: ProgressDialog

    private var mFaceId0: UUID? = null
    private var mFaceId1: UUID? = null

    private lateinit var mFaceListAdapter0: FaceListAdapter
    private lateinit var mFaceListAdapter1: FaceListAdapter

    private var mBitmap0: Bitmap? = null
    private var mBitmap1: Bitmap? = null

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
                addLog(
                    "Response: Success. Face $mFaceId0 and face $mFaceId1 + " + (if (result.isIdentical) " " else " don't ")
                            + "belong to the same person"
                )
            }
            setUiAfterVerification(result)
        }
    }

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
            setUiAfterDetection(result, mIndex, mSucceed)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_verification)

        initializeFaceList(0)
        initializeFaceList(1)

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

            val bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
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

                addLog("Image" + index + ": " + data?.data + "resize to " + bitmap.width + "x" + bitmap.height)

                detect(bitmap, index)
            }
        }
    }

    private fun clearDetectFaces(index: Int) {
        val faceList = findViewById(
            if (index == 0) R.id.list_faces_0 else R.id.list_faces_1
        ) as ListView

        faceList.visibility = View.GONE

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

    private fun initializeFaceList(index: Int) {

        val listview =
            findViewById<ListView>(if (index == 0) R.id.list_faces_0 else R.id.list_faces_1)

        listview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                val faceListAdapter = if (index == 0) mFaceListAdapter0 else mFaceListAdapter1

                if (faceListAdapter.faces[position].faceId != (if (index == 0) mFaceId0 else mFaceId1)) {

                    if (index == 0) {
                        mFaceId0 = faceListAdapter.faces[position].faceId
                    } else {
                        mFaceId1 = faceListAdapter.faces[position].faceId
                    }

                    val imageView =
                        if (index == 0) image_0 else image_1
                    imageView.setImageBitmap(faceListAdapter.faceThumbnails[position])
                    setInfo("")
                }
            }
    }

    private fun setUiAfterVerification(result: VerifyResult?) {

        mProgressDialog.dismiss()

        setAllButtonEnabledStatus(true)

        if (result != null) {

            val formatter = DecimalFormat("#0.00")

            val verificationResult =
                ((if (result.isIdentical) "La misma persona" else "Personas diferentes")
                        + ". La Similitud es " + formatter.format(result.confidence)
                        )

            setInfo(verificationResult)
        }
    }


    private fun setUiAfterDetection(result: Array<Face>?, index: Int, succeed: Boolean) {

        setSelectImageButtonEnabledStatus(true, index)

        if (succeed) {

            addLog(
                "Response: Success. Detected "
                        + result!!.size + " face(s) in image" + index
            )

            setInfo(result.size.toString() + " face" + (if (result.size != 1) "s" else "") + " detected")

            val faceListAdapter = FaceListAdapter(result, index)

            if (faceListAdapter.faces.size != 0) {

                if (index == 0) {
                    mFaceId0 = faceListAdapter.faces.get(0).faceId
                } else {
                    mFaceId1 = faceListAdapter.faces.get(0).faceId
                }
// photo take resultado  ||
                val imageView =
                    findViewById(if (index == 0) R.id.image_0 else R.id.image_1) as ImageView
                imageView.setImageBitmap(faceListAdapter.faceThumbnails[0])
            }

            val listView = findViewById(
                if (index == 0) R.id.list_faces_0 else R.id.list_faces_1
            ) as ListView
            listView.adapter = faceListAdapter
            listView.setVisibility(View.VISIBLE)

            if (index == 0) {
                mFaceListAdapter0 = faceListAdapter
                mBitmap0 = null
            } else {
                mFaceListAdapter1 = faceListAdapter
                mBitmap1 = null
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

        setInfo("Detectando...")

    }

    private fun setInfo(inf: String?) {
        info.text = inf
    }

    private fun addLog(log: String?) {
        LogHelper.addVerificationLog(log)
    }

    private inner class FaceListAdapter
    constructor(detectionResult: Array<Face>?, var mIndex: Int) : BaseAdapter() {

        var faces: List<Face>
        var faceThumbnails: MutableList<Bitmap>

        init {
            faces = ArrayList()
            faceThumbnails = ArrayList()

            if (detectionResult != null) {

                faces = Arrays.asList(*detectionResult)

                for (face in faces) {
                    try {
                        faceThumbnails.add(
                            ImageHelper.generateFaceThumbnail(
                                if (mIndex == 0) mBitmap0 else mBitmap1, face.faceRectangle
                            )
                        )
                    } catch (e: IOException) {
                        setInfo(e.message)
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

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var convertView = convertView

            if (convertView == null) {

                val layoutInflater =
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView =
                    layoutInflater.inflate(R.layout.item_face, parent, false)
            }

            convertView!!.id = position


            var thumbnailToShow = faceThumbnails[position]

            if (mIndex == 0 && faces[position].faceId == mFaceId0) {
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow)
            } else if (mIndex == 1 && faces[position].faceId == mFaceId1) {
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow)
            }

            // convertView.image_face.setImageBitmap(thumbnailToShow)

            return convertView
        }

    }

    companion object {
        private val REQUEST_SELECT_IMAGE_0 = 0
        private val REQUEST_SELECT_IMAGE_1 = 1
    }


    private val clickListener: View.OnClickListener = View.OnClickListener { view ->

        when (view.id) {

            R.id.verify -> {
                setAllButtonEnabledStatus(false)
                mFaceId0?.let { mFaceId1?.let { it1 -> VerificationTask(it, it1).execute() } }
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
