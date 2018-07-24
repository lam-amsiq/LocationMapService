package com.lam.locationmapservicelib.utils

import android.util.Log
import com.lam.locationmapservicelib.BuildConfig

object LMSLog {
    private const val TAG_DEBUG = "DEBUG: "
    private const val TAG_MODULE = "LMS"

    fun i(tag: String = TAG_MODULE, message: String) {
        try {
            if (BuildConfig.DEBUG) {
                Log.i(TAG_DEBUG + tag, message)
            }
        } catch (e: IllegalStateException) {
        }
    }

    fun e(tag: String = TAG_MODULE, message: String) {
        try {
            if (BuildConfig.DEBUG) {
                Log.e(TAG_DEBUG + tag, message)
            }
        } catch (e: IllegalStateException) {
        }
    }

    fun d(tag: String = TAG_MODULE, message: String) {
        try {
            if (BuildConfig.DEBUG) {
                Log.d(TAG_DEBUG + tag, message)
            }
        } catch (e: IllegalStateException) {
        }
    }

    fun v(tag: String = TAG_MODULE, message: String) {
        try {
            if (BuildConfig.DEBUG) {
                Log.v(TAG_DEBUG + tag, message)
            }
        } catch (e: IllegalStateException) {
        }
    }

    fun w(tag: String = TAG_MODULE, message: String) {
        try {
            if (BuildConfig.DEBUG) {
                Log.w(TAG_DEBUG + tag, message)
            }
        } catch (e: IllegalStateException) {
        }
    }
}