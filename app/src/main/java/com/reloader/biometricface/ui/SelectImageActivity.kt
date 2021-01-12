package com.reloader.biometricface.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.reloader.biometricface.R
import java.io.File
import java.io.IOException

class SelectImageActivity : AppCompatActivity() {

    private var mUriPhotoTaken: Uri? = null

    companion object {
        private val REQUEST_TAKE_PHOTO = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        initTakePhoto()

    }

    private fun initTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            try {
                val file = File.createTempFile("IMG_", ".jpg", storageDir)
                mUriPhotoTaken = Uri.fromFile(file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken)
                startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            } catch (e: IOException) {
                setInfo(e.message!!)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("ImageUri", mUriPhotoTaken)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri")!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> if (resultCode == RESULT_OK) {
                val imageUri: Uri?
                if (data == null || data.data == null) {
                    imageUri = mUriPhotoTaken
                } else {
                    imageUri = data.data
                }
                val intent = Intent()
                intent.data = imageUri
                setResult(RESULT_OK, intent)
                finish()
            }
            else -> {
            }
        }
    }


    private fun setInfo(inf: String) {
        val textView = findViewById(R.id.info) as TextView
        textView.text = inf
    }


}
