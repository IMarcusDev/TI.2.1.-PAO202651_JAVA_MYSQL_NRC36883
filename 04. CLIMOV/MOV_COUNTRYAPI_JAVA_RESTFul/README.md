# MOV_COUNTRYAPI_JAVA_RESTFul

Cliente móvil (Android en Java) de CountryAPI. Consume **solo** el servidor RESTful `WS_COUNTRYAPI_JAVA_RESTFul`; nunca llama a MySQL ni a las APIs públicas.

- Gradle 9.8.1 (wrapper incluido), Android Gradle Plugin 9.4.0, Java 17.
- minSdk 24 (Android 7.0), compileSdk y targetSdk 36, Material Components 1.13.
- Gson 2.10.1 para el JSON.
- La interfaz sigue el diseño de referencia:
  - barra superior azul con el usuario y el estado del servidor;
  - menú de tarjetas;
  - listas con encabezado celeste;
  - avisos verdes y rojos.

## Configuración: dónde está el servidor

La dirección inicial está en `gradle.properties`:

```properties
servidor=http://10.0.2.2:8080/WS_COUNTRYAPI_JAVA_RESTFul/api
```

- **Emulador:** `10.0.2.2` es el equipo donde corre Payara. No hace falta cambiar nada.
- **Celular físico:** use la IP del equipo del servidor en la red local, por ejemplo `http://192.168.1.20:8080/WS_COUNTRYAPI_JAVA_RESTFul/api`. El celular y el equipo deben estar en la misma red, y el firewall de Windows debe permitir el puerto 8080.

También se puede cambiar **sin recompilar**: en la pantalla de inicio de sesión, «Cambiar servidor». La nueva dirección queda guardada en la app.

Para compilar con otra dirección sin editar el archivo:

```bash
./gradlew assembleDebug -Pservidor=http://192.168.1.20:8080/WS_COUNTRYAPI_JAVA_RESTFul/api
```

Cada petición espera como máximo 15 segundos (`ConfigServidor.TIMEOUT_MS`).

## Compilar e instalar

Requisitos:

- el servidor desplegado en Payara (ver `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/README.md`);
- la base levantada con `03. BDD/docker-compose.yml`;
- el SDK de Android con la plataforma 36. Gradle lo encuentra por la variable `ANDROID_HOME` o por `sdk.dir` en `local.properties`.

```bash
./gradlew assembleDebug        # genera build/outputs/apk/debug/MOV_COUNTRYAPI_JAVA_RESTFul-debug.apk
./gradlew installDebug         # lo instala en el emulador o celular conectado
```

También se abre directamente con Android Studio (la carpeta del proyecto) y se ejecuta con ▶.

Usuarios de prueba: `monster` / `monster9` y `admin` / `admin123`.

## Pruebas

```bash
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "ec.edu.monster.pruebas.ControladoresTest"
./gradlew lintDebug            # revisa, entre otras cosas, que no se usen APIs de Android 8 o superior
```

Las 65 pruebas son JUnit 5, corren en el equipo (sin emulador) y no necesitan el servidor real:

- **`ValidadorDatosTest`:** las validaciones del ECUD.
- **`ServiciosTest`:** rutas, encabezados, JSON, 401, sin conexión y respuesta no válida, contra un servidor HTTP falso.
- **`ControladoresTest`:** cada controlador con una vista en memoria (`VistaFalsa`) y el servidor falso.
- **`ConfigServidorTest`:** direcciones válidas e inválidas del servidor y cambio de servidor sin reiniciar.

## Estructura (diagrama DC_Clientes)

```
src/main/java/ec/edu/monster/
├── AplicacionCountryApi.java  Crea ClienteRest, la sesión y los servicios una sola vez
├── config/          ConfigServidor (URL de gradle.properties o la elegida en la app, TIMEOUT_MS = 15000)
├── modelos/         Pais, Reporte, Universidad, RespuestaDTO<T>, ...     (iguales al escritorio)
├── servicios/       ClienteRest (HttpURLConnection + Gson), SesionServicio y un servicio por módulo
├── controlador/     Login, Pais, Api, Reporte, Universidad y ValidadorDatos (iguales al escritorio)
└── vista/           Interfaces del diagrama (VistaBase, LoginVista, PaisVista, ...) y su
                     implementación Android: PrincipalActivity (MenuVista) y un fragment por módulo
```

Las dependencias van en un solo sentido: `vista → controlador → servicios → ClienteRest`.

- Los controladores son los mismos del cliente de escritorio y solo conocen las interfaces de vista.
- Cada acción corre en un hilo aparte. Mientras tanto, aparece la barra de progreso y la pantalla no acepta toques.
- Los fragments cambian sus widgets en el hilo principal (`Hilos`). Los diálogos, como el formulario o las confirmaciones, esperan la respuesta del usuario.

## Funciones

- **Inicio y cierre de sesión.**
  - Si el servidor rechaza el token (sesión expirada o servidor reiniciado), la app vuelve al inicio de sesión con el aviso.
  - «Cerrar sesión» está en la barra superior y en el menú.
- **Importar países:** pide confirmación y muestra el resumen (recibidos, registrados y actualizados).
- **Consultar un país en la API:** al menos 2 caracteres. Cada resultado indica si ya está guardado y tiene su botón «Guardar».
- **Países:**
  - búsqueda por nombre y botón «Nuevo»;
  - al tocar un país, «Editar» o «Eliminar», que pide confirmación;
  - el formulario se reabre con lo escrito y el mensaje si los datos no son válidos.
- **Buscar y filtrar:** nombre y capital parciales. Los filtros de región («Todas», «América», ...) vuelven a consultar al elegirlos.
- **Reportes:** país más poblado, países por región y promedio poblacional por región.
- **Universidades:** importar las de un país (en inglés) y ver las guardadas.
- **Formato:** población y área se muestran con separador de miles.
- **Navegación:** la flecha de la barra o «atrás» vuelve al menú. En el menú, «atrás» deja la app en segundo plano sin cerrar la sesión.
