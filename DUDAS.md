# Dudas de la fase 0 (CountryAPI · TI.2.1)

> **Resuelto el 23/11/2026:** el autor aceptó todas las recomendaciones (A1 a A7 y B1 a B5). El código las aplica tal como están escritas abajo.

Cada duda indica dónde está el conflicto o el vacío, las opciones y la que recomiendo.

## A. Necesito tu decisión antes de empezar el servidor

### A1. Formato del encabezado `Authorization`

- **ECUD7, flujo alternativo 1:** si el encabezado no tiene el formato «Bearer &lt;token&gt;», el servidor responde 401.
- **Prompt:** el servidor acepta el valor con o sin el prefijo `Bearer`.
- **Recomiendo:** aceptar ambos, como dice el prompt. Los cuatro clientes enviarán siempre `Bearer <token>`, así que el comportamiento documentado no cambia para ellos.

### A2. Cómo pasa el controlador el código HTTP al servicio web

Los diagramas de secuencia solo muestran `Exception(mensaje)`. El `WebService` debe convertirla en 400, 404, 409, 502 o 500, y `DC_Servidor` no tiene ninguna clase de excepción propia.

- **Opción a (recomendada):** usar excepciones estándar de Java, sin clases nuevas:
  - `IllegalArgumentException` → 400;
  - `NoSuchElementException` → 404;
  - `IllegalStateException` → 409;
  - `IOException` / `UncheckedIOException` → 502;
  - `SQLException` → 500, con el mensaje «Servicio no disponible, intente más tarde»;
  - cualquier otra → 500, con «Ocurrió un error inesperado, intente nuevamente».

  El mensaje de la excepción es el texto exacto del ECUD.
- **Opción b:** crear una clase `NegocioException(mensaje, codigoHttp)`. Es más explícita, pero agrega una clase que no está en el diagrama.

### A3. Dónde va el «total» de la búsqueda

- **RQF-005 y DS_Buscar_Filtrar_Paises:** la búsqueda devuelve «la lista y el total».
- **Diagrama de clases:** `RespuestaDTO` solo tiene `estado`, `mensaje` y `datos`.
- **Recomiendo:** que `datos` sea la lista y que el cliente calcule el total con su tamaño. `BusquedaVista.mostrarResultados(paises, total)` ya recibe el total por separado.

### A4. Qué devuelve `POST /api/Paises` en `datos`

- **DS_Gestionar_Paises:** `RespuestaDTO(OK, «País registrado correctamente», id)`.
- **`PaisControlador.registrarPais`:** devuelve `Pais`.
- **Recomiendo:** devolver el `PaisDTO` completo, que incluye el `id` asignado. Así el cliente tiene el código y los datos guardados.

### A5. Códigos HTTP que la documentación no fija

| Caso | Recomiendo |
| --- | --- |
| `GET /api/Reportes` sin países («No hay países registrados para generar reportes») | 404 |
| `POST /api/Paises/guardar` correcto (puede registrar o actualizar) | 200 |
| `PUT /api/Paises/{id}` correcto | 200 |
| Error al guardar una importación (rollback, «Servicio no disponible, intente más tarde») | 500 |
| `POST /api/Login/logout` con un token que ya no existe | 200 con «Sesión cerrada», porque el ECUD pide que el cliente salga aunque falle |

### A6. Mensajes de validación del servidor que no están documentados

El ECUD4 usa «Ingrese el nombre del país» tanto para un nombre vacío como para uno demasiado largo. Tampoco hay mensajes para:

- capital, subregión o moneda de más de 100 caracteres;
- `pais` vacío en universidades (el servidor);
- nombre de menos de 2 caracteres en `/externo/{nombre}` (el servidor).

**Recomiendo:**

- nombre vacío o de más de 100 caracteres: «Ingrese el nombre del país», como dice el ECUD;
- capital, subregión o moneda de más de 100 caracteres: «La capital, la subregión y la moneda deben tener como máximo 100 caracteres» (texto nuevo, que la versión .NET tendría que copiar);
- `pais` vacío en el servidor: «Ingrese el país», el mismo texto que usa el cliente;
- nombre corto en `/externo/{nombre}` en el servidor: «Ingrese al menos dos caracteres», el mismo texto que usa el cliente.

