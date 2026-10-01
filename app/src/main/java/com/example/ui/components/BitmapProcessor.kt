package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint

object BitmapProcessor {

    fun processBitmap(
        source: Bitmap,
        rotationDegrees: Float,
        filterMode: String,
        contrastMultiplier: Float,
        brightnessOffset: Float
    ): Bitmap {
        // 1. Calculate color matrix
        val cm = ColorMatrix()
        when (filterMode) {
            "সাদাকালো" -> {
                cm.setSaturation(0f)
            }
            "ডকুমেন্ট/স্ক্যান" -> {
                // High contrast text enhancement
                val c = 1.35f * contrastMultiplier
                val b = 25f + brightnessOffset
                cm.set(
                    floatArrayOf(
                        c, 0f, 0f, 0f, b,
                        0f, c, 0f, 0f, b,
                        0f, 0f, c, 0f, b,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "উজ্জ্বল" -> {
                val b = 35f + brightnessOffset
                cm.set(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, b,
                        0f, 1.1f, 0f, 0f, b,
                        0f, 0f, 1.1f, 0f, b,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            else -> {
                if (contrastMultiplier != 1f || brightnessOffset != 0f) {
                    cm.set(
                        floatArrayOf(
                            contrastMultiplier, 0f, 0f, 0f, brightnessOffset,
                            0f, contrastMultiplier, 0f, 0f, brightnessOffset,
                            0f, 0f, contrastMultiplier, 0f, brightnessOffset,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                }
            }
        }

        // 2. Prepare rotation matrix
        val matrix = Matrix().apply {
            postRotate(rotationDegrees)
        }

        val rotatedBitmap = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )

        // 3. Apply color filter if modified
        if (filterMode != "স্বাভাবিক" || contrastMultiplier != 1f || brightnessOffset != 0f) {
            val result = Bitmap.createBitmap(
                rotatedBitmap.width,
                rotatedBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(result)
            val paint = Paint().apply {
                colorFilter = ColorMatrixColorFilter(cm)
            }
            canvas.drawBitmap(rotatedBitmap, 0f, 0f, paint)
            return result
        }

        return rotatedBitmap
    }
}
