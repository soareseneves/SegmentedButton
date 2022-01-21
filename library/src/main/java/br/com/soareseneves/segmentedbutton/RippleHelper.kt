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

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.View
import java.util.*

internal object RippleHelper {
    fun setSelectableItemBackground(context: Context, view: View) {
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val ta = context.obtainStyledAttributes(attrs)
        val drawableFromTheme = ta.getDrawable(0 /* index */)
        ta.recycle()
        BackgroundHelper.setBackground(view, drawableFromTheme)
    }

    fun setRipple(view: View, pressedColor: Int, radius: Int) {
        setRipple(view, pressedColor, null, radius)
    }

    fun setRipple(view: View, pressedColor: Int, normalColor: Int?, radius: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.background = getRippleDrawable(pressedColor, normalColor, radius)
        } else {
            view.setBackgroundDrawable(getStateListDrawable(pressedColor, normalColor, radius))
        }
    }

    private fun getStateListDrawable(pressedColor: Int, normalColor: Int?, radius: Int): StateListDrawable {
        val states = StateListDrawable()
        states.addState(intArrayOf(android.R.attr.state_pressed), getDrawable(pressedColor, radius)
        )
        if (null != normalColor) states.addState(intArrayOf(), getDrawable(normalColor, radius)
        )
        return states
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getRippleDrawable(pressedColor: Int, normalColor: Int?, radius: Int): Drawable {
        val colorStateList = getPressedColorSelector(pressedColor)
        val content: Drawable? = normalColor?.let { ColorDrawable(it) }
        val mask = getRippleMask(Color.WHITE, radius)
        return RippleDrawable(colorStateList, content, mask)
    }

    private fun getPressedColorSelector(pressedColor: Int): ColorStateList {
        return ColorStateList(arrayOf(intArrayOf()), intArrayOf(
                pressedColor
        ))
    }

    private fun getRippleMask(color: Int, radius: Int): Drawable {
        val outerRadii = FloatArray(8)
        Arrays.fill(outerRadii, radius.toFloat())
        val r = RoundRectShape(outerRadii, null, null)
        val shapeDrawable = ShapeDrawable(r)
        shapeDrawable.paint.color = color
        return shapeDrawable
    }

    private fun getDrawable(color: Int?, radius: Int): Drawable? {
        if (color == null) return null
        if (radius == 0) return ColorDrawable(color)
        val outerRadii = FloatArray(8)
        Arrays.fill(outerRadii, radius.toFloat())
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = outerRadii
        shape.setColor(color)
        return shape
    }
}