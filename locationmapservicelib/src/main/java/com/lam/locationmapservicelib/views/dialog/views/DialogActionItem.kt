package com.lam.locationmapservicelib.views.dialog.views

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.lam.locationmapservicelib.R
import kotlinx.android.synthetic.main.dialog_action_item.view.*
import com.lam.locationmapservicelib.utils.ImageLoader
import com.lam.locationmapservicelib.utils.ViewManager
import com.lam.locationmapservicelib.views.dialog.DialogActionItemModel

open class DialogActionItem : FrameLayout {
    var action: Runnable? = null

    constructor(context: Context) : super(context) {
        inflate()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        inflate()
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    private fun inflate() {
        val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        mInflater?.inflate(R.layout.dialog_action_item, this, true)
    }

    fun setActionItem(item: DialogActionItemModel) {
        this.action = item.action
        this.buttonText?.text = item.text
    }

    fun setText(text: String) {
        this.buttonText?.text = text
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