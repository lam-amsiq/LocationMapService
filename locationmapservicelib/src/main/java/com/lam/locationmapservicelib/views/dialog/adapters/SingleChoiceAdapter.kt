package com.lam.locationmapservicelib.views.dialog.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.dialog_action_item.view.*
import com.lam.locationmapservicelib.R
import com.lam.locationmapservicelib.views.dialog.DialogActionItemModel
import com.lam.locationmapservicelib.views.dialog.views.DialogActionItem

class SingleChoiceAdapter(val context: Context, val isConfirmType: Boolean, val actions: Array<out Any>, var selected: Int? = null) : BaseAdapter() {
    override fun getView(position: Int, _convertView: View?, parent: ViewGroup?): View? {
        var convertView = _convertView
        val item = actions[position]

        if (convertView == null) {
            context.let {
                val view = DialogActionItem(it)
                val padding = it.resources.getDimensionPixelSize(R.dimen.padding_6)
                view.setPadding(view.paddingStart, padding, view.paddingEnd, padding)
                convertView = view
            }
        }
        (convertView as? DialogActionItem)?.let { listItemView ->

            if (item is DialogActionItemModel) {
                listItemView.setActionItem(item)
            } else if (item is String) {
                listItemView.buttonText?.text = item
            }

            if (count <= 1) {
                listItemView.setTextColor(R.color.brightest)
                listItemView.setBackground(R.drawable.shape_dialog_action_single_choice_background)
            } else {
                listItemView.setTextColorList(R.color.selector_dialog_action_multiple_choices_text)
                listItemView.setBackground(R.drawable.shape_dialog_action_multiple_single_choices_background)
            }

            val isSelected = position == selected
            listItemView.isSelected = isSelected
        }
        return convertView
    }

    override fun getItem(position: Int): Any {
        return actions[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return actions.size
    }
}