package com.barapp.util.format

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class FormatUtils {
  companion object {
    @JvmStatic
    fun getDateFormat(): DateTimeFormatter {
      return DateTimeFormatter.ofPattern("dd MMM. yyyy", Locale("es", "AR"))
    }

    @JvmStatic
    fun getShortDateFormat(): DateTimeFormatter {
      return DateTimeFormatter.ofPattern("dd MMM.", Locale("es", "AR"))
    }

    @JvmStatic
    fun getPersistDateFormat(): DateTimeFormatter {
      return DateTimeFormatter.ISO_LOCAL_DATE
    }
  }
}