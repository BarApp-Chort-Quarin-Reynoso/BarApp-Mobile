package com.barapp.util

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class AvailableDatesValidator(
  private val datesAvailable: Set<LocalDate> = HashSet()
) : DateValidator {
  constructor(parcel: Parcel) : this()

  private val zonaArgentina: ZoneId = ZoneId.of("America/Buenos_Aires")

  override fun describeContents(): Int {
    TODO("Not yet implemented")
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {

  }

  override fun isValid(date: Long): Boolean {
    val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    val month = YearMonth.of(localDate.year, localDate.month)
    return datesAvailable.contains(localDate)
  }

  companion object CREATOR : Parcelable.Creator<AvailableDatesValidator> {
    override fun createFromParcel(parcel: Parcel): AvailableDatesValidator {
      return AvailableDatesValidator(parcel)
    }

    override fun newArray(size: Int): Array<AvailableDatesValidator?> {
      return arrayOfNulls(size)
    }
  }

}
