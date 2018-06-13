package lam.com.locationmapservice.lib.interfaces

import lam.com.locationmapservice.lib.fragments.LMSFragment

interface IFragment {
    fun beginTransactionTo(fragment: LMSFragment?)
}