package com.lam.locationmapservicelib.views.dialog.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.dialog_action_item.view.*
import com.lam.locationmapservicelib.R
import com.lam.locationmapservicelib.views.dialog.views.DialogActionItemView
import java.util.ArrayList

class MultipleChoiceAdapter (val context: Context, val items: ArrayList<String>, selections: ArrayList<Int> = ArrayList(), private val maxLimit: Int? = null) : BaseAdapter() {
    var selectedItems: ArrayList<Int> = ArrayList()
    private var limitReached = false

    init {
        this.selectedItems = selections
        this.limitReached = selections.size == maxLimit
    }

    override fun getView(position: Int, _convertView: View?, parent: ViewGroup?): View? {
        var convertView = _convertView
        val itemIndex = items[position]

        context.let { convertView = DialogActionItemView(context) }

        (convertView as? DialogActionItemView)?.let { listItemView ->
            listItemView.buttonText?.text = itemIndex

            // Set colors
            listItemView.setBackground(R.drawable.shape_dialog_action_multiple_choices_background)
            listItemView.setTextColorList(R.color.selector_dialog_action_multiple_choice_item_text)

            val isSelected = selectedItems.contains(position)
            listItemView.isSelected = isSelected
            if (isSelected) {
                listItemView.setTypeface(Typeface.DEFAULT_BOLD)
            } else {
                if (limitReached) {
                    listItemView.setBackground(R.drawable.shape_dialog_action_multiple_choices_background_disabled)
                } else {
                    listItemView.setBackground(R.drawable.shape_dialog_action_multiple_choices_background)
                }
            }
        }
        return convertView
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return items.size
    }

    fun selectItem(position: Int) {
        this.limitReached = selectedItems.size == maxLimit

        if (this.selectedItems.contains(position)) {
            this.selectedItems.indexOf(position).let { this.selectedItems.removeAt(it) }
        } else {
            if (!limitReached) {
                this.selectedItems.add(position)
            }
        }

        this.limitReached = selectedItems.size == maxLimit
        notifyDataSetChanged()
    }
}