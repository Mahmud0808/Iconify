package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.MlKit
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.nio.FloatBuffer

class BitmapSubjectSegmenter(context: Context) {

    private var mSegmenter: SubjectSegmenter? = null
    private val mContext: Context = context

    init {
        try {
            MlKit.initialize(context)
        } catch (ignored: Throwable) {
        }

        mSegmenter = try {
            SubjectSegmentation.getClient(
                SubjectSegmenterOptions
                    .Builder()
                    .enableForegroundConfidenceMask()
                    .build()
            )
        } catch (ignored: Throwable) {
            null
        }

        downloadModelIfNeeded()
    }

    private fun downloadModelIfNeeded() {
        if (mSegmenter == null) return

        val moduleInstallClient: ModuleInstallClient = ModuleInstall.getClient(mContext)

        moduleInstallClient
            .areModulesAvailable(mSegmenter!!)
            .addOnSuccessListener { response ->
                if (!response.areModulesAvailable()) {
                    moduleInstallClient
                        .installModules(
                            ModuleInstallRequest
                                .newBuilder()
                                .addApi(mSegmenter!!)
                                .build()
                        )
                }
            }
    }

    fun checkModelAvailability(resultListener: OnSuccessListener<ModuleAvailabilityResponse?>?) {
        if (mSegmenter == null) {
            resultListener?.onSuccess(ModuleAvailabilityResponse(false, 0))
            return
        }

        val moduleInstallClient: ModuleInstallClient = ModuleInstall.getClient(mContext)

        if (resultListener != null) {
            moduleInstallClient.areModulesAvailable(mSegmenter!!)
                .addOnSuccessListener(resultListener)
        }
    }

    fun segmentSubject(inputBitmap: Bitmap, listener: SegmentResultListener) {
        if (mSegmenter == null) return

        val transparentColor = Color.alpha(Color.TRANSPARENT)
        val resultBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true)

        listener.onStart()

        mSegmenter!!
            .process(InputImage.fromBitmap(inputBitmap, 0))
            .addOnSuccessListener { subjectSegmentationResult ->
                val mSubjectMask: FloatBuffer? = subjectSegmentationResult.foregroundConfidenceMask
                resultBitmap.setHasAlpha(true)

                for (y in 0 until inputBitmap.height) {
                    for (x in 0 until inputBitmap.width) {
                        if (mSubjectMask != null) {
                            if (mSubjectMask.get() < .5f) {
                                resultBitmap.setPixel(x, y, transparentColor)
                            }
                        }
                    }
                }

                inputBitmap.recycle()
                listener.onSuccess(resultBitmap)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "onFail:", e)

                inputBitmap.recycle()
                listener.onFail()
            }
    }

    interface SegmentResultListener {
        fun onStart()
        fun onSuccess(result: Bitmap?)
        fun onFail()
    }

    companion object {
        private val TAG = "Iconify - ${BitmapSubjectSegmenter::class.java.simpleName}: "
    }
}