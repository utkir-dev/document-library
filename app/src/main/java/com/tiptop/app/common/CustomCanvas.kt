package com.tiptop.app.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.tiptop.R

class CustomCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var rect = RectF()
    private var listCanvasData = ArrayList<CanvasData>()
    private var listRect = ArrayList<RectF>()
    private var listPaint = ArrayList<Paint>()

    private var redPaint = Paint().apply {
        color = context.resources.getColor(R.color.red_clear, null)
    }
    private var yellowPaint = Paint().apply {
        color = context.resources.getColor(R.color.yellow_clear, null)
    }
    private var greenPaint = Paint().apply {
        color = context.resources.getColor(R.color.green_clear, null)
    }
    private var bluePaint = Paint().apply {
        color = context.resources.getColor(R.color.blue_clear, null)
    }
    private var paint = redPaint
    private var endX = 0f
    private var endY = 0f
    private var touchX = 0f
    private var touchY = 0f

    fun undo() {
        if (listRect.isNotEmpty()) {
            listRect.removeAt(listRect.size - 1)
            listPaint.removeAt(listPaint.size - 1)
            initCanvasData()
            invalidate()
        }
    }

    fun clear() {
        listRect.clear()
        listPaint.clear()
        listCanvasData.clear()
        invalidate()
    }

    fun setData(list: List<CanvasData>) {
        listRect.clear()
        listPaint.clear()
        listCanvasData.clear()
        list.forEach {
            listRect.add(it.rect)
            listPaint.add(it.paint)
        }
        initCanvasData()
        invalidate()
    }

    fun save(funtion: (List<CanvasData>) -> Unit) {
        funtion(listCanvasData)

    }


    fun setColor(color: MyColor) {
        when (color) {
            MyColor.RED -> paint = redPaint
            MyColor.YELLOW -> paint = yellowPaint
            MyColor.GREEN -> paint = greenPaint
            MyColor.BLUE -> paint = bluePaint
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
                endX = event.x
                endY = event.y
                rect = RectF()
                listPaint.add(paint)
                listRect.add(rect)
            }

            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                endY = event.y
                rect.set(touchX, touchY, endX, endY)
                val index = listRect.indexOf(rect)
                listRect[index] = rect
            }

            MotionEvent.ACTION_UP -> {
                initCanvasData()
            }

            else -> {
                return false
            }

        }
        invalidate()
        return true
    }

    private fun initCanvasData() {
        listCanvasData.clear()
        for (i in listRect.indices) {
            listCanvasData.add(
                CanvasData(
                    rect = listRect[i],
                    paint = listPaint[i]
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in listRect.indices) {
            canvas.drawRect(listRect[i], listPaint[i])
        }
    }
}

data class CanvasData(
    var rect: RectF = RectF(),
    var paint: Paint = Paint()
)

enum class MyColor {
    RED, YELLOW, GREEN, BLUE
}