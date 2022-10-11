package com.spozebra.dhl_licenseplate_tag_poc.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


class CircleView: View {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private lateinit var circleIndicator: Paint
    private var centerX = 0
    private var centerY: Int = 0

    private var currentRadius: Float = 0f
    private var maxRadius: Float = 0f
    private var minRadius: Float = 0f

    private fun init() {
        circleIndicator = Paint(Paint.ANTI_ALIAS_FLAG)
        circleIndicator.color = Color.parseColor("#D40511")
        circleIndicator.style = Paint.Style.FILL
    }

    fun updateRadius(distance: Int){
        val newRadius = ((maxRadius * distance) / 100) + minRadius;

        val valueAnimator : ValueAnimator= ValueAnimator.ofFloat(currentRadius, newRadius);
        valueAnimator.duration = 200
        valueAnimator.repeatCount = 0;
        valueAnimator.addUpdateListener { animator ->
            currentRadius = animator!!.animatedValue as Float
            // Clear canvas and draw next frame
            invalidate()
        }

        valueAnimator.start();
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)
        this.setBackgroundColor(Color.TRANSPARENT)

        centerX = canvas.width / 2
        centerY = canvas.height / 2
        maxRadius = ((Math.min(centerX, centerY) * 80) / 100).toFloat()
        minRadius = Math.min(centerX, centerY).toFloat() - maxRadius

        if(currentRadius == 0f)
            currentRadius = minRadius

        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), currentRadius,
            circleIndicator!!
        )
    }
}