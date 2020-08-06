package com.example.findme

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.floor
import kotlin.random.Random

class SpotLightImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var paint = Paint()
    private var shouldDrawSpotLight = false
    private var gameOver = false

    private lateinit var winnerRect: RectF
    private var androidBitmapX = 0f
    private var androidBitmapY = 0f

    private val bitmapAndroid = BitmapFactory.decodeResource(resources, R.drawable.android)
    private val spotlight = BitmapFactory.decodeResource(resources, R.drawable.mask)

    private lateinit var shader: Shader
    private val shaderMatrix = Matrix()
    init {
        val bitmap = Bitmap.createBitmap(spotlight.width, spotlight.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val shaderPaint = Paint().apply { isAntiAlias = true }
        shaderPaint.color = Color.BLACK
        canvas.drawRect(0f, 0f, spotlight.width.toFloat(), spotlight.height.toFloat(), shaderPaint)
        shaderPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        canvas.drawBitmap(spotlight, 0f, 0f, shaderPaint)
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
    }

    private fun setupWinnerRect() {
        androidBitmapX = floor(Random.nextFloat() * (width - bitmapAndroid.width))
        androidBitmapY = floor(Random.nextFloat() * (height - bitmapAndroid.height))

        winnerRect = RectF(
            androidBitmapX,
            androidBitmapY,
            androidBitmapX + bitmapAndroid.width,
            androidBitmapY + bitmapAndroid.height
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupWinnerRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmapAndroid, androidBitmapX, androidBitmapY, paint)
        if(!gameOver) {
            if(shouldDrawSpotLight)
                canvas.drawRect(0f,0f,width.toFloat(), height.toFloat(), paint)
            else
                canvas.drawColor(Color.BLACK)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val motionEventX = event.x
        val motionEventY = event.y
        when(event.action){
            MotionEvent.ACTION_DOWN -> findingTheObject()
            MotionEvent.ACTION_UP -> stoppedFindingTheObject(motionEventX, motionEventY)
        }
        moveTheShader(motionEventX, motionEventY)
        invalidate()
        return true
    }

    private fun moveTheShader(motionEventX: Float, motionEventY: Float) {
        shaderMatrix.setTranslate(
            motionEventX - spotlight.width / 2f,
            motionEventY - spotlight.height / 2f
        )
        shader.setLocalMatrix(shaderMatrix)
    }

    private fun stoppedFindingTheObject(motionEventX: Float, motionEventY: Float) {
        shouldDrawSpotLight = false
        gameOver = winnerRect.contains(motionEventX, motionEventY)
    }

    private fun findingTheObject() {
        shouldDrawSpotLight = true
        if(gameOver) {
            gameOver = false
            setupWinnerRect()
        }
    }
}