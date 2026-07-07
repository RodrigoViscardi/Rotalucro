package com.lucronarota.service.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.lucronarota.MainActivity
import com.lucronarota.R

class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "overlay_channel"
        const val NOTIFICATION_ID = 1002
        private const val TAG = "OverlayService"
        private var overlayView: View? = null
        private var isShowing = false
    }

    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        intent?.let {
            val mensagem = it.getStringExtra("mensagem") ?: ""
            val rPorKm = it.getDoubleExtra("rPorKm", 0.0)
            val rPorHora = it.getDoubleExtra("rPorHora", 0.0)
            val lucro = it.getDoubleExtra("lucro", 0.0)
            val classificacao = it.getStringExtra("classificacao") ?: "AMARELO"

            showOverlay(mensagem, rPorKm, rPorHora, lucro, classificacao)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay de Corridas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificacao para o overlay de corridas"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lucro na Rota")
            .setContentText("Semafaro ativo")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showOverlay(
        mensagem: String,
        rPorKm: Double,
        rPorHora: Double,
        lucro: Double,
        classificacao: String
    ) {
        hideOverlay()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_corrida, null)

        val bgColor = when (classificacao) {
            "VERDE" -> 0xCC4CAF50.toInt()
            "AMARELO" -> 0xCCFFC107.toInt()
            "VERMELHO" -> 0xCCF44336.toInt()
            else -> 0xCCFFC107.toInt()
        }
        overlayView?.setBackgroundColor(bgColor)

        overlayView?.findViewById<TextView>(R.id.tv_mensagem)?.text = mensagem
        overlayView?.findViewById<TextView>(R.id.tv_r_por_km)?.text =
            "R$ ${"%.2f".format(rPorKm)}/km"
        overlayView?.findViewById<TextView>(R.id.tv_r_por_hora)?.text =
            "R$ ${"%.2f".format(rPorHora)}/h"
        overlayView?.findViewById<TextView>(R.id.tv_lucro)?.text =
            if (lucro >= 0) "+R$ ${"%.2f".format(lucro)}" else "-R$ ${"%.2f".format(-lucro)}"

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER
        params.y = 100

        try {
            windowManager.addView(overlayView, params)
            isShowing = true

            handler.postDelayed({
                hideOverlay()
            }, 5000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        if (isShowing && overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
            isShowing = false
        }
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }
}
