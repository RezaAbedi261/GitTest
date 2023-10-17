package com.example.myapplication.music


import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import java.io.IOException

class MusicService : Service() {
    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    var onMediaPlayerPreparedListener: ((MediaPlayer) -> Unit)? = null

    inner class LocalBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun playAudio(url: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer?.reset()
        }

        mediaPlayer?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            try {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { mp ->
                    start()
                    onMediaPlayerPreparedListener?.invoke(mp)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
