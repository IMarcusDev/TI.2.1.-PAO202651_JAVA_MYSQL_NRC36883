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
