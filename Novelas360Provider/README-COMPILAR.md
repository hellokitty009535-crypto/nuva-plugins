# 🚀 Compilar Novelas360Provider (Modo Standalone)

## ⚡ Método Rápido

### 1. Abrir carpeta del plugin
```
Navega a:
D:\todo descargas\cloudstream-master\cloudstream-master\Novelas360Provider
```

### 2. Ejecutar script
```cmd
# Doble click en:
COMPILAR.bat
```

### 3. Esperar
- Primera vez: 10-15 minutos
- Siguientes: 1-2 minutos

### 4. Obtener archivo
```
El archivo .cs3 estará en:
build\Novelas360Provider.cs3
```

---

## 📝 Método Manual (PowerShell)

```powershell
# 1. Navegar a la carpeta
cd "D:\todo descargas\cloudstream-master\cloudstream-master\Novelas360Provider"

# 2. Configurar Java
$env:JAVA_HOME = "D:\Program Files\Android\Android Studio\jbr"

# 3. Compilar
.\gradlew.bat make
```

---

## 📦 Archivo Generado

```
Ubicación:
Novelas360Provider\build\Novelas360Provider.cs3

Tamaño: ~50-100 KB
```

---

## 📱 Instalar en Android

1. Copia el archivo a: `/sdcard/Cloudstream3/plugins/`
2. Abre CloudStream → Configuración → Extensiones
3. Busca "Novelas360"
4. ¡Listo!

---

## ⚙️ Estructura del Proyecto Standalone

```
Novelas360Provider/
├── COMPILAR.bat                    # Script de compilación
├── README-COMPILAR.md              # Este archivo
├── build.gradle.kts                # Configuración independiente
├── settings.gradle.kts             # Settings independientes
├── gradle.properties               # Propiedades
├── gradlew.bat                     # Gradle wrapper
├── gradle/                         # Gradle wrapper files
│
└── src/main/
    ├── AndroidManifest.xml
    └── kotlin/com/nuvamovies/
        ├── Novelas360Plugin.kt
        └── Novelas360Provider.kt
```

---

## 🔧 Ventajas del Modo Standalone

✅ **No depende** del proyecto principal de CloudStream  
✅ **Compilación más rápida** (solo compila este plugin)  
✅ **Más fácil de compartir** (carpeta autocontenida)  
✅ **Menos errores** de dependencias cruzadas  

---

## ⚠️ Solución de Problemas

### Error: JAVA_HOME not set
```
Edita COMPILAR.bat y ajusta la línea 9:
set "ANDROID_STUDIO=TU_RUTA_AQUI"
```

### Error: Could not download dependencies
```
Verifica tu conexión a Internet
Algunas dependencias tardan en descargar
```

### Build tarda mucho
```
Es normal la primera vez (10-15 min)
Gradle descarga ~500MB de librerías
```

---

¡Listo para compilar! 🎬
