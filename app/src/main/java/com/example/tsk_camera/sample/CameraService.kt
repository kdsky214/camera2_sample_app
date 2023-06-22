package com.example.tsk_camera.sample


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors


class CameraService(private val context: Context)  {

    // 카메라 동작 스레드
    val cameraExecutor = Executors.newFixedThreadPool(2)

    //카메라 Parameters
    data class CameraParameters(
        var width:Int,
        var height:Int,
        var lensPosition:Int = 0,
        var previewSurface: Surface,
        var defaultFps: Range<Int>? = null,
    )

    lateinit var param:CameraParameters
    lateinit var cameraId:String
    var cameraDevice: CameraDevice? = null
    lateinit var previewBuilder: CaptureRequest.Builder
    lateinit var previewCaptureRequest: CaptureRequest

    lateinit var imageReaderBuilder: CaptureRequest.Builder
    lateinit var imageReaderCaptureRequest: CaptureRequest


    //raw data
    lateinit var rawBuilder: CaptureRequest.Builder
    lateinit var rawCaptureRequest: CaptureRequest


    //camera Capture Session
    var cameraCaptureSession: CameraCaptureSession? = null

    var captureResult:CaptureRequest? = null
    var totalCaptureResult:TotalCaptureResult? = null

    //ImageReader & DngCreator
    lateinit var imageReader:ImageReader
    lateinit var rawImageReader: ImageReader



    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraId)
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    fun openCamera(width:Int, height:Int, lensPosition:Int, surface: Surface){
        param = CameraParameters(
            width= width,
            height = height,
            lensPosition = lensPosition,
            previewSurface = surface
        )
        //카메라 렌즈 lensPosition 값에 따른 카메라 정보를 가져옴
        // 0 : 후면(Back) 1:전면(Front) ##기기마다 설정값이 다를 수 있음##
        cameraId = cameraManager.cameraIdList[param.lensPosition]

        characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.let{
            it.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
        }
//        characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)

        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let {
            val previewSize = it.getOutputSizes(SurfaceTexture::class.java) //
            // Raw 데이터 output 사이즈를 확인하는 부분
            val rawSize = it.getOutputSizes(ImageFormat.RAW_SENSOR)
//            for(size in rawSize){
//                Log.d("LOG_CAMERA"," rawSize : ${size}")
//            }
            Log.d("LOG_CAMERA"," ## Camera OPEN Success ##")
            cameraManager.openCamera(cameraId, context.mainExecutor, deviceStateCallback)
        }
    }

    fun createCaptureRequest(){

        cameraDevice?.let{ cameraDevice ->
            // Camera Preview Setting
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, param.defaultFps)
            previewBuilder.addTarget(param.previewSurface)
            previewCaptureRequest = previewBuilder.build()


//            imageReaderBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//            imageReaderBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, param.defaultFps)
//            imageReader = ImageReader.newInstance(param.width, param.height, ImageFormat.YUV_420_888,  1)
//            imageReaderBuilder.addTarget(imageReader.surface)
////            imageReader.setOnImageAvailableListener(imageReaderListener, null)
//            imageReaderCaptureRequest = imageReaderBuilder.build()

            /**
             *  Camera RawData Setting
             *   CaptureRequest.Builder 카메라데이터를 그리는 위치를 지정(Surface 지정)하여 ImageReader를 통해 Image를 가져온다
             **/
            rawBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            rawBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, param.defaultFps)
            rawImageReader = ImageReader.newInstance(param.width, param.height, ImageFormat.RAW_SENSOR,  5)

            rawBuilder.addTarget(rawImageReader.surface)
            rawImageReader.setOnImageAvailableListener(rawImageReaderListener, null)
            rawCaptureRequest = rawBuilder.build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cameraDevice.createCaptureSession(createSessionConfiguration())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createSessionConfiguration(): SessionConfiguration {
        val outputConfig = listOf(OutputConfiguration(param.previewSurface),/* OutputConfiguration(imageReader.surface),OutputConfiguration(rawImageReader.surface) */  ) //
        return SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputConfig, context.mainExecutor, cameraCaptureSessionStateCallback)
    }


    /**
    *   CameraDevice State Callback
    * */
    private val deviceStateCallback = object : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureRequest()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice = null
        }
    }

    /**
     *   CameraCaptureSession StateCallback
     * */
    private val cameraCaptureSessionStateCallback = object : CameraCaptureSession.StateCallback(){
        override fun onConfigured(session: CameraCaptureSession) {
            /*
            설정값이 완료되면 Session 생성이 되고 생성된 Session으로 동작방식을 설정
            setRepeatingBurstRequests  여러개의 CaptureRequest를 반복실행하는 설정
            cameraCaptureSession.capture()  1번만 캡처
//                    cameraCaptureSession?.setRepeatingBurst(listOf(previewCaptureRequest, imageReaderCaptureRequest) , captureSessionCallback, null)
//                    cameraCaptureSession?.capture(rawCaptureRequest, captureSessionCallback, null)
            */


            cameraCaptureSession = session
            cameraExecutor.execute {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        cameraCaptureSession?.apply {
                            this.setSingleRepeatingRequest(previewCaptureRequest, cameraExecutor, rawCaptureSessionCallback)
                        }
//                        cameraCaptureSession?.setRepeatingBurstRequests(
//                            listOf(previewCaptureRequest/*PreviewCaptureRequest*/,
//                                /*imageReaderCaptureRequest,*/
//                                 /* rawCaptureRequest RawCaptureRequest*/),
//                            cameraExecutor,
//                            rawCaptureSessionCallback  /*raw CaptureSession Callback*/
//                        )
                    }

                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            cameraCaptureSession = null
            cameraExecutor.shutdown()
        }

    }

    private val imageReaderListener = ImageReader.OnImageAvailableListener { imageReader->
        val image = imageReader.acquireLatestImage()

        if (image != null) {
            val planeList = image.planes
            for((index, plane) in planeList.withIndex()){
                plane.buffer
                plane.pixelStride
                plane.rowStride
//                Log.d("LOG_IMAGE"," =====   [$index]  ========")
//                Log.d("LOG_IMAGE","timeStamp : ${image.timestamp}")
//                Log.d("LOG_IMAGE","format : ${image.format.toString()}")
//                Log.d("LOG_IMAGE","(WxH) : ${image.width} x ${image.height}")
//                Log.d("LOG_IMAGE","pixelStride : ${plane.pixelStride}")
//                Log.d("LOG_IMAGE","rowStride : ${plane.rowStride}")
            }
        }
        image.close()
    }
    private val rawImageReaderListener = ImageReader.OnImageAvailableListener { imageReader->
        val image = imageReader.acquireLatestImage()

        if (image != null) {
            val planeList = image.planes
            Log.d("LOG_IMAGE"," =====   [RAW_IMAGE][${image.format}]  ========")
            /* 저장할 위치 및 파일이름*/
//            val file = File("")
//            totalCaptureResult?.let{ totalCaptureResult->
//                /* raw파일 저장 */
//                writeDngBytesAndClose(
//                    image = image,
//                    captureResult = totalCaptureResult,
//                    characteristics = characteristics,
//                    dngFile = file
//                )
//            }

        }
        image.close()
    }

    val rawCaptureSessionCallback = object :CameraCaptureSession.CaptureCallback(){
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            captureResult = request



        }
    }


    private fun writeDngBytesAndClose(
        image: Image,
        captureResult: TotalCaptureResult,
        characteristics: CameraCharacteristics,
        dngFile: File
    ) {
        try {
            DngCreator(characteristics, captureResult).use { dngCreator ->
                FileOutputStream(dngFile).use { outputStream ->
                    // TODO: Add DngCreator#setThumbnail and add the DNG to the normal
                    // filmstrip.
                    dngCreator.writeImage(outputStream, image)
                    outputStream.close()
                    image.close()
                }
            }
        } catch (e: IOException) {
            Log.e("LOG_DNG", "Could not store DNG file", e)
            return
        }
        Log.i("LOG_DNG", "Successfully stored DNG file: " + dngFile.getAbsolutePath())
    }
}