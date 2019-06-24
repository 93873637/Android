package com.liz.multicamera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        val cameraId = cameraManager.cameraIdList[0]
//        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
//            override fun onOpened(device: CameraDevice) {
//                // Do something with `device`
//            }
//            override fun onDisconnected(device: CameraDevice) {
//                device.close()
//            }
//            override fun onError(device: CameraDevice, error: Int) {
//                onDisconnected(device)
//            }
//        }, null)
        getNextCameraId(cameraManager)
    }

    fun filterCameraIdsFacing(cameraIds: Array<String>, cameraManager: CameraManager,
                              facing: Int): List<String> {
        return cameraIds.filter {
            val characteristics = cameraManager.getCameraCharacteristics(it)
            characteristics.get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    fun getNextCameraId(cameraManager: CameraManager, currCameraId: String? = null): String? {
        // Get all front, back and external cameras in 3 separate lists
        val cameraIds = cameraManager.cameraIdList
        val backCameras = filterCameraIdsFacing(
            cameraIds, cameraManager, CameraMetadata.LENS_FACING_BACK)
        val frontCameras = filterCameraIdsFacing(
            cameraIds, cameraManager, CameraMetadata.LENS_FACING_FRONT)
        val externalCameras = filterCameraIdsFacing(
            cameraIds, cameraManager, CameraMetadata.LENS_FACING_EXTERNAL)

        // The recommended order of iteration is: all external, first back, first front
        val allCameras = (externalCameras + listOf(
            backCameras.firstOrNull(), frontCameras.firstOrNull())).filterNotNull()

        // Get the index of the currently selected camera in the list
        val cameraIndex = allCameras.indexOf(currCameraId)

        // The selected camera may not be on the list, for example it could be an
        // external camera that has been removed by the user
        return if (cameraIndex == -1) {
            // Return the first camera from the list
            allCameras.getOrNull(0)
        } else {
            // Return the next camera from the list, wrap around if necessary
            allCameras.getOrNull((cameraIndex + 1) % allCameras.size)
        }
    }
}
