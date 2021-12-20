package iothoth.edlugora.com.dialogs

import android.content.Context
import android.net.wifi.ScanResult
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import iothoth.edlugora.com.R

fun dialogSetWifiPassword(
    context: Context,
    wifiResult: ScanResult,
    savePassword: (networkSSID: String, networkPass: String) -> Unit
) {
    val dialogSetWifiPassword = BottomSheetDialog(context)

    dialogSetWifiPassword.setContentView(R.layout.dialog_set_wifi_password)

    val ssid = dialogSetWifiPassword.findViewById<TextView>(R.id.wifi_ssid)
    val passwordLayout =
        dialogSetWifiPassword.findViewById<TextInputLayout>(R.id.wifi_password_name)
    val password =
        dialogSetWifiPassword.findViewById<TextInputEditText>(R.id.wifi_password_name_input)
    val save = dialogSetWifiPassword.findViewById<LinearLayout>(R.id.save_wifi_password)

    ssid?.text = wifiResult.SSID

    passwordLayout?.setEndIconOnClickListener {
        if (password?.inputType == InputType.TYPE_CLASS_TEXT) {
            password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            password.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordLayout.setEndIconDrawable(R.drawable.ic_visibility)
        } else {
            password?.inputType = InputType.TYPE_CLASS_TEXT
            password?.transformationMethod = null
            passwordLayout.setEndIconDrawable(R.drawable.ic_visibility_off)
        }
    }

    save?.setOnClickListener {

        val networkSSID = wifiResult.SSID
        val networkPass = password?.text.toString()
        savePassword(networkSSID, networkPass)
        dialogSetWifiPassword.onBackPressed()
    }

    dialogSetWifiPassword.show()
}