package com.lucronarota.ui.screens.jornada

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucronarota.ui.theme.VerdeCorrida
import com.lucronarota.ui.theme.AmareloCorrida
import com.lucronarota.ui.theme.VermelhoCorrida

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JornadaScreen() {
    var kmInicial by remember { mutableStateOf("") }
    var kmFinal by remember { mutableStateOf("") }
    var showKmInput by remember { mutableStateOf(true) }
    var jornadaAtiva by remember { mutableStateOf(false) }
    var jornadaPausada by remember { mutableStateOf(false) }
    var tempoDecorrido by remember { mutableStateOf("00:00:00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Jornada Inteligente",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registre seus quilometros e acompanhe seu tempo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card de status da Jornada
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    jornadaPausada -> AmareloCorrida.copy(alpha = 0.1f)
                    jornadaAtiva -> VerdeCorrida.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when {
                        jornadaPausada -> Icons.Filled.PauseCircle
                        jornadaAtiva -> Icons.Filled.PlayCircle
                        else -> Icons.Filled.StopCircle
                    },
                    contentDescription = "Status",
                    modifier = Modifier.size(64.dp),
                    tint = when {
                        jornadaPausada -> AmareloCorrida
                        jornadaAtiva -> VerdeCorrida
                        else -> VermelhoCorrida
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        jornadaPausada -> "Jornada Pausada"
                        jornadaAtiva -> "Jornada Ativa"
                        else -> "Jornada Parada"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (jornadaAtiva || jornadaPausada) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tempoDecorrido,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (jornadaPausada) AmareloCorrida else VerdeCorrida
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input KM Inicial
        if (showKmInput && !jornadaAtiva) {
            OutlinedTextField(
                value = kmInicial,
                onValueChange = { kmInicial = it },
                label = { Text("KM Inicial (hodometro)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("km ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Input KM Final
        if (jornadaAtiva && !showKmInput) {
            OutlinedTextField(
                value = kmFinal,
                onValueChange = { kmFinal = it },
                label = { Text("KM Final (hodometro)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("km ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botoes de acao
        if (!jornadaAtiva && !jornadaPausada) {
            Button(
                onClick = {
                    if (kmInicial.isNotBlank()) {
                        jornadaAtiva = true
                        showKmInput = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = kmInicial.isNotBlank()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("INICIAR JORNADA", fontSize = 16.sp)
            }
        }

        if (jornadaAtiva && !jornadaPausada) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        jornadaPausada = true
                        jornadaAtiva = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PAUSAR")
                }

                Button(
                    onClick = {
                        jornadaAtiva = false
                        showKmInput = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VermelhoCorrida
                    )
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("FINALIZAR")
                }
            }
        }

        if (jornadaPausada) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        jornadaAtiva = true
                        jornadaPausada = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RETOMAR")
                }

                OutlinedButton(
                    onClick = {
                        jornadaAtiva = false
                        jornadaPausada = false
                        showKmInput = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ENCERRAR")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campos de faturamento
        if (!jornadaAtiva && !jornadaPausada && !showKmInput) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Faturamento da Jornada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("Uber", "99", "InDrive", "Outros").forEach { plataforma ->
                        OutlinedTextField(
                            value = "",
                            onValueChange = { },
                            label = { Text(plataforma) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SALVAR JORNADA")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Historico
        Text(
            text = "Ultimas Jornadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Placeholder para historico
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nenhuma jornada registrada ainda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
