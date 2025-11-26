package com.example.buddyshare.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.buddyshare.databinding.QrcodeactivityBinding
import com.journeyapps.barcodescanner.CaptureManager

class QRCodeScannerActivity: AppCompatActivity() {
    lateinit var binding: QrcodeactivityBinding
    private var capture: CaptureManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=QrcodeactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        capture = CaptureManager(this, binding.barcodeScanner)
        capture?.initializeFromIntent(intent, savedInstanceState)
        capture?.decode()

    }
    override fun onResume() {
        super.onResume()
        capture?.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture?.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture?.onSaveInstanceState(outState)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}