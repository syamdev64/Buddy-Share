package com.example.buddyshare.ui.home.sendfiles.view

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buddyshare.databinding.SendfilesactivityBinding
import com.example.buddyshare.ui.home.sendfiles.adapter.SelectedFilesAdapter
import com.example.buddyshare.ui.home.sendfiles.model.SelectedFilesModel
import java.io.DataOutputStream
import java.io.InputStream
import java.net.ServerSocket
import kotlin.concurrent.thread
import android.database.Cursor

class SendFilesActivity : AppCompatActivity() {

    private lateinit var binding: SendfilesactivityBinding
    private lateinit var adapter: SelectedFilesAdapter
    private val selectedFiles = mutableListOf<SelectedFilesModel>()

    private val SERVER_PORT = 8989

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SendfilesactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        loadIncomingFiles()

        binding.confirmSendButton.setOnClickListener {
            openHotspotSettings()
        }
    }

    /*-------------------------------------------------------------*/
    /*              OPEN SYSTEM HOTSPOT SETTINGS (Option C)        */
    /*-------------------------------------------------------------*/

    private fun openHotspotSettings() {
        startActivity(Intent("android.settings.WIFI_AP_SETTINGS"))

        Toast.makeText(
            this,
            "Enable hotspot and show the system QR code to the receiver.",
            Toast.LENGTH_LONG
        ).show()

        // Wait for user to enable hotspot then start server
        Handler(Looper.getMainLooper()).postDelayed({
            startServerSocket()
        }, 5000)
    }

    /*-------------------------------------------------------------*/
    /*                     SAMSUNG HOTSPOT FIX                     */
    /*-------------------------------------------------------------*/

    private fun bindProcessToHotspotNetwork() {
        try {
            val cm = getSystemService(ConnectivityManager::class.java)

            for (network in cm.allNetworks) {
                val caps = cm.getNetworkCapabilities(network)

                // Hotspot/tethering interface is TRANSPORT_WIFI
                if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    cm.bindProcessToNetwork(network)
                    break
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*-------------------------------------------------------------*/
    /*                     FILE TRANSFER SERVER                    */
    /*-------------------------------------------------------------*/

    private fun startServerSocket() {
        thread {

            // ⭐ IMPORTANT: Samsung hotspot isolation fix
            bindProcessToHotspotNetwork()

            try {
                val serverSocket = ServerSocket(SERVER_PORT)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Waiting for receiver to connect...",
                        Toast.LENGTH_LONG
                    ).show()
                }

                val client = serverSocket.accept()

                runOnUiThread {
                    Toast.makeText(this, "Receiver connected! Sending files…", Toast.LENGTH_SHORT).show()
                }

                val output = DataOutputStream(client.getOutputStream())

                // Number of files
                output.writeInt(selectedFiles.size)

                // Send all files
                for (file in selectedFiles) {
                    output.writeUTF(file.name)
                    output.writeLong(file.size)

                    val input: InputStream? = contentResolver.openInputStream(file.uri)
                    input?.copyTo(output)
                    input?.close()
                }

                output.flush()
                client.close()
                serverSocket.close()

                runOnUiThread {
                    Toast.makeText(this, "Files sent successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error sending files: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /*-------------------------------------------------------------*/
    /*                     FILE PROCESSING                         */
    /*-------------------------------------------------------------*/

    private fun setupRecycler() {
        adapter = SelectedFilesAdapter(selectedFiles)
        binding.sendFilesRecyclerview.adapter = adapter
    }

    private fun loadIncomingFiles() {
        val uriStrings = intent.getStringArrayListExtra("selected_files") ?: return
        uriStrings.map { Uri.parse(it) }.forEach { uri ->
            getFileInfo(uri)?.let { selectedFiles.add(it) }
        }
        adapter.notifyDataSetChanged()
    }

    private fun getFileInfo(uri: Uri): SelectedFilesModel? {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                val name = it.getString(nameIdx)
                val size = it.getLong(sizeIdx)
                return SelectedFilesModel(uri, name, size)
            }
        }
        return null
    }
}
