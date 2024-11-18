package com.example.climbc1.ui.notifications

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.climbc1.R
import com.example.climbc1.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPref = requireContext().getSharedPreferences("ThresholdPreferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Retrieve the stored hint values and set them if they exist
        val a2Threshold1 = sharedPref.getString("a2Threshold1", "--")
        val a2Threshold2 = sharedPref.getString("a2Threshold2", "--")
        val a4Threshold1 = sharedPref.getString("a4Threshold1", "--")
        val a4Threshold2 = sharedPref.getString("a4Threshold2", "--")

        // Set the hints of the fields
        binding.A2ThresholdField1.hint = a2Threshold1
        binding.A2ThresholdField2.hint = a2Threshold2
        binding.A4ThresholdField1.hint = a4Threshold1
        binding.A4ThresholdField2.hint = a4Threshold2



        val teal1 = ContextCompat.getColor(requireContext(), R.color.teal1)
        val black2 = ContextCompat.getColor(requireContext(), R.color.black2)
        val black1 = ContextCompat.getColor(requireContext(), R.color.black)
        val white = ContextCompat.getColor(requireContext(), R.color.white)

        val updateButton: Button = binding.thresholdUpdateButton

        updateButton.setBackgroundColor(teal1)
        updateButton.setTextColor(black1)

        //initialize all editText fields from threshold settings
        val a2ThresholdField1 = binding.A2ThresholdField1
        val a2ThresholdField2 = binding.A2ThresholdField2
        val a4ThresholdField1 = binding.A4ThresholdField1
        val a4ThresholdField2 = binding.A4ThresholdField2

        a2ThresholdField1.setTextColor(teal1)
        a2ThresholdField2.setTextColor(teal1)
        a4ThresholdField1.setTextColor(teal1)
        a4ThresholdField2.setTextColor(teal1)

        updateButton.setOnClickListener {
            // Get the text from each field
            val a2Threshold1Temp = binding.A2ThresholdField1.text.toString()
            val a2Threshold2Temp = binding.A2ThresholdField2.text.toString()
            val a4Threshold1Temp = binding.A4ThresholdField1.text.toString()
            val a4Threshold2Temp = binding.A4ThresholdField2.text.toString()

            //set new defaults to the old threshold, check if valid/empty
            if(a2Threshold1Temp != "" && a2Threshold1Temp.toInt() >= 0 && a2Threshold1Temp.toInt() < 100) {
                //a2ThresholdField1.hint = a2Threshold1
                binding.A2ThresholdField1.hint = a2Threshold1Temp
                editor.putString("a2Threshold1", a2Threshold1Temp)
            } //else hint should stay as whatever the hell it was before lol

            if(a2Threshold2Temp != "" && a2Threshold2Temp.toInt() >= 0 && a2Threshold2Temp.toInt() < 100) {
                //a2ThresholdField2.hint = a2Threshold2
                binding.A2ThresholdField2.hint = a2Threshold2Temp
                editor.putString("a2Threshold2", a2Threshold2Temp)
            }

            if(a4Threshold1Temp != "" && a4Threshold1Temp.toInt() >= 0 && a4Threshold1Temp.toInt() < 100) {
                binding.A4ThresholdField1.hint = a4Threshold1Temp
                editor.putString("a4Threshold1", a4Threshold1Temp)
            }

            if(a4Threshold2Temp != "" && a4Threshold2Temp.toInt() >= 0 && a4Threshold2Temp.toInt() < 100) {
                //a4ThresholdField2.hint = a4Threshold2
                binding.A4ThresholdField2.hint = a4Threshold2Temp //testing
                editor.putString("a4Threshold2", a4Threshold2Temp)
            }

            editor.apply()

            //clear entries
            a2ThresholdField1.setText("")
            a2ThresholdField2.setText("")
            a4ThresholdField1.setText("")
            a4ThresholdField2.setText("")

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}