package com.barapp.util

import android.view.animation.PathInterpolator
import androidx.core.graphics.PathParser

class Interpolator {
  companion object {
    @JvmStatic
    fun emphasizedInterpolator(): PathInterpolator {
      return PathInterpolator(
        PathParser.createPathFromPathData(
          "M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1"
        )
      )
    }
  }
}
