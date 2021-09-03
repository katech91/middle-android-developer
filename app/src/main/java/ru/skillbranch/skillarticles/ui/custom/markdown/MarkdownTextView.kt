package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx


class MarkdownTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val isSizeDepend: Boolean = true
):  AppCompatTextView(context, attrs, defStyleAttr) {

//    override val spannableContent: Spannable
//        get() = text as Spannable


    private val color = context.attrValue(R.attr.colorOnBackground)
    private val focusRect = Rect()
    private val searchPadding = context.dpToIntPx(56)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var searchBgHelper = SearchBgHelper(context) { top, bottom ->
        focusRect.set(0, top - searchPadding, width, bottom + searchPadding)
        //show rect on view with animation
        requestRectangleOnScreen(focusRect, false)
    }

//    init {
//        setTextColor(color)
//        textSize = fontSize
//        movementMethod = LinkMovementMethod()
//    }


    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                searchBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }

    override fun setTextSize(size: Float) {
        Log.e("MarkdownTextView", "set text size = $size")
        if (isSizeDepend) setLineSpacing(context.dpToPx(if (size == 14f) 8 else 10), 1f)
        super.setTextSize(size)
    }
}