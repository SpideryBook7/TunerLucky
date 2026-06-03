# LuckyTuner: Asistente y Optimizador del Sistema (Shizuku)

El objetivo es desarrollar una aplicación Android nativa diseñada específicamente para el hardware del **Meizu Lucky 08** (Snapdragon 7s Gen 2, 12GB RAM) y Flyme OS 11. 

La aplicación funcionará como un "Game Space" al estilo RedMagic, pero mucho más potente, integrando herramientas de control de energía, estabilidad de red y optimización profunda a través de la API de **Shizuku**.

> [!IMPORTANT]
> **Aclaración sobre Shizuku y el WiFi:**
> Tienes mucha razón al preocuparte de que Shizuku requiere depuración inalámbrica (y por tanto WiFi) para encenderse. Sin embargo, hay una excelente noticia: **Los cambios que hagamos con Shizuku son persistentes.** Si la app congela YouTube o cambia la configuración de la antena de red, esa orden se queda grabada en el cerebro de Android. Aunque apagues el WiFi, reinicies el teléfono o Shizuku se desconecte, YouTube seguirá sin gastar batería en segundo plano y tu WiFi seguirá estable. Solo necesitas encender Shizuku cuando quieras *cambiar* la configuración dentro de la app.

## User Review Required

Por favor revisa esta arquitectura general. Al ser una aplicación nueva desde cero, necesitamos definir bien las bases antes de empezar a escribir código. 

## Open Questions

1. **¿Nombre de la app?:** Por ahora la he llamado "LuckyTuner", pero si tienes un nombre en mente (como EmuSpace, MeizuTuner, etc.), dímelo.
2. **¿Overlay Flotante?:** ¿Te gustaría que en el "Modo Juego" incluyamos una pequeña burbuja o menú lateral que puedas abrir mientras juegas para monitorear los FPS o la RAM, al estilo RedMagic?
3. **¿Aesthetic/Diseño?:** Al ser estilo RedMagic, ¿prefieres una interfaz de usuario oscura, moderna y con toques "Gamer" (luces de neón, gráficos de rendimiento)?

## Proposed Changes

Vamos a crear un nuevo proyecto Android (Kotlin + Jetpack Compose) estructurado en los siguientes módulos:

### 1. Interfaz Principal (Estilo Game Space)
- Un panel principal que mostrará pestañas independientes que puedes encender y apagar a voluntad:
  - **Módulo Gaming:** Lanza tus emuladores. Al activarse, inyecta comandos para fijar los Hz de pantalla, limpiar memoria (Force-Stop) y desactivar servicios de Flyme en segundo plano.
  - **Módulo de Energía:** Una lista de todas tus apps instaladas. Seleccionas apps como YouTube o redes sociales, y la app inyecta los comandos `AppOps` (`WAKE_LOCK ignore`, `RUN_IN_BACKGROUND ignore`).
  - **Módulo de Red:** Un interruptor (Toggle) para aplicar los parches de estabilidad WiFi (`wifi_suspend_optimizations_enabled 0`).
  - **Driver Vault:** Un gestor visual para descargar e instalar drivers Turnip.
  - **Mapeo de Botón IA:** Una función para interceptar el botón físico de IA (Power button double-tap/long-press) y reconfigurarlo para que lance el "Game Space" en lugar del asistente de Meizu, logrando la verdadera experiencia RedMagic.

### 2. Integración Core con Shizuku
- Implementación de la librería `rikka.shizuku:api`.
- Se creará un puente (Service) que verificará al abrir la app si Shizuku está corriendo. Si no lo está, mostrará las opciones guardadas anteriormente pero no dejará modificarlas hasta que lo actives.

### 3. Base de Datos Local
- Usaremos `Room` (Base de datos nativa de Android) o `DataStore` para guardar tus preferencias (qué apps están restringidas, si el fix de red está activado, etc.). Así, la interfaz siempre sabrá en qué estado está tu teléfono.

## Verification Plan

### Automated Tests
- Validaremos que los comandos ADB/Shell se ejecuten correctamente sin lanzar errores (ej. `pm list packages`).
- Verificaremos que el puente con Shizuku maneje correctamente las desconexiones.

### Manual Verification
1. **Despliegue:** Generaremos un archivo APK que podrás instalar en tu Meizu.
2. **Prueba de Red:** Aplicarás el parche de WiFi y comprobarás durante 1 o 2 días si las desconexiones desaparecen.
3. **Prueba de Batería:** Restringirás YouTube y verificaremos el gestor de batería de Flyme al final del día.
4. **Prueba de Emulación:** Lanzaremos Yuzu/Sudachi desde el Game Space comprobando que la fluidez mejora al forzar los Hz de pantalla y la limpieza de RAM.
