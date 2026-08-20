package com.example.oficina.controle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LaranjaOficina = Color(0xFFFF6D00)
val AzulOficina = Color(0xFF1A2A4A)
val VerdeSucesso = Color(0xFF2E7D32)
val AmareloAlerta = Color(0xFFF9A825)
val VermelhoErro = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = LaranjaOficina,
    onPrimary = Color.White,
    secondary = AzulOficina,
    onSecondary = Color.White,
    background = Color(0xFFF7F7F9),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = LaranjaOficina,
    onPrimary = Color.Black,
    secondary = AzulOficina,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun OficinaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
