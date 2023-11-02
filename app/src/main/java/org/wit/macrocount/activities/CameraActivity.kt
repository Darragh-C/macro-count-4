package org.wit.macrocount.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.wit.macrocount.databinding.ActivityCameraBinding
import timber.log.Timber
import java.io.File


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private val REQUEST_CODE_CAMERA_PERMISSION = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //camera setup

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Timber.i("camera exception $exc")
            }
        }, ContextCompat.getMainExecutor(this))

        val cameraPermission = android.Manifest.permission.CAMERA
        val hasCameraPermission = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, cameraPermission)

        if (!hasCameraPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), REQUEST_CODE_CAMERA_PERMISSION)
        }

        binding.imageCaptureButton.setOnClickListener {
            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.cameraPreview.display.rotation)
                .build()

            val imageFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")


            val imageCaptureOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            imageCapture.takePicture(imageCaptureOptions, mainExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // TODO: save image
                }

                override fun onError(exc: ImageCaptureException) {
                    Timber.i("image capture error: $exc")
                }
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: camera function
            } else {
                Timber.i("camera permission denied")
                // TODO: camera permission denied function
            }
        }
    }
}