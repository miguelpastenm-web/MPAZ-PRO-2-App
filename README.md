# MPAZ PRO 2.º - Aplicación Móvil de Apoyo Didáctico

Aplicación móvil Android desarrollada en Kotlin y Jetpack Compose para el apoyo pedagógico y la trazabilidad de Objetivos de Aprendizaje (OA) en Segundo Básico de la Escuela Escritora Marcela Paz (San Bernardo).

* **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)
* **Institución:** Duoc UC
* **Evaluación:** Evaluación Parcial 2 / Evaluación Parcial 3

---

## Integrantes del Equipo

* **Miguel Pastén**
* **Fabián Aguilar**

---

## Planificación y Gestión del Proyecto

En conformidad con los criterios de evaluación de trabajo colaborativo (IE 2.3.2):
* **Tablero de Planificación (Trello):** [Tablero Trello - MPAZ PRO DSY1105](https://trello.com/b/M2z1OkvY/mpaz-pro-dsy1105)
* **Estrategia de Versionamiento:** Flujo de trabajo en Git con commits técnicos basados en el estándar Conventional Commits (`feat`, `fix`, `chore`, `docs`).

---

## Contexto Escolar y Problemática

La **Escuela Escritora Marcela Paz** es un establecimiento público, gratuito e inclusivo ubicado en la comuna de San Bernardo, que atiende a estudiantes desde Pre-Kínder hasta 8.º Básico.

### Diagnóstico de la Situación Actual
Actualmente, las actividades y evaluaciones de las cuatro asignaturas principales se gestionan mediante guías impresas en papel. Esta modalidad genera:
* Alta carga horaria para los profesores en redacción, impresión y corrección manual.
* Disgregación física de las evaluaciones, impidiendo consolidar un historial de progreso en tiempo real por Objetivo de Aprendizaje (OA).
* Desafíos de autonomía pedagógica, dado que los estudiantes de 2.º Básico se encuentran en etapa de iniciación lectora y requieren soportes multimedia (audios e imágenes) y baja carga cognitiva.

### Solución Desarrollada (MVP)
**MPAZ PRO 2.º** digitaliza la entrega de contenidos curriculares, la resolución de actividades progresivas y la aplicación de evaluaciones finales con control estricto de intentos configurables por el profesor. Opera de forma 100% local (sin dependencia de servicios externos ni IA), garantizando la privacidad de los datos mediante registros y perfiles simulados.

El sistema contempla flujos diferenciados para los 3 perfiles requeridos:
1. **Jefatura UTP:** Gestión, aprobación y publicación de unidades y Objetivos de Aprendizaje.
2. **Docente de 2.º Básico:** Asignación de actividades, parametrización de intentos y consulta de reportes consolidados.
3. **Estudiante de 2.º Básico:** Interfaz adaptada con textos concisos, apoyo visual/sonoro y resolución de desafíos.

---

## Tecnologías Utilizadas

* **Lenguaje:** Kotlin
* **Plataforma y SDK:** Android (Min SDK: 26 / Target SDK: 34 o superior)
* **Interfaz de Usuario:** Jetpack Compose con lineamientos de Material Design 3
* **Arquitectura:** Patrón MVVM (Model-View-ViewModel) con separación de responsabilidades
* **Persistencia Local:** Room Database / SQLite
* **Gestión de Estado y Concurrencia:** StateFlow, MutableStateFlow y Coroutines
* **Recursos Nativos del Dispositivo:** Acceso seguro a Galería/Almacenamiento y Cámara mediante contratos de actividad (`ActivityResultContracts`)
* **Control de Versiones y Gestión:** Git, GitHub y Trello

---

## Funcionalidades Principales Implementadas

1. **Gestión Curricular y de Asignaturas:** Soporte estructurado para las 4 asignaturas troncales del nivel:
   * Lenguaje y Comunicación
   * Matemática
   * Ciencias Naturales
   * Historia, Geografía y Ciencias Sociales
2. **Formularios con Validación Desacoplada:** Lógica de validación centralizada en el ViewModel, con respuesta de estado en tiempo real, mensajes de apoyo e iconos de advertencia en caso de datos inválidos.
3. **Interfaz Adaptada a Lectores Iniciales:** Tipografías legibles, distribución de elementos con baja carga cognitiva y animaciones funcionales de fluidez y confirmación (`AnimatedVisibility`).
4. **Persistencia de Datos en Tiempo Real (Room):** Modelado relacional de entidades (`User`, `Subject`, `Unit`, `LearningObjective`, `Progress`) con operaciones asíncronas de inserción y consulta en almacenamiento local.
5. **Integración Segura de Recursos Nativos:** Acceso controlado a recursos del dispositivo con gestión de permisos en tiempo de ejecución para la selección de material gráfico y soporte visual.

---

## Instrucciones de Instalación y Ejecución

### Requisitos Previos
* Android Studio (versión Jellyfish, Koala o superior).
* Java Development Kit (JDK) 17 instalado y seleccionado como Gradle JDK.
* Dispositivo físico Android (con Depuración por USB habilitada) o Emulador con Android 8.0 (API 26) o superior.

### Pasos para Clonar y Ejecutar

1. Clonar el repositorio en su equipo local:
   ```bash
   git clone https://github.com/miguelpastenm-web/MPAZ-PRO-2-App.git
   ```

2. Abrir Android Studio y seleccionar **Open**, navegando hasta la carpeta clonada.

3. Esperar la sincronización inicial de Gradle. Verificar que en `File → Settings → Build, Execution, Deployment → Build Tools → Gradle`, la opción **Gradle JDK** esté apuntando a JDK 17.

4. Conectar un dispositivo físico o iniciar un dispositivo virtual desde el **Device Manager**.

5. En la barra superior de Android Studio, verificar que el dispositivo objetivo esté seleccionado y hacer clic en el botón **Run 'app'** (o presionar `Shift + F10`).

6. Al utilizar las funciones de carga de imágenes o captura, conceder los permisos solicitados por el sistema operativo en pantalla.
