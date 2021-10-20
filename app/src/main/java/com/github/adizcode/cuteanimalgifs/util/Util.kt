package com.github.adizcode.cuteanimalgifs.util

import android.content.Context

object Util {
    fun dpToPx(context: Context, dp: Int): Float {
        return dp * context.resources.displayMetrics.density
    }
}