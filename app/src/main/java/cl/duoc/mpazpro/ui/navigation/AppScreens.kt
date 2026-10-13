package cl.duoc.mpazpro.ui.navigation

/**
 * Rutas selladas para navegacion tipada y desacoplada (IE 2.1.1)
 */
sealed class AppScreens(val route: String) {
    data object RoleSelectionScreen : AppScreens("role_selection")
    data object TeacherFormScreen : AppScreens("teacher_form")
    data object StudentHomeScreen : AppScreens("student_home")
    data object ProfileNativeScreen : AppScreens("profile_native")
}
