package com.reloader.biometricface.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.reloader.biometricface.R
import kotlinx.android.synthetic.main.activity_verification_menu.*

class VerificationMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_menu)


        select_face_face_verification.setOnClickListener(clickListener)
        select_face_person_verification.setOnClickListener(clickListener)

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
}
