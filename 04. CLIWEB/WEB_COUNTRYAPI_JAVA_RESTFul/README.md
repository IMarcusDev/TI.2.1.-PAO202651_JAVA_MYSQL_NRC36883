# WEB_COUNTRYAPI_JAVA_RESTFul

Cliente web (JSP + Servlets) de CountryAPI. Corre en Payara y consume **solo** el servidor RESTful `WS_COUNTRYAPI_JAVA_RESTFul`; nunca llama a MySQL ni a las APIs públicas.

- Java 17, Jakarta EE 10 (Servlet 6, JSP y JSTL de Payara) y Maven. Gson 2.10.1 para el JSON.
- Diseño de referencia:
  - barra superior azul con navegación, usuario e indicador del servidor;
  - menú de tarjetas;
  - tablas con encabezado celeste;
  - avisos verdes y rojos.
- La página se adapta a pantallas pequeñas.
- Todo funciona sin JavaScript. El JS solo actualiza el indicador cada 10 segundos y envía el filtro de región al elegirlo.

## Configuración: dónde está el servidor

La dirección está en `src/main/resources/servidor.properties`:

```properties
servidor.url=http://localhost:8080/WS_COUNTRYAPI_JAVA_RESTFul/api
```

Para cambiarla **sin recompilar**:

1. Cree un archivo con esa misma clave.
2. Apunte Payara a ese archivo y reinicie la aplicación:

   ```bash
   asadmin create-system-properties "countryapi.config=C\:/ruta/servidor.properties"
   ```

En Git Bash, anteponga `MSYS2_ARG_CONV_EXCL="countryapi"` y escape los `:` como `\:`.

## Compilar y desplegar

Requisitos:

- el servidor desplegado en Payara (ver `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/README.md`);
- la base levantada con `03. BDD/docker-compose.yml`.

```bash
mvn package                                   # genera target/WEB_COUNTRYAPI_JAVA_RESTFul.war
/c/payara7/bin/asadmin deploy --force=true target/WEB_COUNTRYAPI_JAVA_RESTFul.war
```

Abra <http://localhost:8080/WEB_COUNTRYAPI_JAVA_RESTFul/>. Usuarios de prueba: `monster` / `monster9` y `admin` / `admin123`.

## Pruebas

```bash
mvn test
mvn test -Dtest=WebTest                         # una clase
mvn test -Dtest='WebTest$Paises'                # un grupo
```

Las 69 pruebas son JUnit 5 y no necesitan Payara ni el servidor real:

- **`ValidadorDatosTest`, `ServiciosTest` y `ControladoresTest`:** las mismas del cliente de escritorio, contra un servidor HTTP falso.
- **`WebTest`:** servlets y filtros con un navegador simulado (Mockito) y el servidor falso. Cubre:
  - inicio y cierre de sesión;
  - CSRF y control de acceso;
  - el formulario de país que se reabre con lo escrito (validación y 409);
  - eliminar solo con confirmación;
  - guardar desde la consulta a la API sin repetir la consulta;
  - la sesión expirada.

## Seguridad

- **Token en la sesión.** El token del servidor vive en la `HttpSession`, dentro del `SesionServicio` de cada usuario; nunca llega al navegador.
- **Cookie de sesión.** Es `HttpOnly`, solo por cookie, y caduca a los 15 minutos, igual que el token. Al iniciar sesión se cambia el id de sesión.
- **`AuthFilter`.** Sin sesión, las pantallas protegidas redirigen a `/login`.
- **`CsrfFilter`.** Todo POST lleva el token CSRF de la sesión: sin él, se responde 403, y en el inicio de sesión se muestra un formulario nuevo.
- **Errores.** Los textos se escapan con `<c:out>`. Los errores del contenedor (403, 404 y 500) muestran una página propia, sin trazas.

## Estructura (diagrama DC_Clientes)

```
src/main/java/ec/edu/monster/
├── config/        ConfigServidor (URL_BASE desde servidor.properties, TIMEOUT_MS = 15000)
├── modelos/       Pais, Reporte, Universidad, RespuestaDTO<T>, ...      (iguales al escritorio)
├── servicios/     ClienteRest (HttpURLConnection + Gson), SesionServicio y un servicio por módulo
├── controlador/   Login, Pais, Api, Reporte, Universidad y ValidadorDatos (iguales al escritorio)
├── vista/         Interfaces del diagrama y su implementación web (LoginVistaWeb, PaisVistaWeb, ...)
├── web/           Servlets (uno por pantalla), ModulosSesion, Pagina y AplicacionWeb
└── filtros/       AuthFilter y CsrfFilter
src/main/webapp/
├── WEB-INF/vista/ JSP de cada pantalla y partes comunes (barra, avisos, token CSRF)
├── css/           countryapi.css (paleta de referencia)
└── js/            countryapi.js (indicador del servidor y filtros automáticos)
```

**Cómo encajan los controladores de escritorio con la web:**

- Cada usuario tiene en su sesión sus vistas y controladores (`ModulosSesion`).
- En cada petición, el servlet enlaza las vistas con la petición y llama al controlador:
  - lo que el controlador «muestra» queda como atributo y lo dibuja la JSP;
  - lo que «lee» sale de los parámetros del formulario.
- Si el controlador reabre el formulario de país por un error, `PaisVistaWeb` lo deja pendiente con lo escrito y la JSP lo vuelve a mostrar con el mensaje.
- Después de un POST que funcionó, se redirige (POST/Redirect/GET) y el aviso pasa la redirección guardado en la sesión.

## Pantallas

| Ruta | Pantalla |
|---|---|
| `/login` | Inicio de sesión |
| `/menu` | Menú de tarjetas (orden del ECUD1) |
| `/importar` | Importar países, con panel de confirmación y resumen |
| `/consultar` | Consultar país en la API; «Guardar» por fila |
| `/paises` | Lista, búsqueda por nombre, nuevo, editar y eliminar (con confirmación) |
| `/buscar` | Buscar y filtrar: nombre, capital y región; la región vuelve a consultar al elegirla |
| `/reportes` | País más poblado, países por región y promedio poblacional |
| `/universidades` | Importar y ver las universidades de un país (en inglés) |
| `/estado` | JSON del indicador del servidor |
