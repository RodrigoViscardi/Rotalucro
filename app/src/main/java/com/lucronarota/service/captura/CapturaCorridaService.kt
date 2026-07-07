package com.lucronarota.service.captura

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.lucronarota.data.local.entity.CorridaEntity
import com.lucronarota.data.model.ClassificacaoCorrida
import com.lucronarota.data.model.Plataforma
import com.lucronarota.service.calculo.CalculoEngine
import kotlinx.coroutines.runBlocking
import java.util.*

class CapturaCorridaService : AccessibilityService() {

    companion object {
        private const val TAG = "CapturaCorrida"
        private const val PREFS_NAME = "captura_prefs"
        private const val KEY_CUSTO_KM = "custo_por_km"
        private const val KEY_META_KM = "meta_por_km"
        private const val KEY_META_HORA = "meta_por_hora"
        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, CapturaCorridaService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CapturaCorridaService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var tts: TextToSpeech
    private lateinit var prefs: SharedPreferences
    private val engine = CalculoEngine()
    private var lastProcessedPackage = ""
    private var lastProcessedTime = 0L

    private val TARGET_PACKAGES = setOf(
        "com.ubercab",
        "com.ubercab.driver",
        "com.taxis99",
        "com.nine9",
        "br.com.apsenha.appmotorista",
        "com.indriver",
        "br.com.ifood"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }

        isRunning = true
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("pt", "BR")
            }
        }

        Log.d(TAG, "Servico de captura iniciado")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName == null) return
        val packageName = event.packageName.toString()

        // So processa se for um app de corrida conhecido
        if (!TARGET_PACKAGES.any { packageName.contains(it, ignoreCase = true) }) return

        // Evita processar o mesmo pacote muitas vezes em curto espaco de tempo
        val now = System.currentTimeMillis()
        if (packageName == lastProcessedPackage && now - lastProcessedTime < 2000) return
        lastProcessedPackage = packageName
        lastProcessedTime = now

        val rootNode = rootInActiveWindow ?: return

        try {
            processarOferta(rootNode, packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar oferta", e)
        } finally {
            rootNode.recycle()
        }
    }

    private fun processarOferta(rootNode: AccessibilityNodeInfo, packageName: String) {
        val textoCompleto = extrairTexto(rootNode)

        val valor = extrairValor(textoCompleto)
        val distancia = extrairDistancia(textoCompleto)
        val tempo = extrairTempo(textoCompleto)
        val destino = extrairDestino(textoCompleto)

        if (valor == null || distancia == null) return

        val plataforma = identificarPlataforma(packageName)
        val custoPorKm = prefs.getFloat(KEY_CUSTO_KM, 1.10f).toDouble()
        val metaPorKm = prefs.getFloat(KEY_META_KM, 2.50f).toDouble()
        val metaPorHora = prefs.getFloat(KEY_META_HORA, 40.0f).toDouble()

        val resultado = engine.calcular(
            valor = valor,
            distanciaKm = distancia,
            tempoMin = tempo ?: 15.0,
            custoPorKm = custoPorKm,
            metaPorKm = metaPorKm,
            metaPorHora = metaPorHora
        )

        Log.d(TAG, "Corrida ${resultado.classificacao.name}: " +
                "R$${"%.2f".format(valor)} | ${"%.1f".format(distancia)}km | " +
                "R$${"%.2f".format(resultado.rPorKm)}/km | ${resultado.mensagem}")

        // Feedback sonoro e vibratorio
        darFeedback(resultado.classificacao)

        // Anunciar por voz
        falar(resultado.mensagem)

        // Disparar overlay
        dispararOverlay(resultado.mensagem, resultado.rPorKm, resultado.rPorHora, resultado.lucro, resultado.classificacao)

        // Salvar corrida no banco
        salvarCorrida(
            valor = valor,
            distanciaKm = distancia,
            tempoMin = tempo ?: 15.0,
            resultado = resultado,
            plataforma = plataforma,
            destino = destino
        )
    }

    private fun extrairTexto(node: AccessibilityNodeInfo): String {
        val builder = StringBuilder()
        extrairTextoRecursivo(node, builder)
        return builder.toString()
    }

    private fun extrairTextoRecursivo(node: AccessibilityNodeInfo, builder: StringBuilder) {
        if (node.text != null) {
            builder.append(node.text).append(" ")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extrairTextoRecursivo(child, builder)
            child.recycle()
        }
    }

    private fun extrairValor(texto: String): Double? {
        // Procura por padroes como R$ 15,00 | R$15,00 | 15,00 | 15.00
        val regex = Regex("""R?\$?\s*(\d{1,3}(?:\.\d{3})*,\d{2}|\d+,\d{2}|\d+\.\d{2})""")
        val match = regex.find(texto)
        if (match != null) {
            val valorStr = match.groupValues[1]
                .replace(".", "")
                .replace(",", ".")
            return valorStr.toDoubleOrNull()
        }
        return null
    }

    private fun extrairDistancia(texto: String): Double? {
        val regex = Regex("""(\d+[.,]?\d*)\s*(?:km|quilometro|km)""", RegexOption.IGNORE_CASE)
        val match = regex.find(texto)
        if (match != null) {
            return match.groupValues[1].replace(",", ".").toDoubleOrNull()
        }
        return null
    }

    private fun extrairTempo(texto: String): Double? {
        // Procura por padroes como "12 min", "12min", "12 minutos"
        val regex = Regex("""(\d+)\s*(?:min|minuto|minutos)""", RegexOption.IGNORE_CASE)
        val match = regex.find(texto)
        if (match != null) {
            return match.groupValues[1].toDoubleOrNull()
        }
        return null
    }

    private fun extrairDestino(texto: String): String? {
        // Tenta extrair endereco de destino - logica simplificada
        val linhas = texto.split("\n")
        if (linhas.size >= 3) {
            return linhas.last().trim().take(100)
        }
        return null
    }

    private fun identificarPlataforma(packageName: String): Plataforma {
        return when {
            packageName.contains("uber", ignoreCase = true) -> Plataforma.UBER
            packageName.contains("99", ignoreCase = true) || packageName.contains("nine9", ignoreCase = true) -> Plataforma.APP99
            packageName.contains("indriver", ignoreCase = true) -> Plataforma.INDRIVE
            packageName.contains("ifood", ignoreCase = true) -> Plataforma.IFOOD
            else -> Plataforma.OUTROS
        }
    }

    private fun darFeedback(classificacao: ClassificacaoCorrida) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }

        val effect = when (classificacao) {
            ClassificacaoCorrida.VERDE -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            ClassificacaoCorrida.AMARELO -> {
                val pattern = longArrayOf(0, 100, 100, 100)
                VibrationEffect.createWaveform(pattern, -1)
            }
            ClassificacaoCorrida.VERMELHO -> {
                val pattern = longArrayOf(0, 200, 100, 200, 100, 200)
                VibrationEffect.createWaveform(pattern, -1)
            }
        }
        vibrator?.vibrate(effect)
    }

    private fun falar(mensagem: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(mensagem, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            @Suppress("DEPRECATION")
            tts.speak(mensagem, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private fun dispararOverlay(
        mensagem: String,
        rPorKm: Double,
        rPorHora: Double,
        lucro: Double,
        classificacao: ClassificacaoCorrida
    ) {
        val intent = Intent(this, com.lucronarota.service.overlay.OverlayService::class.java).apply {
            putExtra("mensagem", mensagem)
            putExtra("rPorKm", rPorKm)
            putExtra("rPorHora", rPorHora)
            putExtra("lucro", lucro)
            putExtra("classificacao", classificacao.name)
        }
        startService(intent)
    }

    private fun salvarCorrida(
        valor: Double,
        distanciaKm: Double,
        tempoMin: Double,
        resultado: com.lucronarota.data.model.ResultadoCalculo,
        plataforma: Plataforma,
        destino: String?
    ) {
        // Salva no banco via corotina (executado em background pelo service)
        Thread {
            try {
                val db = com.lucronarota.data.local.database.AppDatabase.getInstance(this)
                val corrida = CorridaEntity(
                    plataforma = plataforma,
                    valor = valor,
                    distanciaKm = distanciaKm,
                    tempoMin = tempoMin,
                    rPorKm = resultado.rPorKm,
                    rPorHora = resultado.rPorHora,
                    lucro = resultado.lucro,
                    classificacao = resultado.classificacao,
                    timestamp = System.currentTimeMillis()
                )
                runBlocking {
                    db.corridaDao().insertCorrida(corrida)
                }
                Log.d(TAG, "Corrida salva: ${resultado.classificacao.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar corrida", e)
            }
        }.start()
    }

    override fun onInterrupt() {
        Log.d(TAG, "Servico de captura interrompido")
    }

    override fun onDestroy() {
        isRunning = false
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    fun updateConfig(custoPorKm: Float, metaPorKm: Float, metaPorHora: Float) {
        prefs.edit().apply {
            putFloat(KEY_CUSTO_KM, custoPorKm)
            putFloat(KEY_META_KM, metaPorKm)
            putFloat(KEY_META_HORA, metaPorHora)
            apply()
        }
    }

}
