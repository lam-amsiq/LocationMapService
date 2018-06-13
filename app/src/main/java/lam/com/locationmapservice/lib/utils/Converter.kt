package lam.com.locationmapservice.lib.utils

import android.content.Context

object Converter {
    fun getDpInInteger(dp: Float, context: Context): Int {
        return Math.round(context.resources.displayMetrics.density * dp)
    }
}