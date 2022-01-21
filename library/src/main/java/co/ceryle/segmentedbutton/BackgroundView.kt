package co.ceryle.segmentedbutton

import android.content.Context
import android.util.AttributeSet
import android.view.View

class BackgroundView : View {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = 0
        var h = 0
        w = Math.max(w, suggestedMinimumWidth)
        h = Math.max(h, suggestedMinimumHeight)
        val widthSize = resolveSizeAndState(w, widthMeasureSpec, 0)
        val heightSize = resolveSizeAndState(h, heightMeasureSpec, 0)
        setMeasuredDimension(widthSize, heightSize)
    }
}