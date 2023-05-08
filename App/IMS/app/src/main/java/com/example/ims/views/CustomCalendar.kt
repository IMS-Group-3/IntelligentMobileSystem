package com.example.ims.views

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.children
import com.example.ims.R
import com.example.ims.util.loadSelectedDates
import com.example.ims.util.saveSelectedDates
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*



class CustomCalendar(private val calendarView: CalendarView,  private val monthTitleTextView: TextView, private val context: Context) {
    private val selectedDates = mutableListOf<LocalDateTime>()

    init {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val daysOfWeek = daysOfWeek()

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)
        calendarView.setup(startMonth, endMonth, WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        calendarView.monthScrollListener = { visibleMonth ->
            updateMonthTitle(visibleMonth.yearMonth)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            textView.text = title

                            updateMonthTitle(currentMonth)
                        }
                }
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                val container = DayViewContainer(view)

                container.textView.setOnClickListener {
                    val day = container.day
                    if (day.position == DayPosition.MonthDate) {
                        val selectedDateTime = selectedDates.find { it.toLocalDate() == day.date }
                        if (selectedDates.any { it.toLocalDate() == day.date }) {

                            if (selectedDateTime != null) {
                                showDateTimeDialog(day.date, selectedDateTime.toLocalTime())
                            }

                        } else {
                            // Show TimePickerDialog
                            val currentTime = LocalTime.now()
                            val timePickerDialog = TimePickerDialog(
                                context,
                                R.style.CustomTimePickerDialog,
                                { _, hourOfDay, minute ->
                                    val selectedTime = LocalTime.of(hourOfDay, minute)
                                    // Combine selected date and time
                                    val selectedDateTime = day.date.atTime(selectedTime)
                                    // Add the selected date with the time to the list
                                    selectedDates.add(selectedDateTime)
                                    // Reload the newly selected date so the dayBinder is called
                                    calendarView.notifyDateChanged(day.date)
                                    saveSelectedDates(context, selectedDates)
                                },
                                currentTime.hour,
                                currentTime.minute,
                                true
                            )
                            timePickerDialog.show()
                        }
                    }
                }

                return container
            }

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val day = data
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.position == DayPosition.MonthDate) {
                    textView.visibility = View.VISIBLE
                    if (selectedDates.any { it.toLocalDate() == day.date }) {
                        textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.calendar_date_selection_background)
                    } else {
                        textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                }
            }
        }

        val loadedDates = loadSelectedDates(context)
        selectedDates.addAll(loadedDates)
        loadedDates.forEach { calendarView.notifyDateChanged(it.toLocalDate()) }
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        val monthTitleTextView = monthTitleTextView.findViewById<TextView>(R.id.monthTitle)
        val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val monthTitle = "$monthName ${yearMonth.year}"
        monthTitleTextView.text = monthTitle
    }
    private fun showDateTimeDialog(date: LocalDate, time: LocalTime) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_date_time)
        dialog.setCancelable(false)

        val selectedTimeText: TextView = dialog.findViewById(R.id.selected_time_text)
        val cancelButton: Button = dialog.findViewById(R.id.cancel_date_button)
        val closeButton: ImageButton = dialog.findViewById(R.id.close_dialog_button)

        selectedTimeText.text = "${time.format(DateTimeFormatter.ofPattern("hh:mm a"))}"

        cancelButton.setOnClickListener {
            selectedDates.removeAll { it.toLocalDate() == date }
            calendarView.notifyDateChanged(date)
            saveSelectedDates(context, selectedDates)
            dialog.dismiss()
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


}