Todos van con HTTP 400.

### A7. Versión de Jakarta EE y de Payara

- **Diagrama de arquitectura:** Payara 6 (Jakarta EE 10).
- **Este equipo:** tiene **Payara Server 7.2026.9** (Jakarta EE 11) en `C:\payara7`.
- **Referencia TG.1.3:** compila contra `jakarta.jakartaee-api 11.0.0-M1`.
- **Recomiendo:** compilar contra `jakarta.jakartaee-api 10.0.0` (provided) con bytecode Java 17. El mismo `.war` funciona en Payara 6 y en Payara 7 y respeta el diagrama.

## B. Diferencias que encontré (no bloquean)

### B1. El PDM no coincide exactamente con `init.sql`

`init.sql` manda; no toco ninguno de los dos.

- El PDM no tiene los `DEFAULT` (`estado = 'ACTIVO'`, `poblacion = 0`, `area = 0`, `CURRENT_TIMESTAMP ON UPDATE`) ni los `CHECK` (`estado`, `poblacion >= 0`, `area >= 0`).
- En la FK `FK_PAIS_REGION`, el PDM dice `UPDATE RESTRICT` y `init.sql` dice `ON UPDATE CASCADE`.
- El SGBD destino del PDM es «MySQL 5.0», y el ERS dice MySQL 8.

Tablas, columnas, tipos, PK y UNIQUE sí coinciden.

### B2. Actividad 5 del taller

El taller pide «Región con más países», y el ERS y el ECUD lo cambiaron por «Promedio poblacional por región». El reporte de países por región sale ordenado de mayor a menor, así que la primera fila es la región con más países.

- **Recomiendo:** dejarlo así.
- **Alternativa:** que los clientes la muestren también como un dato destacado, sin cambiar el servidor.

### B3. `ApiControlador` del cliente no tiene atributos de vista

`ConsultaApiVista` e `ImportacionVista` dependen de `ApiControlador`, pero el controlador solo tiene `servicio`. **Recomiendo:** agregar `vistaConsulta: ConsultaApiVista` y `vistaImportacion: ImportacionVista`; sin ellos no hay MVC.

Además, `MenuVista` depende de `LoginControlador` y no existe un `MenuControlador`. Lo respeto: el menú lo maneja `LoginControlador`.

### B4. Lista de regiones en los clientes

No hay ningún endpoint de regiones. `IRegionDAO.listar()` existe, pero ningún `WebService` lo expone.

- **Recomiendo:** que los clientes tengan las seis regiones documentadas como constante (Todas, América, Europa, Asia, África, Oceanía, Antártida) para los filtros y el desplegable del formulario.
- **Alternativa:** crear `GET /api/Regiones`, pero es un endpoint que no está documentado.

### B5. Filtro de región: una a la vez

El prompt dice «marcar o desmarcar una región». El ECUD5 dice que se elige una región o «Todas» y que el servidor compara la región por igualdad. Lo implemento como selección única (radio, chips o lista), donde «Todas» quita el filtro.

## C. Fase 1 (servidor)

### C1. REST Countries v3.1 ya no existe

> **Resuelto el 23/11/2026 con la opción a.** El autor creó una clave gratuita y el servidor usa la v5. Detalle en «Cómo quedó» al final de esta duda.

Lo comprobé el 23/11/2026:

- **v3.1 retirada:** `https://restcountries.com/v3.1/...` responde 301 hacia un archivo heredado que devuelve `{"success": false, "errors": [{"message": "This API version has been deprecated ... migrate to our new version (v5)."}]}`.
- **v5 exige clave:** la nueva versión, `https://api.restcountries.com/v5/...`, responde 401 «Authorization key required» sin una clave. El plan gratuito permite 1000 peticiones al mes.
- **Cambia el formato:** según el aviso del proveedor, la v5 cambia los nombres de algunos campos y la forma de la respuesta, así que el mapeo del ERS (tabla 41) no sirve tal cual.

