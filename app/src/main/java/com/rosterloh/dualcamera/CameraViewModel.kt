package com.rosterloh.dualcamera

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class CameraViewModel : ViewModel() {

    val camera0Open: MutableLiveData<Boolean> = MutableLiveData()
    val camera0Preview: MutableLiveData<Boolean> = MutableLiveData()
    val camera1Open: MutableLiveData<Boolean> = MutableLiveData()
    val camera1Preview: MutableLiveData<Boolean> = MutableLiveData()

    init {
        camera0Open.value = false
        camera0Preview.value = false
        camera1Open.value = false
        camera1Preview.value = false
    }

    fun openCamera(id: Int) = when(id) {
        0 -> camera0Open.value = camera0Open.value?.not()
        1 -> camera1Open.value = camera1Open.value?.not()
        else -> Timber.e("Unknown camera id $id")
    }

    fun previewCamera(id: Int) = when(id) {
        0 -> camera0Preview.value = camera0Preview.value?.not()
        1 -> camera1Preview.value = camera1Preview.value?.not()
        else -> Timber.e("Unknown camera id $id")
    }
}