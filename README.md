# MPAZ PRO 2.0 — Aplicación móvil de apoyo didáctico

Aplicación móvil Android desarrollada en **Kotlin y Jetpack Compose** para el apoyo pedagógico, la realización de actividades didácticas y el seguimiento de resultados por Objetivos de Aprendizaje (OA) en Segundo Básico de la Escuela Escritora Marcela Paz, ubicada en San Bernardo.

- **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)
- **Institución:** Duoc UC
- **Evaluación:** Evaluaciones Parciales 4 y 5
- **Plataforma:** Android
- **Modalidad:** Aplicación Android con persistencia local (Room) e integración con microservicios Spring Boot ejecutados en el computador

---

## Integrantes del Equipo

- **Miguel Pastén**
- **Fabián Aguilar**

---

## Planificación y Gestión del Proyecto

El proyecto se desarrolla mediante una metodología de trabajo colaborativo, utilizando herramientas de planificación y control de versiones.

- **Tablero de Planificación (Trello):** [Tablero Trello - MPAZ PRO DSY1105](https://trello.com/b/M2z1OkvY/mpaz-pro-dsy1105)
- **Repositorio Android:** [MPAZ PRO 2 App](https://github.com/miguelpastenm-web/MPAZ-PRO-2-App)
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

**MPAZ PRO 2.0** propone una solución móvil que permite consultar actividades didácticas, responder preguntas de selección múltiple, recibir retroalimentación inmediata y almacenar resultados por Objetivo de Aprendizaje.

La aplicación incorpora formularios para configurar parámetros de evaluación, mecanismos de apoyo pedagógico, reportes de resultados y acceso a recursos nativos del dispositivo.

La app conserva los parámetros, los resultados de las evaluaciones y las actividades precargadas en el dispositivo. Utiliza microservicios locales para el catálogo curricular y el historial remoto, y consulta Dog CEO para la actividad de observación de perros.

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
- **SDK de Android.** API 35 para compilar y como destino. API 26 (Android 8.0) como mínimo.
- **Interfaz de usuario.** Jetpack Compose, Material 3 y Navigation Compose.
- **Arquitectura:** MVVM (Model-View-ViewModel) con separación de responsabilidades.
- **Persistencia local.** Room sobre SQLite, con KSP para generar las implementaciones.
- **Preferencias locales:** SharedPreferences.
- **Estado de la interfaz.** AndroidX Lifecycle y ViewModel con `StateFlow`.
- **Concurrencia:** Kotlin Coroutines.
- **Red.** Retrofit 2.11 con Gson y OkHttp.
- **Backend local:** Spring Boot 3 (Java 17) con H2, repositorio [mpaz-backend](https://github.com/miguelpastenm-web/mpaz-backend).
- **Pruebas.** JUnit 4 y JUnit 5, Kotest, MockK y kotlinx-coroutines-test. El módulo declara AndroidX Test, Espresso y Compose UI Test para las pruebas instrumentadas.
- **Cobertura:** JaCoCo (reporte de Android Gradle Plugin).
- **Acceso a datos:** DAO y patrón Repository.
- **Animaciones:** AnimatedVisibility, transiciones Fade/Slide y animateContentSize.
- **Recursos nativos:** Cámara del dispositivo y Storage Access Framework (SAF).
- **Control de versiones:** Git y GitHub.
- **Planificación del trabajo:** Trello.
- **Entorno de desarrollo:** Android Studio.
- **Compilación.** JDK 17, Gradle Wrapper 8.13 y Android Gradle Plugin 8.13.1. Las versiones de las dependencias están en [gradle/libs.versions.toml](gradle/libs.versions.toml).

---

## Funcionalidades Principales Implementadas

### 1. Gestión de Actividades Curriculares

La aplicación incorpora actividades didácticas para cuatro asignaturas de Segundo Básico:

- Lenguaje y Comunicación.
- Matemática.
- Ciencias Naturales.
- Historia y Geografía.

Room incluye **ocho actividades precargadas** asociadas a objetivos de aprendizaje. En **Panel Docente / UTP → Gestionar objetivos y actividades**, el docente puede listar, crear, editar y eliminar los objetivos de la asignatura seleccionada y las actividades de cada objetivo. También puede cambiar la autorización del objetivo. El catálogo se guarda en `ms-contenidos`.

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

El máximo de intentos se aplica al estudiante simulado cuando hay parámetros guardados para la asignatura. La fecha límite se valida al guardar el formulario y se muestra al estudiante.

### 3. Actividades Interactivas para Estudiantes

La pantalla `StudentActivityScreen` consulta las actividades de los objetivos remotos autorizados de la asignatura. Usa Room si falla la conexión, si la asignatura carece de objetivos remotos o si sus objetivos autorizados todavía no tienen actividades.

Sus características incluyen:

- Presentación de preguntas de selección múltiple.
- Alternativas de respuesta según las opciones disponibles en cada actividad.
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

Room registra el número de intento por objetivo para el estudiante simulado. Los resultados e intentos de las actividades remotas se agrupan por el objetivo local de cada asignatura. El historial remoto tiene su propio flujo de consulta y edición.

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

El botón **Escuchar Instrucción** usa Text-to-Speech de Android cuando el motor admite español.

---

## Microservicios y configuración del emulador

Los dos servicios son aplicaciones Spring Boot 3 (Java 17) con bases de datos H2 independientes. El código y los contratos están en [mpaz-backend](https://github.com/miguelpastenm-web/mpaz-backend).

| Servicio | URL base en el emulador | Rutas que consume Android |
|---|---|---|
| `ms-resultados` | `http://10.0.2.2:9081/` | `/api/resultados` (GET y POST), `/api/resultados/{id}` (GET, PUT y DELETE). El listado admite `estudianteId` u `oaId` como filtros. |
| `ms-contenidos` | `http://10.0.2.2:9082/` | `/api/objetivos` y `/api/actividades` (GET y POST), y sus rutas `/{id}` (GET, PUT y DELETE). El listado de actividades admite el filtro `oaId`. |

Las URL están definidas en `data/remote/RetrofitClient.kt`. En el emulador estándar, `10.0.2.2` apunta al equipo anfitrión. En un teléfono físico conectado a la misma red, sustituye `10.0.2.2` por la IP del anfitrión en las dos URL locales y recompila. El manifiesto declara el permiso `INTERNET` y permite el tráfico HTTP de desarrollo.

Instala JDK 17 en el anfitrión. Cada servicio incluye su Maven Wrapper (`mvnw`), que descarga Maven la primera vez. Clona el backend y ejecuta cada servicio desde su propia carpeta, en terminales distintas.

```bash
git clone https://github.com/miguelpastenm-web/mpaz-backend.git
```

Terminal de resultados (9081)

```bash
cd mpaz-backend/ms-resultados
./mvnw spring-boot:run
```

Terminal de contenidos (9082)

```bash
cd mpaz-backend/ms-contenidos
./mvnw spring-boot:run
```

- Desde **Mis Asignaturas → Mi Historial en el Servidor**, el estudiante puede consultar, crear, editar y eliminar registros de `ms-resultados`.
- Desde **Panel Docente / UTP → Gestionar objetivos y actividades**, el docente administra el catálogo de `ms-contenidos`. Cuando la asignatura tiene objetivos remotos, el estudiante recibe las actividades de los objetivos autorizados.
- Los repositorios remotos exponen los errores HTTP y de conexión a sus ViewModels. En contenidos, el mensaje del servidor se muestra cuando está disponible para HTTP 400 o 404.

![Pantalla Historial en el servidor](docs/capturas/historial-servidor.png)

## API externa Dog CEO

En **Mis Asignaturas → Conoce a los perros**, una foto aleatoria acompaña preguntas para observar y describir al animal. La pantalla indica cuándo está cargando, permite solicitar otra foto y ofrece reintentar si ocurre un error.

`DogCeoApi` consulta `GET https://dog.ceo/api/breeds/image/random` sin autenticación. `DogCeoRepository` usa la URL devuelta para descargar la foto desde `images.dog.ceo` y el ViewModel prepara la imagen para Compose. La pantalla necesita acceso a Internet y funciona independientemente de los microservicios locales. [Documentación de Dog CEO](https://dog.ceo/dog-api/documentation/random)

## Estructura general del proyecto

Las pantallas Compose observan estados publicados por los ViewModels. `AppRepository` accede a Room mediante sus DAO. Los repositorios remotos utilizan Retrofit con Gson para consultar cada servicio. Las preferencias de evaluación y perfil usan SharedPreferences.

```text
app/
├── src/main/
│   ├── AndroidManifest.xml
│   └── java/cl/duoc/mpazpro/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── local/         Base de datos, preferencias y almacenamiento de reportes
│       │   ├── remote/        RetrofitClient, interfaces de API y dto/
│       │   └── repository/    AppRepository y repositorios de cada API
│       ├── ui/
│       │   ├── components/    Campos validados y lector de instrucciones
│       │   ├── navigation/    Rutas y grafo de navegación
│       │   ├── screens/      Pantallas student/ y teacher/
│       │   └── theme/         Colores, tipografía y tema
│       └── viewmodel/         Estados y lógica de presentación
├── src/test/                 Pruebas unitarias JVM
└── src/androidTest/          Pruebas instrumentadas
docs/capturas/               Evidencias de ejecución
release/                     APK firmado del repositorio
```

## Instrucciones de Instalación y Ejecución

### Requisitos Previos

- Android Studio compatible con Android Gradle Plugin 8.13.1.
- Java Development Kit (JDK) 17.
- Android SDK Platform 35 instalado. Instala también Android SDK Build-Tools si vas a usar `apksigner`.
- Dispositivo físico o emulador con Android 8.0 (API 26) o superior.
- Conexión a Internet para descargar las dependencias durante la sincronización inicial de Gradle.

### Pasos para Clonar y Ejecutar

1. Clonar el repositorio:

   ```bash
   git clone --branch main https://github.com/miguelpastenm-web/MPAZ-PRO-2-App.git
   cd MPAZ-PRO-2-App
   ```

2. Abrir Android Studio y seleccionar **Open**.

3. Seleccionar la carpeta donde se clonó el proyecto.

4. Esperar la sincronización de Gradle.

5. Verificar que **Gradle JDK** esté configurado para utilizar JDK 17.

6. Crear o iniciar un dispositivo virtual mediante **Device Manager**, o conectar un dispositivo físico Android.

7. Para probar las pantallas remotas, inicia los servicios correspondientes en el anfitrión. Luego selecciona la configuración de ejecución `app`.

8. Presionar **Run** o utilizar `Shift + F10`.

9. Conceder los permisos necesarios cuando se utilicen recursos del dispositivo, como la cámara.

## Pruebas unitarias y compilación

Desde la raíz del repositorio, ejecuta los siguientes comandos con el Gradle Wrapper. En Windows, usa `gradlew.bat` en lugar de `./gradlew`.

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

- **Pruebas unitarias.** Los resultados XML quedan en `app/build/test-results/testDebugUnitTest/` y el informe HTML en `app/build/reports/tests/testDebugUnitTest/index.html`. La suite incluye pruebas de ambos microservicios y Dog CEO con respuestas simuladas.
- **APK de depuración.** El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.
- **Pruebas instrumentadas.** Ejecuta `./gradlew connectedDebugAndroidTest` con un emulador iniciado o un teléfono conectado.
- **Cobertura JaCoCo.** Ejecuta `./gradlew createDebugUnitTestCoverageReport`. El informe HTML queda en `app/build/reports/coverage/test/debug/index.html`.

## Capturas y evidencias de ejecución

Las evidencias están en `docs/capturas/`:

- Pantalla de la app **Mi Historial en el Servidor**, conectada a `ms-resultados` (ver captura en la sección de microservicios).
- [Resumen de 42 pruebas unitarias](docs/capturas/test-resultados-42.png), de la integración con `ms-resultados`.
- Cobertura JaCoCo del [repositorio remoto](docs/capturas/jacoco-repositorio.png) y del [ViewModel](docs/capturas/jacoco-viewmodel.png).
- Verificación de la firma del APK ([apksigner](docs/capturas/apk-firmado-apksigner.txt)) y datos del almacén de claves ([keytool](docs/capturas/keystore-jks-keytool.txt)).

Las capturas de pruebas y JaCoCo corresponden a la integración de resultados. En la revisión `4f8b6e3` se ejecutaron 80 pruebas unitarias, todas aprobadas, incluidas las de contenidos y Dog CEO.

## APK firmado

El repositorio incluye el [APK release firmado](release/mpaz-pro-release.apk) y el almacén de claves `keystore/mpaz-release.jks` (alias `mpaz`). La configuración `signingConfigs` de `app/build.gradle.kts` los usa en la variante `release`. Se versionan por requisito de la entrega académica; en un proyecto real, el almacén y sus credenciales se mantienen fuera del repositorio.

Para generar un APK con los cambios actuales:

```bash
./gradlew assembleRelease
cp app/build/outputs/apk/release/app-release.apk release/mpaz-pro-release.apk
```

Para comprobar la firma del APK y el almacén, usa `apksigner` de Android SDK Build-Tools y `keytool`:

```bash
apksigner verify --verbose release/mpaz-pro-release.apk
keytool -list -v -keystore keystore/mpaz-release.jks
```

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
- El máximo de intentos se aplica solo cuando el docente configuró la asignatura; si no la configuró, el estudiante no tiene límite. Los intentos se cuentan sobre el estudiante simulado.
- El catálogo remoto depende de la autorización del objetivo y de las actividades asociadas a él.
- La lectura en voz alta de las instrucciones depende del motor Text-to-Speech y del idioma español instalados en el dispositivo; si no están disponibles, no se reproduce.
- Los reportes representan los resultados efectivamente almacenados, pero no disponen de información real de matrícula del establecimiento.
- El sistema puede ampliarse con gestión integral de usuarios, cursos y evaluaciones.

---

## Posibles Mejoras Futuras

- Incorporar autenticación y perfiles de usuario.
- Registrar estudiantes y cursos reales.
- Incorporar el control de la fecha límite a la rendición de evaluaciones.
- Ampliar la cantidad de actividades y objetivos de aprendizaje.
- Generar reportes adicionales en PDF.
- Incorporar nuevas visualizaciones del progreso académico.

---

## Conclusión

**MPAZ PRO 2.0** integra herramientas de desarrollo móvil Android para ofrecer una experiencia didáctica interactiva y un mecanismo local de registro de resultados académicos.

El proyecto permite demostrar el uso de **Kotlin, Jetpack Compose, arquitectura MVVM, Room Database, StateFlow, validaciones desacopladas, recursos nativos y trabajo colaborativo mediante GitHub y Trello**.

La incorporación de evaluaciones almacenadas en Room, pistas configurables y reportes JSON basados en resultados reales constituye una base funcional para continuar desarrollando herramientas digitales de apoyo educativo.