El servidor está implementado según la documentación (v3.1). Hoy responde 502 «La API REST Countries devolvió una respuesta no válida», como pide ECUD8 para un JSON inesperado. El resto de la aplicación no depende de esta API (RNF-008). Probé la importación completa (transacción, lotes, upsert, valores por defecto, región desconocida y duplicados) contra un servidor local con el formato de la v3.1, y funciona.

**Opciones:**

- **a) Migrar a la v5 con una clave gratuita (recomendada).**
  - Te registras en restcountries.com y me pasas la clave, o la pones tú en la configuración.
  - La clave iría en una propiedad del servidor, no en el código.
  - Solo cambian `ConsumoAPI` y sus pruebas, como anticipa el ERS en Recomendaciones.
  - Habría que actualizar el ERS (v3.1 → v5, URL y campos) y avisar al equipo de la versión .NET.
- **b) Dejarlo como está.** Se documenta que la API fue retirada y se presenta la importación con los datos del seed. La importación real no funcionaría.
- **c) Usar otra fuente sin clave** (un espejo o un archivo JSON público con el formato de la v3.1). Cambia la fuente documentada y depende de un tercero no oficial.

**Cómo quedó (opción a):**

- **Dirección:** `https://api.restcountries.com/countries/v5`, con el encabezado `Authorization: Bearer <clave>`.
- **Endpoints:**
  - Importar: `GET /countries/v5?limit=100&offset=N&response_fields=...`, paginado; unos 250 países son 3 peticiones.
  - Consultar por nombre: `GET /countries/v5/name?q={nombre}`, que busca en los nombres común, oficial, alternativos y nativos.
- **Mapeo actualizado (ERS, tabla 41):**

  | Dato | Campo en v3.1 | Campo en v5 |
  | --- | --- | --- |
  | nombre | `translations.spa.common` / `name.common` | `names.translations.spa.common` / `names.common` |
  | capital | `capital[0]` | `capitals[].name` (la marcada como `primary`, o la primera) |
  | región, subregión, población | `region`, `subregion`, `population` | sin cambios |
  | área | `area` | `area.kilometers` |
  | moneda | `currencies` (objeto por código) | `currencies` (arreglo de `{code, name}`) |

  Los valores por defecto no cambian: «Sin capital», «Sin subregión», «Sin moneda» y área 0.
- **Dónde está la clave:**
  - `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/src/main/resources/api.properties`, que **no se sube a git**; la plantilla es `api.properties.ejemplo`.
  - También puede venir de la propiedad del sistema `countryapi.restcountries.clave` o de la variable de entorno `COUNTRYAPI_RESTCOUNTRIES_CLAVE`.
  - Sin clave, o si la API la rechaza, se responde 502 «La API REST Countries no está disponible, intente más tarde».
- **Probado con la API real:** se recibieron 254 países (242 registrados y 12 actualizados), con nombres en español y la capital principal correcta, y ninguno descartado.
- **Cuota:** el plan gratuito tiene 1000 peticiones al mes. Cada importación usa 3 y cada consulta por nombre, 1.
- **Pendiente tuyo:**
  - Actualizar en el ERS y el ECUD la versión (v3.1 → v5), la URL, la clave y la tabla 41. Yo no modifico `01. UML`.
  - Avisar al equipo de la versión .NET (TI.2.2), que tiene el mismo problema.

### C2. Decisiones menores que tomé (avísame si alguna no te parece)

- **Mensaje de los listados:** «Consulta realizada correctamente» para `GET /api/Paises` y `GET /api/Universidades`. El ECUD no fija un mensaje para el listado; es un texto nuevo que la versión .NET tendría que copiar.
- **JSON del contrato REST:** lo escribe y lee JSON-B, que viene con Payara. Gson se usa solo para el JSON de las APIs públicas, como dicen el ERS y el RNF-002.
- **Payara local:** para que Payara 7 arranque en este equipo, Hazelcast de `domain1` busca solo `127.0.0.1:4900`. Antes chocaba con TightVNC en el puerto 5900. La copia original está en `domain.xml.bak-antes-countryapi`.

