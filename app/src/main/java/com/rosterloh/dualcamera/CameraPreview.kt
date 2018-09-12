package com.rosterloh.dualcamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import timber.log.Timber

class CameraPreview : FrameLayout, SurfaceHolder.Callback {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val selectSpinner = Spinner(context)
    private val surfaceView = SurfaceView(context)
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            Timber.d("Camera $cameraId open")
            this@CameraPreview.cameraDevice = cameraDevice
        }

        override fun onDisconnected(camera: CameraDevice) {
            Timber.d("Camera $cameraId disconnected")
            cameraDevice?.close()
            this@CameraPreview.cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Timber.e("Camera $cameraId error $error")
            cameraDevice?.close()
            this@CameraPreview.cameraDevice = null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        keepScreenOn = true

        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        keepScreenOn = false

        cameraDevice?.close()
        cameraDevice = null

        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Timber.e(e)
        }
    }

    private fun addPreview() {
        surfaceView.holder.addCallback(this)
        this.addView(surfaceView)
    }


    private fun addCameraSelect() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            selectSpinner.adapter = ArrayAdapter<CharSequence>(context, R.layout.support_simple_spinner_dropdown_item, manager.cameraIdList)
            selectSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = parent?.getItemAtPosition(position) as String
                    Timber.d("Camera $selected selected")
                    cameraId = selected
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            this.addView(selectSpinner)
        } catch (e: CameraAccessException) {
            Timber.e(e)
        }
    }

    @SuppressWarnings("MissingPermission")
    fun connectCamera() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP) ?:
        throw RuntimeException("Cannot get available preview/video sizes")
        map.getOutputSizes(SurfaceTexture::class.java).forEach { Timber.d("$it") }
        manager.openCamera(cameraId, stateCallback, null)
        this@CameraPreview.removeView(selectSpinner)
        addPreview()
    }

    fun disconnectCamera() {
        cameraDevice?.close()
        cameraDevice = null

        this@CameraPreview.removeView(surfaceView)
        addCameraSelect()
    }

    fun startPreview() {
        if (cameraDevice == null) return

        try {
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surfaceView.holder.surface)

            cameraDevice?.createCaptureSession(listOf(surfaceView.holder.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            captureSession = session
                            captureSession?.setRepeatingRequest(previewRequestBuilder.build(),
                                    null, backgroundHandler)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Timber.e("Capture configuration failed")
                        }
                    }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Timber.e(e)
        }
    }

    fun stopPreview() {
        captureSession?.close()
        captureSession = null
    }
}