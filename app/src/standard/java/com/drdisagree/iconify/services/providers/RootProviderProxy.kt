package com.drdisagree.iconify.services.providers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.utils.FileUtils
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_FG_FILE
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.BitmapSubjectSegmenter
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
import java.io.File
import java.io.FileOutputStream

class RootProviderProxy : BaseRootProviderProxy() {

    override fun createProxy(): BaseRootProviderProxyIPC {
        return RootProviderProxyIPC(this)
    }

    inner class RootProviderProxyIPC(context: Context) : BaseRootProviderProxyIPC(context) {

        override fun extractWallpaperSubject(
            input: Bitmap,
            callback: IExtractSubjectCallback,
            resultPath: String
        ) {
            val tag = BitmapSubjectSegmenter::class.java.simpleName

            try {
                val bitmapSubjectSegmenter = BitmapSubjectSegmenter(applicationContext)

                bitmapSubjectSegmenter.segmentSubject(
                    input,
                    object : BitmapSubjectSegmenter.SegmentResultListener {
                        override fun onStart() {
                            callback.onStart(getString(R.string.depth_wallpaper_subject_extraction_started))
                        }

                        override fun onSuccess(result: Bitmap?) {
                            try {
                                val fgFile = DEPTH_WALL_FG_FILE
                                val tempFile = File.createTempFile(
                                    fgFile.nameWithoutExtension,
                                    ".${fgFile.extension}"
                                )

                                val outputStream = FileOutputStream(tempFile)
                                result!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                                outputStream.close()
                                result.recycle()

                                val isSuccess = FileUtils.copyToIconifyDir(
                                    tempFile,
                                    File(resultPath).name
                                )

                                tempFile.delete()

                                callback.onResult(
                                    isSuccess,
                                    if (isSuccess) {
                                        getString(R.string.depth_wallpaper_subject_extraction_success)
                                    } else {
                                        getString(R.string.depth_wallpaper_subject_extraction_failed)
                                    }
                                )
                            } catch (throwable: Throwable) {
                                Log.e(TAG, "$tag - onSuccess: $throwable")

                                callback.onResult(
                                    false,
                                    getString(R.string.depth_wallpaper_subject_extraction_failed)
                                )
                            }
                        }

                        override fun onFail() {
                            bitmapSubjectSegmenter.checkModelAvailability { moduleAvailabilityResponse: ModuleAvailabilityResponse ->
                                callback.onResult(
                                    false,
                                    if (moduleAvailabilityResponse.areModulesAvailable()) {
                                        getString(R.string.depth_wallpaper_subject_extraction_failed)
                                    } else {
                                        getString(R.string.depth_wallpaper_missing_ai_model)
                                    }
                                )
                            }
                        }
                    }
                )
            } catch (throwable: Throwable) {
                Log.e(TAG, "$tag - segmentSubject: $throwable")

                callback.onResult(
                    false,
                    getString(R.string.depth_wallpaper_subject_extraction_failed)
                )
            }
        }
    }
}