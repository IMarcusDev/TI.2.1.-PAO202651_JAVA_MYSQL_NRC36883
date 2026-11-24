# Plan de implementación de CountryAPI (TI.2.1)

Este plan resume lo que entendí de la documentación aprobada (ERS, ECUD, `DC_*`, `DS_*`, `DA_*`, `ARQ`, `init.sql` y `seed.sql`) y cómo voy a construir el sistema. Las dudas abiertas están en [DUDAS.md](DUDAS.md). Donde una duda afecta al plan, aplico la recomendación y lo marco con «(ver Ax)».

## 1. Base de datos `api_paises` (MySQL 8, utf8mb4, InnoDB)

| Tabla | Columnas | Restricciones |
| --- | --- | --- |
| `usuario` | id_usuario, username, password_hash CHAR(64), nombre, estado | username UNIQUE; estado `ACTIVO`/`INACTIVO` |
| `region` | id_region, nombre | nombre UNIQUE; 6 filas: América, Europa, Asia, África, Oceanía, Antártida |
| `pais` | id, nombre, capital, id_region, subregion, poblacion BIGINT, area DOUBLE, moneda, fecha_actualizacion | nombre UNIQUE; CHECK poblacion y area ≥ 0; FK a region |
| `universidad` | id, nombre, dominio, sitio_web, pais, fecha_actualizacion | UNIQUE (nombre, pais) |

- `fecha_actualizacion` se actualiza sola (`ON UPDATE CURRENT_TIMESTAMP`).
- La collation `utf8mb4_0900_ai_ci` hace que `LIKE` no distinga mayúsculas ni tildes: «peru» encuentra «Perú».
- Usuarios de prueba: `monster/monster9` y `admin/admin123`.

## 2. Contrato REST

**URL base:** `http://<host>:8080/WS_COUNTRYAPI_JAVA_RESTFul/api`

**Respuesta de todas las operaciones:** `{ "estado": "OK"|"ERROR", "mensaje": "...", "datos": ... }`. En una respuesta con error, `datos` es `null`.

Todos los endpoints, salvo `login`, exigen el token en `Authorization` (con o sin `Bearer`, ver A1). Sin token válido responden 401 con «Token inválido o sesión expirada».

| Método y ruta | Entrada | `datos` si sale bien | Mensajes y códigos |
| --- | --- | --- | --- |
| `POST /Login/login` | `{usuario, clave}` | `LoginResponseDTO {token, nombre}` | 200 «Ingreso de sesión exitoso» · 401 «Credenciales incorrectas» · 500 «Servicio no disponible, intente más tarde» |
| `POST /Login/logout` | encabezado | `null` | 200 «Sesión cerrada» (también si el token ya no existe, ver A5) |
| `GET /Paises[?nombre=]` | — | `List<PaisDTO>` ordenada por nombre | 200 (lista vacía si no hay países) |
| `POST /Paises` | `PaisDTO` | `PaisDTO` con el id (ver A4) | 201 «País registrado correctamente» · 400 validación · 409 «Ya existe un país con ese nombre» |
| `PUT /Paises/{id}` | `PaisDTO` | `PaisDTO` | 200 «País actualizado correctamente» · 400 · 404 «El país no existe» · 409 |
| `DELETE /Paises/{id}` | — | `null` | 200 «País eliminado correctamente» · 404 |
| `GET /Paises/buscar?nombre=&capital=&region=` | criterios opcionales | `List<PaisDTO>` (el cliente calcula el total, ver A3) | 200 «Búsqueda realizada correctamente» · 400 «Región no válida» |
| `POST /Paises/importar` | — | `ResumenImportacion {recibidos, registrados, actualizados}` | 200 «Importación finalizada» · 502 «La API REST Countries no está disponible, intente más tarde» o «La API REST Countries devolvió una respuesta no válida» · 500 si hay rollback |
| `GET /Paises/externo/{nombre}` | nombre de 2 o más caracteres | `List<PaisDTO>` con `guardado` | 200 «Consulta en la API exitosa» · 404 «No se encontraron países con ese nombre en la API» · 502 · 400 si el nombre es corto (ver A6) |
| `POST /Paises/guardar` | `PaisDTO` | `PaisDTO` | 200 «País guardado correctamente» (ver A5) · 400 |
| `GET /Reportes` | — | `ReporteDTO {paisesPorRegion, paisMasPoblado, promedioPorRegion}` | 200 «Reportes generados correctamente» · 404 «No hay países registrados para generar reportes» (ver A5) |
| `POST /Universidades/importar?pais=` | país en inglés | `ResumenImportacion` | 200 «Importación de universidades finalizada» · 404 «No se encontraron universidades para ese país» · 502 «La API de universidades no está disponible, intente más tarde» · 400 si el país está vacío |
| `GET /Universidades?pais=` | — | `List<Universidad>` ordenada por nombre | 200 (lista vacía si no hay) |

