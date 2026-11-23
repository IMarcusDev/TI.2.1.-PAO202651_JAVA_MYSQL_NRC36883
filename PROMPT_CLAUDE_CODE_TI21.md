# Prompt para Claude Code: implementación de CountryAPI (TI.2.1, Java + MySQL + MVC)

> Abre Claude Code en la carpeta
> `C:\IMarcusDev\Universidad\8vo_semestre\ArquitecturaSw_36883_GR04\PARCIAL_2\TI.2.1. PAO202651_JAVA_MYSQL_NRC36883`
> y pega todo lo que está debajo de la línea.

---

## Contexto

Vas a implementar el sistema **CountryAPI** del taller TI.2.1 («Consumo de APIs Públicas en Java con MySQL aplicando el Patrón MVC»). Es un trabajo universitario de la materia Arquitectura de Software (ESPE). Su autor es Marcos Escobar.

La documentación y los diagramas ya están terminados y aprobados. **Son la fuente de verdad.** Tu trabajo es convertirlos en código sin inventar funcionalidad, endpoints, tablas ni clases que no estén documentados.

Si algo de la documentación es ambiguo, contradictorio o imposible de implementar tal como está:

- no lo resuelvas por tu cuenta;
- anótalo en `DUDAS.md`, en la raíz del proyecto;
- pregúntame antes de seguir.

No modifiques nada en `01. UML`, `02. MER` ni `03. BDD`, ni el documento del taller.

Existe una versión .NET del mismo sistema (TI.2.2) con exactamente la misma funcionalidad. Por eso los nombres de clases, endpoints, mensajes y reglas deben ser los documentados, sin variaciones.

## Fase 0: leer la documentación (obligatoria, antes de escribir código)

Lee en este orden:

1. **`03. BDD/init.sql` y `03. BDD/seed.sql`.** Esquema real de MySQL 8, base `api_paises`, con las tablas `usuario`, `region`, `pais` y `universidad`. Son la referencia exacta de columnas, tipos, restricciones (UNIQUE, CHECK, FK) y datos de prueba. Los usuarios de prueba son `monster/monster9` y `admin/admin123`.
2. **`02. MER/02_MER_COUNTRYAPI.pdm`.** Modelo físico de PowerDesigner. Úsalo para confirmar que coincide con `init.sql`. Si hay diferencias, anótalas en `DUDAS.md`.
3. **`01. UML/01. ERS/ERS_JAVA_MYSQL_COUNTRYAPI_ESCOBAR.docx`.** Requisitos:
   - funcionales RQF-001 a RQF-011;
   - no funcionales RNF-001 a RNF-013;
   - tabla de mapeo del JSON de REST Countries a columnas;
   - códigos HTTP y mensajes exactos.
4. **`01. UML/02. ECUD/ECUD_JAVA_MYSQL_COUNTRYAPI_ESCOBAR.docx`.** Flujos básico y alternativos de cada caso de uso (11 ECUD). Los mensajes que ves ahí son los que debe devolver el servidor y mostrar los clientes.
5. **`01. UML/03. DIAGRAMAS/`.** Archivos XML de PowerDesigner:
   - `04_CLASES/DC_Servidor.oom` y `04_CLASES/DC_Clientes.oom`: paquetes, clases, atributos, operaciones y relaciones. **Usa exactamente estos nombres.**
   - `03_SECUENCIA/DS_*.oom`: orden de las llamadas (WebService → TokenService → Controlador → DAO / ConsumoAPI) y SQL de cada operación.
   - `02_ACTIVIDAD/DA_*.oom`: decisiones y validaciones de cada flujo.
   - `05_ARQUITECTURA/ARQ_CountryAPI.eam`: despliegue y capas.

Los `.docx` y los `.oom` no se leen como texto plano. Conviértelos tú:

- **`.docx`:** con `pandoc`, con `python-docx` o descomprimiendo `word/document.xml`.
- **`.oom` / `.pdm`:** son XML. Escribe un script pequeño que liste, por diagrama, cada `o:Class` con sus `c:Attributes` y `c:Operations` (campos `a:Name`, `a:DataType`, `a:Visibility`), y cada mensaje de los diagramas de secuencia.

Guarda esos extractos en una carpeta temporal **fuera** del proyecto, o bórralos al terminar.

Al terminar la fase 0:

- escribe en `PLAN.md` un resumen de lo que entendiste (endpoints, clases por proyecto, tablas) y el plan por fases;
- **detente y espera mi confirmación.**

## Resumen del contrato (para verificar tu lectura; la documentación manda)

### Servidor

| Aspecto | Valor |
|---|---|
| Proyecto | `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul` (Maven, `war`) |
| Plataforma | Java 17, Jakarta EE / JAX-RS sobre **Payara**, puerto 8080, ruta base `/api` (`ApplicationConfig`) |
| Librerías | Gson 2.10.1, mysql-connector-j 8.3.0, JDBC puro con DAO (sin JPA) |
| Paquete raíz | `ec.edu.monster` |
| Subpaquetes | `config`, `controladores`, `servicios`, `ws`, `modelos.data`, `modelos.dto`, `modelos.enums`, `modelos.interfaces`, `modelos.repositorios` |

