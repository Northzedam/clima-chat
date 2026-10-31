# Clima Chat

Aplicación Android nativa de clima, completamente en español. Está desarrollada con Java y vistas XML, sin Node.js, npm, React, Capacitor ni dependencias externas de aplicación.

[![Compilar APK Android](https://github.com/Northzedam/clima-chat/actions/workflows/android-apk.yml/badge.svg)](https://github.com/Northzedam/clima-chat/actions/workflows/android-apk.yml)

## Funcionalidades

- Mendoza, Argentina, como ubicación inicial.
- Temperatura, sensación térmica, humedad, viento y estado del cielo actuales.
- Máxima y mínima del día y pronóstico de cinco días.
- Búsqueda mundial de ciudades con geocodificación de Open-Meteo.
- Actualización manual, estados visuales de carga y errores de conexión.
- Interfaz oscura mobile-first, adaptable y completamente en español.
- Conexiones HTTPS con tiempos máximos para evitar esperas indefinidas.

## Arquitectura liviana

La aplicación utiliza solamente componentes incluidos en Android:

- `HttpURLConnection` para acceder a Open-Meteo.
- `org.json` para procesar las respuestas.
- Java y XML para lógica e interfaz.
- Gradle y Android SDK únicamente durante la compilación remota.

No hay dependencias que deban instalarse en el teléfono. El workflow tampoco ejecuta npm.

## Compilar el APK desde un celular

1. Subí estos archivos a un repositorio de GitHub.
2. Reemplazá `Northzedam/clima-chat` en la insignia superior por la ruta real del repositorio.
3. Abrí la pestaña **Actions** en GitHub.
4. Elegí **Compilar APK Android** y tocá **Run workflow**. También se ejecutará automáticamente con cada actualización de `main`.
5. Durante la ejecución podrás ver el avance en pasos separados: Java, Android SDK, componentes Android, Gradle, compilación y publicación.
6. Al finalizar, abrí la ejecución y descargá el artifact **clima-chat-debug-apk**.
7. Descomprimí el ZIP e instalá `app-debug.apk`. Android puede solicitar permiso para instalar aplicaciones desde el navegador o gestor de archivos.

La compilación usa GitHub Actions y queda sujeta a los minutos gratuitos incluidos por GitHub.

## Compilación local opcional

Con Java 17 y Android SDK configurados:

```bash
gradle assembleDebug
```

El resultado se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## API

Clima Chat usa gratuitamente los endpoints de pronóstico y geocodificación de [Open-Meteo](https://open-meteo.com/). No necesita API key ni backend propio.
