package cl.duoc.mpazpro.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import cl.duoc.mpazpro.ui.screens.admin.ExportReportScreen
import cl.duoc.mpazpro.ui.screens.profile.ProfileCameraScreen
import cl.duoc.mpazpro.ui.screens.admin.TeacherFormScreen
import cl.duoc.mpazpro.ui.screens.student.StudentActivityScreen
import cl.duoc.mpazpro.ui.screens.student.StudentHomeScreen

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MPAZ PRO 2.º - Acceso") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Escuela Escritora Marcela Paz",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecciona tu perfil de acceso:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToTeacher,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Panel Docente / UTP", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onNavigateToStudent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.School, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Estudiante 2.º Básico", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseScreenPlaceholder(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack) {
                        Text("Regresar al Inicio")
                    }
                }
            }
        }
    }
}
