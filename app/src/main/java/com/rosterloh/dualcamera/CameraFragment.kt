package com.rosterloh.dualcamera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.rosterloh.dualcamera.databinding.FragmentCameraBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class CameraFragment : Fragment() {

    private val cameraViewModel: CameraViewModel by viewModel()

    private lateinit var binding: FragmentCameraBinding

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) =
            texture.setDefaultBufferSize(320, 240)

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false).apply {
            setLifecycleOwner(this@CameraFragment)
            vm = this@CameraFragment.cameraViewModel
        }

        cameraViewModel.camera0Open.observe(this, Observer { open ->
            open?.let {
                Timber.d("Left Camera %s", if (open) "open" else "close")
                if (open) {
                    binding.leftPreview.connectCamera()
                } else {
                    binding.leftPreview.disconnectCamera()
                }
            }
        })

        cameraViewModel.camera0Preview.observe(this, Observer { preview ->
            preview?.let {
                Timber.d("Left Camera preview %s", if (preview) "on" else "off")
                if (preview) {
                    binding.leftPreview.startPreview()
                } else {
                    binding.leftPreview.stopPreview()
                }
            }
        })

        cameraViewModel.camera1Open.observe(this, Observer { open ->
            open?.let {
                Timber.d("Right Camera %s", if (open) "open" else "close")
                if (open) {
                    binding.rightPreview.connectCamera()
                } else {
                    binding.rightPreview.disconnectCamera()
                }
            }
        })

        cameraViewModel.camera1Preview.observe(this, Observer { preview ->
            preview?.let {
                Timber.d("Right Camera preview %s", if (preview) "on" else "off")
                if (preview) {
                    binding.rightPreview.startPreview()
                } else {
                    binding.rightPreview.stopPreview()
                }
            }
        })

        return binding.root
    }
}