package com.example.facerecorder.ui.theme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.facerecorder.R
import java.io.File
import java.io.FileOutputStream


class SecondActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var signatureView: SignatureView
    private lateinit var clearButton: Button
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        imageView = findViewById(R.id.imageView)
        signatureView = findViewById(R.id.signatureView)
        clearButton = findViewById(R.id.clearButton)
        saveButton = findViewById(R.id.saveButton)


        // Get the file path from the intent
        val filePath = intent.getStringExtra("image_file_path")
        if (filePath != null) {
            // Load the image from the file path
            val bitmap = BitmapFactory.decodeFile(filePath)
            imageView.setImageBitmap(bitmap)
        } else {
            Log.e("SecondActivity", "File path is null")
        }

        // Handle clear button click
        clearButton.setOnClickListener {
            signatureView.clear()
        }

        // Handle save button click
        saveButton.setOnClickListener {
            val signatureBitmap = signatureView.signatureBitmap

            if (signatureBitmap != null) {
                saveSignatureToFile(signatureBitmap)
            }
            signatureView.clear()
        }
    }

    private fun saveSignatureToFile(signatureBitmap: Bitmap) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signature_${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(file).use { outStream ->
                signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                outStream.flush()
            }
            // Load the image from the file path
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}