package com.example.myapplication.music


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View

class VisualizerView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private var visualizer: Visualizer? = null
    private var currentWaveform: ByteArray? = null

    init {
        paint.color = Color.BLUE
        paint.strokeWidth = 3f
        paint.isAntiAlias = true
        paint.isAntiAlias = false
    }

    fun startVisualizer(mediaPlayer: MediaPlayer) {
        stopVisualizer()

        val sessionId = mediaPlayer.audioSessionId
        if (sessionId != Visualizer.ERROR_BAD_VALUE) {
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                scalingMode = Visualizer.SCALING_MODE_NORMALIZED
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        currentWaveform = waveform
                        invalidate()
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false)
                enabled = true
            }
        }
    }

    fun stopVisualizer() {
        visualizer?.release()
        visualizer = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2
        val startX = 0
        val endX = width

        val waveform = currentWaveform
        if (waveform != null) {
            for (i in waveform.indices - 1) {
                val y1 = centerY + (waveform[i].toInt() / 128)
                val y2 = centerY + (waveform[i + 1].toInt() / 128)
                canvas.drawLine(startX.toFloat(), y1.toFloat(), endX.toFloat(), y2.toFloat(), paint)
            }
        }
    }
}

