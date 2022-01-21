package co.ceryle.segmentedbutton.sample.drawable

import android.graphics.*
import co.ceryle.segmentedbutton.SegmentedButtonGroup.getPosition
import co.ceryle.segmentedbutton.SegmentedButtonGroup.setOnClickedButtonListener
import co.ceryle.segmentedbutton.SegmentedButtonGroup.setPosition
import co.ceryle.segmentedbutton.SegmentedButtonGroup.setEnabled
import co.ceryle.segmentedbutton.SegmentedButton.setDrawable
import android.graphics.drawable.Drawable
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import android.os.Bundle
import co.ceryle.segmentedbutton.sample.R
import co.ceryle.segmentedbutton.SegmentedButtonGroup.OnClickedButtonListener
import co.ceryle.segmentedbutton.sample.drawable.BadgeDrawable
import co.ceryle.segmentedbutton.SegmentedButton

class BadgeDrawable(color: Int, width: Int, height: Int, borderWidth: Int, borderRadius: Int) : Drawable() {
    private val paint: Paint
    private val color: Int
    private val width: Int
    private val height: Int
    private val borderWidth: Int
    private val borderRadius: Int
    private val rect: RectF
    private val path: Path
    var count = 10
    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    override fun onBoundsChange(bounds: Rect) {
        path.reset()
        path.addRect(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat(), Path.Direction.CW)
        rect[(bounds.left + borderWidth).toFloat(), (bounds.top + borderWidth).toFloat(), (
                bounds.right - borderWidth).toFloat()] = (bounds.bottom - borderWidth).toFloat()
        path.addRoundRect(rect, borderRadius.toFloat(), borderRadius.toFloat(), Path.Direction.CW)
    }

    override fun draw(canvas: Canvas) {
        paint.color = color
        canvas.drawPath(path, paint)
        val textBounds = Rect()
        val countString = count.toString()
        paint.getTextBounds(countString, 0, countString.length, textBounds)
        canvas.drawText(
                countString,
                rect.right - (rect.right - rect.left) / 2 - textBounds.width() / 2,
                rect.top + textBounds.height() / 2 + (rect.bottom - rect.top) / 2,
                paint
        )
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint.textSize = 32f
        path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        rect = RectF()
        this.color = color
        this.width = width
        this.height = height
        this.borderWidth = borderWidth
        this.borderRadius = borderRadius
    }
}