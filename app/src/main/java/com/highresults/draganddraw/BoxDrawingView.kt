package com.highresults.draganddraw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

private const val TAG = "BoxDrawingView"

class BoxDrawingView(context: Context, attrs: AttributeSet? = null) :
        View(context, attrs) {
    private val path = Path()
    private var matrixPath: Matrix = Matrix()
    private var currentBox: Box? = null
    private var firstRotatedTouch = PointF()
    private var boxen = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
// Заполнение фона
        canvas.drawARGB(80, 102, 204, 255)
        boxen.forEach { box ->
            path.reset()
            path.addRect(box.left, box.top,
                    box.right, box.bottom, Path.Direction.CW)
            if (box.rotation != 0f) {
                val center = PointF((box.right - box.left) / 2 + box.left, (box.top - box.bottom) / 2 + box.bottom)
                canvas.drawCircle(center.x, center.y, 10F, boxPaint)
                matrixPath.let {
                    it.reset()
                    it.setRotate(box.rotation, center.x, center.y)
                    path.transform(it)
                }
            }
            canvas.drawPath(path, boxPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        boxen.forEach { box ->
            box.start = PointF(box.start.x * width, box.start.y * height)
            box.end = PointF(box.end.x * width, box.end.y * height)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        boxen.forEach { box ->
            box.start = PointF(box.start.x / width, box.start.y / height)
            box.end = PointF(box.end.x / width, box.end.y / height)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        // индекс касания
        event.actionIndex
        // число касаний
        event.pointerCount
        var action = ""
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                action = "ACTION_DOWN"
                currentBox = Box(current).also {
                    boxen.add(it)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                action = "ACTION_POINTER_DOWN"
                if (event.pointerCount == 2)
                    firstRotatedTouch = current

            }
            MotionEvent.ACTION_MOVE -> {
                action = "ACTION_MOVE"
                if (event.pointerCount == 2) {
                    if (kotlin.math.abs(current.x - firstRotatedTouch.x) > kotlin.math.abs(current.y - firstRotatedTouch.y)) {
                        currentBox?.let { it.rotation += current.x - firstRotatedTouch.x }
                    } else {
                        currentBox?.let { it.rotation += current.y - firstRotatedTouch.y }
                    }
                }
                updateCurrentBox(current)
            }
            MotionEvent.ACTION_UP -> {
                action = "ACTION_UP"
                updateCurrentBox(current)
                currentBox = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                action = "ACTION_POINTER_UP"
            }
            MotionEvent.ACTION_CANCEL -> {
                action = "ACTION_CANCEL"
                currentBox = null
            }
        }
        Log.i(TAG, "$action at x=${current.x}, y=${current.y}")
        return true
    }


    private fun updateCurrentBox(current: PointF) {
        if (currentBox?.rotation == 0f)
            currentBox?.let {
                it.end = current
            }
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putSerializable("boxen", boxen.toTypedArray()) // ... save stuff

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var st: Parcelable? = null
        if (state is Bundle) // implicit null check
        {
            boxen = (state.getSerializable("boxen") as Array<Box>).toMutableList()
            st = state.getParcelable("superState")
        }
        super.onRestoreInstanceState(st)
    }
}
