package edu.uw.ischool.shiina12.awty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

//private const val TAG = "Main"

class MainActivity : AppCompatActivity() {
    private var hasStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get all UI elements
        val userInputMessageEditText: EditText = findViewById(R.id.userInputMessage)
        val userInputPhoneEditText: EditText = findViewById(R.id.userInputPhone)
        val userInputNagTimeEditText: EditText = findViewById(R.id.userInputNagTime)
        val startButton: Button = findViewById(R.id.startButton)

        startButton.isEnabled = false
        hasStarted = false

        var userInputMessageIsValid = false
        var userInputPhoneIsValid = false
        var userInputNagTimeIsValid = false

        userInputMessageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val userInput = s.toString()
                if (userInput.isNotEmpty()) {
                    userInputMessageIsValid = true
                }
                validateInput(
                    userInputMessageIsValid,
                    userInputPhoneIsValid,
                    userInputNagTimeIsValid,
                    startButton
                )
            }
        })

        userInputPhoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val formattedNumber = PhoneNumberUtils.formatNumber(s.toString(), "US")
                if (PhoneNumberUtils.isGlobalPhoneNumber(formattedNumber) && formattedNumber.isNotEmpty()) {
                    userInputPhoneIsValid = true
                }
                validateInput(
                    userInputMessageIsValid,
                    userInputPhoneIsValid,
                    userInputNagTimeIsValid,
                    startButton
                )
            }
        })

        userInputNagTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val userInput = userInputNagTimeEditText.text.toString()
                if (TextUtils.isDigitsOnly(userInput) && userInput.toInt() > 0) {
                    userInputNagTimeIsValid = true
                }
                validateInput(
                    userInputMessageIsValid,
                    userInputPhoneIsValid,
                    userInputNagTimeIsValid,
                    startButton
                )
            }
        })


        startButton.setOnClickListener {
            val message = userInputMessageEditText.text.toString()
            val phoneNum = userInputPhoneEditText.text.toString()
            val nagTime = userInputNagTimeEditText.text.toString().toInt()

            if (!hasStarted) {
                startNag(message, phoneNum, nagTime, startButton)
            } else {
                endNag(startButton)
            }

        }
    }

    fun validateInput(
        userInputMessageIsValid: Boolean,
        userInputPhoneIsValid: Boolean,
        userInputNagTimeIsValid: Boolean,
        startButton: Button
    ) {
        startButton.isEnabled =
            userInputMessageIsValid && userInputPhoneIsValid && userInputNagTimeIsValid
    }

    private fun startNag(
        message: String,
        phoneNum: String,
        nagTime: Int,
        startButton: Button
    ) {
        hasStarted = true
        startButton.text = "@string/stopButtonText"
        val toastText = "$phoneNum: $message"
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
    }

    private fun endNag(startButton: Button) {
        hasStarted = false
        startButton.text = "@string/startButtonText"
    }
}
