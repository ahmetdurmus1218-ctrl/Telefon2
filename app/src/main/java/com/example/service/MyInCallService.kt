package com.example.service

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.example.CallActivity

object CallManager {
    var activeCall: Call? = null
}

class MyInCallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("MyInCallService", "Call added: $call")
        CallManager.activeCall = call

        val intent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("MyInCallService", "Call removed: $call")
        if (CallManager.activeCall == call) {
            CallManager.activeCall = null
        }
    }
}