**Respuesta uniforme.** Todas las operaciones devuelven `RespuestaDTO<T>` con la forma `{ "estado": "OK" | "ERROR", "mensaje": "...", "datos": ... }`.

- Códigos HTTP: 200 o 201, 400, 401, 404, 409 (país repetido), 502 (falla de una API externa) y 500.
- En una respuesta con error, `datos` va vacío.
- Los mensajes nunca exponen SQL ni trazas.

**Seguridad.**

- `POST /api/Login/login` recibe `{usuario, clave}` y devuelve `{token, nombre}`.
- `POST /api/Login/logout` cierra la sesión.
- El token es un UUID guardado en memoria por `TokenService`. Expira tras 15 minutos sin uso y cada uso renueva ese tiempo.
- Viaja en el encabezado `Authorization`. Los clientes lo envían como `Bearer <token>` y el servidor acepta el valor con o sin el prefijo.
- La clave se compara como hash SHA-256 (`HashService`). Solo pueden entrar usuarios en estado `ACTIVO`.

**Endpoints de países.**

- `GET /api/Paises` (acepta `?nombre=`).
- `POST /api/Paises`.
- `PUT /api/Paises/{id}`.
- `DELETE /api/Paises/{id}`.
- `GET /api/Paises/buscar?nombre=&capital=&region=`. Nombre y capital se comparan por coincidencia parcial y sin distinguir mayúsculas; la región, por igualdad. Si la región no existe, responde «Región no válida».
- `POST /api/Paises/importar`. Usa REST Countries `/v3.1/all?fields=name,translations,capital,region,subregion,population,area,currencies`. Hace upsert por nombre en **una** transacción con `executeBatch` y devuelve `ResumenImportacion {recibidos, registrados, actualizados}`.
- `GET /api/Paises/externo/{nombre}`. Usa `/v3.1/name/{nombre}`.
- `POST /api/Paises/guardar`.

**Otros endpoints.**

- `GET /api/Reportes` devuelve, en un solo `ReporteDTO`:
  - países por región;
  - país más poblado;
  - promedio poblacional por región (`AVG ... GROUP BY`).
- `POST /api/Universidades/importar?pais=` usa `http://universities.hipolabs.com/search?country=`.
  - Toma el nombre, el primer dominio, la primera página web y el país.
  - Hace upsert por (nombre, país) en una transacción.
- `GET /api/Universidades?pais=` lista las universidades guardadas de ese país.

**Mapeo del JSON de REST Countries.**

| Dato | Regla |
|---|---|
| nombre | `translations.spa.common`; si no existe, `name.common` |
| capital | la primera; si no hay, «Sin capital» |
| región | traducida al español |
| subregión | si no hay, «Sin subregión» |
| área | si no hay, 0 |
| moneda | «Nombre (COD)» de la primera; si no hay, «Sin moneda» |

**APIs externas.** `ConsumoAPI` y `ConsumoAPIUniversidades` usan `HttpURLConnection` con un tiempo de espera de 15 s. Si la API falla, devuelven 502 sin tocar la base de datos.

**Clases.** Las del diagrama `DC_Servidor`: `LoginWebService`, `PaisWebService`, `ReporteWebService`, `UniversidadWebService`, sus controladores, `TokenService`, `HashService`, `ConsumoAPI`, `ConsumoAPIUniversidades`, los DAO con sus interfaces (`IUsuarioDAO`, `IPaisDAO`, `IRegionDAO`, `IUniversidadDAO`), los `data`, los `dto` y los `enums`.

**Conexión a la base.** `DBConfig` lee la URL, el usuario y la clave desde un `db.properties`, no escritos en el código. Valores por defecto: `jdbc:mysql://localhost:3306/api_paises`, usuario `root`.

### Clientes (los cuatro con MVC y la misma capa de modelo)

Todos consumen **solo** el servidor; nunca MySQL ni las APIs externas.

**Estructura común** (diagrama `DC_Clientes`):

- `vista`: `VistaBase` más Login, Menu, Pais, Busqueda, ConsultaApi, Importacion, Reporte y Universidad.
- `controlador`: un controlador por módulo y `ValidadorDatos`.
- `servicios`: `SesionServicio`, `LoginServicio`, `PaisServicio`, `ReporteServicio`, `UniversidadServicio` y `ClienteRest` (con Gson).
- `config`: `ConfigServidor`, con `URL_BASE` y `TIMEOUT_MS = 15000`, leído de un archivo de configuración para cambiar la IP sin recompilar.
- `modelos`.

**Proyectos, en este orden:**

