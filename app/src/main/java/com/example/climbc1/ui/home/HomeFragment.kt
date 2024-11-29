package com.example.climbc1.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.climbc1.databinding.FragmentHomeBinding
import kotlin.math.max
import kotlin.math.min

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        val sharedPref = requireContext().getSharedPreferences("ThresholdPreferences", Context.MODE_PRIVATE)
        //val editor = sharedPref.edit()

        val a2Threshold1 = sharedPref.getString("a2Threshold1", "--")
        val a2Threshold2 = sharedPref.getString("a2Threshold2", "--")
        val a4Threshold1 = sharedPref.getString("a4Threshold1", "--")
        val a4Threshold2 = sharedPref.getString("a4Threshold2", "--")

        //cheese way of getting around kotlin null exception rules
        var a2Threshold1ToDisplay = 0

        if (!a2Threshold1.isNullOrEmpty()) {
            val a2Threshold1Value = a2Threshold1.toIntOrNull()
            if (a2Threshold1Value != null && a2Threshold1Value >= 0) {
                a2Threshold1ToDisplay = a2Threshold1.toInt()
            }
        }

        var a2Threshold2ToDisplay = 0

        if (!a2Threshold2.isNullOrEmpty()) {
            val a2Threshold2Value = a2Threshold2.toIntOrNull()
            if (a2Threshold2Value != null && a2Threshold2Value >= 0) {
                a2Threshold2ToDisplay = a2Threshold2.toInt()
            }
        }

        var a4Threshold1ToDisplay = 0

        if (!a4Threshold1.isNullOrEmpty()) {
            val a4Threshold1Value = a4Threshold1.toIntOrNull()
            if (a4Threshold1Value != null && a4Threshold1Value >= 0) {
                a4Threshold1ToDisplay = a4Threshold1.toInt()
            }
        }

        var a4Threshold2ToDisplay = 0

        if (!a4Threshold2.isNullOrEmpty()) {
            val a4Threshold2Value = a4Threshold2.toIntOrNull()
            if (a4Threshold2Value != null && a4Threshold2Value >= 0) {
                a4Threshold2ToDisplay = a4Threshold2.toInt()
            }
        }

        val a2ThresholdDisplayed = min(a2Threshold1ToDisplay, a2Threshold2ToDisplay)
        val a4ThresholdDisplayed = min(a4Threshold1ToDisplay, a4Threshold2ToDisplay)

        binding.A2ThresholdText.text = a2ThresholdDisplayed.toString()
        binding.A4ThresholdText.text = a4ThresholdDisplayed.toString()

        val maxA2ToDisplay = sharedPref.getInt("maxA2", 0)
        val maxA4ToDisplay = sharedPref.getInt("maxA4", 0)

        binding.A2MaxText.text = maxA2ToDisplay.toString()
        binding.A4MaxText.text = maxA4ToDisplay.toString()


        /*former code to change text on home to "fuck rectangles"
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        */

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}