package com.lam.locationmapservicelib.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView

object ViewManager {
    fun setTextColor(textView: TextView?, color: Int?, context: Context?) {
        context?.let { contextLet ->
            color?.let { color ->
                ContextCompat.getColor(contextLet, color)
            }
        }?.let { resultColor ->
            textView?.setTextColor(resultColor)
        }
    }

    fun setColorFilter(drawable: Drawable?, color: Int?, mode: PorterDuff.Mode?, context: Context?) {
        context?.let { contextLet ->
            color?.let { color ->
                ContextCompat.getColor(contextLet, color)
            }
        }?.let { colorResult ->
            drawable?.setColorFilter(colorResult, mode)
        }
    }

    fun setBackgroundColor(view: View?, color: Int?, context: Context?) {
        context?.let { contextLet ->
            color?.let { color ->
                ContextCompat.getColor(contextLet, color)
            }
        }?.let { resultColor ->
            view?.setBackgroundColor(resultColor)
        }
    }

    fun setColorSateList(context: Context?, drawableId: Int, colorList: Int): Drawable? {
        context?.let { innerContext ->
            val drawable = try {
                ImageLoader.getDrawable(context, drawableId)
            } catch (e: Resources.NotFoundException) {
                VectorDrawableCompat.create(innerContext.resources, drawableId, context.theme)
            }
            val colorStateList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                innerContext.resources.getColorStateList(colorList, innerContext.theme)
            } else {
                ContextCompat.getColorStateList(innerContext, colorList)
            }
            drawable?.let {
                val d = DrawableCompat.wrap(drawable)
                DrawableCompat.setTintList(d, colorStateList)
                return d
            }
        }
        return null
    }

    fun getForegroundColorSpan(color: Int?, context: Context?): ForegroundColorSpan? {
        return context?.let { contextLet ->
            color?.let { color ->
                ContextCompat.getColor(contextLet, color)
            }
        }?.let { resultColor ->
            ForegroundColorSpan(resultColor)
        }
    }

    fun getColor(color: Int?, context: Context?): Int? {
        return context?.let { contextLet ->
            color?.let { color ->
                ContextCompat.getColor(contextLet, color)
            }
        }
    }
}