1. **Escritorio:** `04. CLIESC/ESC_COUNTRYAPI_JAVA_RESTFul`. Swing con Maven y `JTable`.
2. **Móvil:** `04. CLIMOV/MOV_COUNTRYAPI_JAVA_RESTFul`. Android en Java con Gradle, minSdk 24 y Material Components. La URL del servidor es configurable; en el emulador es `10.0.2.2:8080`.
3. **Consola:** `04. CLICON/CONSOLE_COUNTRYAPI_JAVA_RESTFul`. Maven, menú en texto y tablas formateadas.
4. **Web:** `04. CLIWEB/WEB_COUNTRYAPI_JAVA_RESTFul`. JSP con Servlets sobre Payara. El token se guarda en la sesión HTTP y hay filtro de autenticación.

**Funciones que deben tener los cuatro:**

- iniciar y cerrar sesión;
- importar países, mostrando el resumen;
- consultar un país en la API y guardarlo;
- CRUD de países, con confirmación antes de eliminar;
- buscar y filtrar: filtros de región dinámicos, que vuelven a consultar al marcar o desmarcar una región;
- los tres reportes;
- importar y listar universidades por país.

**Validaciones** (en `ValidadorDatos`, iguales a las del ECUD):

- nombre obligatorio y de 100 caracteres como máximo;
- población entera mayor o igual que 0;
- área mayor o igual que 0;
- consulta en la API con al menos 2 caracteres.

Si la respuesta es 401, el cliente cierra la sesión y vuelve al login.

## Referencias de interfaz y de estructura

Son trabajos anteriores del mismo grupo. **Úsalos para el diseño visual y la organización del código, no para la lógica de negocio.**

- **Diseño visual principal (EXAMEN):** `C:\IMarcusDev\Universidad\8vo_semestre\ArquitecturaSw_36883_GR04\PARCIAL_1\PAO202651_EXAMEN_CELULARES_RESTFUL_DOTNET_GR04\04. CLI*`. Lee primero los `README.md` del cliente de escritorio y del web.
  - El estilo es:
    - barra superior azul con la navegación y el nombre del usuario;
    - menú principal con tarjetas;
    - tablas con encabezado celeste;
    - alertas verdes (éxito) y rojas (error);
    - indicador de si el servidor responde.
  - Paleta de `wwwroot/css/estilos.css`:

    | Token | Color | Token | Color |
    |---|---|---|---|
    | azul | `#1F4E79` | texto | `#1F2933` |
    | azul oscuro | `#183D60` | texto suave | `#4B5563` |
    | azul claro | `#E8EFF7` | borde | `#D1D5DB` |
    | fondo | `#F3F4F6` | verde | `#22C55E` |
    | rojo | `#B91C1C` | rojo claro | `#FDECEC` |

  - Traduce ese diseño a cada tecnología: WinForms a Swing, Razor a JSP y MAUI a Android.
- **Misma tecnología Java REST con BDD (TG.1.3):** `C:\IMarcusDev\Universidad\8vo_semestre\ArquitecturaSw_36883_GR04\PARCIAL_1\CONBDD\TG.1.3. PAO202651_RESTFUL_JAVA_CONBDD_NRC36883_GR04\04. *`. Es la referencia de:
  - organización de paquetes;
  - `pom.xml` y `build.gradle`: Java 17, Jakarta EE, JUnit 5, compileSdk 36, Material;
  - `Estilos.java` de Swing;
  - `AuthFilter` y CSS de la web;
  - fragments y layouts de Android;
  - pruebas con servidor falso.
- **Si hace falta comparar:** los demás TG con BDD, en `...\PARCIAL_1\CONBDD\TG.1.1` a `TG.1.4`.

El título de las aplicaciones es **CountryAPI**. No copies textos, nombres de clases ni datos de EurekaBank ni de Celulares.

## Forma de trabajo

- **Trabaja por fases y detente al final de cada una:**
  1. servidor;
  2. escritorio;
  3. móvil;
  4. consola;
  5. web.

  Al detenerte, dime qué hiciste, cómo probarlo y qué dudas quedaron.
- **Antes de dar por terminado el servidor:**
  - compílalo (`mvn package`);
  - deja en `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/pruebas/` un archivo con comandos `curl` para cada endpoint (incluidos los casos de error 400, 401, 404, 409 y 502).
  - Si tienes acceso a MySQL y Payara, pruébalo de verdad. Si no, dime exactamente qué debo ejecutar yo.
- **Pruebas unitarias con JUnit 5**, como en las referencias:
  - en el servidor, para `HashService`, `TokenService`, el mapeo de `ConsumoAPI` con JSON de ejemplo y los validadores;
  - en los clientes, para `ValidadorDatos` y los controladores con un servidor falso.
- **Código claro y comentado en español**, sobre todo el consumo de las APIs, el mapeo del JSON y los DAO (RNF-013). Usa consultas parametrizadas (`PreparedStatement`) siempre.
- **Cada proyecto lleva un `README.md` corto** que explique cómo configurarlo (URL del servidor o de la base de datos), cómo ejecutarlo y cómo correr sus pruebas.
- **No hagas commits ni borres archivos** que no hayas creado tú en esta tarea.
