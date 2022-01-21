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
package br.com.soareseneves.segmentedbutton

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import co.ceryle.segmentedbutton.R
import java.util.*

class SegmentedButtonGroup : LinearLayout {
    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    private var mainGroup: LinearLayout? = null
    private var rippleContainer: LinearLayout? = null
    private var dividerContainer: LinearLayout? = null
    private var draggable = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val selectorWidth: Float
        var offsetX: Float
        var position = 0
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                selectorWidth = width.toFloat() / numberOfButtons / 2f
                offsetX = (event.x - selectorWidth) * numberOfButtons / width
                position = Math.floor(offsetX + 0.5).toInt()
                run {
                    lastPositionOffset = offsetX
                    toggledPositionOffset = lastPositionOffset
                }
                toggle(position, selectorAnimationDuration, true)
            }
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                if (!draggable) return true
                selectorWidth = width.toFloat() / numberOfButtons / 2f
                offsetX = (event.x - selectorWidth) * numberOfButtons / width.toFloat()
                position = Math.floor(offsetX.toDouble()).toInt()
                offsetX -= position.toFloat()
                if (event.rawX - selectorWidth < left) {
                    offsetX = 0f
                    animateViews(position + 1, offsetX)
                    return true
                }
                if (event.rawX + selectorWidth > right) {
                    offsetX = 1f
                    animateViews(position - 1, offsetX)
                    return true
                }
                animateViews(position, offsetX)
            }
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class ButtonOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.measuredWidth, view.measuredHeight, radius.toFloat())
        }
    }

    private fun init(attrs: AttributeSet?) {
        getAttributes(attrs)
        setWillNotDraw(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = ButtonOutlineProvider()
        }
        isClickable = true
        buttons = ArrayList()
        val container = FrameLayout(context)
        container.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(container)
        mainGroup = LinearLayout(context)
        mainGroup!!.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        mainGroup!!.orientation = HORIZONTAL
        container.addView(mainGroup)
        rippleContainer = LinearLayout(context)
        rippleContainer!!.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        rippleContainer!!.orientation = HORIZONTAL
        rippleContainer!!.isClickable = false
        rippleContainer!!.isFocusable = false
        rippleContainer!!.setPadding(borderSize, borderSize, borderSize, borderSize)
        container.addView(rippleContainer)
        dividerContainer = LinearLayout(context)
        dividerContainer!!.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        dividerContainer!!.orientation = HORIZONTAL
        dividerContainer!!.isClickable = false
        dividerContainer!!.isFocusable = false
        container.addView(dividerContainer)
        initInterpolations()
        setContainerAttrs()
        setDividerAttrs()
        rectF = RectF()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private var rectF: RectF? = null
    private var paint: Paint? = null
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        rectF!![0f, 0f, width] = height
        paint!!.style = Paint.Style.FILL
        paint!!.color = backgroundColor
        canvas.drawRoundRect(rectF!!, radius.toFloat(), radius.toFloat(), paint!!)
        if (borderSize > 0) {
            val bSize = borderSize / 2f
            rectF!![0 + bSize, 0 + bSize, width - bSize] = height - bSize
            paint!!.style = Paint.Style.STROKE
            paint!!.color = borderColor
            paint!!.strokeWidth = borderSize.toFloat()
            canvas.drawRoundRect(rectF!!, radius.toFloat(), radius.toFloat(), paint!!)
        }
    }

    private fun setBackgroundColor(v: View, d: Drawable?, c: Int) {
        if (null != d) {
            BackgroundHelper.setBackground(v, d)
        } else {
            v.setBackgroundColor(c)
        }
    }

    private fun setDividerAttrs() {
        if (!isHasDivider) return
        dividerContainer!!.showDividers = SHOW_DIVIDER_MIDDLE
        // Divider Views
        RoundHelper.makeDividerRound(
            dividerContainer,
            dividerColor,
            dividerRadius,
            dividerSize,
            dividerBackgroundDrawable
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            dividerContainer!!.dividerPadding = dividerPadding
        }
    }

    private var numberOfButtons = 0
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is SegmentedButton) {
            val button = child
            val position = numberOfButtons++
            button.setSelectorColor(selectorColor)
            button.setSelectorRadius(radius)
            button.setBorderSize(borderSize)
            if (position == 0) button.hasBorderLeft(true)
            if (position > 0) buttons!![position - 1].hasBorderRight(false)
            button.hasBorderRight(true)
            mainGroup!!.addView(child, params)
            buttons!!.add(button)
            if (this.position == position) {
                button.clipToRight(1f)
                toggledPosition = position
                lastPosition = toggledPosition
                toggledPositionOffset = position.toFloat()
                lastPositionOffset = toggledPositionOffset
            }

            // RIPPLE
            val rippleView = BackgroundView(context)
            if (!draggable) {
                rippleView.setOnClickListener(object : OnClickListener {
                    override fun onClick(v: View) {
                        if (clickable && enabled) toggle(position, selectorAnimationDuration, true)
                    }
                })
            }
            setRipple(rippleView, enabled && clickable)
            rippleContainer!!.addView(rippleView,
                    LayoutParams(button.buttonWidth, ViewGroup.LayoutParams.MATCH_PARENT, button.weight))
            ripples.add(rippleView)
            if (!isHasDivider) return
            val dividerView = BackgroundView(context)
            dividerContainer!!.addView(dividerView,
                    LayoutParams(button.buttonWidth, ViewGroup.LayoutParams.MATCH_PARENT, button.weight))
        } else super.addView(child, index, params)
    }

    private val ripples = ArrayList<BackgroundView>()
    private fun setRipple(v: View, isClickable: Boolean) {
        if (isClickable) {
            if (isHasRippleColor) RippleHelper.setRipple(
                v,
                rippleColor,
                radius
            ) else if (isRipple) RippleHelper.setSelectableItemBackground(context, v) else {
                for (button in buttons!!) {
                    if (button is SegmentedButton && button.hasRipple()) RippleHelper.setRipple(
                        v,
                        button.rippleColor,
                        radius
                    )
                }
            }
        } else {
            BackgroundHelper.setBackground(v, null)
        }
    }

    private fun setContainerAttrs() {
        if (isInEditMode) mainGroup!!.setBackgroundColor(backgroundColor)
    }

    private var buttons: ArrayList<SegmentedButton>? = null

    /**
     * @param selectorColor sets color to selector
     * default: Color.GRAY
     */
    var selectorColor = 0

    /**
     * @param animateSelector is used to give an animation to selector with the given interpolator constant
     */
    var selectorAnimation = 0

    /**
     * @param animateSelectorDuration sets how long selector animation should last
     */
    var selectorAnimationDuration = 0
    private var position = 0
    private var backgroundColor = 0
    private var dividerColor = 0
    private var radius = 0
    private var dividerSize = 0

    /**
     * @param rippleColor sets ripple color and adds ripple when a button is hovered
     * default: Color.GRAY
     */
    var rippleColor = 0
    private var dividerPadding = 0
    private var dividerRadius = 0
    private var borderSize = 0
    private var borderColor = 0
    private var clickable = false
    private var enabled = false

    /**
     * @param ripple applies android's default ripple on layout
     */
    var isRipple = false
    var isHasRippleColor = false
        private set
    var isHasDivider = false
        private set
    //private var backgroundDrawable: Drawable? = null
    private var selectorBackgroundDrawable: Drawable? = null
    private var dividerBackgroundDrawable: Drawable? = null

    /**
     * Get attributes
     */
    private fun getAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SegmentedButtonGroup)
        isHasDivider = typedArray.hasValue(R.styleable.SegmentedButtonGroup_sbg_dividerSize)
        dividerSize = typedArray.getDimensionPixelSize(R.styleable.SegmentedButtonGroup_sbg_dividerSize, 0)
        dividerColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_sbg_dividerColor, Color.WHITE)
        dividerPadding = typedArray.getDimensionPixelSize(R.styleable.SegmentedButtonGroup_sbg_dividerPadding, 0)
        dividerRadius = typedArray.getDimensionPixelSize(R.styleable.SegmentedButtonGroup_sbg_dividerRadius, 0)
        selectorColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_sbg_selectorColor, Color.GRAY)
        selectorAnimation = typedArray.getInt(R.styleable.SegmentedButtonGroup_sbg_animateSelector, 0)
        selectorAnimationDuration = typedArray.getInt(R.styleable.SegmentedButtonGroup_sbg_animateSelectorDuration, 500)
        radius = typedArray.getDimensionPixelSize(R.styleable.SegmentedButtonGroup_sbg_radius, 0)
        position = typedArray.getInt(R.styleable.SegmentedButtonGroup_sbg_position, 0)
        backgroundColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_sbg_backgroundColor, Color.TRANSPARENT)
        isRipple = typedArray.getBoolean(R.styleable.SegmentedButtonGroup_sbg_ripple, false)
        isHasRippleColor = typedArray.hasValue(R.styleable.SegmentedButtonGroup_sbg_rippleColor)
        rippleColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_sbg_rippleColor, Color.GRAY)
        borderSize = typedArray.getDimensionPixelSize(R.styleable.SegmentedButtonGroup_sbg_borderSize, 0)
        borderColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_sbg_borderColor, Color.BLACK)
        //backgroundDrawable = typedArray.getDrawable(R.styleable.SegmentedButtonGroup_sbg_backgroundDrawable)
        selectorBackgroundDrawable = typedArray.getDrawable(R.styleable.SegmentedButtonGroup_sbg_selectorBackgroundDrawable)
        dividerBackgroundDrawable = typedArray.getDrawable(R.styleable.SegmentedButtonGroup_sbg_dividerBackgroundDrawable)
        enabled = typedArray.getBoolean(R.styleable.SegmentedButtonGroup_sbg_enabled, true)
        draggable = typedArray.getBoolean(R.styleable.SegmentedButtonGroup_sbg_draggable, false)
        try {
            clickable = typedArray.getBoolean(R.styleable.SegmentedButtonGroup_android_clickable, true)
        } catch (ex: Exception) {
            Log.d("SegmentedButtonGroup", ex.toString())
        }
        typedArray.recycle()
    }

    /**
     * @param interpolatorSelector is used to give an animation to selector with the given one of android's interpolator.
     * Ex: [FastOutSlowInInterpolator], [BounceInterpolator], [LinearInterpolator]
     */
    var interpolatorSelector: Interpolator? = null
    private fun initInterpolations() {
        val interpolatorList: ArrayList<Class<*>?> = object : ArrayList<Class<*>?>() {
            init {
                add(androidx.interpolator.view.animation.FastOutSlowInInterpolator::class.java)
                add(BounceInterpolator::class.java)
                add(LinearInterpolator::class.java)
                add(DecelerateInterpolator::class.java)
                add(CycleInterpolator::class.java)
                add(AnticipateInterpolator::class.java)
                add(AccelerateDecelerateInterpolator::class.java)
                add(AccelerateInterpolator::class.java)
                add(AnticipateOvershootInterpolator::class.java)
                add(androidx.interpolator.view.animation.FastOutLinearInInterpolator::class.java)
                add(androidx.interpolator.view.animation.LinearOutSlowInInterpolator::class.java)
                add(OvershootInterpolator::class.java)
            }
        }
        try {
            interpolatorSelector = interpolatorList[selectorAnimation]?.newInstance() as Interpolator
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var onPositionChangedListener: OnPositionChangedListener? = null

    /**
     * @param onPositionChangedListener set your instance that you have created to listen any position change
     */
    fun setOnPositionChangedListener(onPositionChangedListener: OnPositionChangedListener?) {
        this.onPositionChangedListener = onPositionChangedListener
    }

    /**
     * Use this listener if you want to know any position change.
     * Listener is called when one of segmented button is clicked or setPosition is called.
     */
    interface OnPositionChangedListener {
        fun onPositionChanged(position: Int)
    }

    private var onClickedButtonListener: OnClickedButtonListener? = null

    /**
     * @param onClickedButtonListener set your instance that you have created to listen clicked positions
     */
    fun setOnClickedButtonListener(onClickedButtonListener: OnClickedButtonListener?) {
        this.onClickedButtonListener = onClickedButtonListener
    }

    /**
     * Use this listener if  you want to know which button is clicked.
     * Listener is called when one of segmented button is clicked
     */
    interface OnClickedButtonListener {
        fun onClickedButton(position: Int)
    }

    /**
     * @param position is used to select one of segmented buttons
     */
    fun setPosition(position: Int) {
        this.position = position
        if (null == buttons) {
            toggledPosition = position
            lastPosition = toggledPosition
            toggledPositionOffset = position.toFloat()
            lastPositionOffset = toggledPositionOffset
        } else {
            toggle(position, selectorAnimationDuration, false)
        }
    }

    /**
     * @param position is used to select one of segmented buttons
     * @param duration determines how long animation takes to finish
     */
    fun setPosition(position: Int, duration: Int) {
        this.position = position
        if (null == buttons) {
            toggledPosition = position
            lastPosition = toggledPosition
            toggledPositionOffset = position.toFloat()
            lastPositionOffset = toggledPositionOffset
        } else {
            toggle(position, duration, false)
        }
    }

    /**
     * @param position      is used to select one of segmented buttons
     * @param withAnimation if true default animation will perform
     */
    fun setPosition(position: Int, withAnimation: Boolean) {
        this.position = position
        if (null == buttons) {
            toggledPosition = position
            lastPosition = toggledPosition
            toggledPositionOffset = position.toFloat()
            lastPositionOffset = toggledPositionOffset
        } else {
            if (withAnimation) toggle(position, selectorAnimationDuration, false) else toggle(position, 1, false)
        }
    }

    /**
     * @param backgroundColor sets background color of whole layout including buttons on top of it
     * default: Color.WHITE
     */
    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    /**
     * @param hasRippleColor if true ripple will be shown.
     * if setRipple(boolean) is also set to false, there will be no ripple
     */
    fun setRippleColor(hasRippleColor: Boolean) {
        isHasRippleColor = hasRippleColor
    }

    /**
     * @param radius determines how round layout's corners should be
     */
    fun setRadius(radius: Int) {
        this.radius = radius
    }

    /**
     * @param dividerPadding adjusts divider's top and bottom distance to its container
     */
    override fun setDividerPadding(dividerPadding: Int) {
        this.dividerPadding = dividerPadding
    }

    /**
     * @param dividerColor changes divider's color with the given one
     * default: Color.WHITE
     */
    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        RoundHelper.makeDividerRound(
            dividerContainer,
            dividerColor,
            dividerRadius,
            dividerSize,
            dividerBackgroundDrawable
        )
    }

    /**
     * @param dividerSize sets thickness of divider
     * default: 0
     */
    fun setDividerSize(dividerSize: Int) {
        this.dividerSize = dividerSize
        RoundHelper.makeDividerRound(
            dividerContainer,
            dividerColor,
            dividerRadius,
            dividerSize,
            dividerBackgroundDrawable
        )
    }

    /**
     * @param dividerRadius determines how round divider should be
     * default: 0
     */
    fun setDividerRadius(dividerRadius: Int) {
        this.dividerRadius = dividerRadius
        RoundHelper.makeDividerRound(
            dividerContainer,
            dividerColor,
            dividerRadius,
            dividerSize,
            dividerBackgroundDrawable
        )
    }

    /**
     * @param hasDivider if true divider will be shown.
     */
    fun setDivider(hasDivider: Boolean) {
        isHasDivider = hasDivider
    }

    /**
     * @param borderSize sets thickness of border
     * default: 0
     */
    fun setBorderSize(borderSize: Int) {
        this.borderSize = borderSize
    }

    /**
     * @param borderColor sets border color to the given one
     * default: Color.BLACK
     */
    fun setBorderColor(borderColor: Int) {
        this.borderColor = borderColor
    }

    fun getDividerSize(): Int {
        return dividerSize
    }

    fun getPosition(): Int {
        return position
    }

    fun getBackgroundColor(): Int {
        return backgroundColor
    }

    fun getDividerColor(): Int {
        return dividerColor
    }

    fun getRadius(): Float {
        return radius.toFloat()
    }

    override fun getDividerPadding(): Int {
        return dividerPadding
    }

    fun getDividerRadius(): Float {
        return dividerRadius.toFloat()
    }

    private fun setRippleState(state: Boolean) {
        for (v in ripples) {
            setRipple(v, state)
        }
    }

    private fun setEnabledAlpha(enabled: Boolean) {
        var alpha = 1f
        if (!enabled) alpha = 0.5f
        setAlpha(alpha)
    }

    /**
     * @param enabled set it to:
     * false, if you want buttons to be unclickable and add grayish looking which gives disabled look,
     * true, if you want buttons to be clickable and remove grayish looking
     */
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        setRippleState(enabled)
        setEnabledAlpha(enabled)
    }

    /**
     * @param clickable set it to:
     * false for unclickable buttons,
     * true for clickable buttons
     */
    override fun setClickable(clickable: Boolean) {
        this.clickable = clickable
        setRippleState(clickable)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("state", super.onSaveInstanceState())
        bundle.putInt("position", position)
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            position = bundle.getInt("position")
            state = bundle.getParcelable("state")
            setPosition(position, false)
        }
        super.onRestoreInstanceState(state)
    }

    /**
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    private var toggledPosition = 0
    private var toggledPositionOffset = 0f
    private fun toggle(position: Int, duration: Int, isToggledByTouch: Boolean) {
        if (!draggable && toggledPosition == position) return
        toggledPosition = position
        val animator = ValueAnimator.ofFloat(toggledPositionOffset, position.toFloat())
        animator.addUpdateListener { animation ->
            toggledPositionOffset = animation.animatedValue as Float
            val animatedValue = toggledPositionOffset
            val position = animatedValue.toInt()
            val positionOffset = animatedValue - position
            animateViews(position, positionOffset)
            invalidate()
        }
        animator.interpolator = interpolatorSelector
        animator.duration = duration.toLong()
        animator.start()
        if (null != onClickedButtonListener && isToggledByTouch) onClickedButtonListener!!.onClickedButton(position)
        if (null != onPositionChangedListener) onPositionChangedListener!!.onPositionChanged(position)
        this.position = position
    }

    private var lastPosition = 0
    private var lastPositionOffset = 0f
    private fun animateViews(position: Int, positionOffset: Float) {
        val realPosition = position + positionOffset
        val lastRealPosition = lastPosition + lastPositionOffset
        if (realPosition == lastRealPosition) {
            return
        }
        var nextPosition = position + 1
        if (positionOffset == 0.0f) {
            if (lastRealPosition <= realPosition) {
                nextPosition = position - 1
            }
        }
        if (lastPosition > position) {
            if (lastPositionOffset > 0f) {
                toNextPosition(nextPosition + 1, 1f)
            }
        }
        if (lastPosition < position) {
            if (lastPositionOffset < 1.0f) {
                toPosition(position - 1, 0f)
            }
        }
        toNextPosition(nextPosition, 1.0f - positionOffset)
        toPosition(position, 1.0f - positionOffset)
        lastPosition = position
        lastPositionOffset = positionOffset
    }

    private fun toPosition(position: Int, clip: Float) {
        if (position >= 0 && position < numberOfButtons) buttons!![position].clipToRight(clip)
    }

    private fun toNextPosition(position: Int, clip: Float) {
        if (position >= 0 && position < numberOfButtons) buttons!![position].clipToLeft(clip)
    }

    companion object {
        const val FastOutSlowInInterpolator = 0
        const val BounceInterpolator = 1
        const val LinearInterpolator = 2
        const val DecelerateInterpolator = 3
        const val CycleInterpolator = 4
        const val AnticipateInterpolator = 5
        const val AccelerateDecelerateInterpolator = 6
        const val AccelerateInterpolator = 7
        const val AnticipateOvershootInterpolator = 8
        const val FastOutLinearInInterpolator = 9
        const val LinearOutSlowInInterpolator = 10
        const val OvershootInterpolator = 11
    }
}