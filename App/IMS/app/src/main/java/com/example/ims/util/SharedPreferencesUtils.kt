package com.example.ims.util

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun localDateToString(date: LocalDate): String {
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun stringToLocalDate(dateString: String): LocalDate {
    return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
}

fun saveSelectedDates(context: Context, selectedDates: MutableList<LocalDate>) {
    val sharedPreferences = context.getSharedPreferences("selected_dates", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val dateSet = selectedDates.map { localDateToString(it) }.toHashSet()

    editor.putStringSet("dates", dateSet)
    editor.apply()
}

fun loadSelectedDates(context: Context): MutableList<LocalDate> {
    val sharedPreferences = context.getSharedPreferences("selected_dates", Context.MODE_PRIVATE)
    val dateSet = sharedPreferences.getStringSet("dates", emptySet()) ?: emptySet()
    return dateSet.map { stringToLocalDate(it) }.toMutableList()
}
