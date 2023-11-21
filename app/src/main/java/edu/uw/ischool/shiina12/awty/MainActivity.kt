package edu.uw.ischool.shiina12.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

private const val TAG = "Main"
const val ALARM_ACTION = "edu.uw.ischool.shiina12.ALARM"

class MainActivity : AppCompatActivity() {
    private var hasStarted = false
    private var message: String = ""
    private var phoneNum: String = ""
    private var nagTime: Int = 0
    private var receiver: BroadcastReceiver? = null

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
                if (userInput.isNotEmpty() && TextUtils.isDigitsOnly(userInput) && userInput.toInt() > 0) {
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
            message = userInputMessageEditText.text.toString()
            phoneNum = userInputPhoneEditText.text.toString()
//            toastMessage = "$phoneNum: $message"
            nagTime = userInputNagTimeEditText.text.toString().toInt() * 60 * 1000

            if (!hasStarted) {
                Log.d(TAG, "starting SMS nagging.")
                startNag(startButton)
            } else {
                Log.d(TAG, "ending SMS nagging.")
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
        startButton: Button
    ) {
        hasStarted = true
        startButton.text = getString(R.string.stopButtonText)

        Log.i(TAG, "message in startNag: $message")
//        val activityThis = this

        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
//                    Toast.makeText(activityThis, toastMessage, Toast.LENGTH_SHORT).show()
                    if (context != null) {
                        sendSMS(message, context)
                    }
                }

            }
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }

        // create the PendingIntent
        val intent = Intent(ALARM_ACTION)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // get the alarm manager
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            nagTime.toLong(),
            pendingIntent
        )
    }

    private fun sendSMS(message: String, context: Context) {
        try {
//            val smsManager: SmsManager = SmsManager.getDefault()
            val smsManager: SmsManager? =
                context.getSystemService(SmsManager::class.java) as? SmsManager
            smsManager?.sendTextMessage(phoneNum, null, message, null, null)
            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Message failed to send.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "text send failure: $e")
        }
    }

    private fun endNag(startButton: Button) {
        hasStarted = false
        startButton.text = getString(R.string.startButtonText)

        // create the PendingIntent
        val intent = Intent(ALARM_ACTION)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // get the alarm manager
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
