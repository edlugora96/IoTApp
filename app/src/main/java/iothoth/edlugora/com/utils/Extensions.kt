package iothoth.edlugora.com.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import iothoth.edlugora.com.R
import iothoth.edlugora.com.ui.DetectNetworkFragment
import java.io.IOException
import java.io.InputStream


const val TAG = "Utils"

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showLongToast(@StringRes resourceId: Int) {
    Toast.makeText(this, resourceId, Toast.LENGTH_LONG).show()
}

private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
    permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

class PermissionHandler(
    private val context: Context,
    private val PERMISSIONS: Array<String>,
    private val requestPermissions: (callback: () -> Unit) -> Unit
) {
    fun ifPermissionsAreGranted(callback: () -> Unit) {
        if (!hasPermissions(context, *PERMISSIONS)) {
            requestPermissions(callback)
        } else {
            callback()
        }
    }
}


fun Context.changeColorStatusBar(activity: Activity, lightColor: Int, nightColor: Int) {
    when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> {
            activity.window.statusBarColor =
                ContextCompat.getColor(activity, lightColor)
        }
        Configuration.UI_MODE_NIGHT_YES -> {
            activity.window.statusBarColor =
                ContextCompat.getColor(activity, nightColor)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun Activity.setLightStatusBar() {
    this.window.decorView.windowInsetsController?.setSystemBarsAppearance(
        APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS
    )

}

fun Context.changeColorStatusBar(activity: Activity, color: Int) {
    activity.window.statusBarColor =
        ContextCompat.getColor(activity, color)
}

fun Context.showConfirmDialog(message: String, title: String, accept: () -> Unit) {
    MaterialAlertDialogBuilder(this).setMessage(message)
        .setTitle(title)
        .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
            accept()
        }
        .show()
}

fun Context.showConfirmDialog(message: String, title: String) {
    MaterialAlertDialogBuilder(this).setMessage(message)
        .setTitle(title)
        .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
            {  }
        }
        .show()
}

fun Context.showConfirmDialog(message: String, accept: () -> Unit, decline: () -> Unit) {
    MaterialAlertDialogBuilder(this).setMessage(message)
        .setNegativeButton(resources.getString(R.string.decline)) { _, _ ->
            decline()
        }
        .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
            accept()
        }
        .show()
}

fun Context.showConfirmDialog(
    message: String,
    title: String,
    accept: () -> Unit,
    decline: () -> Unit
) {
    MaterialAlertDialogBuilder(this).setMessage(message)
        .setTitle(title)
        .setNegativeButton(resources.getString(R.string.decline)) { _, _ ->
            decline()
        }
        .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
            accept()
        }
        .show()
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

@Throws(IOException::class)
fun loadImage(context: Context, imageUri: Uri, maxImageDimension: Int): Bitmap? {
    var inputStreamForSize: InputStream? = null
    var inputStreamForImage: InputStream? = null
    try {
        inputStreamForSize = context.contentResolver.openInputStream(imageUri)
        var opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStreamForSize, null, opts)/* outPadding= */
        val inSampleSize =
            (opts.outWidth / maxImageDimension).coerceAtLeast(opts.outHeight / maxImageDimension)

        opts = BitmapFactory.Options()
        opts.inSampleSize = inSampleSize
        inputStreamForImage = context.contentResolver.openInputStream(imageUri)
        val decodedBitmap =
            BitmapFactory.decodeStream(inputStreamForImage, null, opts)/* outPadding= */
        return maybeTransformBitmap(
            context.contentResolver,
            imageUri,
            decodedBitmap
        )
    } finally {
        inputStreamForSize?.close()
        inputStreamForImage?.close()
    }
}

fun maybeTransformBitmap(resolver: ContentResolver, uri: Uri, bitmap: Bitmap?): Bitmap? {
    val matrix: Matrix? = when (getExifOrientationTag(resolver, uri)) {
        ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL ->
            // Set the matrix to be null to skip the image transform.
            null
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { postScale(-1.0f, 1.0f) }

        ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { postRotate(90f) }
        ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply { postScale(-1.0f, 1.0f) }
        ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { postRotate(180.0f) }
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { postScale(1.0f, -1.0f) }
        ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { postRotate(-90.0f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply {
            postRotate(-90.0f)
            postScale(-1.0f, 1.0f)
        }
        else ->
            // Set the matrix to be null to skip the image transform.
            null
    }

    return if (matrix != null) {
        Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

fun getExifOrientationTag(resolver: ContentResolver, imageUri: Uri): Int {
    if (ContentResolver.SCHEME_CONTENT != imageUri.scheme && ContentResolver.SCHEME_FILE != imageUri.scheme) {
        return 0
    }

    var exif: ExifInterface? = null
    try {
        resolver.openInputStream(imageUri)?.use { inputStream -> exif = ExifInterface(inputStream) }
    } catch (e: IOException) {
        Log.e(TAG, "Failed to open file to read rotation meta data: $imageUri", e)
    }

    return if (exif != null) {
        exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } else {
        ExifInterface.ORIENTATION_UNDEFINED
    }
}

fun isWifiLocked(scanResult: ScanResult): Boolean {
    val cap: String = scanResult.capabilities
    val securityModes = arrayOf("WEP", "WPA", "WPA2", "WPA_EAP", "IEEE8021X")
    for (i in securityModes.indices.reversed()) {
        if (cap.contains(securityModes[i])) {
            return true
        }
    }
    return false
}