package com.example.tsk_camera.sample

import android.Manifest
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import com.example.tsk_camera.sample.databinding.ActivityMainBinding
import com.kdang.library.common.BaseActivity


class MainActivity : BaseActivity<ActivityMainBinding>() {
    val cameraService:CameraService = CameraService(this)
    var surfaceHolder:SurfaceHolder? = null
    override fun getLayoutResourceId() = R.layout.activity_main

    override fun initPermission() {
        val permissionList = arrayOf(
            Manifest.permission.CAMERA,
        )
        checkPermissionList(permissionList)
    }

    override fun initView() {
        binding.surfaceCameraPreview.holder.addCallback(surfaceHolderCallback)
    }

    override fun initObserver() {}

    override fun initEventListener() {}

    private val surfaceHolderCallback = object :SurfaceHolder.Callback{
        override fun surfaceCreated(holder: SurfaceHolder) {
            surfaceHolder = holder
            surfaceHolder?.let{
                //4032x3024 or 1920x1080
                Log.e("tag", " surfaceCreated ")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    cameraService.openCamera(
                        width = 1920,
                        height = 1080,
                        lensPosition = 4/*BackCamera : 0*/,
                        surface =  it.surface)
                }
            }
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            Log.e("tag", " surfaceChanged")
            surfaceHolder = holder
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            surfaceHolder?.let{
                surfaceHolder = null
            }
        }

    }

}