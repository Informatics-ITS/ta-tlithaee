package com.example.tugasakhir.presentation.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class EditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rulerStartX = 0f
    private var rulerStartY = 0f
    private var rulerEndX = 0f
    private var rulerEndY = 0f
    private var currentMode: EditMode = EditMode.NONE
    private var isDrawingRuler = false
    private var annotationBitmap: Bitmap? = null
    private var emptyCanvas: Canvas? = null
    private var lastX = 0f
    private var lastY = 0f
    private val currentPath = Path()
    private var hasCommitted = false
    private var baseBitmap: Bitmap? = null
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        alpha = 255
    }

    fun setEditMode(mode: EditMode) {
        currentMode = mode
        currentPath.reset()
        hasCommitted = false
        isDrawingRuler = false
        invalidate()
    }

    fun setEmptyCanvas(bitmap: Bitmap) {
        annotationBitmap = bitmap
        emptyCanvas = Canvas(bitmap)
//        Log.d("PenView", "Ukuran view = $width x $height, bitmap = ${bitmap.width} x ${bitmap.height}")
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (currentMode == EditMode.NONE) return false

        when (currentMode) {
            EditMode.PEN, EditMode.ERASER -> handlePenOrEraser(event)
            EditMode.RULER -> handleRuler(event)
            else -> {}
        }

        invalidate()
        return true
    }

    private fun handlePenOrEraser(event: MotionEvent) {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f)
                lastX = x
                lastY = y
            }
        }
    }

    private fun handleRuler(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rulerStartX = event.x
                rulerStartY = event.y
                rulerEndX = event.x
                rulerEndY = event.y
                isDrawingRuler = true
            }
            MotionEvent.ACTION_MOVE -> {
                rulerEndX = event.x
                rulerEndY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val bmp = annotationBitmap ?: return
                val canvas = emptyCanvas ?: return

                val scaleX = bmp.width.toFloat() / width
                val scaleY = bmp.height.toFloat() / height

                val scaledPaint = Paint(paint).apply {
                    strokeWidth = paint.strokeWidth * scaleX
                }

                canvas.drawLine(
                    rulerStartX * scaleX,
                    rulerStartY * scaleY,
                    rulerEndX * scaleX,
                    rulerEndY * scaleY,
                    scaledPaint
                )

                isDrawingRuler = false
            }
        }
    }

    fun setBaseBitmap(bitmap: Bitmap) {
        baseBitmap = bitmap
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        baseBitmap?.let {
            val srcRect = Rect(0, 0, it.width, it.height)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(it, srcRect, dstRect, null)
        }

        annotationBitmap?.let {
            val srcRect = Rect(0, 0, it.width, it.height)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(it, srcRect, dstRect, null)
        }

        if (currentMode == EditMode.RULER && isDrawingRuler) {
            canvas.drawLine(rulerStartX, rulerStartY, rulerEndX, rulerEndY, paint)


        }

        // Gambarkan jalur saat eraser atau pen sedang digunakan
        if (currentMode == EditMode.PEN) {
            canvas.drawPath(currentPath, paint)
        } else if (currentMode == EditMode.ERASER) {
            val previewPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 20f
                pathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)
            }
            canvas.drawPath(currentPath, previewPaint)
        }
//        if (currentMode == EditMode.RULER && isDrawingRuler) {
//            canvas.drawLine(rulerStartX, rulerStartY, rulerEndX, rulerEndY, paint)
//        } else if (currentMode == EditMode.ERASER || currentMode == EditMode.PEN) {
//            if (currentMode == EditMode.PEN) {
//                canvas.drawPath(currentPath, paint)
//            }
//        }
    }

    fun commitDrawingToBitmapDirect() {
        val bmp = annotationBitmap ?: return
        if (currentPath.isEmpty) return

        val scaleX = bmp.width.toFloat() / width
        val scaleY = bmp.height.toFloat() / height

        val matrix = Matrix().apply { setScale(scaleX, scaleY) }
        val transformedPath = Path(currentPath).apply { transform(matrix) }

        if (currentMode == EditMode.ERASER) {
            val canvas = emptyCanvas ?: return
            val scaledEraser = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 20f * scaleX
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
            canvas.drawPath(transformedPath, scaledEraser)
        }
        else {
            val canvas = emptyCanvas ?: return
            val scaledPaint = Paint(paint).apply {
                strokeWidth = paint.strokeWidth * scaleX
            }
            canvas.drawPath(transformedPath, scaledPaint)
        }

        currentPath.reset()
        hasCommitted = true
        invalidate()
    }

}
