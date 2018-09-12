package com.rosterloh.dualcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rosterloh.dualcamera.databinding.ActivityCameraBinding
import com.rosterloh.dualcamera.utils.containsOnly
import com.rosterloh.dualcamera.utils.isPermissionGranted
import com.rosterloh.dualcamera.utils.requestPermission
import com.rosterloh.dualcamera.utils.shouldShowPermissionRationale
import kotlinx.android.synthetic.main.activity_camera.*
import timber.log.Timber

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)

        val navController = Navigation.findNavController(this, R.id.camera_nav_fragment)

        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            if (shouldShowPermissionRationale(Manifest.permission.CAMERA)) {
                notifyPermissionRequired()
            } else {
                Timber.d("Requesting permission")
                requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA)
            }
        } else {
            Timber.d("Loading camera fragment")
            navController.navigate(R.id.action_blankFragment_to_cameraFragment)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                findNavController(R.id.camera_nav_fragment).navigate(R.id.action_blankFragment_to_cameraFragment)
            } else {
                notifyPermissionRequired()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun notifyPermissionRequired() {
        Snackbar.make(fragment_container, "CAMERA permission required", Snackbar.LENGTH_INDEFINITE)
                .setAction("Request") {
                    requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA)
                }
                .show()
    }

    companion object {
        const val REQUEST_CAMERA = 0
    }
}
