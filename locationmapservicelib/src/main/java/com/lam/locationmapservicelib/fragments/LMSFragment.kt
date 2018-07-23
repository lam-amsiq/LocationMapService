package com.lam.locationmapservicelib.fragments

import com.trello.rxlifecycle2.components.support.RxFragment
import org.androidannotations.annotations.EFragment

@EFragment
abstract class LMSFragment : RxFragment() {
    open val TAG = "lms_fragment"
}