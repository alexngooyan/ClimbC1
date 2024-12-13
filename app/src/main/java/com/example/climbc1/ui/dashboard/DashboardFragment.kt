package com.example.climbc1.ui.dashboard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.climbc1.MainActivity
import com.example.climbc1.R
import com.example.climbc1.WorkoutDataDatabase
import com.example.climbc1.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var sessionHistoryButton: Button
    lateinit var selectSessionButton: Button

    private var currentSelectedSessionFrame: FrameLayout? = null
    private var currentSelectedSession: Int? = null


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

        tempSessionUpdater()

        selectSessionButton = binding.selectButton

        selectSessionButton.setOnClickListener {
            binding.scrollableCharts.visibility = View.VISIBLE
            binding.sessionHistory.visibility = View.GONE
            setChartRoutine()
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
        mainActivity?.setChartData(binding.lineChart0,0, currentSelectedSession)
        mainActivity?.setChartData(binding.lineChart1, 1, currentSelectedSession)
        mainActivity?.setChartData(binding.lineChart2, 2, currentSelectedSession)
        mainActivity?.setChartData(binding.lineChart3, 3, currentSelectedSession)
    }

    private fun tempSessionUpdater() {
        val db = WorkoutDataDatabase.getDatabase(requireContext())

        val container = binding.tempSessionContainer

        val numberOfFrames = 10 // Replace this with your derived variable

        val jetBrainsFont = ResourcesCompat.getFont(requireContext(), R.font.jetbrains_mono_regular)

        val db0 = CoroutineScope(Dispatchers.Main).launch {
            val sessions = db.dao.getWorkoutSessions()

            for (i in sessions) {
                val frameLayout = FrameLayout(requireContext()).apply {
                    id = View.generateViewId() // Generate a unique ID for each FrameLayout
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(16, 100, 16, 0) // Optional: Add margins
                    }
                    setTag(i)

                    setOnClickListener {
                        // Reset all other FrameLayouts to their default colors
                        if (currentSelectedSessionFrame != null) {
                            currentSelectedSessionFrame.let { selectedFrameLayout ->
                                if (selectedFrameLayout != null) {
                                    for (j in 0 until selectedFrameLayout.childCount) {
                                        val selectedChild = selectedFrameLayout.getChildAt(j) as? TextView
                                        selectedChild?.let { textView ->
                                            resetColors(textView)
                                        }
                                    }
                                }
                            }
                        }

                        // Loop through all child views in the clicked FrameLayout (TextViews)
                        for (j in 0 until childCount) {
                            val child = getChildAt(j) as? TextView
                            child?.let { textView ->
                                // Invert colors: change background and text colors
                                val currentTextColor = textView.currentTextColor
                                val currentBackgroundColor = (textView.background as? ColorDrawable)?.color

                                // Set the new colors by swapping the current background and text colors
                                textView.setTextColor(currentBackgroundColor ?: ContextCompat.getColor(requireContext(), R.color.black))
                                textView.setBackgroundColor(currentTextColor)
                            }
                        }

                        // Update the current selected FrameLayout
                        currentSelectedSessionFrame = this
                        currentSelectedSession = getTag() as? Int
                    }

                }

                val titleTextView = TextView(requireContext()).apply {
                    text = "SESSION $i"
                    typeface = jetBrainsFont
                    textSize = 40f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(0,0,0,170)
                    }
                    tag = "sessionTitle"
                }
                titleTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white)) //white
                titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // black

                val timeHandleTextView = TextView(requireContext()).apply {
                    text = "TIME:"
                    typeface = jetBrainsFont
                    textSize = 25f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(0,170,520,0)
                    }
                    tag = "timeLabel"
                }
                timeHandleTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black2))
                timeHandleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                val time = db.dao.getMaxTimeFromWorkoutID(i)

                val timeTextView = TextView(requireContext()).apply {
                    val timeAsDouble: Double = time.toDouble() / 10
                    text = timeAsDouble.toString()
                    typeface = jetBrainsFont
                    textSize = 25f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(520,170,0,0)
                    }
                    tag = "timeValue"
                }
                timeTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal1))
                timeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

                frameLayout.addView(titleTextView)
                frameLayout.addView(timeHandleTextView)
                frameLayout.addView(timeTextView)

                container.addView(frameLayout)
            }
        }
    }

    private fun resetColors(textView: TextView) {
        when (textView.tag) {
            "sessionTitle" -> {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white)) // white
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // black
            }
            "timeLabel" -> {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black2)) // black2
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // white
            }
            "timeValue" -> {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal1)) // teal1
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // black
            }
        }
    }
}