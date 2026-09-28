# 📺 Novelas360 Provider

Extension de CloudStream para ver telenovelas latinas desde **novelas360.com**

## ✨ Características

- ✅ Telenovelas mexicanas (Televisa, TV Azteca)
- ✅ Telenovelas colombianas (RCN, Caracol)
- ✅ Telenovelas turcas (doramas turcos)
- ✅ Series latinas completas
- ✅ Audio en español latino
- ✅ Calidad HD
- ✅ Soporte para Chromecast
- ✅ Descarga de episodios

## 📋 Contenido Disponible

### Telenovelas Populares:
- **Carita de Ángel** (México)
- **Pedro el Escamoso** (Colombia)
- **El Señor de los Cielos** (México/EE.UU.)
- **La Sombra de Helena** (Brasil)
- **El Bronx** (Colombia)
- Y muchas más...

### Categorías:
- 📺 Telenovelas Recientes
- 🇲🇽 Telenovelas Mexicanas
- 🇹🇷 Telenovelas Turcas
- 🇨🇴 Telenovelas Colombianas
- 📺 Series

## 🛠️ Compilar el Plugin

### Requisitos:
- JDK 17 o superior
- Android SDK
- Gradle

### Pasos:

1. **Clona el repositorio** (si aún no lo has hecho)
   ```bash
   git clone https://github.com/TU-USUARIO/cloudstream-master.git
   cd cloudstream-master/cloudstream-master
   ```

2. **Compila el plugin**
   
   **Windows:**
   ```powershell
   .\gradlew.bat Novelas360Provider:make
   ```
   
   **Linux/Mac:**
   ```bash
   ./gradlew Novelas360Provider:make
   ```

3. **El archivo .cs3 se generará en:**
   ```
   Novelas360Provider/build/Novelas360Provider.cs3
   ```

## 📱 Instalar el Plugin

### Método 1: Desde Archivo (Recomendado)

1. **Copia el archivo .cs3** a tu dispositivo Android en:
   ```
   /storage/emulated/0/Cloudstream3/plugins/
   ```

2. **Abre CloudStream** → Configuración → Extensiones

3. **El plugin aparecerá automáticamente** en la lista

### Método 2: Instalar Directamente via ADB

```bash
.\gradlew.bat Novelas360Provider:deployWithAdb
```

## 🎬 Cómo Usar

1. **Abre CloudStream/NuvaMovies**

2. **Selecciona el provider**
   - Toca "None" en la pantalla principal
   - Selecciona **"Novelas360"**

3. **Busca tu telenovela**
   - Usa el buscador 🔍
   - Escribe el nombre: "Carita de ángel", "Pedro el escamoso", etc.

4. **Reproduce**
   - Selecciona el episodio/capítulo
   - ¡Disfruta! 📺

## 🔧 Desarrollo

### Estructura del Plugin:

```
Novelas360Provider/
├── build.gradle.kts              # Configuración de compilación
├── README.md                     # Este archivo
├── src/
│   └── main/
│       ├── AndroidManifest.xml   # Manifest de Android
│       └── kotlin/
│           └── com/
│               └── nuvamovies/
│                   ├── Novelas360Plugin.kt    # Plugin principal
│                   └── Novelas360Provider.kt  # Provider/Scraper
```

### Características Técnicas:

- **Web Scraping**: Jsoup para parsear HTML
- **HTTP Client**: NiceHttp (OkHttp wrapper)
- **Extracción de Videos**: Soporte para iframes, embeds, y enlaces directos
- **Tipos Soportados**: TvSeries, AsianDrama
- **Idioma**: Español (es)

## 📝 Actualizar el Plugin

Para actualizar la versión del plugin:

1. **Edita `build.gradle.kts`**:
   ```kotlin
   version = 2  // Incrementa el número
   ```

2. **Recompila**:
   ```bash
   .\gradlew.bat Novelas360Provider:make
   ```

## 🐛 Reportar Problemas

Si encuentras algún problema:

1. Verifica que novelas360.com esté funcionando
2. Comprueba tu conexión a Internet
3. Actualiza CloudStream a la última versión
4. Reporta el issue con detalles específicos

## ⚠️ Descargo de Responsabilidad

Este plugin es solo un **agregador de enlaces**. No almacena, aloja ni distribuye ningún contenido. Todos los enlaces provienen de novelas360.com, un sitio de terceros.

El uso de este plugin es bajo tu propia responsabilidad y sujeto a las leyes de tu país.

## 📄 Licencia

Este proyecto está basado en el sistema de plugins de CloudStream y sigue las mismas directrices.

## 🙏 Créditos

- **CloudStream**: [recloudstream/cloudstream](https://github.com/recloudstream/cloudstream)
- **Novelas360**: novelas360.com
- **Desarrollador**: NuvaMovies Team

---

**Versión**: 1.0  
**Estado**: ✅ Funcional  
**Última Actualización**: 2026-09-28
