package lam.com.locationmapservice.demo.interfaces

import lam.com.locationmapservice.demo.fragments.LMSFragment

interface IFragment {
    fun beginTransactionTo(fragment: LMSFragment?)
}