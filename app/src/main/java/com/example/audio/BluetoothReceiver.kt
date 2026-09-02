package com.example.audio

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Monitors Bluetooth headset connection state changes.
 * Pauses playback when a headset disconnects and resumes when it reconnects,
 * mimicking the behavior of popular music players.
 */
class BluetoothReceiver(
    private val onBluetoothDisconnected: () -> Unit,
    private val onBluetoothConnected: () -> Unit
) : BroadcastReceiver() {

    companion object {
        private const val TAG = "BluetoothReceiver"
        private const val EXTRA_CONNECTION_STATE = "android.bluetooth.headset.profile.extra.CONNECTION_STATE"
        private const val EXTRA_PREVIOUS_STATE = "android.bluetooth.headset.profile.extra.PREVIOUS_CONNECTION_STATE"
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Log.d(TAG, "Bluetooth device disconnected")
                onBluetoothDisconnected()
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.d(TAG, "Bluetooth device connected")
                onBluetoothConnected()
            }
            "android.headset.profile.action.CONNECTION_STATE_CHANGED" -> {
                val state = intent.getIntExtra(EXTRA_CONNECTION_STATE, -1)
                val previousState = intent.getIntExtra(EXTRA_PREVIOUS_STATE, -1)
                Log.d(TAG, "Headset state changed: $previousState -> $state")

                when (state) {
                    STATE_DISCONNECTED -> {
                        // Only pause if we were previously connected (not on app start)
                        if (previousState == STATE_CONNECTED) {
                            onBluetoothDisconnected()
                        }
                    }
                    STATE_CONNECTED -> {
                        onBluetoothConnected()
                    }
                }
            }
        }
    }
}
