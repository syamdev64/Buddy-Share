package com.example.buddyshare.ui.home

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.buddyshare.databinding.QrcodeactivityBinding
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.CaptureManager
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Socket
import kotlin.concurrent.thread

class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var binding: QrcodeactivityBinding
    private var capture: CaptureManager? = null

    private val SERVER_PORT = 8989

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QrcodeactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askStoragePermission()
        setupScanner(savedInstanceState)
        registerWifiConnectionListener()
    }

    /* ------------------------------------------------------------- */
    /*               ZXING QR CODE SCANNER SETUP                    */
    /* ------------------------------------------------------------- */

    private fun setupScanner(savedInstanceState: Bundle?) {
        capture = CaptureManager(this, binding.barcodeScanner)
        capture?.initializeFromIntent(intent, savedInstanceState)
        capture?.decode()

        binding.barcodeScanner.decodeContinuous(BarcodeCallback { result ->
            val qrText = result?.text ?: return@BarcodeCallback

            binding.barcodeScanner.pause()

            if (!qrText.startsWith("WIFI:")) {
                Toast.makeText(this, "Invalid Hotspot QR Code", Toast.LENGTH_LONG).show()
                binding.barcodeScanner.resume()
                return@BarcodeCallback
            }

            // FORMAT: WIFI:T:WPA;S:<SSID>;P:<PASSWORD>;;
            val ssid = qrText.substringAfter("S:").substringBefore(";")
            val password = qrText.substringAfter("P:").substringBefore(";")

            Toast.makeText(this, "Connecting to: $ssid", Toast.LENGTH_SHORT).show()

            connectToHotspot(ssid, password)
        })
    }

    /* ------------------------------------------------------------- */
    /*     CONNECT TO HOTSPOT USING WifiNetworkSuggestion API       */
    /* ------------------------------------------------------------- */

    private fun connectToHotspot(ssid: String, password: String) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val status = wifiManager.addNetworkSuggestions(listOf(suggestion))

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Toast.makeText(this,
                "Tap the popup notification to connect to hotspot!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this,
                "Failed to suggest network: $status",
                Toast.LENGTH_LONG
            ).show()

            binding.barcodeScanner.resume()
        }
    }

    /* ------------------------------------------------------------- */
    /*         DETECT WHEN THE DEVICE CONNECTS TO HOTSPOT           */
    /* ------------------------------------------------------------- */

    private fun registerWifiConnectionListener() {
        val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                val info =
                    intent?.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)

                if (info?.isConnected == true) {
                    val senderIp = getGatewayIp()

                    Toast.makeText(
                        this@QRCodeScannerActivity,
                        "Connected! Receiving files…",
                        Toast.LENGTH_LONG
                    ).show()

                    startFileReceiver(senderIp)
                }
            }
        }, filter)
    }

    /* ------------------------------------------------------------- */
    /*                      FILE RECEIVER SOCKET                    */
    /* ------------------------------------------------------------- */

    private fun startFileReceiver(serverIp: String) {
        thread {
            try {
                val socket = Socket(serverIp, SERVER_PORT)
                val input = DataInputStream(socket.getInputStream())

                val fileCount = input.readInt()

                val downloadDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )

                repeat(fileCount) {
                    val fileName = input.readUTF()
                    val fileSize = input.readLong()

                    val file = File(downloadDir, fileName)
                    val fos = FileOutputStream(file)

                    var remaining = fileSize
                    val buffer = ByteArray(8192)

                    while (remaining > 0) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        fos.write(buffer, 0, bytesRead)
                        remaining -= bytesRead
                    }

                    fos.close()
                }

                runOnUiThread {
                    Toast.makeText(this, "Files received!", Toast.LENGTH_LONG).show()
                    finish()
                }

                socket.close()
                input.close()

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Transfer failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /* ------------------------------------------------------------- */
    /*             AUTO-DETECT ROUTER GATEWAY / SENDER IP           */
    /* ------------------------------------------------------------- */

    private fun getGatewayIp(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiManager.dhcpInfo ?: return "192.168.43.1"

        return String.format(
            "%d.%d.%d.%d",
            dhcp.gateway and 0xFF,
            dhcp.gateway shr 8 and 0xFF,
            dhcp.gateway shr 16 and 0xFF,
            dhcp.gateway shr 24 and 0xFF
        )
    }

    /* ------------------------------------------------------------- */
    /*                          PERMISSIONS                          */
    /* ------------------------------------------------------------- */

    private fun askStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001
                )
            }
        }
    }

    override fun onResume() { super.onResume(); capture?.onResume() }
    override fun onPause() { super.onPause(); capture?.onPause() }
    override fun onDestroy() { super.onDestroy(); capture?.onDestroy() }
}
