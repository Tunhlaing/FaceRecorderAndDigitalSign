package com.example.facerecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facerecorder.ui.theme.FaceFrameOverlay
import com.example.facerecorder.ui.theme.SecondActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var faceFrameOverlay: FaceFrameOverlay
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        faceFrameOverlay = findViewById(R.id.faceFrameOverlay)
        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startCamera()
        }

        captureButton.setOnClickListener {
            captureAndSaveImage()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
            }

            // Set up the image analysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            // Select the camera
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Bind the use cases to the lifecycle
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Detect faces
            detectFace(inputImage){
                imageProxy.close()
            }

        } else {
            imageProxy.close() // Close if mediaImage is null
        }
    }

    private fun detectFace(image: InputImage, onComplete: () -> Unit) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces: List<Face> ->
                val imageWidth = image.width
                val imageHeight = image.height

                if (faces.isNotEmpty()) {
                    for (face in faces) {
                        val faceBounds = face.boundingBox

                        // Scale face bounds to match the overlay's size
                        val scaledFaceBounds = scaleFaceBoundsToOverlay(faceBounds, imageWidth, imageHeight, faceFrameOverlay)

                        // Check if the scaled face is within the frame
                        if (faceFrameOverlay.isFaceWithinFrame(scaledFaceBounds)) {
                            Log.d("TAGzzzze", "Face detected.")
                            faceFrameOverlay.frameColor = Color.GREEN
                            captureButton.isEnabled = true

                        } else {
                            Log.d("TAGzzzz", "Face is outside the frame.")
                            faceFrameOverlay.frameColor = Color.YELLOW
                            captureButton.isEnabled = false

                        }
                    }
                } else {
                    faceFrameOverlay.frameColor = Color.GRAY // No face detected
                    captureButton.isEnabled = false
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("TAGzzzz", "Face detection failed: ${e.message}", e)
                onComplete()
            }
    }


    // Ensure face bounds are scaled properly
    private fun scaleFaceBoundsToOverlay(faceBounds: Rect, imageWidth: Int, imageHeight: Int, overlay: FaceFrameOverlay): RectF {
        val scaleX = overlay.width.toFloat() / imageWidth.toFloat()   // Scale factor for width
        val scaleY = overlay.height.toFloat() / imageHeight.toFloat() // Scale factor for height

        return RectF(
            faceBounds.left * scaleX,
            faceBounds.top * scaleY,
            faceBounds.right * scaleX,
            faceBounds.bottom * scaleY
        )
    }

    // Method to capture the preview and save the image
    private fun captureAndSaveImage() {
        previewView.bitmap?.let { bitmap ->
            navigateToSecondActivity(bitmap)
            //saveImageToFile(bitmap)
        } ?: run {
            Log.e("MainActivity", "Failed to capture preview bitmap")
        }
    }

    private fun saveImageToFile(bitmap: Bitmap) : String? {
        try {
            // Create a mutable copy of the bitmap
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            // Create a Canvas to draw on the bitmap
            val canvas = Canvas(mutableBitmap)

            // Prepare paint for drawing text
            val paint = Paint().apply {
                color = Color.BLUE // Set text color
                textSize = 32f // Adjust text size as needed
                isAntiAlias = true
                setShadowLayer(5f, 10f, 10f, Color.WHITE) // Add shadow for better visibility
            }

            // Get the current date and time
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )

            // Draw the date and time on the bitmap (you can adjust the coordinates as needed)
            canvas.drawText(currentDate, 50f, mutableBitmap.height - 100f, paint)

            // Save the bitmap with the drawn date and time
            val filename = "detected_face_${System.currentTimeMillis()}.jpg"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)

            // Open file output stream
            val outputStream = FileOutputStream(file)
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            Log.d("MainActivityzz", "Image saved: ${file.absolutePath}")

            return file.absolutePath // Return the file path

        } catch (e: IOException) {
            Log.e("MainActivityzz", "Failed to save image", e)
            return null
        }
    }

    private fun navigateToSecondActivity(bitmap: Bitmap) {
        val filePath = saveImageToFile(bitmap)
        if (filePath != null) {
            val intent = Intent(this, SecondActivity::class.java).apply {
                putExtra("image_file_path", filePath)
            }
            startActivity(intent)
        }
    }
}
