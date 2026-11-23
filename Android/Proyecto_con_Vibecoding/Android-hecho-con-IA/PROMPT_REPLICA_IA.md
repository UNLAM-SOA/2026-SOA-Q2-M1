

Quiero que crees desde cero, en esta carpeta vacía, una app Android nativa para un
gimnasio. Es un trabajo práctico de la materia "Sistemas Operativos Avanzados" (SOA):
la app es el cliente móvil de un sistema embebido/IoT. El gimnasio tiene una puerta
con cerradura controlada por un microcontrolador y un sensor que cuenta cuánta gente hay
adentro. Los dos hablan por MQTT y un backend en Node-RED expone una API HTTP para los
usuarios.

No partas de ningún repositorio ni código existente: diseñá e implementá todo vos. Las
decisiones de arquitectura, nombres de clases, diseño visual y organización de paquetes
son tuyas, siempre que cumplas los requisitos de abajo.

## CONFIGURACIÓN (editá estos valores)

- Nombre de la app: `GymPro` (o el que prefieras)
- Package / applicationId: `com.soa.gympro`
- Lenguaje: **Java** (no Kotlin), con Gradle Kotlin DSL (`build.gradle.kts`) y version catalog (`gradle/libs.versions.toml`)
- minSdk 24, target/compile SDK: el último estable que tengas disponible
- URL base de la API HTTP: `https://soa.alfatechnologies.com.ar/`
- URI del broker MQTT: `wss://soa.alfatechnologies.com.ar/mqtt` (WebSocket seguro)
- Topic de ocupación (la app se suscribe): `gimnasio/cantidadPersonas`
- Topic de apertura de puerta (la app publica): `gimnasio/puerta/abrirPuerta`
- Idioma de la interfaz, comentarios y commits: **español**

Todos estos valores tienen que quedar en un único lugar fácil de cambiar
(por ejemplo `BuildConfig` o una clase de constantes). No los repitas por el código.

## Funcionalidad

1. **Login**
   - Pantalla de inicio con DNI y contraseña.
   - Llama a `GET {API}/login?dni=...&clave=...`.
   - Respuesta 200: `{ "ok": true, "nombre": "...", "estado": true|false }`
     (`estado` indica si el socio tiene la cuota al día).
   - Respuesta 400/401: `{ "error": "mensaje" }`. Mostrá ese mensaje al usuario.
   - Si el login sale bien, guardá la sesión (DNI y nombre) para no pedirla de nuevo al abrir la app.
   - Registrá cada ingreso a la app en la base local (DNI, nombre y fecha/hora).
   - Link a la pantalla de registro.

2. **Registro**
   - Campos: DNI, nombre, apellido, contraseña y confirmación de la contraseña.
   - Validaciones del lado del cliente (campos vacíos, DNI numérico, contraseñas que coincidan, largo mínimo).
   - Llama a `POST {API}/registrar` con body JSON `{ "dni", "clave", "nombre", "apellido" }`.
   - Respuesta 200 (y también 500): `{ "ok": bool, "estado": "..." }`; 400/409: `{ "error": "..." }`
     (409 = el DNI ya existe). Mostrá mensajes claros en cada caso.

3. **Menú principal** (después de loguearse)
   - Saludo con el nombre del socio y un indicador del estado de la cuota.
   - Indicador del estado de la conexión MQTT (conectando / conectado / reconectando / sin conexión).
   - Accesos a: ocupación del gimnasio, abrir puerta, historial de pagos y cerrar sesión.

4. **Ocupación del gimnasio en tiempo real**
   - Suscripción al topic de ocupación. El payload es JSON: `{ "cantidadPersonasActual": <int o null> }`
     (si viene `null`, tomalo como 0; si el JSON es inválido, logueá el error sin crashear).
   - Mostrá la cantidad en grande, con algún indicador visual de qué tan lleno está
     (por ejemplo, una barra o un color según un aforo máximo configurable).
   - Al entrar a la pantalla, mostrá el último valor recibido aunque haya llegado antes.

5. **Abrir la puerta desde el celular**
   - Botón que publica un mensaje en el topic de apertura. Elegí y documentá el formato
     del payload (por ejemplo JSON con el DNI del socio y un timestamp).
   - Solo se habilita si la cuota está al día y hay conexión MQTT.
   - Feedback al usuario (enviado / error) y evitar envíos repetidos por doble tap.

