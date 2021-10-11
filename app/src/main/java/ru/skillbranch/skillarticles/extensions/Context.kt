package ru.skillbranch.skillarticles.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import ru.skillbranch.skillarticles.R
import kotlin.reflect.KProperty

fun Context.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics

    )
}

fun Context.dpToIntPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics
    ).toInt()
}

fun Context.attrValue(res: Int): Int {
    val tv = TypedValue()
    if(this.theme.resolveAttribute(res, tv, true)) return tv.data
    else throw Resources.NotFoundException("Unknonw resurse with id $res")
}

@SuppressLint("ServiceCast")
fun Context.hideKeyboard(view : View) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

val Context.isNetworkAvailable: Boolean
    get() {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork?.run {
                val nc = cm.getNetworkCapabilities(this)
                nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            } ?: false
        } else {
            cm.activeNetworkInfo?.run { isConnectedOrConnecting } ?: false
        }
    }