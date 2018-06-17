package lam.com.locationmapservice.lib.utils

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import lam.com.locationmapservice.R

object ImageLoader {
    private val TAG = "Image Loader"

    fun getDrawable(context: Context?, resID: Int?): Drawable? {
        return if (context == null || resID == null) return null else AppCompatResources.getDrawable(context, resID)
    }

    fun addGradient(originalBitmap: Bitmap, context: Context): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)

        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        val paint = Paint()
        val shader = LinearGradient(0f, height.toFloat(), width.toFloat(), 0f, ContextCompat.getColor(context, R.color.rose_gradient_start), ContextCompat.getColor(context, R.color.rose_gradient_end), Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        return updatedBitmap
    }

    fun setIcon(context: Context?, view: ImageView?, icon: Any?) {
        if (context is Activity && context.isDestroyed) return

        context?.let { contextInner ->
            view?.let { v ->
                icon?.let { iconInner ->
                    if (iconInner is Drawable) {
                        v.setImageDrawable(iconInner)
                    } else {
                        Glide.with(contextInner)
                                .load(iconInner)
                                .apply(RequestOptions()
                                        .fitCenter())
                                .into(v)
                    }
                }
            }
        }
    }
}