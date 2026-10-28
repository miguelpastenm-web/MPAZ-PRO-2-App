package cl.duoc.mpazpro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = BlancoInstitucional,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulInstitucional,
    secondary = AzulPrincipal,
    onSecondary = BlancoInstitucional,
    secondaryContainer = AzulClaro,
    onSecondaryContainer = AzulInstitucional,
    tertiary = AzulPrincipal,
    onTertiary = BlancoInstitucional,
    tertiaryContainer = AzulClaro,
    onTertiaryContainer = AzulInstitucional,
    background = FondoInstitucional,
    onBackground = AzulInstitucional,
    surface = BlancoInstitucional,
    onSurface = AzulInstitucional,
    surfaceVariant = BlancoInstitucional,
    onSurfaceVariant = AzulInstitucional,
    outline = Color(0xFF8798B7)
)

@Composable
fun MPAZPROTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
