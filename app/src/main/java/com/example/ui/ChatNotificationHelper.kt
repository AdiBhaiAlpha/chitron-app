package com.example.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.example.MainActivity
import com.example.R
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ChatNotificationHelper {
    const val CHANNEL_ID = "chitron_messenger_channel_v1"
    const val EXTRA_VISITOR_ID = "extra_open_chat_visitor_id"
    const val EXTRA_IS_ADMIN = "extra_open_chat_is_admin"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Messenger & Live Chat",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time notifications for incoming visitor and admin messages"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0L, 140L, 80L, 180L)
                    if (defaultSoundUri != null) {
                        setSound(defaultSoundUri, audioAttrs)
                    }
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Plays a crisp Messenger-style two-tone pop chime using AudioTrack + system notification ringtone fallback.
     */
    fun playMessageSound(context: Context) {
        thread(start = true, isDaemon = true, name = "MessengerSoundThread") {
            try {
                val sampleRate = 24000
                val durationMs = 240
                val numSamples = (sampleRate * durationMs) / 1000
                val samples = ShortArray(numSamples)

                // Two-note Messenger pop chime: 587.33 Hz (D5) -> 880.0 Hz (A5)
                val splitSample = (numSamples * 0.42).toInt()
                for (i in 0 until numSamples) {
                    val isSecondNote = i >= splitSample
                    val localIdx = if (isSecondNote) i - splitSample else i
                    val localLen = if (isSecondNote) numSamples - splitSample else splitSample
                    val freq = if (isSecondNote) 880.0 else 587.33
                    val t = localIdx.toDouble() / sampleRate.toDouble()

                    // Fast attack, smooth exponential decay envelope
                    val attack = (localIdx.toDouble() / (sampleRate * 0.008)).coerceAtMost(1.0)
                    val decay = exp(-5.5 * (localIdx.toDouble() / localLen.toDouble()))
                    val env = attack * decay

                    val wave = sin(2.0 * PI * freq * t) + 0.25 * sin(4.0 * PI * freq * t)
                    val pcm = (wave * env * 14000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    samples[i] = pcm.toShort()
                }

                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack(
                    audioAttrs,
                    format,
                    samples.size * 2,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep((durationMs + 60).toLong())
                track.release()
            } catch (_: Throwable) {
                try {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (uri != null) {
                        RingtoneManager.getRingtone(context.applicationContext, uri)?.play()
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    /**
     * Posts a Messenger-style notification to the Android system notification panel.
     */
    fun showIncomingMessageNotification(
        context: Context,
        visitorId: String,
        senderName: String,
        messageText: String,
        isForAdmin: Boolean
    ) {
        ensureNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_VISITOR_ID, visitorId)
            putExtra(EXTRA_IS_ADMIN, isForAdmin)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val notificationId = visitorId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val person = Person.Builder()
            .setName(senderName.ifBlank { "Visitor" })
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle(if (isForAdmin) "Messenger • $senderName" else "Chitron's Archive")
            .addMessage(messageText, System.currentTimeMillis(), person)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(senderName.ifBlank { "New Message" })
            .setContentText(messageText)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }
}
