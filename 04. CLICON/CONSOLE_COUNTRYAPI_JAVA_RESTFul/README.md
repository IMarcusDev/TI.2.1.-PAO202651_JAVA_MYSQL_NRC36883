# CONSOLE_COUNTRYAPI_JAVA_RESTFul

Cliente de consola de CountryAPI. Consume **solo** el servidor RESTful `WS_COUNTRYAPI_JAVA_RESTFul`; nunca llama a MySQL ni a las APIs públicas.

- Java 17 y Maven. Gson 2.10.1 para el JSON.
- Menú numerado y tablas de texto con bordes.
- Los errores se muestran con el prefijo `ERROR:`.
- En terminales que entienden colores ANSI (Windows Terminal, Git Bash, Linux, macOS), los títulos salen en azul, los mensajes en verde y los errores en rojo. La variable `NO_COLOR` los desactiva. El texto es el mismo con o sin color.

## Configuración: dónde está el servidor

Igual que en el cliente de escritorio, la dirección está en `servidor.properties`:

```properties
servidor.url=http://localhost:8080/WS_COUNTRYAPI_JAVA_RESTFul/api
```

El archivo se busca en este orden:

1. la ruta indicada con `-Dcountryapi.config=<ruta>`;
2. `servidor.properties` en la carpeta desde donde se ejecuta. Maven lo copia a `target/`, junto al jar;
3. el que va dentro del jar.

Cada petición espera como máximo 15 segundos (`ConfigServidor.TIMEOUT_MS`).

## Compilar y ejecutar

Requisitos:

- el servidor desplegado en Payara (ver `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/README.md`);
- la base levantada con `03. BDD/docker-compose.yml`.

```bash
mvn package                        # genera target/CONSOLE_COUNTRYAPI_JAVA_RESTFul.jar
cd target
java -jar CONSOLE_COUNTRYAPI_JAVA_RESTFul.jar
```

En Windows, el programa cambia la consola a UTF-8 (`chcp 65001`) para que las tildes se vean bien.

Usuarios de prueba: `monster` / `monster9` y `admin` / `admin123`. Para salir, escriba `0` como usuario.

## Uso

- **Menú principal**, en el orden del ECUD1:
  1. Importar países
  2. Consultar país en la API
  3. Países
  4. Buscar y filtrar
  5. Reportes
  6. Universidades
  7. Cerrar sesión

  Encima de las opciones se indica si el servidor responde.
- **Países:**
  - al entrar se muestra la tabla;
  - opciones: `1` listar, `2` buscar por nombre, `3` nuevo, `4` editar, `5` eliminar, `0` volver;
  - para editar o eliminar se escribe el **ID** de la tabla, y eliminar pide confirmación.
- **Formulario de país:**
  - pide cada campo mostrando el valor actual entre corchetes: `Enter` lo conserva y `-` deja vacío un campo opcional;
  - la región se elige por número o por nombre;
  - al final pregunta `¿Guardar los datos? (S/N)`, y `N` cancela;
  - si los datos no son válidos o el país ya existe, muestra el error y vuelve a pedir los datos con lo escrito.
- **Buscar y filtrar:** nombre y capital parciales (`Enter` significa «cualquiera») y región por número (`0` todas).
- **Consultar país en la API:** al menos 2 caracteres. La tabla indica si cada país ya está guardado; para guardar uno, se escribe su **N°**.
- **Reportes:** país más poblado, países por región y promedio poblacional por región.
- **Universidades:** `1` importa las de un país (en inglés, por defecto el último escrito) y `2` muestra las guardadas.
- **Sesión expirada:** si el servidor rechaza el token (sesión expirada o servidor reiniciado), se muestra el error y se vuelve al inicio de sesión.

## Pruebas

```bash
mvn test
mvn test -Dtest=ConsolaTest                 # una clase
mvn test -Dtest=ValidadorDatosTest#credenciales
```

Las 64 pruebas son JUnit 5 y no necesitan el servidor real:

- **`ValidadorDatosTest`, `ServiciosTest` y `ControladoresTest`:** las mismas del cliente de escritorio. Prueban validaciones, servicios y controladores contra un servidor HTTP falso.
- **`ConsolaTest`:**
  - tablas de texto;
  - el formulario campo por campo y la selección por ID;
  - el ciclo completo de `App` con entrada simulada: inicio fallido, inicio correcto, opción no válida, reporte, cierre de sesión y salida;
  - el regreso al inicio de sesión cuando la sesión expira.

## Estructura (diagrama DC_Clientes)

```
src/main/java/ec/edu/monster/
├── App.java          Punto de entrada: arma servicios, pantallas y controladores; ciclo login → menú
├── config/           ConfigServidor (URL_BASE desde servidor.properties, TIMEOUT_MS = 15000)
├── modelos/          Pais, Reporte, Universidad, RespuestaDTO<T>, ...      (iguales al escritorio)
├── servicios/        ClienteRest (HttpURLConnection + Gson), SesionServicio y un servicio por módulo
├── controlador/      Login, Pais, Api, Reporte, Universidad y ValidadorDatos (iguales al escritorio)
└── vista/            Interfaces del diagrama (VistaBase, LoginVista, PaisVista, ...) y su
                      implementación de texto (LoginConsola, MenuConsola, PaisConsola, ...),
                      Consola (entrada y salida) y Tabla (tablas de texto)
```

Las dependencias van en un solo sentido: `vista → controlador → servicios → ClienteRest`. Modelo, servicios y controladores son los mismos archivos del cliente de escritorio, sin cambios.
