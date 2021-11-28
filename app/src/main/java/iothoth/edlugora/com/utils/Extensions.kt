package iothoth.edlugora.com.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import iothoth.edlugora.com.R

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showLongToast(@StringRes resourceId: Int) {
    Toast.makeText(this, resourceId, Toast.LENGTH_LONG).show()
}

fun Activity.showLongSnackBar(viewId: Int, message: String, color: Int) {
    Snackbar.make(
        findViewById(viewId),
        message, Snackbar.LENGTH_LONG
    ).setBackgroundTint(color).show()

}

fun Activity.showLongSnackBar(viewId: Int, message: Int, color: Int) {
    Snackbar.make(
        findViewById(viewId),
        message, Snackbar.LENGTH_LONG
    ).setBackgroundTint(color).show()

}
fun Activity.showLongSnackBar(view: View, message: String, color: Int) {
    Snackbar.make(
        view,
        message, Snackbar.LENGTH_LONG
    ).setBackgroundTint(color).show()

}

fun Activity.showLongSnackBar(view: View, message: Int, color: Int) {
    Snackbar.make(
        view,
        message, Snackbar.LENGTH_LONG
    ).setBackgroundTint(color).show()

}