6. **Historial de pagos**
   - Lista (RecyclerView) de los pagos del socio guardados en la base local: monto, fecha y concepto.
   - Como todavía no hay pasarela de pagos real, cargá datos de ejemplo la primera vez
     y dejalo aclarado en el README.

## Requisitos técnicos

- **MQTT**: usá un cliente que funcione en Android 13+ (la librería
  `org.eclipse.paho.android.service` original está abandonada y crashea en API 33+ por el
  `RECEIVER_EXPORTED`). Buscá una alternativa mantenida (por ejemplo el fork
  `com.github.hannesa2:paho.mqtt.android` vía JitPack, o HiveMQ MQTT Client) y justificá la elección en el README.
  - Una sola conexión compartida por toda la app (singleton o similar), con reconexión
    automática, clientId persistente, resuscripción al reconectar y listeners por topic.
  - Nada de trabajo de red en el hilo principal: los callbacks que tocan la UI tienen que volver al main thread.
  - Hay que soportar WebSocket seguro (`wss://`).
- **HTTP**: Retrofit + Gson (o equivalente), con timeouts razonables y manejo de errores
  de red (sin conexión, timeout, cuerpo de error JSON).
- **Persistencia local**: Room para usuarios/ingresos/pagos y SharedPreferences para la sesión.
- **UI**: Material Components, layouts en XML, soporte de modo oscuro, ícono propio de la app
  (vector drawable) y textos en `strings.xml`.
- **Permisos**: solo los necesarios (INTERNET, ACCESS_NETWORK_STATE, y WAKE_LOCK si el cliente MQTT lo pide).
- Manejo de ciclo de vida: registrar/desregistrar listeners en `onStart`/`onStop` (o equivalente) para no perder memoria.
- Código limpio y comentado en español, separado en paquetes con sentido (por ejemplo `ui`, `data`, `mqtt`, `api`).
- Al menos algunos tests unitarios de la lógica que no depende de Android (parseo del payload de ocupación, validaciones del registro).

## Git

- Ejecutá `git init` con rama principal `main` y un `.gitignore` adecuado para Android/Gradle
  (que excluya `build/`, `.gradle/`, `local.properties`, `*.iml` y la mayor parte de `.idea/`).
- Incluí el Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) para que el proyecto compile sin instalar Gradle.
- Trabajá en commits chicos y descriptivos en español, siguiendo el avance real, por ejemplo:
  1. estructura base del proyecto
  2. login y registro contra la API
  3. conexión MQTT y ocupación en tiempo real
  4. apertura de puerta
  5. historial de pagos y Room
  6. README y documentación
- Si querés, usá ramas `feature/...` y mergealas a `main`.

## README.md

Que incluya:
- Descripción del proyecto y su contexto (TP de SOA, sistema embebido del gimnasio).
- Diagrama simple de la arquitectura (app ↔ broker MQTT ↔ microcontrolador, app ↔ API Node-RED), en Mermaid o ASCII.
- Funcionalidades y capturas (dejá placeholders para las capturas).
- Tabla de topics MQTT con dirección (publica/suscribe) y formato del payload.
- Tabla de endpoints HTTP con request y respuestas.
- Requisitos y pasos para compilar y correr (Android Studio y `./gradlew assembleDebug`).
- Cómo probar MQTT localmente con Mosquitto (`mosquitto_sub` / `mosquitto_pub`) y `adb reverse tcp:1883 tcp:1883` si se usa un broker local desde el emulador.
- Decisiones técnicas y limitaciones conocidas (pagos simulados, etc.).
- Aclaración de que el proyecto fue generado íntegramente con IA (Claude Code) como experimento.

## Cómo trabajar

1. Antes de escribir código, mostrame un plan corto: arquitectura, paquetes, librerías y versiones.
2. Después implementalo de punta a punta sin esperar confirmación en cada paso.
3. Al terminar, corré `./gradlew assembleDebug` y `./gradlew test` y arreglá los errores hasta
   que compile y pasen los tests. Si no hay Android SDK en esta máquina, avisame qué falta
   en vez de dar por hecho que compila.
4. Cerrá con un resumen: qué se hizo, qué quedó pendiente y cómo probarlo.