## D. Fase 2 (cliente de escritorio)

Decisiones que tomé; ninguna cambia el contrato del servidor.

- **D1. Las vistas del diagrama son interfaces.**
  - `VistaBase`, `LoginVista`, `MenuVista`, `PaisVista`, `BusquedaVista`, `ConsultaApiVista`, `ImportacionVista`, `ReporteVista` y `UniversidadVista` conservan sus nombres y métodos.
  - Los paneles Swing las implementan, y la consola, la web y Android tendrán su propia implementación. Así los controladores se prueban sin pantalla.
  - Agregué los métodos mínimos que exige una interfaz gráfica:
    - `PaisVista.leerPais(Pais inicial)`, para editar con los datos actuales;
    - `PaisVista.paisSeleccionado()`, para saber qué fila eligió el usuario.
- **D2. Controladores y vistas (B3 aplicado).**
  - `ApiControlador` tiene las dos vistas que dependen de él: consulta e importación.
  - `LoginControlador` recibe además `MenuVista`, para abrir el menú después del inicio de sesión.
  - `PaisControlador` recibe además `BusquedaVista`.
- **D3. Mensajes nuevos del cliente**, que no están en el ECUD:
  - «Seleccione un país de la tabla», al editar, eliminar o guardar sin elegir una fila;
  - «Bienvenido, <nombre>.» en el menú;
  - en Reportes, cuando no hay países, se agrega «Use «Importar países» para cargarlos.» al mensaje del servidor, como sugiere el ECUD6.
- **D4. Población o área no numéricas.** El formulario devuelve -1 o NaN y `ValidadorDatos` muestra el mensaje del ECUD («La población debe ser un número entero mayor o igual que cero»). Se aceptan separadores de miles en la población y coma o punto decimal en el área.

## E. Fase 3 (cliente móvil)

Decisiones que tomé; ninguna cambia el contrato del servidor ni los controladores.

- **E1. Mismo modelo, servicios y controladores que el escritorio.** `modelos`, `servicios`, `controlador` y las interfaces de `vista` son copias del cliente Swing. Solo cambia `ClienteRest`, en tres puntos:
  - usa métodos que existen en Android 7 (minSdk 24);
  - la dirección del servidor se puede cambiar en caliente (`setUrlBase`);
  - ya no tiene constructor sin argumentos, porque la URL sale de `ConfigServidor`, que en Android lee las preferencias de la app.
- **E2. Una sola actividad.** `PrincipalActivity` implementa `MenuVista`, como `VentanaPrincipal` en escritorio, y muestra fragments: inicio de sesión, menú de tarjetas y un fragment por módulo, que implementa su interfaz de vista.
  - Los controladores corren en un hilo aparte y piden datos a la vista de forma síncrona, igual que en Swing.
  - Los diálogos (formulario, confirmaciones) bloquean ese hilo hasta que el usuario responde.
- **E3. Selección en Países.** No hay tabla: al tocar un país se marca y aparece el menú «Editar / Eliminar». En «Consultar país en la API», cada resultado tiene su botón «Guardar».
- **E4. Dirección del servidor configurable desde la app.**
  - Por defecto es `http://10.0.2.2:8080/WS_COUNTRYAPI_JAVA_RESTFul/api` (el emulador). Se cambia en `gradle.properties` o en la pantalla de inicio de sesión con «Cambiar servidor».
  - Mensajes nuevos de esa pantalla: «Ingrese una dirección que empiece con http:// o https://» y «Servidor actualizado: <url>».
- **E5. Error de validación dentro del formulario.** Cuando el controlador reabre el formulario por un error (validación, 400 o 409), el mensaje aparece arriba del formulario, además de en la pantalla, porque el diálogo la tapa.
- **E6. Pruebas.** Se reutilizan las 49 pruebas del escritorio. El servidor falso se reescribió con `ServerSocket`, porque las pruebas de Android se compilan contra `android.jar`, que no trae `com.sun.net.httpserver`. Hay 16 pruebas nuevas de `ConfigServidor` y del cambio de servidor.

