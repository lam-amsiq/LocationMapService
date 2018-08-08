package com.lam.locationmapservicelib.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

object Converter {
    fun dpToPx(dp: Float, context: Context): Int {
        return Math.round(context.resources.displayMetrics.density * dp)
    }

    fun convertToBitmap(drawable: Drawable, height: Int? = null, width: Int? = null): Bitmap {
        val mutableBitmap = Bitmap.createBitmap(height?.let { it } ?: kotlin.run { drawable.intrinsicHeight }, width?.let { it } ?: kotlin.run { drawable.intrinsicWidth }, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(mutableBitmap)
        drawable.setBounds(0, 0, drawable.intrinsicHeight, drawable.intrinsicHeight)
        drawable.draw(canvas)

        return mutableBitmap
    }
}