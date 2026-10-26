package cl.duoc.mpazpro.ui.navigation

/**
 * Rutas selladas para navegacion tipada y desacoplada
 */
sealed class AppScreens(val route: String) {
    data object InicioRol : AppScreens("inicio_rol")
    data object FormularioDocente : AppScreens("formulario_docente")
    data object HomeEstudiante : AppScreens("home_estudiante")
    data object ActividadesEstudiante : AppScreens("actividades_estudiante/{oaId}") {
        fun createRoute(oaId: Int) = "actividades_estudiante/$oaId"
    }
    data object PerfilCamara : AppScreens("perfil_camara")
    data object ExportarReporte : AppScreens("exportar_reporte")
}
