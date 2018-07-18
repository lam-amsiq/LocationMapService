package lam.com.locationmapservice.lib.views.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.dialog_action_item.view.*
import kotlinx.android.synthetic.main.shared_dialog.view.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.utils.ImageLoader
import lam.com.locationmapservice.lib.views.dialog.adapters.MultipleChoiceAdapter
import lam.com.locationmapservice.lib.views.dialog.adapters.SingleChoiceAdapter
import lam.com.locationmapservice.lib.views.dialog.views.DialogActionItem
import org.androidannotations.annotations.EViewGroup
import java.util.ArrayList

@EViewGroup(R.layout.shared_dialog)
open class Dialog : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var adapter: BaseAdapter? = null
    private var maxLimit: Int? = null

    private fun setup(illustration: Any?, title: String?, textContent: String?, cancelAction: DialogActionItemModel?, actions: Array<out DialogActionItemModel>, dialog: MaterialDialog?): Dialog {
        setupIllustration(illustration)
        setupTitle(title)
        setupSubText(textContent)
        setupCancel(cancelAction, dialog)

        if (actions.isEmpty()) {
            actionListView?.visibility = View.GONE
            cancelActionView?.setTextColor(R.color.brightest)
            cancelActionView?.setBackground(R.drawable.shape_dialog_action_single_choice_background)
        } else {
            actionListView?.choiceMode = ListView.CHOICE_MODE_SINGLE

            // Set margins
            context?.resources?.getDimensionPixelOffset(R.dimen.padding_48)?.let { marginSides ->
                actionListView?.setPadding(marginSides, 0, marginSides, 0)

                context?.resources?.getDimensionPixelOffset(R.dimen.padding_12)?.let { marginTopBottom ->
                    val cancelLayoutParams = actionContainer?.layoutParams as? MarginLayoutParams
                    cancelLayoutParams?.setMargins(marginSides, marginTopBottom, marginSides, marginTopBottom)
                    cancelLayoutParams?.let { newLayoutParams ->
                        actionContainer?.layoutParams = newLayoutParams
                    }
                }
            }

            adapter = SingleChoiceAdapter(context, actions.size > 2, actions)
            actionListView?.setOnItemClickListener { _, view, _, _ ->
                dialog?.dismiss()
                val dialogItemView = view as? DialogActionItem
                dialogItemView?.action?.run()
            }

            actionListView?.adapter = adapter

            activateSeperatorIfNeeded()
        }

        return this
    }

    private fun setupSelectList(illustration: Any?, title: String?, textContent: String?, cancelAction: DialogActionItemModel?, items: ArrayList<String>, dialog: MaterialDialog?, listCallback: SingleChoiceDialogResult? = null, selected: Int?): Dialog {
        setupIllustration(illustration)
        setupTitle(title)
        setupSubText(textContent)

        if (items.isEmpty()) {
            throw CustomDialogException("List choice dialog items can't be empty")
        } else {
            actionListView?.choiceMode = ListView.CHOICE_MODE_SINGLE

            // Set margins
            context?.resources?.getDimensionPixelOffset(R.dimen.padding_48)?.let { marginSides ->
                actionListView?.setPadding(marginSides, 0, marginSides, 0)

                context?.resources?.getDimensionPixelOffset(R.dimen.padding_12)?.let { marginTopBottom ->
                    val cancelLayoutParams = actionContainer?.layoutParams as? MarginLayoutParams
                    cancelLayoutParams?.setMargins(marginSides, marginTopBottom, marginSides, marginTopBottom)
                    cancelLayoutParams?.let { newLayoutParams ->
                        actionContainer?.layoutParams = newLayoutParams
                    }
                }
            }

            adapter = SingleChoiceAdapter(context, items.size > 2, items.toArray(), selected)
            actionListView?.setOnItemClickListener { _, view, _, _ ->
                val dialogItemView = view as? DialogActionItem
                dialog?.dismiss()
                dialogItemView?.action?.run()
            }

            actionListView?.adapter = adapter

            activateSeperatorIfNeeded()

            // Add item click listener
            actionListView?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                dialog?.dismiss()
                listCallback?.onResult(items[position])
            }
        }
        setupCancel(cancelAction, dialog)

        scrollToFirstSelected(selected)

        return this
    }

    private fun activateSeperatorIfNeeded() {
        actionListView?.let {
            it.post {
                val lastVisibleIndex = actionListView?.lastVisiblePosition ?: 0
                val lasIndexInList = adapter?.count?.minus(1) ?: 0
                if (lastVisibleIndex < lasIndexInList && lasIndexInList != 0) {
                    listTopDivider?.visibility = View.VISIBLE
                    listBottomDivider?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun scrollToFirstSelected(index: Int? = null) {
        index?.let { index_ ->
            actionListView?.post {
                actionListView?.postDelayed({
                    actionListView?.setSelection(index_)
                }, 100)
            }
        }
    }

    private fun setupMultiple(illustration: Any?, title: String?, textContent: String?, cancelAction: DialogActionItemModel?, items: ArrayList<String>, dialog: MaterialDialog?, listCallback: MultipleChoiceDialogResult? = null, maxLimit: Int? = null, selections: ArrayList<Int>?): Dialog {
        this.maxLimit = maxLimit

        setupIllustration(illustration)

        // Set Text
        setupTitle(title)
        setupSubText(textContent)

        if (items.isEmpty()) {
            throw CustomDialogException("Multiple choice dialog items can't be empty")
        } else {
            // Setup ok view next to cancel
            actionListView?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            adapter = MultipleChoiceAdapter(context, items, (selections ?: ArrayList()), maxLimit)

            // Set limit text if exists
            computeMaxLimitState()?.let {
                setupSubText(it)
            }

            scrollToFirstSelected(selections?.toList()?.sortedDescending()?.reversed()?.firstOrNull())

            //Set sizes and margins
            actionContainer?.weightSum = 2F
            context?.resources?.getDimensionPixelOffset(R.dimen.padding_12)?.let { margin ->
                if (subTextView?.visibility == View.GONE) {
                    (titleView?.layoutParams as? MarginLayoutParams)?.bottomMargin = margin
                } else {
                    (subTextView?.layoutParams as? MarginLayoutParams)?.bottomMargin = margin
                }
            }

            context?.resources?.getDimensionPixelOffset(R.dimen.padding_24)?.let { margin ->

                // Update action container padding
                actionContainer?.let { it.setPadding(it.paddingStart, margin, it.paddingEnd, it.paddingBottom) }

                // Update cancel action padding
                val cancelActionViewParams = cancelActionView?.layoutParams as? LinearLayout.LayoutParams
                cancelActionViewParams?.marginStart = margin
                cancelActionViewParams?.marginEnd = margin / 2
                cancelActionView?.layoutParams = cancelActionViewParams

                // Update ok action padding
                val okActionViewParams = okActionView?.layoutParams as? LinearLayout.LayoutParams
                okActionViewParams?.weight = 1F
                okActionViewParams?.marginStart = margin / 2
                okActionViewParams?.marginEnd = margin
                okActionView?.layoutParams = okActionViewParams
            }

            listTopDivider?.visibility = View.VISIBLE
            listBottomDivider?.visibility = View.VISIBLE
            actionListView?.divider = ImageLoader.getDrawable(context, R.drawable.shape_dialog_action_multiple_choices_divider)

            // Add item click listener
            actionListView?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectMultipleChoiceItem(position)
            }

            // Setup ok button
            okActionView?.buttonText?.text = context.getString(R.string.shared_action_ok)
            okActionView?.setTextColor(R.color.custom_dialog_multiple_single_choice_pressed_text)
            okActionView?.setOnClickListener {
                dialog?.dismiss()
                listCallback?.onResult((adapter as? MultipleChoiceAdapter)?.selectedItems)
            }

            // Add cancel listener
            cancelActionView?.setOnClickListener {
                dialog?.dismiss()
                (adapter as? MultipleChoiceAdapter)?.let { adapter -> listCallback?.onResult(adapter.selectedItems) }
            }

            // Set adapter
            actionListView?.adapter = adapter
        }

        setupCancel(cancelAction, dialog, true)

        return this
    }

    private fun selectMultipleChoiceItem(position: Int) {
        (adapter as? MultipleChoiceAdapter)?.let { adapter ->

            adapter.selectItem(position)

            computeMaxLimitState()?.let {
                setupSubText(it)
            }
        }
    }

    private fun setupIllustration(illustration: Any?) {
        if (illustration != null) {
            if (illustration is Drawable? || illustration is Int?) {
                ImageLoader.setIcon(context, illustrationView, illustration)
                illustrationView?.visibility = View.VISIBLE
            }
        } else {
            illustrationView?.visibility = View.GONE
        }
    }

    private fun setupTitle(title: String?) {
        this.titleView?.text = title
    }

    private fun setupSubText(textContent: String?) {
        if (textContent.isNullOrEmpty()) {
            subTextView?.visibility = View.GONE
        } else {
            subTextView?.visibility = View.VISIBLE
            subTextView?.text = textContent
        }
    }

    private fun computeMaxLimitState(): String? {
        return this.maxLimit?.let {
            val selectedCount = (adapter as? MultipleChoiceAdapter)?.selectedItems?.size ?: 0
            "$selectedCount/$it"
        }
    }

    private fun setupCancel(cancelAction: DialogActionItemModel?, dialog: MaterialDialog?, isMultiple: Boolean = false) {
        cancelAction?.let {
            cancelActionView?.setActionItem(cancelAction)
        } ?: cancelActionView?.setActionItem(DialogActionItemModel(context.getString(R.string.shared_action_cancel), null))

        if (!isMultiple) {
            context?.resources?.getDimensionPixelOffset(R.dimen.padding_48)?.let { marginSides ->
                (actionContainer?.layoutParams as? MarginLayoutParams)?.let { cancelParams ->
                    cancelParams.marginStart = marginSides
                    cancelParams.marginEnd = marginSides
                }
            }
        }

        cancelActionView?.setTextColorList(R.color.selector_dialog_action_cancel_item_text)
        cancelActionView?.setTypeface(Typeface.DEFAULT_BOLD)
        cancelActionView?.setOnClickListener {
            dialog?.cancel()
        }
    }

    class CustomDialogException(override var message: String) : Exception(message)

    companion object {
        private val TAG: String = "DialogV2"
        private var isShowing = false

        fun show(context: Context?, illustrationResId: Int?, title: String?, textContent: String?, cancelAction: DialogActionItemModel?, vararg actions: DialogActionItemModel, cancelable: Boolean = true): MaterialDialog? {
            (context as? Activity)?.let { activity ->
                val view = buildView(activity)
                val dialog = attachToDialog(activity, view, cancelAction?.action)

                view.setup(illustrationResId, title, textContent, cancelAction, actions, dialog)
                return show(dialog, cancelable)
            } ?: return null
        }

        fun showSelectListDialog(context: Context?, illustrationResId: Int?, title: String?, textContent: String?, cancelAction: DialogActionItemModel?, items: ArrayList<String>, listCallback: SingleChoiceDialogResult, selection: Int?, cancelable: Boolean = true): MaterialDialog? {
            (context as? Activity)?.let { activity ->
                val view = buildView(activity)
                val dialog = attachToDialog(activity, view, cancelAction?.action)

                view.setupSelectList(illustrationResId, title, textContent, cancelAction, items, dialog, listCallback, selection)
                return show(dialog, cancelable)
            } ?: return null
        }

        fun showMultipleChoiceDialog(context: Context?, illustrationResId: Int?, title: String?, textContent: String?, cancelAction: DialogActionItemModel?, items: ArrayList<String>, listCallback: MultipleChoiceDialogResult? = null, maxLimit: Int? = null, selections: ArrayList<Int>?, cancelable: Boolean = true): MaterialDialog? {
            (context as? Activity)?.let { activity ->
                val view = buildView(activity)

                val dialog = attachToDialog(activity, view, cancelAction?.action)

                view.setupMultiple(illustrationResId, title, textContent, cancelAction, items, dialog, listCallback, maxLimit, selections)
                return show(dialog, cancelable)
            } ?: return null
        }

        private fun buildView(context: Context?): Dialog {
            val view = Dialog_.build(context)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.gravity = Gravity.CENTER
            return view
        }

        private fun attachToDialog(context: Context, view: View, cancelAction: Runnable?): MaterialDialog {
            val dialog = MaterialDialog.Builder(context)
                    .customView(view, false)
                    .cancelListener {
                        cancelAction?.run()
                    }
                    .dismissListener {
                        Dialog.isShowing = false
                    }
                    .showListener {
                        Dialog.isShowing = true
                    }
                    .build()
            return dialog
        }

        private fun show(dialog: MaterialDialog?, cancelable: Boolean): MaterialDialog? {

            dialog?.setCancelable(cancelable)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setDimAmount(0.8f)

            Handler(Looper.getMainLooper()).post {
                dialog?.show()
            }

            return dialog
        }

        // Shared methods
        fun showSimpleDialog(context: Context?, title: String, content: String?, buttonTxt: String, illustrationResId: Int? = null) {
            show(context, illustrationResId,
                    title,
                    content,
                    DialogActionItemModel(buttonTxt, null)
            )
        }

        fun showSimpleDialog(context: Context?, title: Int, content: Int?, buttonTxt: Int, illustrationResId: Int? = null) {
            context?.let { contextInner ->
                show(context, illustrationResId,
                        contextInner.resources?.getString(title),
                        content?.let { contextInner.resources?.getString(it) },
                        DialogActionItemModel(contextInner.resources?.getString(buttonTxt), null)
                )
            }
        }

        fun showFinishingAppDialog(context: Context?) {
            context?.let {
                show(it, null,
                        it.getString(R.string.shared_app_name),
                        it.getString(R.string.shared_dialog_close_app_title),
                        DialogActionItemModel(it.getString(R.string.shared_action_cancel), null),
                        DialogActionItemModel(it.getString(R.string.shared_dialog_close_app_action_accept), Runnable {
                            (context as? Activity)?.finish()
                        })
                )
            }
        }

        fun showDialogNoInternet(context: Context?) {
            context?.let { contextInner ->
                Dialog.show(contextInner, null, contextInner.resources?.getString(R.string.location_dialog_no_internet_title), contextInner.resources?.getString(R.string.location_dialog_no_internet_content),
                        DialogActionItemModel(contextInner.resources?.getString(R.string.location_dialog_no_internet_action), null)
                )
            }
        }
    }
}