Cualquier error no previsto responde 500 con «Ocurrió un error inesperado, intente nuevamente» y deja el detalle solo en el log.

### Reglas de negocio clave

**Mapeo de REST Countries v5** (`ConsumoAPI`; la v3.1 documentada fue retirada, ver DUDAS.md C1):

| Dato | Regla |
| --- | --- |
| nombre | `names.translations.spa.common`; si no existe, `names.common` |
| capital | `capitals[].name`, la marcada como `primary` o la primera; si no hay, «Sin capital» |
| región | Americas→América, Europe→Europa, Asia→Asia, Africa→África, Oceania→Oceanía, Antarctic→Antártida; si llega otra, el país se descarta y se registra en el log |
| subregión | tal cual; si no hay, «Sin subregión» |
| población | `long` |
| área | `area.kilometers` como `double`; si no viene, 0 |
| moneda | «name (code)» del primer elemento de `currencies`; si no hay, «Sin moneda» |

**Importación de países:**

1. Se cargan una sola vez los nombres que ya existen.
2. Se separan los países nuevos (lote de `INSERT`) de los existentes (lote de `UPDATE`).
3. Todo corre en una transacción con `executeBatch`. Si algo falla, se hace rollback y se responde 500.
4. Si la API falla, se responde 502 antes de tocar la base.
5. Si dos países de la API tienen el mismo nombre en español, se guarda uno solo, para no violar el UNIQUE.

**Mapeo de Hipolabs** (`ConsumoAPIUniversidades`):

- `name`;
- `domains[0]`, o «Sin dominio» si no hay;
- `web_pages[0]`, o «Sin sitio web» si no hay;
- `country`.

Se descartan los nombres repetidos, el país se codifica para la URL, se hace upsert por (nombre, país) en una transacción y, si la lista llega vacía, se responde 404 sin tocar la base.

**Validación del país** (`PaisControlador.validar`, repetida en el servidor):

- nombre obligatorio y de 100 caracteres como máximo;
- región que exista en `region` («Seleccione una región»);
- población ≥ 0;
- área ≥ 0;
- capital, subregión y moneda de 100 caracteres como máximo.

**Actualizar un país:**

1. `buscarPorId`; si no existe, 404.
2. Si el nombre cambió y `existePorNombre`, 409.
3. `actualizar`.

**Reportes:**

- `COUNT` con `region LEFT JOIN pais ... GROUP BY`, ordenado de mayor a menor;
- `ORDER BY poblacion DESC, nombre LIMIT 1`;
- `ROUND(AVG(...))` con `LEFT JOIN`, con 0 para las regiones sin países.

Siempre aparecen las seis regiones.

**Token** (`TokenService`):

- `ConcurrentHashMap<String, Sesion>` con UUID;
- se valida y se renueva `ultimoUso` en cada uso;
- expira tras 15 minutos sin uso y entonces se elimina.

## 3. Servidor: `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul`

**Proyecto:** Maven `war`, Java 17, `jakarta.jakartaee-api 10.0.0` (provided, ver A7), Gson 2.10.1, mysql-connector-j 8.3.0 y JUnit 5.

Clases con los nombres exactos de `DC_Servidor`, bajo el paquete `ec.edu.monster`:

| Paquete | Clases |
| --- | --- |
| `config` | `ApplicationConfig` (`@ApplicationPath("/api")`), `DBConfig` (lee `db.properties` del classpath; por defecto `jdbc:mysql://localhost:3306/api_paises` y `root`) |
| `ws` | `LoginWebService` (`/Login`), `PaisWebService` (`/Paises`), `ReporteWebService` (`/Reportes`), `UniversidadWebService` (`/Universidades`) |
| `controladores` | `LoginControlador`, `PaisControlador`, `ReporteControlador`, `UniversidadControlador` |
| `servicios` | `TokenService`, `HashService`, `ConsumoAPI`, `ConsumoAPIUniversidades` |
| `modelos.interfaces` | `IUsuarioDAO`, `IPaisDAO`, `IRegionDAO`, `IUniversidadDAO` |
| `modelos.repositorios` | `UsuarioDAO`, `PaisDAO`, `RegionDAO`, `UniversidadDAO` (JDBC puro con `PreparedStatement`) |
| `modelos.data` | `Usuario`, `Region`, `Pais`, `Sesion`, `Universidad` |
| `modelos.dto` | `RespuestaDTO<T>`, `LoginRequestDTO`, `LoginResponseDTO`, `PaisDTO`, `ResumenImportacion`, `ConteoRegion`, `PromedioRegion`, `ReporteDTO` |
| `modelos.enums` | `EstadoUsuario`, `EstadoRespuesta` |

**Cómo se construye una respuesta:**

1. El `WebService` valida el token.
2. Llama al controlador.
3. Convierte el resultado en `RespuestaDTO` con su código HTTP, o la excepción en ERROR con su código (ver A2).

Las instancias se conectan a mano en los constructores, sin CDI, como en la referencia TG.1.3. `TokenService` es un *singleton* compartido para que las sesiones sean las mismas en todos los servicios web.

