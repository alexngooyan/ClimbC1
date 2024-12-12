package com.example.climbc1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.climbc1.MainActivity
import com.example.climbc1.R
import com.example.climbc1.WorkoutDataDatabase
import com.example.climbc1.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.selects.select

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var sessionHistoryButton: Button
    lateinit var selectSessionButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val teal1 = ContextCompat.getColor(requireContext(), R.color.teal1)
        val black2 = ContextCompat.getColor(requireContext(), R.color.black2)
        val black1 = ContextCompat.getColor(requireContext(), R.color.black)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val grey1 = ContextCompat.getColor(requireContext(), R.color.grey1)
        val orange1 = ContextCompat.getColor(requireContext(), R.color.orange1)
        val orange2 = ContextCompat.getColor(requireContext(), R.color.orange2)
        val lightgrey = ContextCompat.getColor(requireContext(), R.color.lightgrey)

        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        sessionHistoryButton = binding.sessionHistoryButton

        //make visibility of session history scrollable container gone, open session history screen (container)
        sessionHistoryButton.setOnClickListener {
            binding.scrollableCharts.visibility = View.GONE
            binding.sessionHistory.visibility = View.VISIBLE
        }

        val session1Button = binding.TempSession1

        session1Button.setOnClickListener {
            binding.sessionNum1.setBackgroundColor(white)
            binding.sessionDate1.setBackgroundColor(black2)
            binding.sessionTime1.setBackgroundColor(teal1)

            binding.sessionNum1.setTextColor(black1)
            binding.sessionDate1.setTextColor(white)
            binding.sessionTime1.setTextColor(black1)

            //set colors of button2
            binding.sessionNum2.setBackgroundColor(black1)
            binding.sessionDate2.setBackgroundColor(grey1)
            binding.sessionTime2.setBackgroundColor(lightgrey)

            binding.sessionNum2.setTextColor(white)
            binding.sessionDate2.setTextColor(white)
            binding.sessionTime2.setTextColor(black1)

            //set colors of button3
            binding.sessionNum3.setBackgroundColor(black1)
            binding.sessionDate3.setBackgroundColor(grey1)
            binding.sessionTime3.setBackgroundColor(lightgrey)

            binding.sessionNum3.setTextColor(white)
            binding.sessionDate3.setTextColor(white)
            binding.sessionTime3.setTextColor(black1)

            //set colors of button4
            binding.sessionNum4.setBackgroundColor(black1)
            binding.sessionDate4.setBackgroundColor(grey1)
            binding.sessionTime4.setBackgroundColor(lightgrey)

            binding.sessionNum4.setTextColor(white)
            binding.sessionDate4.setTextColor(white)
            binding.sessionTime4.setTextColor(black1)
        }

        val session2Button = binding.TempSession2

        session2Button.setOnClickListener {
            binding.sessionNum2.setBackgroundColor(white)
            binding.sessionDate2.setBackgroundColor(black2)
            binding.sessionTime2.setBackgroundColor(teal1)

            binding.sessionNum2.setTextColor(black1)
            binding.sessionDate2.setTextColor(white)
            binding.sessionTime2.setTextColor(black1)

            //set colors of button1
            binding.sessionNum1.setBackgroundColor(black1)
            binding.sessionDate1.setBackgroundColor(grey1)
            binding.sessionTime1.setBackgroundColor(lightgrey)

            binding.sessionNum1.setTextColor(white)
            binding.sessionDate1.setTextColor(white)
            binding.sessionTime1.setTextColor(black1)

            //set colors of button3
            binding.sessionNum3.setBackgroundColor(black1)
            binding.sessionDate3.setBackgroundColor(grey1)
            binding.sessionTime3.setBackgroundColor(lightgrey)

            binding.sessionNum3.setTextColor(white)
            binding.sessionDate3.setTextColor(white)
            binding.sessionTime3.setTextColor(black1)

            //set colors of button4
            binding.sessionNum4.setBackgroundColor(black1)
            binding.sessionDate4.setBackgroundColor(grey1)
            binding.sessionTime4.setBackgroundColor(lightgrey)

            binding.sessionNum4.setTextColor(white)
            binding.sessionDate4.setTextColor(white)
            binding.sessionTime4.setTextColor(black1)
        }

        val session3Button = binding.TempSession3

        session3Button.setOnClickListener {
            binding.sessionNum3.setBackgroundColor(white)
            binding.sessionDate3.setBackgroundColor(black2)
            binding.sessionTime3.setBackgroundColor(teal1)

            binding.sessionNum3.setTextColor(black1)
            binding.sessionDate3.setTextColor(white)
            binding.sessionTime3.setTextColor(black1)

            //set colors of button2
            binding.sessionNum2.setBackgroundColor(black1)
            binding.sessionDate2.setBackgroundColor(grey1)
            binding.sessionTime2.setBackgroundColor(lightgrey)

            binding.sessionNum2.setTextColor(white)
            binding.sessionDate2.setTextColor(white)
            binding.sessionTime2.setTextColor(black1)

            //set colors of button3
            binding.sessionNum1.setBackgroundColor(black1)
            binding.sessionDate1.setBackgroundColor(grey1)
            binding.sessionTime1.setBackgroundColor(lightgrey)

            binding.sessionNum1.setTextColor(white)
            binding.sessionDate1.setTextColor(white)
            binding.sessionTime1.setTextColor(black1)

            //set colors of button4
            binding.sessionNum4.setBackgroundColor(black1)
            binding.sessionDate4.setBackgroundColor(grey1)
            binding.sessionTime4.setBackgroundColor(lightgrey)

            binding.sessionNum4.setTextColor(white)
            binding.sessionDate4.setTextColor(white)
            binding.sessionTime4.setTextColor(black1)
        }

        val session4Button = binding.TempSession4

        session4Button.setOnClickListener {
            binding.sessionNum4.setBackgroundColor(white)
            binding.sessionDate4.setBackgroundColor(black2)
            binding.sessionTime4.setBackgroundColor(teal1)

            binding.sessionNum4.setTextColor(black1)
            binding.sessionDate4.setTextColor(white)
            binding.sessionTime4.setTextColor(black1)

            //set colors of button2
            binding.sessionNum2.setBackgroundColor(black1)
            binding.sessionDate2.setBackgroundColor(grey1)
            binding.sessionTime2.setBackgroundColor(lightgrey)

            binding.sessionNum2.setTextColor(white)
            binding.sessionDate2.setTextColor(white)
            binding.sessionTime2.setTextColor(black1)

            //set colors of button3
            binding.sessionNum3.setBackgroundColor(black1)
            binding.sessionDate3.setBackgroundColor(grey1)
            binding.sessionTime3.setBackgroundColor(lightgrey)

            binding.sessionNum3.setTextColor(white)
            binding.sessionDate3.setTextColor(white)
            binding.sessionTime3.setTextColor(black1)

            //set colors of button4
            binding.sessionNum1.setBackgroundColor(black1)
            binding.sessionDate1.setBackgroundColor(grey1)
            binding.sessionTime1.setBackgroundColor(lightgrey)

            binding.sessionNum1.setTextColor(white)
            binding.sessionDate1.setTextColor(white)
            binding.sessionTime1.setTextColor(black1)
        }

        selectSessionButton = binding.selectButton

        selectSessionButton.setOnClickListener {
            binding.scrollableCharts.visibility = View.VISIBLE
            binding.sessionHistory.visibility = View.GONE
        }

        setChartRoutine()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChartRoutine() {
        val mainActivity = activity as? MainActivity
        mainActivity?.setChartData(binding.lineChart0,0, null)
        mainActivity?.setChartData(binding.lineChart1, 1, null)
        mainActivity?.setChartData(binding.lineChart2, 2, null)
        mainActivity?.setChartData(binding.lineChart3, 3, null)
    }
}