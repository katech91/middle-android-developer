package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting

class IconLinkSpan(
    private val linkDrawable: Drawable,
    @Px
    private val gap: Float,
    @ColorInt
    private val textColor: Int,
    dotWidth: Float = 6f
) : ReplacementSpan() {

    private var iconSize = 0
    private var textWidth = 0f
    private val dashs = DashPathEffect(floatArrayOf(dotWidth, dotWidth), 0f)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val textStart = x + iconSize + gap

        paint.forLine {
            path.reset()
            path.moveTo(textStart, bottom.toFloat())
            path.lineTo(textStart + textWidth, bottom.toFloat())
            canvas.drawPath(path, paint)
        }

        canvas.save()   //save canvas position

        val transY = (bottom - linkDrawable.bounds.bottom.toFloat())  //translate canvas to link position
        canvas.translate(x + gap/2f, transY)
        linkDrawable.draw(canvas)  //draw icon
        canvas.restore()  //restore position

        paint.forText {
            canvas.drawText(text, start, end, textStart, y.toFloat(), paint)
        }
    }


    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            iconSize = fm.descent - fm.ascent
        }
        if (iconSize != 0) {
            linkDrawable.setBounds(0, 0, iconSize, iconSize)    //set drawable bounds as line height
            textWidth = paint.measureText(text.toString(), start, end)
        }
        return (iconSize + gap + textWidth).toInt()
    }


    private inline fun Paint.forLine(block: () -> Unit) {
        val oldStyle = style
        val oldWidth = strokeWidth

        strokeWidth = 0f
        style = Paint.Style.STROKE
        pathEffect = dashs
        color = textColor

        block()

        //restore
        pathEffect = null
        strokeWidth = oldWidth
        style = oldStyle
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldColor = color
        color = textColor

        block()

        //restore
        color = oldColor
    }
}