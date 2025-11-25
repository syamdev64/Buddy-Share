package com.example.buddyshare.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.buddyshare.R
import com.example.buddyshare.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

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
    }
    private fun animateButtonsIn() {
        val constraintSet = ConstraintSet()
        // Clone the final constraints from the root layout
        constraintSet.clone(binding.rootLayout)

        // Clear the horizontal constraints for sendBtn and connect it to the parent
        constraintSet.clear(R.id.sendBtn, ConstraintSet.START)
        constraintSet.clear(R.id.sendBtn, ConstraintSet.END)
        constraintSet.connect(R.id.sendBtn, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 32) // 32dp margin
        constraintSet.connect(R.id.sendBtn, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 32) // 32dp margin

        // Clear the horizontal constraints for receiveBtn and connect it to sendBtn
        constraintSet.clear(R.id.receiveBtn, ConstraintSet.START)
        constraintSet.clear(R.id.receiveBtn, ConstraintSet.END)
        constraintSet.connect(R.id.receiveBtn, ConstraintSet.START, R.id.sendBtn, ConstraintSet.START)
        constraintSet.connect(R.id.receiveBtn, ConstraintSet.END, R.id.sendBtn, ConstraintSet.END)

        // Create and configure the transition
        val transition = ChangeBounds()
        transition.duration = 800 // Animation duration in milliseconds
        transition.interpolator = AnticipateOvershootInterpolator(1.0f)

        // Begin the transition
        TransitionManager.beginDelayedTransition(binding.rootLayout, transition)
        // Apply the new constraints
        constraintSet.applyTo(binding.rootLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}