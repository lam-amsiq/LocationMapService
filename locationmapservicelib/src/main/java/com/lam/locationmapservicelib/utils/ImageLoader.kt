package com.lam.locationmapservicelib.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object ImageLoader {
    fun getDrawable(context: Context?, resID: Int?): Drawable? {
        return if (context == null || resID == null) return null else AppCompatResources.getDrawable(context, resID)
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