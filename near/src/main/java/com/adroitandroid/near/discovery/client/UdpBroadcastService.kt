package com.adroitandroid.near.discovery.client

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adroitandroid.near.R
import com.adroitandroid.near.discovery.server.UdpServerService
import com.adroitandroid.near.model.Host
import org.json.JSONObject


class UdpBroadcastService : Service() {
    private var broadcastThread: BroadcastThread? = null
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_START_BROADCAST == intent.getStringExtra(BUNDLE_ACTION)) {
            val hostJsonString: String? = intent.getStringExtra(BUNDLE_HOST_JSON)
            val hostJson: JSONObject = if(hostJsonString != null) {
                JSONObject(hostJsonString)
            } else {
                JSONObject(mapOf(Host.JSON_NAME to Host.DUMMY, Host.JSON_FILTER_TEXT to ""))
            }

            broadcastThread?.stopBroadcast()
            broadcastThread = BroadcastThread(
                    hostJson,
                    intent.getLongExtra(BUNDLE_INTERVAL, DEFAULT_BROADCAST_INTERVAL),
                    intent.getIntExtra(BUNDLE_DISCOVERY_PORT, UdpServerService.DISCOVERY_PORT))
            broadcastThread!!.start()
        } else if (ACTION_STOP_BROADCAST == intent.getStringExtra(BUNDLE_ACTION)) {
            broadcastThread?.stopBroadcast()
        }


        return START_STICKY_COMPATIBILITY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {

        stopSelf()
       /* var builder = NotificationCompat.Builder(this, WIFI_P2P_SERVICE)
                .setSmallIcon(R.drawable.messager)
                .setContentTitle("Wifi Chat")
                .setContentText("Сервис всё ещё работает")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(this)
        startForeground(24, builder.build())*/

        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val BUNDLE_HOST_JSON = "bundle_host_json"
        const val BUNDLE_ACTION = "bundle_action"
        const val BUNDLE_INTERVAL = "bundle_interval"
        const val BUNDLE_DISCOVERY_PORT = "bundle_discovery_port"
        const val ACTION_START_BROADCAST = "start_broadcast"
        const val ACTION_STOP_BROADCAST = "stop_broadcast"
        private const val DEFAULT_BROADCAST_INTERVAL: Long = 5000
    }
}