package com.example.ims.util

import android.content.Context
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun localDateTimeToString(dateTime: LocalDateTime): String {
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun stringToLocalDateTime(dateTimeString: String): LocalDateTime {
    return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}


fun saveSelectedDates(context: Context, selectedDates: MutableList<LocalDateTime>) {
    val sharedPreferences = context.getSharedPreferences("selected_dates", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val dateSet = selectedDates.map { localDateTimeToString(it) }.toHashSet()
    editor.putStringSet("dates", dateSet)
    editor.apply()
}

fun loadSelectedDates(context: Context): MutableList<LocalDateTime> {
    val sharedPreferences = context.getSharedPreferences("selected_dates", Context.MODE_PRIVATE)
    val dateSet = sharedPreferences.getStringSet("dates", emptySet()) ?: emptySet()
    Log.e("Loaded DateTimes", "Loaded date-time strings: $dateSet")
    return dateSet.map { stringToLocalDateTime(it) }.toMutableList()
}

