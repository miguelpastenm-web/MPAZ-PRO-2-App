# MPAZ-PRO-2.º_App_moviles

**MPAZ PRO 2.º - Aplicación móvil de apoyo didáctico para Segundo Básico**  
Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105)  
Institución: Duoc UC  

---

## 👥 Integrantes del Equipo

* **Miguel Pastén**
* **Fabián Aguilar**

---

## 🏫 Contexto Escolar y Problemática

La **Escuela Escritora Marcela Paz** es un establecimiento público, gratuito e inclusivo ubicado en la comuna de San Bernardo, que atiende a estudiantes desde Pre-Kínder hasta 8.º Básico.

### Diagnóstico del Problema
Actualmente, las actividades y evaluaciones de las asignaturas troncales de Segundo Básico se gestionan mediante guías impresas en papel. Esta metodología genera:
* Alta carga horaria para los docentes en impresión y corrección manual.
* Disgregación de la información en papel, impidiendo el registro consolidado del avance y el seguimiento individual por Objetivo de Aprendizaje (OA).
* Desafíos de inclusión pedagógica, ya que los estudiantes de 2.º Básico se encuentran en etapa de iniciación lectora y requieren soportes multimedia, oraciones concisas y retroalimentación guiada para trabajar con autonomía.

### Solución Propuesta (MVP)
**MPAZ PRO 2.º** digitaliza la entrega de actividades pedagógicas y la aplicación de pruebas finales. Proporciona una interfaz adaptada a lectores iniciales, control estricto de intentos de evaluación parametrizable por el docente, persistencia local para asegurar el registro de avance y reportes pedagógicos reservados para el equipo educador.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Kotlin
* **Entorno de Desarrollo:** Android Studio
* **Interfaz de Usuario (UI):** Jetpack Compose con directrices de **Material Design 3**
* **Patrón Arquitectónico:** MVVM (Model-View-ViewModel) con desacoplamiento de capas
* **Persistencia Local:** Room Database / SQLite
* **Gestión Asíncrona y Estado:** Coroutines, StateFlow y Kotlin State
* **Recursos Nativos:** Integración de hardware nativo (Cámara y Almacenamiento) con solicitud y control de permisos en tiempo de ejecución
* **Herramientas de Colaboración:** Git, GitHub y planificación en tablero Kanban de Trello

---

## 🚀 Funcionalidades Principales

1. **Gestión Curricular y de Formularios:** Formularios validados de forma desacoplada desde el ViewModel con retroalimentación visual inmediata ante datos inválidos.
2. **Interfaz Adaptada a Estudiantes:** Pantallas visuales intuitivas, baja carga cognitiva, textos legibles y animaciones funcionales de transición y confirmación (`AnimatedVisibility`).
3. **Persistencia de Datos en Tiempo Real:** Almacenamiento local mediante Room para registrar alumnos, intentos de evaluación, respuestas y progreso pedagógico.
4. **Módulo de Recursos Nativos:** Acceso seguro y funcional a recursos nativos del dispositivo (captura de foto de perfil con cámara y gestión de archivos locales).

---

## 💻 Instrucciones Técnicas de Instalación y Ejecución

### Requisitos Previos
* Android Studio (versión Hedgehog, Iguana o superior recomendada).
* JDK 17 o superior configurado en el entorno.
* Dispositivo físico Android o Emulador configurado con **Android 8.0 (API 26)** o superior.
* Conexión a internet para la descarga inicial de dependencias de Gradle.

### Pasos para Clonar y Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/](https://github.com/)[TU_USUARIO]/MPAZ-PRO-2.º_App_moviles.git