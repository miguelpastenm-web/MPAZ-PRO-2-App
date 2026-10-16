package cl.duoc.mpazpro.ui.navigation

/**
 * Rutas selladas para navegacion tipada y desacoplada (IE 2.1.1)
 */
sealed class AppScreens(val route: String) {
    data object InicioRol : AppScreens("inicio_rol")
    data object FormularioDocente : AppScreens("formulario_docente")
    data object ActividadesEstudiante : AppScreens("actividades_estudiante")
    data object PerfilCamara : AppScreens("perfil_camara")
    data object ExportarReporte : AppScreens("exportar_reporte")
}
