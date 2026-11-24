# ESC_COUNTRYAPI_JAVA_RESTFul

Cliente de escritorio (Java Swing) de CountryAPI. Consume **solo** el servidor RESTful `WS_COUNTRYAPI_JAVA_RESTFul`; nunca llama a MySQL ni a las APIs públicas.

- Java 17 y Maven. Gson 2.10.1 para el JSON.
- La interfaz sigue el diseño de referencia:
  - barra superior azul con la navegación y el usuario;
  - menú de tarjetas;
  - tablas con encabezado celeste;
  - alertas verdes y rojas;
  - indicador de si el servidor responde.

## Configuración: dónde está el servidor

La dirección está en `servidor.properties`:

```properties
servidor.url=http://localhost:8080/WS_COUNTRYAPI_JAVA_RESTFul/api
```

Si el servidor está en otra PC, cambie solo la IP, por ejemplo `http://192.168.1.20:8080/WS_COUNTRYAPI_JAVA_RESTFul/api`.

El archivo se busca en este orden:

1. la ruta indicada con `-Dcountryapi.config=<ruta>`;
2. `servidor.properties` en la carpeta desde donde se ejecuta. Al empaquetar, Maven lo copia a `target/`, junto al jar, y ahí se puede editar sin recompilar;
3. el que va dentro del jar.

Cada petición espera como máximo 15 segundos (`ConfigServidor.TIMEOUT_MS`).

## Compilar y ejecutar

Requisitos:

- el servidor desplegado en Payara (ver `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/README.md`);
- la base levantada con `03. BDD/docker-compose.yml`.

```bash
mvn package                       # genera target/ESC_COUNTRYAPI_JAVA_RESTFul.jar
cd target
java -jar ESC_COUNTRYAPI_JAVA_RESTFul.jar
```

También se puede ejecutar desde NetBeans (clase principal `ec.edu.monster.App`) o con `mvn exec:java`.

Usuarios de prueba: `monster` / `monster9` y `admin` / `admin123`.

## Pruebas

```bash
mvn test
mvn test -Dtest=ControladoresTest          # una clase
mvn test -Dtest=ValidadorDatosTest#credenciales
```

Las 49 pruebas no necesitan el servidor real:

- **`ValidadorDatosTest`:** las validaciones del ECUD.
- **`ServiciosTest`:** rutas, encabezados, JSON, 401, sin conexión y respuesta no válida, contra un servidor HTTP falso.
- **`ControladoresTest`:** cada controlador con una vista en memoria (`VistaFalsa`) y el servidor falso.

## Estructura (diagrama DC_Clientes)

```
src/main/java/ec/edu/monster/
├── App.java          Punto de entrada: crea servicios, vistas y controladores y los conecta
├── config/           ConfigServidor (URL_BASE desde servidor.properties, TIMEOUT_MS = 15000)
├── modelos/          Pais, Reporte, Universidad, RespuestaDTO<T>, ...
├── servicios/        ClienteRest (HttpURLConnection + Gson), SesionServicio y un servicio por módulo
├── controlador/      Login, Pais, Api, Reporte, Universidad y ValidadorDatos
└── vista/            Interfaces del diagrama (VistaBase, LoginVista, PaisVista, ...) y su
                      implementación Swing (LoginPanel, PaisPanel, ..., VentanaPrincipal, Estilos)
```

Las dependencias van en un solo sentido: `vista → controlador → servicios → ClienteRest`. Los controladores solo conocen las interfaces de vista; por eso se prueban sin pantalla.

## Funciones

- **Inicio y cierre de sesión.** Si el servidor rechaza el token (sesión expirada o servidor reiniciado), la aplicación vuelve al inicio de sesión con el aviso.
- **Importar países:** pide confirmación y muestra el resumen (recibidos, registrados y actualizados).
- **Consultar un país en la API:** al menos 2 caracteres. Se ve si cada país ya está guardado y se puede guardar el elegido.
- **Países:** `JTable` con búsqueda por nombre y botones Nuevo, Editar y Eliminar.
  - Eliminar pide confirmación.
  - El formulario valida nombre, región, población y área y, si hay error, se reabre con lo escrito.
  - Atajos: Alt+N, Alt+E y Alt+L; doble clic en una fila para editar.
- **Buscar y filtrar:** nombre y capital parciales. Los filtros de región («Todas», «América», ...) vuelven a consultar al elegirlos.
- **Reportes:** países por región, país más poblado y promedio poblacional por región.
- **Universidades:** importar las de un país (en inglés) y ver las guardadas.
- **Mientras se procesa una petición:**
  - aparece la barra de progreso y la ventana no acepta clics;
  - la llamada corre fuera del hilo de Swing, así que la interfaz no se congela;
  - un error inesperado se muestra en pantalla y no cierra la aplicación.
- **Formato:** población y área se muestran con separador de miles.
