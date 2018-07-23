package com.lam.locationmapservicelib.utils.extensions

import android.graphics.drawable.Drawable
import android.net.Uri
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.io.File

fun Picasso.load(image: Any?): RequestCreator? {
    return when (image) {
        is Int -> this.load(image)
        is String -> this.load(image)
        is Uri -> this.load(image)
        is File -> this.load(image)
        else -> null
    }
}

fun RequestCreator.error(image: Any?): RequestCreator {
    when (image) {
        is Int -> this.error(image)
        is Drawable -> this.error(image)
    }
    return this
}

fun RequestCreator.placeholder(image: Any?): RequestCreator {
    when (image) {
        is Int -> this.placeholder(image)
        is Drawable -> this.placeholder(image)
    }
    return this
}

fun RequestCreator.fitCenter(): RequestCreator {
    this.fit()
    this.centerInside()
    return this
}

fun RequestCreator.resize(size: Int?): RequestCreator {
    size?.let { overrideSize ->
        this.resize(overrideSize, overrideSize)
    }
    return this
}