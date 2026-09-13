package com.simwarga.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.simwarga.R
import com.simwarga.databinding.ActivityPinBinding
import com.simwarga.util.PinHelper
import com.simwarga.util.ValidationHelper

class PinActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val MODE_CREATE = "mode_create"
        const val MODE_VERIFY = "mode_verify"
    }

    private lateinit var binding: ActivityPinBinding
    private var currentMode = MODE_VERIFY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_VERIFY

        setupView()
        setupListeners()
    }

    private fun setupView() {
        if (currentMode == MODE_CREATE) {
            binding.tvTitle.setText(R.string.pin_buat_judul)
            binding.tvSubtitle.setText(R.string.pin_buat_petunjuk)
            binding.layoutKonfirmasi.visibility = View.VISIBLE
            binding.btnSubmit.setText(R.string.pin_simpan)
        } else {
            binding.tvTitle.setText(R.string.pin_masuk_judul)
            binding.tvSubtitle.setText(R.string.pin_masuk_petunjuk)
            binding.layoutKonfirmasi.visibility = View.GONE
            binding.btnSubmit.setText(R.string.masuk)
        }
    }

    private fun setupListeners() {
        binding.etPin.doAfterTextChanged { binding.tilPin.error = null }
        binding.etKonfirmasiPin.doAfterTextChanged { binding.tilKonfirmasiPin.error = null }

        binding.btnSubmit.setOnClickListener {
            handleAction()
        }

        binding.etPin.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (currentMode == MODE_VERIFY) {
                    handleAction()
                    true
                } else {
                    binding.etKonfirmasiPin.requestFocus()
                    true
                }
            } else false
        }

        binding.etKonfirmasiPin.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                handleAction()
                true
            } else false
        }
    }

    private fun handleAction() {
        val pin = binding.etPin.text?.toString().orEmpty()

        if (!ValidationHelper.isValidPin(pin)) {
            binding.tilPin.error = getString(R.string.pin_format_error)
            return
        }

        if (currentMode == MODE_CREATE) {
            val konfirmasi = binding.etKonfirmasiPin.text?.toString().orEmpty()
            if (pin != konfirmasi) {
                binding.tilKonfirmasiPin.error = getString(R.string.pin_tidak_cocok)
                return
            }
            PinHelper.setPin(this, pin)
            navigateToMain()
        } else {
            if (PinHelper.verifyPin(this, pin)) {
                navigateToMain()
            } else {
                binding.etPin.text?.clear()
                binding.tilPin.error = getString(R.string.pin_salah)
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
