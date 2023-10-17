package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityFlowsTestBinding
import com.google.android.material.snackbar.Snackbar
import com.snappbox.passenger.SnappboxMainActivity

class FlowsTestActivity : AppCompatActivity() {


    private val KEY_TOKEN = "token"
    private val KEY_PAYLOAD = "payload"
    private val KEY_LOCALE = "locale"
    private val KEY_DEEP_LINK = "deep_link"
    private val KEY_SERVICE_ID = "service_id"
    private val KEY_APP_VERSION = "app_version"
    private var _binding: ActivityFlowsTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlowsTestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFlowsTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLiveData.setOnClickListener {
            viewModel.triggerLiveData()
        }

        binding.btnFlow.setOnClickListener {
            viewModel.triggerFlow()
        }

        binding.btnStateFlow.setOnClickListener {
            viewModel.triggerStateFlow()
        }

        binding.btnSharedFlow.setOnClickListener {
            viewModel.triggerSharedFlow()
        }

        binding.snappBoxStarter.setOnClickListener {
            val token = binding.token.text.toString()
            val bundle = Bundle()
            bundle.putString(KEY_TOKEN, token)
            bundle.putString(KEY_LOCALE, "fa-IR")
            bundle.putString(KEY_PAYLOAD, null)
            bundle.putString(KEY_DEEP_LINK, "https://app.snpb.ir/auth/jek?deliveryCategory=bike-without-box&serviceId=5&token=DL.Rj26swDj%3APhSwzEYnZurHFT79_PGdV-_AETIu-SO_5H_orbZnaEdC6lARSy745LDEbu6Cn9DwU2DSp_WAxEK0Ug")
            bundle.putInt(KEY_SERVICE_ID, 5)
            bundle.putString(KEY_APP_VERSION, "8.1.4")
            startActivity(Intent(this, SnappboxMainActivity::class.java),bundle)
        }




    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}