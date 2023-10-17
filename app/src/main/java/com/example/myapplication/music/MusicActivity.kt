package com.example.myapplication.music

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlinx.coroutines.*

class MusicActivity : AppCompatActivity() {
    private lateinit var playButton: Button
    private var musicService: MusicService? = null
    private var isBound = false
    private val musicUrl = "https://xx.show-music.ir/archive/M/Moein/Moein%20-%20Lahzeha/01%20Gozashteh.mp3"

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicService.LocalBinder
            musicService = binder.service
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            musicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        playButton = findViewById<Button>(R.id.play_button).apply {
            setOnClickListener {
                musicService?.playAudio(musicUrl)
            }
        }

        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        musicService?.onMediaPlayerPreparedListener = { mediaPlayer ->
            findViewById<VisualizerView>(R.id.visualizer_view).startVisualizer(mediaPlayer)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        musicService?.onMediaPlayerPreparedListener = null
        findViewById<VisualizerView>(R.id.visualizer_view).stopVisualizer()
    }
}