# NaviEat

Android app que gestiona tu dieta a partir del PDF que te entrega tu dietista.
Una IA (Gemini Flash por defecto, intercambiable) parsea el PDF a un plan
estructurado, te dice qué comer ahora según día/hora, te sugiere alternativas
para platos que no te apetezcan y genera la lista de la compra. Opcional:
empuja la lista a Microsoft To Do.

> Estado: scaffolding completo y listo para compilar. El primer build de
> GitHub Actions debería producir un APK debug funcional. Algunas piezas
> (auth de Microsoft, replicación visual fina) son intencionalmente mínimas
> para iterar sobre ellas.

## Stack

- Kotlin 2.0 + Jetpack Compose Material 3
- MVVM + Hilt para inyección de dependencias
- Room para persistencia local del plan y la lista de la compra
- DataStore para preferencias y API keys
- Retrofit + OkHttp + kotlinx.serialization
- PdfBox-Android para extraer texto del PDF
- MSAL para autenticación con Microsoft (Graph → Tasks.ReadWrite)
- GitHub Actions para empaquetar el APK

## Estructura

```
app/src/main/java/com/navieat/app
├── MainActivity.kt
├── NaviEatApplication.kt
├── domain/        # modelos puros y contratos de repositorio
├── data/
│   ├── local/     # Room (entities, DAOs, database, converters)
│   ├── preferences/
│   ├── ai/        # AiProvider + GeminiProvider + Prompts
│   ├── pdf/       # PdfTextExtractor con PdfBox-Android
│   ├── microsoft/ # MicrosoftAuthManager + GraphTodoClient
│   └── repository/
├── di/            # Hilt modules
└── ui/
    ├── theme/
    └── screens/{home,plan,shopping,settings}
```

## Qué tienes que configurar tú

### 1. Repositorio en GitHub

Crea un repo nuevo y empuja esta carpeta:

```bash
cd /Users/vimorera/PersonalProjects/navieat-app
git add .
git commit -m "Initial scaffolding"
git branch -M main
git remote add origin git@github.com:<tu-usuario>/navieat-app.git
git push -u origin main
```

A partir del primer push, GitHub Actions ejecutará el workflow de
`.github/workflows/build.yml` y subirá el APK debug como **artifact** que
puedes descargar desde la pestaña *Actions*.

### 2. API key de Gemini (gratis)

1. Ve a [aistudio.google.com](https://aistudio.google.com/) → *Get API key*.
2. Copia la key.
3. Abre la app instalada → *Settings* → pega la key en "Gemini API key".

### 3. Microsoft To Do (opcional, para enviar la lista)

1. Entra en [portal.azure.com](https://portal.azure.com/) → *Microsoft Entra
   ID* → *App registrations* → *New registration*.
2. Nombre: `NaviEat`. Supported account types: *Accounts in any organizational
   directory and personal Microsoft accounts*.
3. *Authentication* → *Add a platform* → *Android*. Package name:
   `com.navieat.app`. Para el **Signature hash** del debug keystore corre:
   ```bash
   keytool -exportcert -alias androiddebugkey \
     -keystore ~/.android/debug.keystore -storepass android -keypass android \
     | openssl sha1 -binary | openssl base64
   ```
4. *API permissions* → *Microsoft Graph* → *Delegated* → `Tasks.ReadWrite`.
5. Copia el **Application (client) ID** y pégalo en
   `app/src/main/res/raw/msal_config.json` reemplazando `REPLACE_WITH_AZURE_CLIENT_ID`.
6. En el mismo archivo y en `AndroidManifest.xml`, sustituye
   `REPLACE_WITH_SIGNATURE_HASH` / `SIGNATURE_HASH_PLACEHOLDER` por el hash del
   paso 3 (URL-encoded: el carácter `=` queda como `%3D`).

### 4. Firmar el APK release (opcional, solo si quieres distribuir)

Genera un keystore una vez:

```bash
keytool -genkey -v -keystore navieat-release.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias navieat
```

En el repo, configura estos *Repository secrets* (Settings → Secrets → Actions):

- `KEYSTORE_BASE64` → `base64 -i navieat-release.jks`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS` → `navieat`
- `KEY_PASSWORD`

Luego dispara el workflow desde la pestaña *Actions* → *Build APK* →
*Run workflow* → *Build type: release*. Si etiquetas un commit con `v0.1.0`,
además se crea un *Release* con el APK adjunto.

## Cómo testear el APK

Bájalo del artifact de GitHub Actions, mándalo al móvil (AirDrop, email,
Telegram, lo que sea), y al abrirlo Android te pedirá habilitar "Instalar apps
desconocidas" para esa fuente. Acepta y se instala.

## Cosas que faltan (TODOs marcados en el código)

- Cifrado de las API keys con Android Keystore (ahora viven en DataStore en
  texto plano, aceptable para uso personal).
- Recordatorios push por hora de comida (WorkManager + NotificationCompat).
- Cambiar el `AiProvider` activo desde Settings (la implementación pluggable
  está, falta el binding dinámico).
- Tests unitarios e instrumentados (esqueletos vacíos).
- Iconos finales de la app.

## Iteración

Pídele a Claude lo siguiente cuando quieras avanzar:

- "Añade soporte para Bedrock Kimi como AiProvider"
- "Implementa recordatorios push antes de cada comida"
- "Añade pantalla de histórico de comidas seguidas vs saltadas"
- "Cifra las API keys con EncryptedSharedPreferences"
