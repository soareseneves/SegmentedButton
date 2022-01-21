package co.ceryle.segmentedbutton.sample

import android.graphics.Color
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import co.ceryle.segmentedbutton.SegmentedButtonGroup.OnClickedButtonListener
import co.ceryle.segmentedbutton.sample.drawable.BadgeDrawable
import co.ceryle.segmentedbutton.SegmentedButton

class MainActivity : AppCompatActivity() {
    private var button: Button? = null
    private var group: SegmentedButtonGroup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        group = findViewById(R.id.segmentedButtonGroup) as SegmentedButtonGroup
        button = findViewById(R.id.button) as Button
        updateButton(group!!.getPosition())
        group!!.setOnClickedButtonListener(object : OnClickedButtonListener {
            override fun onClickedButton(position: Int) {
                updateButton(position)
            }
        })
        button!!.setOnClickListener {
            var position = group!!.getPosition()
            position = ++position % 3
            updateButton(position)
            group!!.setPosition(position, true)
        }
        group!!.isEnabled = false
        val handler = Handler()
        val runnable = Runnable { group!!.isEnabled = true }
        handler.postDelayed(runnable, 5000)
        setupDynamicDrawables()
    }

    private fun setupDynamicDrawables() {
        val drawable = BadgeDrawable(Color.RED, 80, 50, 3, 3)
        val leftButton = findViewById(R.id.left_button) as SegmentedButton
        leftButton.setDrawable(drawable)
        val group = findViewById(R.id.dynamic_drawable_group) as SegmentedButtonGroup
        group.setOnClickedButtonListener(object : OnClickedButtonListener {
            override fun onClickedButton(position: Int) {
                if (position == 0) {
                    drawable.count = drawable.count + 1
                    leftButton.requestLayout()
                }
            }
        })
        val rightButton = findViewById(R.id.right_button) as SegmentedButton
        rightButton.setDrawable(R.drawable.ic_b1)
    }

    private fun updateButton(position: Int) {
        button!!.text = "Position: $position"
    }
}