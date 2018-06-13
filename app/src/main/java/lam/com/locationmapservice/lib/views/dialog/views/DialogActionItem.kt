package lam.com.locationmapservice.lib.views.dialog.views

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_action_item.view.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.utils.ImageLoader
import lam.com.locationmapservice.lib.utils.ViewManager
import lam.com.locationmapservice.lib.views.dialog.DialogActionItemModel
import org.androidannotations.annotations.EViewGroup

@EViewGroup(R.layout.dialog_action_item)
open class DialogActionItem : FrameLayout {

    var action: Runnable? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setActionItem(item: DialogActionItemModel) {
        this.action = item.action
        this.buttonText?.text = item.text
    }

    fun setTextColor(textColorRes: Int) {
        ViewManager.setTextColor(this.buttonText, textColorRes, context)
    }

    fun setTextColorList(textColor: Int) {
        this.buttonText?.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.resources.getColorStateList(textColor, context.theme)
                } else {
                    ContextCompat.getColorStateList(context, textColor)
                })
    }

    fun setTypeface(typeface: Typeface) {
        this.buttonText?.typeface = typeface
    }

    fun setBackground(backgroundResource: Int) {
        this.container?.background = ImageLoader.getDrawable(context, backgroundResource)
    }

    override fun setSelected(selected: Boolean) {
        this.container?.isSelected = selected
    }
}