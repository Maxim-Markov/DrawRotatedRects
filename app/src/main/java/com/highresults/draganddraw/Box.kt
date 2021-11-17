package com.highresults.draganddraw

import android.graphics.PointF
import java.io.Serializable

class Box(var start: PointF) : Serializable {
    var rotation = 0f
    var end: PointF = start
    val left: Float
        get() = start.x.coerceAtMost(end.x)
    val right: Float
        get() = start.x.coerceAtLeast(end.x)
    val top: Float
        get() = start.y.coerceAtMost(end.y)
    val bottom: Float
        get() = start.y.coerceAtLeast(end.y)
}