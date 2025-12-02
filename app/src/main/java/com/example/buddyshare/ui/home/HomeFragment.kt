package com.example.buddyshare.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.buddyshare.R
import com.example.buddyshare.databinding.FragmentHomeBinding
import com.example.buddyshare.ui.home.sendfiles.view.SendFilesActivity


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 1. Create a launcher to get the result from the system file picker.
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = mutableListOf<Uri>()
                if (result.data?.clipData != null) {
                    val clipData = result.data!!.clipData!!
                    for (i in 0 until clipData.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                } else if (result.data?.data != null) {
                    uris.add(result.data!!.data!!)
                }
                if (uris.isNotEmpty()) {
                    launchSendFilesActivity(uris)
                }
            } else {
                Toast.makeText(requireContext(), "No file selected.", Toast.LENGTH_SHORT).show()
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.post {
            animateButtonsIn()
        }
        binding.receiveBtn.setOnClickListener {
            val intent = Intent(requireContext(), QRCodeScannerActivity::class.java)
            startActivity(intent)
        }
        binding.sendBtn.setOnClickListener {
            openFileExplorer()
        }
    }

    private fun launchSendFilesActivity(uris: List<Uri>) {
        val intent = Intent(requireContext(), SendFilesActivity::class.java)
        // We must pass the URIs as Strings because Uri is not directly serializable for older APIs
        val uriStrings = uris.map { it.toString() }
        intent.putStringArrayListExtra("selected_files", ArrayList(uriStrings))
        startActivity(intent)
    }


    // 2. This function creates and launches the standard Android file picker intent.
    private fun openFileExplorer() {
        // Use Intent.ACTION_GET_CONTENT to open the system's file browser.
        val intent = Intent(Intent.ACTION_GET_CONTENT)

        // Set the MIME type to */* to allow selection of all file types.
        intent.type = "*/*"

        // You can allow the user to select multiple files at once.
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        // Launch the intent using the activity result launcher we created.
        filePickerLauncher.launch(intent)
    }


// In HomeFragment.kt

    private fun animateButtonsIn() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.rootLayout)

        // Move Send button horizontally to center
        constraintSet.clear(R.id.sendBtn, ConstraintSet.START)
        constraintSet.clear(R.id.sendBtn, ConstraintSet.END)
        constraintSet.connect(
            R.id.sendBtn,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            64
        )
        constraintSet.connect(
            R.id.sendBtn,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            64
        )

        // Move Receive button horizontally to center
        constraintSet.clear(R.id.receiveBtn, ConstraintSet.START)
        constraintSet.clear(R.id.receiveBtn, ConstraintSet.END)
        constraintSet.connect(
            R.id.receiveBtn,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            64
        )
        constraintSet.connect(
            R.id.receiveBtn,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            64
        )

        // Apply the transition
        val transition = ChangeBounds().apply {
            duration = 800
            interpolator = AnticipateOvershootInterpolator(1.0f)
        }
        TransitionManager.beginDelayedTransition(binding.rootLayout, transition)
        constraintSet.applyTo(binding.rootLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}