## F. Fase 4 (cliente de consola)

Decisiones que tomé; ninguna cambia el contrato del servidor ni los controladores.

- **F1. Mismo código que el escritorio.** `modelos`, `servicios`, `controlador`, `config` y las interfaces de `vista` son copias sin ningún cambio del cliente Swing. Las pantallas de texto (`LoginConsola`, `PaisConsola`, ...) implementan esas interfaces.
- **F2. Navegación.**
  - El menú principal está numerado del 1 al 7, en el orden del ECUD1, con «Cerrar sesión» como 7.
  - Países y Universidades tienen su propio submenú con «0. Volver».
  - Buscar y Consultar en la API preguntan «¿Hacer otra búsqueda?» / «¿Consultar otro país?».
- **F3. Cómo se elige un país.** En Países se escribe el **ID** de la tabla; en la consulta a la API, el **N°** del resultado.
- **F4. Formulario por campos.**
  - `Enter` conserva el valor entre corchetes y `-` deja vacío un campo opcional.
  - Al final pregunta «¿Guardar los datos? (S/N)»; responder N equivale a «Cancelar».
- **F5. Textos nuevos de la consola**, que no están en el ECUD:
  - «Opción no válida»;
  - «Escriba 0 como usuario para salir»;
  - «Hasta luego.».

  El programa también termina si se acaba la entrada (Ctrl+Z / Ctrl+D).
- **F6. Colores.** Solo en terminales que entienden ANSI; el texto es idéntico sin ellos y los errores siempre llevan «ERROR:».
- **F7. Tabla completa después de guardar.** Igual que en escritorio, después de registrar, editar o eliminar se vuelve a mostrar la tabla completa (254 filas con la base importada). Aceptado así el 23/11/2026.

## G. Fase 5 (cliente web)

Decisiones que tomé; ninguna cambia el contrato del servidor ni los controladores.

- **G1. Mismo código que el escritorio.** `modelos`, `servicios`, `controlador`, `config` y las interfaces de `vista` son copias sin cambios del cliente Swing. Las vistas web (`LoginVistaWeb`, `PaisVistaWeb`, ...) implementan esas interfaces.
- **G2. Controladores por usuario.**
  - Cada `HttpSession` guarda su `SesionServicio` (con el token), sus vistas y sus controladores (`ModulosSesion`).
  - En cada petición, las vistas se enlazan con ella mientras el controlador trabaja.
  - Así `ApiControlador` recuerda la última consulta y «Guardar» no gasta otra consulta a REST Countries.
- **G3. Formulario y confirmaciones sin JavaScript.**
  - El formulario de país se muestra en la misma página (`?nuevo`, `?editar=ID`). Si hay error de validación, 400 o 409, se vuelve a mostrar con lo escrito y el mensaje.
  - Eliminar e importar muestran un panel de confirmación con un botón que envía `confirmado=si`.
- **G4. POST/Redirect/GET.** Después de una operación exitosa se redirige y el aviso viaja guardado en la sesión, para que recargar la página no repita la operación. La excepción es la búsqueda en la API: es un GET y recargarla vuelve a consultar.
- **G5. Seguridad.**
  - Se agregó `CsrfFilter`, igual al de la referencia, además del `AuthFilter` que pide el prompt.
  - Al iniciar sesión se cambia el id de sesión.
  - La sesión HTTP dura 15 minutos, como el token.
  - Los enlaces a sitios web de universidades solo se crean si empiezan con http(s).
- **G6. Textos nuevos de la web**, que no están en el ECUD:
  - en la página de error: «La solicitud no es válida o la página caducó. Vuelva a intentarlo desde el menú.» (403) y «La página no existe.» (404);
  - el contador «N resultados» en la consulta a la API.

  El resto son los textos del escritorio.
- **G7. Dirección del servidor.** `servidor.properties` va dentro del war. Para cambiar la IP sin recompilar se usa la propiedad de sistema `countryapi.config` de Payara, la misma que en escritorio y consola.
