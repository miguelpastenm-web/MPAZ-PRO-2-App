package cl.duoc.mpazpro.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.mpazpro.ui.screens.student.ProfileCameraScreen
import cl.duoc.mpazpro.ui.screens.student.StudentActivityScreen
import cl.duoc.mpazpro.ui.screens.student.StudentHomeScreen
import cl.duoc.mpazpro.ui.screens.teacher.ExportReportScreen
import cl.duoc.mpazpro.ui.screens.teacher.TeacherFormScreen

/**
 * Grafo centralizado de navegacion con transiciones fluidas
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.InicioRol.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(AppScreens.InicioRol.route) {
            RoleSelectionPlaceholderScreen(
                onNavigateToTeacher = { navController.navigate(AppScreens.FormularioDocente.route) },
                onNavigateToStudent = { navController.navigate(AppScreens.HomeEstudiante.route) }
            )
        }
        composable(AppScreens.HomeEstudiante.route) {
            StudentHomeScreen(
                onBack = { navController.popBackStack() },
                onSelectMateria = { oaId ->
                    navController.navigate(AppScreens.ActividadesEstudiante.createRoute(oaId))
                },
                onNavigateToProfile = { navController.navigate(AppScreens.PerfilCamara.route) }
            )
        }
        composable(AppScreens.FormularioDocente.route) {
            TeacherFormScreen(
                onBack = { navController.popBackStack() },
                onNavigateToExport = { navController.navigate(AppScreens.ExportarReporte.route) }
            )
        }
        composable(
            route = AppScreens.ActividadesEstudiante.route,
            arguments = listOf(
                navArgument("oaId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val oaId = backStackEntry.arguments?.getInt("oaId") ?: -1
            StudentActivityScreen(
                onBack = { navController.popBackStack() },
                oaId = oaId
            )
        }
        composable(AppScreens.ExportarReporte.route) {
            ExportReportScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppScreens.PerfilCamara.route) {
            ProfileCameraScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
private fun RoleSelectionPlaceholderScreen(
    onNavigateToTeacher: () -> Unit,
    onNavigateToStudent: () -> Unit
) {
    val azulOscuro = Color(0xFF102B61)
    val azulPrincipal = Color(0xFF2258A2)
    val azulClaro = Color(0xFFDCE6FF)
    val fondo = Color(0xFFF0F4FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado institucional
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF102B61),
                                Color(0xFF234B98),
                                Color(0xFF3264BB)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 35.dp)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 29.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "📖",
                        fontSize = 72.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "MPAZ PRO 2.0",
                        color = Color.White,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Aprender • Evaluar • Crecer",
                        color = Color(0xFFD7E5FF),
                        fontSize = 13.sp
                    )
                }
            }

            // Tarjeta de selección de perfil
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .offset(y = (-28).dp),
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 22.dp,
                        vertical = 34.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escuela Escritora\nMarcela Paz",
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = azulOscuro,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Selecciona tu perfil de acceso",
                        fontSize = 14.sp,
                        color = Color(0xFF53678C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = onNavigateToTeacher,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = azulPrincipal,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Panel Docente / UTP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        Text("›", fontSize = 28.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = onNavigateToStudent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = azulClaro,
                            contentColor = azulOscuro
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Estudiante 2.º Básico",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        Text("›", fontSize = 28.sp)
                    }
                }
            }

            // Mensaje institucional inferior
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 30.dp,
                        end = 30.dp,
                        bottom = 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✦",
                    fontSize = 25.sp,
                    color = Color(0xFFF4BC45)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "La educación es la base de un futuro\ncon más oportunidades",
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = azulPrincipal,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Escuela Marcela Paz",
                    fontSize = 12.sp,
                    color = Color(0xFF6D80A3)
                )
            }
        }
    }
}

