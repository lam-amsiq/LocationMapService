package lam.com.locationmapservice.demo.fragments

import com.trello.rxlifecycle2.components.support.RxFragment
import org.androidannotations.annotations.EFragment

@EFragment
abstract class DemoFragment : RxFragment() {
    open val TAG = "demo_fragment"
}