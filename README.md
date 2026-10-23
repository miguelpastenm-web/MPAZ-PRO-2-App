# MPAZ PRO 2.º - Aplicación Móvil de Apoyo Didáctico

Aplicación móvil Android desarrollada en **Kotlin y Jetpack Compose** para el apoyo pedagógico, la realización de actividades didácticas y el seguimiento de resultados por Objetivos de Aprendizaje (OA) en Segundo Básico de la Escuela Escritora Marcela Paz, ubicada en San Bernardo.

- **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)
- **Institución:** Duoc UC
- **Evaluación:** Evaluación Parcial 2 / Evaluación Parcial 3
- **Plataforma:** Android
- **Modalidad:** Aplicación local con persistencia de datos

---

## Integrantes del Equipo

- **Miguel Pastén**
- **Alberto Aguilar**

---

## Planificación y Gestión del Proyecto

El proyecto se desarrolla mediante una metodología de trabajo colaborativo, utilizando herramientas de planificación y control de versiones conforme al indicador IE 2.3.2.

- **Tablero de Planificación (Trello):** [Tablero Trello - MPAZ PRO DSY1105](https://trello.com/b/M2z1OkvY/mpaz-pro-dsy1105)
- **Repositorio GitHub:** [MPAZ PRO 2 App](https://github.com/miguelpastenm-web/MPAZ-PRO-2-App)
- **Estrategia de Versionamiento:** Flujo de trabajo mediante ramas independientes, commits descriptivos e integración de cambios.
- **Convención de Commits:** Uso de prefijos como `feat`, `fix`, `chore`, `docs` y `refactor`.

La utilización de GitHub y Trello permite registrar el avance del proyecto, organizar las tareas y evidenciar la participación de los integrantes.

---

## Contexto Escolar y Problemática

La **Escuela Escritora Marcela Paz** es un establecimiento público, gratuito e inclusivo ubicado en San Bernardo, que atiende a estudiantes desde Pre-Kínder hasta 8.º Básico.

### Diagnóstico de la Situación Actual

En el escenario planteado, las actividades y evaluaciones de las asignaturas principales se gestionan mediante guías impresas. Esta modalidad presenta dificultades como:

- Alta carga de trabajo docente en la preparación, distribución y corrección de actividades.
- Dificultad para consolidar resultados y mantener un historial organizado por Objetivo de Aprendizaje.
- Limitaciones para ofrecer retroalimentación inmediata al estudiante.
- Necesidad de interfaces sencillas y apoyos pedagógicos adecuados para estudiantes que se encuentran en etapas iniciales de lectura.

### Solución Desarrollada (MVP)

**MPAZ PRO 2.º** propone una solución móvil que permite consultar actividades didácticas, responder preguntas de selección múltiple, recibir retroalimentación inmediata y almacenar resultados por Objetivo de Aprendizaje.

La aplicación incorpora formularios para configurar parámetros de evaluación, mecanismos de apoyo pedagógico, reportes de resultados y acceso a recursos nativos del dispositivo.

Su funcionamiento principal es local, utilizando **Room Database, SQLite y SharedPreferences**, sin requerir servicios externos para realizar actividades o consultar los datos almacenados.

Para fines académicos y de demostración, se utilizan actividades precargadas e identificadores de estudiantes simulados.

### Perfiles Considerados

El proyecto contempla tres perfiles dentro del contexto del sistema:

1. **Jefatura UTP:** Perfil considerado dentro de la estructura curricular y de los objetivos de aprendizaje.
2. **Docente de Segundo Básico:** Acceso a la configuración de parámetros de evaluación y generación de reportes académicos.
3. **Estudiante de Segundo Básico:** Acceso a actividades interactivas con selección de respuestas, retroalimentación y ayudas pedagógicas configurables.

---

## Tecnologías Utilizadas

- **Lenguaje de programación:** Kotlin.
- **Plataforma:** Android.
- **SDK mínimo:** API 26 (Android 8.0).
- **Interfaz de usuario:** Jetpack Compose y Material Design 3.
- **Arquitectura:** MVVM (Model-View-ViewModel) con separación de responsabilidades.
- **Persistencia local:** Room Database y SQLite.
- **Preferencias locales:** SharedPreferences.
- **Gestión de estados:** StateFlow y MutableStateFlow.
- **Concurrencia:** Kotlin Coroutines.
- **Acceso a datos:** DAO y patrón Repository.
- **Animaciones:** AnimatedVisibility, transiciones Fade/Slide y animateContentSize.
- **Recursos nativos:** Cámara del dispositivo y Storage Access Framework (SAF).
- **Control de versiones:** Git y GitHub.
- **Planificación del trabajo:** Trello.
- **Entorno de desarrollo:** Android Studio.
- **JDK:** Java 17.

---

## Funcionalidades Principales Implementadas

### 1. Gestión de Actividades Curriculares

La aplicación incorpora actividades didácticas para cuatro asignaturas de Segundo Básico:

- Lenguaje y Comunicación.
- Matemática.
- Ciencias Naturales.
- Historia y Geografía.

Las actividades se almacenan en Room y están relacionadas con Objetivos de Aprendizaje.

Actualmente se dispone de **ocho actividades precargadas**, utilizadas para demostrar el funcionamiento de las evaluaciones.

### 2. Formularios con Validaciones Desacopladas

El formulario docente permite ingresar y configurar:

- Asignatura a evaluar.
- Nombre del docente.
- Código o identificador institucional.
- Máximo de intentos permitido, entre 1 y 5.
- Fecha límite de rendición.
- Activación o desactivación de pistas pedagógicas.
- Confirmación de los parámetros mediante Checkbox.

La validación se encuentra centralizada en `TeacherFormViewModel`, separando la lógica de negocio de los componentes visuales.

Se implementaron las siguientes verificaciones:

- Campos obligatorios.
- Longitud mínima del nombre y código docente.
- Valores numéricos dentro del rango permitido.
- Formato de fecha `AAAA-MM-DD`.
- Rechazo de fechas inexistentes o anteriores a la fecha actual.
- Confirmación obligatoria antes de aceptar los parámetros.

Cuando existen errores, los campos muestran mensajes específicos e indicadores visuales. Cuando los datos son válidos, aparece una confirmación mediante una animación.

**Nota:** El máximo de intentos se valida y configura en el formulario docente, pero su aplicación como restricción efectiva sobre las evaluaciones del estudiante queda pendiente de integración.

### 3. Actividades Interactivas para Estudiantes

La pantalla `StudentActivityScreen` obtiene las actividades almacenadas en Room y presenta una interfaz adaptada para estudiantes de Segundo Básico.

Sus características incluyen:

- Presentación de preguntas de selección múltiple.
- Cuatro alternativas por actividad.
- Distribución aleatoria de las alternativas mediante `shuffled()`.
- Verificación de la respuesta seleccionada.
- Retroalimentación visual inmediata.
- Indicador de avance entre actividades.
- Navegación progresiva entre desafíos.
- Posibilidad de iniciar una nueva evaluación al finalizar.

Las respuestas se registran temporalmente durante la evaluación y luego se envían al ViewModel para calcular los resultados.

### 4. Pistas Pedagógicas Configurables

El formulario docente incorpora un Switch denominado **Permitir pistas pedagógicas**.

Su configuración permite:

- Activar las pistas para mostrar la sección de ayuda guiada.
- Desactivar las pistas para ocultar dicha sección en las actividades del estudiante.
- Conservar la preferencia mediante almacenamiento local.

Esta funcionalidad utiliza `SharedPreferences` a través de `ConfiguracionEvaluacion.kt`.

La pantalla del estudiante consulta esta preferencia para determinar si debe mostrar u ocultar la ayuda guiada.

### 5. Arquitectura MVVM y Persistencia con Room

El proyecto utiliza una estructura modular que separa la interfaz de usuario, la lógica de presentación y el almacenamiento.

Las principales entidades utilizadas son:

- `DocenteEntity`
- `ObjetivoAprendizajeEntity`
- `ActividadEntity`
- `ResultadoPruebaEntity`

Entre los componentes relevantes se encuentran:

- **AppDatabase:** Configuración principal de Room.
- **DAO:** Interfaces para ejecutar operaciones sobre las tablas.
- **AppRepository:** Centraliza las operaciones de acceso a datos.
- **StudentActivityViewModel:** Administra la carga de actividades, respuestas y resultados.
- **StudentActivityViewModelFactory:** Permite construir el ViewModel entregándole el repositorio.
- **TeacherFormViewModel:** Gestiona el estado y las validaciones del formulario docente.

Para administrar cambios en la estructura de la base de datos se implementó `MIGRATION_1_2`, que incorpora tres columnas de alternativas incorrectas a las actividades existentes sin eliminar sus registros.

### 6. Registro y Persistencia de Resultados

Al finalizar una evaluación, la aplicación:

1. Comprueba que las actividades estén respondidas.
2. Agrupa las preguntas según su Objetivo de Aprendizaje.
3. Calcula los aciertos por objetivo.
4. Obtiene el porcentaje de logro correspondiente.
5. Guarda los resultados mediante el repositorio y Room.

Los resultados se almacenan en la tabla `resultados_prueba` y contienen:

- Identificador del registro.
- Identificador ficticio del estudiante.
- Identificador del Objetivo de Aprendizaje.
- Puntaje obtenido.
- Campo de intentos utilizados.
- Fecha de registro.

Se utiliza una operación de inserción múltiple para guardar conjuntamente los resultados de una evaluación.

Los registros permanecen almacenados después de cerrar y volver a abrir la aplicación.

**Nota:** El modelo actual utiliza un identificador de estudiante simulado y el campo `intentosUsados` todavía no representa un contador real de reintentos.

### 7. Generación de Reportes JSON con Resultados Reales

La aplicación permite generar reportes académicos en formato JSON utilizando los registros almacenados en Room.

La información del reporte incluye:

- Establecimiento educacional.
- Comuna.
- Nivel educativo.
- Curso.
- Docente evaluador.
- Asignatura.
- Fecha de emisión.
- Cantidad de registros de resultados.
- Porcentaje promedio de logro.
- Resultados agrupados por Objetivo de Aprendizaje.

Los resultados se pueden **filtrar por asignatura**, evitando incluir objetivos que no correspondan a la materia seleccionada.

Las estadísticas son calculadas desde los registros disponibles en la base de datos, en lugar de utilizar porcentajes ficticios.

Cuando no existen resultados para una asignatura, el reporte puede representar la ausencia de información mediante una lista vacía y un promedio nulo.

La cantidad de registros representa resultados almacenados por objetivo, no necesariamente la cantidad de estudiantes o evaluaciones completas.

### 8. Almacenamiento y Exportación de Reportes

MPAZ PRO incorpora dos mecanismos de almacenamiento:

**Almacenamiento privado de la aplicación**

Permite generar y guardar reportes JSON dentro del almacenamiento interno de Android.

**Exportación mediante Storage Access Framework (SAF)**

Permite seleccionar una ubicación y guardar el documento mediante el gestor de archivos del dispositivo.

La aplicación también incluye una interfaz para consultar los reportes almacenados localmente.

Ambos métodos fueron probados durante el desarrollo.

### 9. Integración de Recursos Nativos

Se implementó acceso a recursos nativos de Android:

**Cámara del dispositivo**

La aplicación permite capturar una fotografía para el perfil y conservarla en el almacenamiento privado de la aplicación.

**Storage Access Framework**

Se utiliza para exportar reportes JSON a ubicaciones seleccionadas por el usuario.

Estas funcionalidades utilizan mecanismos nativos de Android y contratos de actividad.

### 10. Interfaz Adaptada y Animaciones

La interfaz utiliza componentes de Material Design 3 con un diseño orientado a facilitar la navegación y la lectura.

Entre los elementos implementados se encuentran:

- Tipografías legibles.
- Botones amplios.
- Indicadores de carga.
- Mensajes de error y confirmación.
- Retroalimentación mediante colores e iconos.
- Animaciones de entrada y salida.
- Expansión de tarjetas de ayuda guiada.
- Transiciones visuales entre actividades.

Se utilizan componentes como `AnimatedVisibility`, `CircularProgressIndicator` y `animateContentSize`.

El botón **Escuchar Instrucción** presenta el texto de apoyo de la actividad. La reproducción de audio real no forma parte de las funcionalidades verificadas de esta versión.

---

## Estructura General del Proyecto

```text
cl.duoc.mpazpro
│
├── data
│   ├── local
│   │   ├── dao
│   │   ├── entities
│   │   ├── AppDatabase.kt
│   │   ├── DatabaseMigrations.kt
│   │   ├── DatosInicialesCallback.kt
│   │   ├── ConfiguracionEvaluacion.kt
│   │   └── ReportStorageManager.kt
│   │
│   └── repository
│       └── AppRepository.kt
│
├── ui
│   ├── components
│   ├── navigation
│   ├── screens
│   │   ├── admin
│   │   ├── student
│   │   └── profile
│   └── theme
│
├── viewmodel
│   ├── StudentActivityViewModel.kt
│   ├── StudentActivityViewModelFactory.kt
│   ├── TeacherFormViewModel.kt
│   └── TeacherFormUiState.kt
│
└── MainActivity.kt
```

---

## Instrucciones de Instalación y Ejecución

### Requisitos Previos

- Android Studio compatible con el proyecto.
- Java Development Kit (JDK) 17.
- Android SDK configurado.
- Dispositivo físico o emulador con Android 8.0 (API 26) o superior.
- Conexión a Internet para descargar las dependencias durante la sincronización inicial de Gradle.

### Pasos para Clonar y Ejecutar

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/miguelpastenm-web/MPAZ-PRO-2-App.git
   ```

2. Abrir Android Studio y seleccionar **Open**.

3. Seleccionar la carpeta donde se clonó el proyecto.

4. Esperar la sincronización de Gradle.

5. Verificar que **Gradle JDK** esté configurado para utilizar JDK 17.

6. Crear o iniciar un dispositivo virtual mediante **Device Manager**, o conectar un dispositivo físico Android.

7. Seleccionar la configuración de ejecución `app`.

8. Presionar **Run** o utilizar `Shift + F10`.

9. Conceder los permisos necesarios cuando se utilicen recursos del dispositivo, como la cámara.

### Consideración sobre las ramas

El repositorio utiliza `main` como rama principal y ramas de trabajo para el desarrollo colaborativo.

Para utilizar la versión definitiva del proyecto se recomienda comprobar que `main` incluya las últimas funcionalidades integradas y verificadas.

---

## Pruebas Funcionales Realizadas

Durante el desarrollo se efectuaron pruebas en un emulador Android, incluyendo:

- Compilación del proyecto después de integrar las ramas de trabajo.
- Carga de actividades desde Room.
- Presentación de preguntas y alternativas.
- Retroalimentación para respuestas correctas e incorrectas.
- Registro de resultados de evaluación en SQLite.
- Validación de campos obligatorios del formulario docente.
- Rechazo de intentos fuera del rango permitido.
- Validación de fechas inexistentes y anteriores a la fecha actual.
- Funcionamiento del Checkbox de confirmación.
- Activación y desactivación de pistas pedagógicas.
- Guardado de preferencias mediante SharedPreferences.
- Generación de reportes JSON a partir de resultados de Room.
- Filtrado de los reportes por asignatura.
- Almacenamiento privado de reportes.
- Exportación de documentos mediante SAF.
- Captura y almacenamiento de fotografías de perfil.

Estas pruebas permitieron comprobar el funcionamiento de las características principales implementadas en el MVP.

---

## Limitaciones de la Versión Actual

Por tratarse de un proyecto académico desarrollado como MVP, existen aspectos que pueden ampliarse en futuras versiones:

- La identificación de estudiantes utiliza registros simulados.
- El máximo de intentos configurable todavía no limita efectivamente la cantidad de evaluaciones realizadas.
- La selección de asignatura del formulario docente no controla automáticamente todas las actividades que aparecen al estudiante.
- La reproducción de audio real para las instrucciones todavía no está implementada.
- Los reportes representan los resultados efectivamente almacenados, pero no disponen de información real de matrícula del establecimiento.
- El sistema puede ampliarse con gestión integral de usuarios, cursos y evaluaciones.

---

## Posibles Mejoras Futuras

- Incorporar autenticación y perfiles de usuario.
- Registrar estudiantes y cursos reales.
- Implementar el control efectivo de intentos por evaluación.
- Aplicar automáticamente los parámetros docentes a la selección de actividades.
- Incorporar lectura de instrucciones mediante Text-to-Speech.
- Ampliar la cantidad de actividades y objetivos de aprendizaje.
- Generar reportes adicionales en PDF.
- Incorporar nuevas visualizaciones del progreso académico.

---

## Conclusión

**MPAZ PRO 2.º** integra herramientas de desarrollo móvil Android para ofrecer una experiencia didáctica interactiva y un mecanismo local de registro de resultados académicos.

El proyecto permite demostrar el uso de **Kotlin, Jetpack Compose, arquitectura MVVM, Room Database, StateFlow, validaciones desacopladas, recursos nativos y trabajo colaborativo mediante GitHub y Trello**.

La incorporación de evaluaciones almacenadas en Room, pistas configurables y reportes JSON basados en resultados reales constituye una base funcional para continuar desarrollando herramientas digitales de apoyo educativo.
