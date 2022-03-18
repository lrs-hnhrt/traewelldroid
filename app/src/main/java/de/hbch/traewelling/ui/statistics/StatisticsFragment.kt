package de.hbch.traewelling.ui.statistics

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import de.hbch.traewelling.databinding.FragmentStatisticsBinding
import java.util.*
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.api.models.trip.ProductType
import okhttp3.internal.Version


class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var binding: FragmentStatisticsBinding
    private val operatorChart: BarChart get() = binding.chartStatsOperators
    private val productTypeChart: BarChart get() = binding.chartStatsProductType
    private val dataSetColors = mutableListOf<Int>()

    init {
        dataSetColors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
        dataSetColors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
        dataSetColors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = this@StatisticsFragment.viewModel
            statisticsFragment = this@StatisticsFragment
            lifecycleOwner = this@StatisticsFragment.viewLifecycleOwner
        }

        initChart(operatorChart)
        initChart(productTypeChart)

        viewModel.dateRange.observe(viewLifecycleOwner) {
            requestAndDisplayStatistics()
        }

        return binding.root
    }

    fun selectDateRange() {
        val range = viewModel.dateRange.value ?: Pair(Date(), Date())

        val picker = MaterialDatePicker
            .Builder
            .dateRangePicker()
            .setSelection(
                androidx.core.util.Pair(range.first.time, range.second.time)
            )
            .setTitleText(R.string.title_select_statistics_date_range)
            .build()

        picker.addOnPositiveButtonClickListener { dateRange ->
            val startCalendar =  GregorianCalendar()
            startCalendar.time = Date(dateRange.first)
            val endCalendar = GregorianCalendar()
            endCalendar.time = Date(dateRange.second)

            startCalendar.set(Calendar.HOUR_OF_DAY, 0)
            startCalendar.set(Calendar.MINUTE, 0)
            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
            endCalendar.set(Calendar.MINUTE, 59)

            viewModel
                .dateRange
                .postValue(
                    Pair(
                        startCalendar.time,
                        endCalendar.time
                    )
                )
        }

        picker.show(childFragmentManager, "DateRangePicker")
    }

    private fun initChart(chart: BarChart) {
        chart.axisLeft.setDrawGridLines(false)
        chart.legend.isWordWrapEnabled = true
        chart.description.isEnabled = false
        chart.animateY(500)
        chart.setDrawGridBackground(false)
        chart.xAxis.isEnabled = false
        chart.setScaleEnabled(false)

        if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            chart.legend.textColor = Color.WHITE
            chart.xAxis.textColor = Color.WHITE
            chart.axisLeft.textColor = Color.WHITE
            chart.axisRight.textColor = Color.WHITE
        }
    }

    private fun requestAndDisplayStatistics() {
        viewModel.getPersonalStatisticsForSelectedTimeRange(
            { statistics -> displayStatistics(statistics) },
            {}
        )
    }

    private fun displayStatistics(statistics: PersonalStatistics) {
        val operatorEntries = mutableListOf<IBarDataSet>()
        val productTypeEntries = mutableListOf<IBarDataSet>()

        statistics.operators.forEachIndexed { index, operatorStatistics ->
            val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), operatorStatistics.checkInCount.toFloat())), operatorStatistics.operatorName)
            dataSet.color = dataSetColors[index % dataSetColors.size]
            operatorEntries.add(dataSet)
        }
        statistics.categories.forEachIndexed { index, categoryStatistics ->
            val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), categoryStatistics.checkInCount.toFloat())), ProductType.toString(resources, categoryStatistics.productType))
            dataSet.color = dataSetColors[index % dataSetColors.size]
            productTypeEntries.add(dataSet)
        }

        val operatorData = BarData(operatorEntries)
        val productTypeData = BarData(productTypeEntries)

        operatorChart.data = operatorData
        operatorChart.animateY(500)
        operatorChart.invalidate()
        operatorChart.notifyDataSetChanged()
        productTypeChart.data = productTypeData
        productTypeChart.animateY(500)
        productTypeChart.invalidate()
        productTypeChart.notifyDataSetChanged()

        operatorChart.setDrawGridBackground(false)
        productTypeChart.setDrawGridBackground(false)
    }
}