**Pruebas JUnit 5:**

- `HashService`, con los hashes de `seed.sql`;
- `TokenService`: genera, valida, renueva, expira y elimina;
- `ConsumoAPI.convertir` y `traducirRegion`, con JSON de ejemplo (país completo, sin traducción, sin capital, sin moneda, sin área y región desconocida);
- `ConsumoAPIUniversidades.convertir`;
- la validación de `PaisControlador`, con DAO falsos.

**Entregables:** `pruebas/curl_countryapi.sh` (Git Bash), con todos los endpoints y los casos 400, 401, 404, 409 y 502, y un `README.md`.

## 4. Clientes: estructura común (`DC_Clientes`)

Paquete raíz `ec.edu.monster`.

| Capa | Clases |
| --- | --- |
| `vista` | `VistaBase` + `LoginVista`, `MenuVista`, `PaisVista`, `BusquedaVista`, `ConsultaApiVista`, `ImportacionVista`, `ReporteVista`, `UniversidadVista` |
| `controlador` | `LoginControlador`, `PaisControlador`, `ApiControlador` (más `vistaConsulta` y `vistaImportacion`, ver B3), `ReporteControlador`, `UniversidadControlador`, `ValidadorDatos` |
| `servicios` | `SesionServicio`, `LoginServicio`, `PaisServicio`, `ReporteServicio`, `UniversidadServicio`, `ClienteRest` (`HttpURLConnection` + Gson) |
| `config` | `ConfigServidor` (`URL_BASE` leída de un archivo de configuración, `TIMEOUT_MS = 15000`) |
| `modelos` | `Pais`, `LoginRequest`, `LoginResponse`, `ResumenImportacion`, `ConteoRegion`, `PromedioRegion`, `Reporte`, `Universidad`, `CriteriosBusqueda`, `RespuestaDTO<T>`, `EstadoRespuesta` |

Comportamiento común:

- Los mensajes del cliente son los del ECUD: «Ingrese el usuario», «Ingrese al menos dos caracteres», «No se pudo conectar con el servidor. Verifique la red e intente nuevamente.», «Su sesión expiró o no es válida. Inicie sesión nuevamente.», etc.
- Una respuesta 401 cierra la sesión y vuelve al login.
- Se pide confirmación antes de importar y antes de eliminar.
- Las llamadas al servidor se hacen fuera del hilo de la interfaz, con un indicador de carga.
- Población y área se muestran con separador de miles.
- Las regiones son una constante (ver B4).
- El filtro de región es de selección única con «Todas» (ver B5).
- Pruebas JUnit 5: `ValidadorDatos`, y los controladores contra un servidor falso (`com.sun.net.httpserver.HttpServer`, como en TG.1.3).

Estilo visual de la referencia EXAMEN:

- barra superior azul `#1F4E79` con la navegación y el nombre del usuario;
- menú con tarjetas;
- tablas con encabezado celeste `#E8EFF7`;
- alertas verdes y rojas;
- indicador de si el servidor responde.

## 5. Fases (me detengo al final de cada una)

| Fase | Proyecto | Detalles |
| --- | --- | --- |
| 1 | Servidor `WS_COUNTRYAPI_JAVA_RESTFul` | `mvn package` y `mvn test`. Lo despliego en `C:\payara7` contra el MySQL de Docker y ejecuto los `curl` de prueba. |
| 2 | Escritorio `ESC_COUNTRYAPI_JAVA_RESTFul` | Swing + `JTable`, `Estilos.java`, `SwingWorker`, `servidor.properties` junto al `.jar` |
| 3 | Móvil `MOV_COUNTRYAPI_JAVA_RESTFul` | Android en Java, Gradle, minSdk 24, compileSdk 36, Material. Fragments por módulo; URL configurable desde la app, por defecto `http://10.0.2.2:8080/...` |
| 4 | Consola `CONSOLE_COUNTRYAPI_JAVA_RESTFul` | Menú numerado, tablas de texto, errores con el prefijo «ERROR:» |
| 5 | Web `WEB_COUNTRYAPI_JAVA_RESTFul` | JSP + Servlets en Payara, token en `HttpSession`, `AuthFilter`, CSS adaptable. Los filtros de región vuelven a consultar al cambiar. |

Al inicio de cada fase leo la referencia de esa tecnología (TG.1.3 para la estructura y EXAMEN para lo visual) y solo tomo de ahí el diseño y la organización.

## 6. Entorno detectado

- Maven 3.9.11 y JDK 21 (Temurin): compilo con `--release 17`.
- Gradle está disponible.
- Payara Server 7.2026.9 está en `C:\payara7`.
- MySQL no está instalado. Para las pruebas uso `03. BDD/docker-compose.yml` (MySQL 8.4 con `init.sql` y `seed.sql` ya cargados, root/`root` en el puerto 3306 por defecto) sobre Docker Desktop.
