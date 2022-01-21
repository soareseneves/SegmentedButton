/*
 * Copyright (C) 2016 ceryle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.ceryle.segmentedbutton

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import co.ceryle.segmentedbutton.SegmentedButton.DrawableGravity

class SegmentedButton : View {
    //private var context: Context? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private var mClipAmount = 0f
    private var clipLeftToRight = false
    private var mTextPaint: TextPaint? = null
    private var mStaticLayout: StaticLayout? = null
    private var mStaticLayoutOverlay: StaticLayout? = null
    private val mTextBounds = Rect()
    private var mRadius = 0
    private var mBorderSize = 0
    private var hasBorderLeft = false
    private var hasBorderRight = false

    // private RectF rectF = new RectF();
    private fun init(context: Context, attrs: AttributeSet?) {
        //this.context = context
        getAttributes(attrs)
        initText()
        initBitmap()
        mRectF = RectF()
        mPaint = Paint()
        mPaint!!.color = Color.BLACK
        mPaint!!.isAntiAlias = true
    }

    fun setSelectorColor(color: Int) {
        mPaint!!.color = color
    }

    fun setSelectorRadius(radius: Int) {
        mRadius = radius
    }

    fun setBorderSize(borderSize: Int) {
        mBorderSize = borderSize
    }

    fun hasBorderLeft(hasBorderLeft: Boolean) {
        this.hasBorderLeft = hasBorderLeft
    }

    fun hasBorderRight(hasBorderRight: Boolean) {
        this.hasBorderRight = hasBorderRight
    }

    private var mRectF: RectF? = null
    private var mPaint: Paint? = null
    private fun initText() {
        if (!hasText) return
        mTextPaint = TextPaint()
        mTextPaint!!.isAntiAlias = true
        mTextPaint!!.textSize = textSize
        mTextPaint!!.color = textColor
        if (hasTextTypefacePath) setTypeface(textTypefacePath) else if (null != textTypeface) {
            setTypeface(textTypeface)
        }

        // default to a single line of text
        val width = mTextPaint!!.measureText(text).toInt()
        mStaticLayout = StaticLayout(text, mTextPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
        mStaticLayoutOverlay = StaticLayout(text, mTextPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
    }

    private fun initBitmap() {
        if (hasDrawable) {
            mDrawable = ContextCompat.getDrawable(context, drawable)
        }
        if (hasDrawableTint) {
            mBitmapNormalColor = PorterDuffColorFilter(drawableTint, PorterDuff.Mode.SRC_IN)
        }
        if (hasDrawableTintOnSelection) mBitmapClipColor = PorterDuffColorFilter(drawableTintOnSelection, PorterDuff.Mode.SRC_IN)
    }

    private fun measureTextWidth(width: Int) {
        if (!hasText) return
        val bitmapWidth = if (hasDrawable && drawableGravity!!.isHorizontal) mDrawable!!.intrinsicWidth else 0
        val textWidth = width - (bitmapWidth + paddingLeft + paddingRight)
        if (textWidth < 0) return
        mStaticLayout = StaticLayout(text, mTextPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
        mStaticLayoutOverlay = StaticLayout(text, mTextPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthRequirement = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightRequirement = MeasureSpec.getSize(heightMeasureSpec)
        var width = 0
        val bitmapWidth = if (hasDrawable) mDrawable!!.intrinsicWidth else 0
        val textWidth = if (hasText) mStaticLayout!!.width else 0
        var height = paddingTop + paddingBottom
        val bitmapHeight = if (hasDrawable) mDrawable!!.intrinsicHeight else 0
        val textHeight = if (hasText) mStaticLayout!!.height else 0
        when (widthMode) {
            MeasureSpec.EXACTLY -> if (width < widthRequirement) {
                width = widthRequirement
                measureTextWidth(width)
            }
            MeasureSpec.AT_MOST -> {
                width = if (drawableGravity!!.isHorizontal) {
                    textWidth + bitmapWidth + drawablePadding
                } else {
                    Math.max(bitmapWidth, textWidth)
                }
                width += paddingLeft * 2 + paddingRight * 2
            }
            MeasureSpec.UNSPECIFIED -> width = textWidth + bitmapWidth
        }
        if (hasText) mTextPaint!!.getTextBounds(text, 0, text!!.length, mTextBounds)
        when (heightMode) {
            MeasureSpec.EXACTLY -> if (drawableGravity!!.isHorizontal) {
                height = heightRequirement
                val h = Math.max(textHeight, bitmapHeight) + paddingTop + paddingBottom
                if (heightRequirement < h) {
                    height = h
                }
            } else {
                val h = textHeight + bitmapHeight + paddingTop + paddingBottom
                height = if (heightRequirement < h) h else heightRequirement + paddingTop - paddingBottom
            }
            MeasureSpec.AT_MOST -> {
                val vHeight: Int
                vHeight = if (drawableGravity!!.isHorizontal) {
                    Math.max(textHeight, bitmapHeight)
                } else {
                    textHeight + bitmapHeight + drawablePadding
                }
                height = vHeight + paddingTop * 2 + paddingBottom * 2
            }
            MeasureSpec.UNSPECIFIED -> {}
        }
        calculate(width, height)
        setMeasuredDimension(width, height)
    }

    private var text_X = 0.0f
    private var text_Y = 0.0f
    private var bitmap_X = 0.0f
    private var bitmap_Y = 0.0f
    private fun calculate(width: Int, height: Int) {
        var textHeight = 0f
        var textWidth = 0f
        var textBoundsWidth = 0f
        if (hasText) {
            textHeight = mStaticLayout!!.height.toFloat()
            textWidth = mStaticLayout!!.width.toFloat()
            textBoundsWidth = mTextBounds.width().toFloat()
        }
        var bitmapHeight = 0f
        var bitmapWidth = 0f
        if (hasDrawable) {
            bitmapHeight = mDrawable!!.intrinsicHeight.toFloat()
            bitmapWidth = mDrawable!!.intrinsicWidth.toFloat()
        }
        if (drawableGravity!!.isHorizontal) {
            if (height > Math.max(textHeight, bitmapHeight)) {
                text_Y = height / 2f - textHeight / 2f + paddingTop - paddingBottom
                bitmap_Y = height / 2f - bitmapHeight / 2f + paddingTop - paddingBottom
            } else if (textHeight > bitmapHeight) {
                text_Y = paddingTop.toFloat()
                bitmap_Y = text_Y + textHeight / 2f - bitmapHeight / 2f
            } else {
                bitmap_Y = paddingTop.toFloat()
                text_Y = bitmap_Y + bitmapHeight / 2f - textHeight / 2f
            }
            text_X = paddingLeft.toFloat()
            bitmap_X = textWidth
            var remainingSpace = width - (textBoundsWidth + bitmapWidth)
            if (remainingSpace > 0) {
                remainingSpace /= 2f
            }
            if (drawableGravity == DrawableGravity.RIGHT) {
                text_X = remainingSpace + paddingLeft - paddingRight - drawablePadding / 2f
                bitmap_X = text_X + textBoundsWidth + drawablePadding
            } else if (drawableGravity == DrawableGravity.LEFT) {
                bitmap_X = remainingSpace + paddingLeft - paddingRight - drawablePadding / 2f
                text_X = bitmap_X + bitmapWidth + drawablePadding
            }
        } else {
            if (drawableGravity == DrawableGravity.TOP) {
                bitmap_Y = paddingTop - paddingBottom - drawablePadding / 2f
                val vHeight = (height - (textHeight + bitmapHeight)) / 2f
                if (vHeight > 0) bitmap_Y += vHeight
                text_Y = bitmap_Y + bitmapHeight + drawablePadding
            } else if (drawableGravity == DrawableGravity.BOTTOM) {
                text_Y = paddingTop - paddingBottom - drawablePadding / 2f
                val vHeight = height - (textHeight + bitmapHeight)
                if (vHeight > 0) text_Y += vHeight / 2f
                bitmap_Y = text_Y + textHeight + drawablePadding
            }
            if (width > Math.max(textBoundsWidth, bitmapWidth)) {
                text_X = width / 2f - textBoundsWidth / 2f + paddingLeft - paddingRight
                bitmap_X = width / 2f - bitmapWidth / 2f + paddingLeft - paddingRight
            } else if (textBoundsWidth > bitmapWidth) {
                text_X = paddingLeft.toFloat()
                bitmap_X = text_X + textBoundsWidth / 2f - bitmapWidth / 2f
            } else {
                bitmap_X = paddingLeft.toFloat()
                text_X = bitmap_X + bitmapWidth / 2f - textBoundsWidth / 2f
            }
        }
    }

    private var mBitmapNormalColor: PorterDuffColorFilter? = null
    private var mBitmapClipColor: PorterDuffColorFilter? = null
    private var mDrawable: Drawable? = null
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = canvas.width
        val height = canvas.height
        canvas.save()
        if (clipLeftToRight) canvas.translate(-width * (mClipAmount - 1), 0f) else canvas.translate(width * (mClipAmount - 1), 0f)
        mRectF!!.set(if (hasBorderLeft) mBorderSize.toFloat() else 0f, mBorderSize.toFloat(), if (hasBorderRight) (width - mBorderSize).toFloat() else width.toFloat(), (height - mBorderSize).toFloat())
        canvas.drawRoundRect(mRectF!!, mRadius.toFloat(), mRadius.toFloat(), mPaint!!)
        canvas.restore()
        canvas.save()
        if (hasText) {
            canvas.translate(text_X, text_Y)
            if (hasTextColorOnSelection) mTextPaint!!.color = textColor
            mStaticLayout!!.draw(canvas)
            canvas.restore()
        }
        canvas.save()

        // Bitmap normal
        if (hasDrawable) {
            drawDrawableWithColorFilter(canvas, mBitmapNormalColor)
        }
        // NORMAL -end


        // CLIPPING
        if (clipLeftToRight) {
            canvas.clipRect(width * (1 - mClipAmount), 0f, width.toFloat(), height.toFloat())
        } else {
            canvas.clipRect(0f, 0f, width * mClipAmount, height.toFloat())
        }

        // CLIP -start
        // Text clip
        canvas.save()
        if (hasText) {
            canvas.translate(text_X, text_Y)
            if (hasTextColorOnSelection) mTextPaint!!.color = textColorOnSelection
            mStaticLayoutOverlay!!.draw(canvas)
            canvas.restore()
        }

        // Bitmap clip
        if (hasDrawable) {
            drawDrawableWithColorFilter(canvas, mBitmapClipColor)
        }
        // CLIP -end
        canvas.restore()
    }

    private fun drawDrawableWithColorFilter(canvas: Canvas, colorFilter: ColorFilter?) {
        val drawableX = bitmap_X.toInt()
        val drawableY = bitmap_Y.toInt()
        var drawableWidth = mDrawable!!.intrinsicWidth
        if (hasDrawableWidth) {
            drawableWidth = this.drawableWidth
        }
        var drawableHeight = mDrawable!!.intrinsicHeight
        if (hasDrawableHeight) {
            drawableHeight = this.drawableHeight
        }
        mDrawable!!.colorFilter = colorFilter
        mDrawable!!.setBounds(drawableX, drawableY, drawableX + drawableWidth, drawableY + drawableHeight)
        mDrawable!!.draw(canvas)
    }

    fun clipToLeft(clip: Float) {
        clipLeftToRight = false
        mClipAmount = 1.0f - clip
        invalidate()
    }

    fun clipToRight(clip: Float) {
        clipLeftToRight = true
        mClipAmount = clip
        invalidate()
    }

    /**
     * @return drawable's tint color when selector is on the button
     */
    var drawableTintOnSelection = 0
        private set
    /**
     * @return button's text color when selector is on the button
     */
    /**
     * @param textColorOnSelection set button's text color when selector is on the button
     */
    var textColorOnSelection = 0
    var textColor = 0

    /**
     * @return button's current ripple color
     */
    var rippleColor = 0
        private set
    var buttonWidth = 0
        private set
    private var drawable = 0
    /**
     * @return drawable's tint color
     */
    /**
     * If button has any drawable, it sets drawable's tint color without changing drawable's position.
     *
     * @param color is used to set drawable's tint color
     */
    var drawableTint = 0
    private var drawableWidth = 0
    private var drawableHeight = 0
    private var drawablePadding = 0
    private var hasTextColorOnSelection = false
    private var hasRipple = false
    private var hasWidth = false
    private var hasWeight = false
    private var hasDrawableTintOnSelection = false
    private var hasDrawableWidth = false
    private var hasDrawableHeight = false
    private var hasDrawableTint = false
    private var hasTextTypefacePath = false
    var weight = 0f
        private set
    var textSize = 0f
    private var textTypefacePath: String? = null
    var text: String? = null
    private var textTypeface: Typeface? = null
    private fun getAttributes(attrs: AttributeSet?) {
        val ta = getContext().obtainStyledAttributes(attrs, R.styleable.SegmentedButton)
        drawableTintOnSelection = ta.getColor(R.styleable.SegmentedButton_sb_drawableTint_onSelection, Color.WHITE)
        hasDrawableTintOnSelection = ta.hasValue(R.styleable.SegmentedButton_sb_drawableTint_onSelection)
        textColorOnSelection = ta.getColor(R.styleable.SegmentedButton_sb_textColor_onSelection, Color.WHITE)
        hasTextColorOnSelection = ta.hasValue(R.styleable.SegmentedButton_sb_textColor_onSelection)
        rippleColor = ta.getColor(R.styleable.SegmentedButton_sb_rippleColor, 0)
        hasRipple = ta.hasValue(R.styleable.SegmentedButton_sb_rippleColor)
        text = ta.getString(R.styleable.SegmentedButton_sb_text)
        hasText = ta.hasValue(R.styleable.SegmentedButton_sb_text)
        textSize = ta.getDimension(R.styleable.SegmentedButton_sb_textSize, ConversionHelper.spToPx(getContext(), 14))
        textColor = ta.getColor(R.styleable.SegmentedButton_sb_textColor, Color.GRAY)
        textTypefacePath = ta.getString(R.styleable.SegmentedButton_sb_textTypefacePath)
        hasTextTypefacePath = ta.hasValue(R.styleable.SegmentedButton_sb_textTypefacePath)
        val typeface = ta.getInt(R.styleable.SegmentedButton_sb_textTypeface, 1)
        when (typeface) {
            0 -> textTypeface = Typeface.MONOSPACE
            1 -> textTypeface = Typeface.DEFAULT
            2 -> textTypeface = Typeface.SANS_SERIF
            3 -> textTypeface = Typeface.SERIF
        }
        try {
            hasWeight = ta.hasValue(R.styleable.SegmentedButton_android_layout_weight)
            weight = ta.getFloat(R.styleable.SegmentedButton_android_layout_weight, 0f)
            buttonWidth = ta.getDimensionPixelSize(R.styleable.SegmentedButton_android_layout_width, 0)
        } catch (ex: Exception) {
            hasWeight = true
            weight = 1f
        }
        hasWidth = !hasWeight && buttonWidth > 0
        drawable = ta.getResourceId(R.styleable.SegmentedButton_sb_drawable, 0)
        drawableTint = ta.getColor(R.styleable.SegmentedButton_sb_drawableTint, -1)
        drawableWidth = ta.getDimensionPixelSize(R.styleable.SegmentedButton_sb_drawableWidth, -1)
        drawableHeight = ta.getDimensionPixelSize(R.styleable.SegmentedButton_sb_drawableHeight, -1)
        drawablePadding = ta.getDimensionPixelSize(R.styleable.SegmentedButton_sb_drawablePadding, 0)
        hasDrawable = ta.hasValue(R.styleable.SegmentedButton_sb_drawable)
        hasDrawableTint = ta.hasValue(R.styleable.SegmentedButton_sb_drawableTint)
        hasDrawableWidth = ta.hasValue(R.styleable.SegmentedButton_sb_drawableWidth)
        hasDrawableHeight = ta.hasValue(R.styleable.SegmentedButton_sb_drawableHeight)
        drawableGravity = DrawableGravity.getById(ta.getInteger(R.styleable.SegmentedButton_sb_drawableGravity, 0))
        ta.recycle()
    }

    /**
     * Typeface.NORMAL: 0
     * Typeface.BOLD: 1
     * Typeface.ITALIC: 2
     * Typeface.BOLD_ITALIC: 3
     *
     * @param typeface you can use above variations using the bitwise OR operator
     */
    fun setTypeface(typeface: Typeface?) {
        mTextPaint!!.typeface = typeface
    }

    /**
     * @param location is .ttf file's path in assets folder. Example: 'fonts/my_font.ttf'
     */
    fun setTypeface(location: String?) {
        if (null != location && location != "") {
            val typeface = Typeface.createFromAsset(getContext().assets, location)
            mTextPaint!!.typeface = typeface
        }
    }

    /**
     * GRAVITY
     */
    private var drawableGravity: DrawableGravity? = null

    enum class DrawableGravity(private val intValue: Int) {
        LEFT(0), TOP(1), RIGHT(2), BOTTOM(3);

        val isHorizontal: Boolean
            get() = intValue == 0 || intValue == 2

        companion object {
            fun getById(id: Int): DrawableGravity? {
                for (e in values()) {
                    if (e.intValue == id) return e
                }
                return null
            }
        }
    }

    private var hasDrawable = false
    private var hasText = false

    /**
     * Sets button's drawable by given drawable object and its position
     *
     * @param resId is your drawable's resource id
     */
    fun setDrawable(resId: Int) {
        setDrawable(ContextCompat.getDrawable(context, resId))
    }

    /**
     * Sets button's drawable by given drawable object and its position
     *
     * @param drawable is your drawable object
     */
    fun setDrawable(drawable: Drawable?) {
        mDrawable = drawable
        hasDrawable = true
        requestLayout()
    }

    /**
     * Sets button's drawable by given drawable id and its position
     *
     * @param gravity specifies button's drawable position relative to text position.
     * These values can be given to position:
     * {DrawableGravity.LEFT} sets drawable to the left of button's text
     * {DrawableGravity.TOP} sets drawable to the top of button's text
     * {DrawableGravity.RIGHT} sets drawable to the right of button's text
     * {DrawableGravity.BOTTOM} sets drawable to the bottom of button's text
     */
    fun setGravity(gravity: DrawableGravity?) {
        drawableGravity = gravity
    }

    /**
     * removes drawable's tint
     */
    fun removeDrawableTint() {
        hasDrawableTint = false
    }

    fun removeDrawableTintOnSelection() {
        hasDrawableTintOnSelection = false
    }

    fun removeTextColorOnSelection() {
        hasTextColorOnSelection = false
    }

    /**
     * @return true if the button has a ripple effect
     */
    fun hasRipple(): Boolean {
        return hasRipple
    }

    /**
     * @return true if button's drawable is not empty
     */
    fun hasDrawableTint(): Boolean {
        return hasDrawableTint
    }

    /**
     * @return true if button's drawable has tint when selector is on the button
     */
    fun hasDrawableTintOnSelection(): Boolean {
        return hasDrawableTintOnSelection
    }

    /**
     *
     */
    fun hasWeight(): Boolean {
        return hasWeight
    }

    fun hasWidth(): Boolean {
        return hasWidth
    }

    fun hasTextColorOnSelection(): Boolean {
        return hasTextColorOnSelection
    }
}