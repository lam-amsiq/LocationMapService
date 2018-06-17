package lam.com.locationmapservice.lib.activities

import com.afollestad.materialdialogs.MaterialDialog
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import org.androidannotations.annotations.EActivity

@EActivity
abstract class LMSActivity: RxAppCompatActivity() {
    var currentDialog: MaterialDialog? = null
}