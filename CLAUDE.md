# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

University assignment TI.2.1 (Arquitectura de Software, ESPE): **CountryAPI**, a Java + MySQL system using MVC. It consumes the public REST Countries and Hipolabs universities APIs. A Java REST server stores the data in MySQL, and four clients consume that server. All code, comments, messages and docs are written in **Spanish**.

The full implementation brief is [PROMPT_CLAUDE_CODE_TI21.md](PROMPT_CLAUDE_CODE_TI21.md). It covers the endpoint contract, JSON mapping rules, class list, validations, reference projects and how to work in phases. Read it before implementing anything.

## Source of truth and hard rules

- The approved documentation is the source of truth. Do not invent endpoints, tables, classes or messages that it does not contain. A .NET twin of this system (TI.2.2) must match it exactly, so names, routes and message texts must be the documented ones, word for word.
- **Never modify** `01. UML/`, `02. MER/`, `03. BDD/` or the taller `.docx` in the root.
- Log any ambiguity or contradiction in `DUDAS.md` at the root and ask the user. Do not resolve it on your own.
- Work in phases: server → desktop → mobile → console → web. Stop after each phase and report what was done, how to test it and what doubts remain.
- Do not commit, and do not delete files you did not create.

## Where things are documented

| Need | Source |
|---|---|
| DB schema (`api_paises`: `usuario`, `region`, `pais`, `universidad`) and test data | `03. BDD/init.sql`, `03. BDD/seed.sql` (users `monster/monster9`, `admin/admin123`; passwords stored as SHA-256) |
| Physical model (check that it matches `init.sql`) | `02. MER/02_MER_COUNTRYAPI.pdm` |
| Requirements RQF-001..011, RNF-001..013, HTTP codes, exact messages, REST Countries field mapping | `01. UML/01. ERS/*.docx` |
| Basic and alternative flows of the 11 use cases | `01. UML/02. ECUD/*.docx` |
| Exact package, class, attribute and operation names | `01. UML/03. DIAGRAMAS/04_CLASES/DC_Servidor.oom`, `DC_Clientes.oom` |
| Call order (WebService → TokenService → Controlador → DAO / ConsumoAPI) and the SQL of each operation | `03_SECUENCIA/DS_*.oom` |
| Decisions and validations | `02_ACTIVIDAD/DA_*.oom` |
| Deployment and layers | `05_ARQUITECTURA/ARQ_CountryAPI.eam` |

`.docx` files are not plain text. Convert them with `pandoc`, `python-docx`, or by unzipping `word/document.xml`. `.oom`, `.pdm` and `.eam` files are PowerDesigner XML: parse the `o:Class` elements, their `c:Attributes` and `c:Operations`, and the `a:Name`, `a:DataType` and `a:Visibility` fields. Write the extracts **outside** the repository, for example in the scratchpad.

## Architecture (planned projects)

```
04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul      Maven war, Java 17, Jakarta EE JAX-RS on Payara :8080, base path /api
04. CLIESC/ESC_COUNTRYAPI_JAVA_RESTFul       Swing (Maven), JTable
04. CLIMOV/MOV_COUNTRYAPI_JAVA_RESTFul       Android Java (Gradle), minSdk 24, Material; emulator → 10.0.2.2:8080
04. CLICON/CONSOLE_COUNTRYAPI_JAVA_RESTFul   Maven console app, text menu
04. CLIWEB/WEB_COUNTRYAPI_JAVA_RESTFul       JSP + Servlets on Payara, token in HttpSession, auth filter
```

**Server** (root package `ec.edu.monster`): `ws` (JAX-RS resources) → `controladores` → `modelos.repositorios` (DAOs behind `modelos.interfaces`), using plain JDBC with `PreparedStatement` and no JPA. `servicios` holds `TokenService` (in-memory UUID token, 15-minute sliding expiry), `HashService` (SHA-256), `ConsumoAPI` and `ConsumoAPIUniversidades` (`HttpURLConnection`, 15 s timeout). `config` holds `ApplicationConfig` and `DBConfig`, which reads `db.properties` (no hard-coded credentials). Libraries: Gson 2.10.1, mysql-connector-j 8.3.0.

Contracts that cut across the whole server:
- Every endpoint returns `RespuestaDTO<T>` `{estado: "OK"|"ERROR", mensaje, datos}`. On error, `datos` is empty. Messages never expose SQL or stack traces.
- HTTP codes: 200/201, 400, 401, 404, 409 (duplicate country), 502 (external API failed, and the DB is left untouched), 500.
- The `Authorization` header is accepted with or without the `Bearer ` prefix.
- Bulk imports (countries, universities) upsert inside **one** transaction using `executeBatch`.
- Controllers signal errors with standard exceptions. `RespuestaDTO.codigoHttp` maps them to status codes:
  - `IllegalArgumentException` → 400
  - `SecurityException` → 401
  - `NoSuchElementException` → 404
  - `IllegalStateException` → 409
  - `IOException` → 502
  - anything else → 500, with a fixed message

  Do not add custom exception classes.
- Each `*WebService` validates the token, calls its controller and builds the response. JSON-B (bundled with Payara) serializes the REST contract; Gson is used only for the external APIs' JSON.
- All accepted design decisions and open questions live in `DUDAS.md`. Read it before changing behavior.

**Clients**: all four share the same MVC layout from `DC_Clientes`: `vista` (`VistaBase` + one view per module), `controlador` (one per module + `ValidadorDatos`), `servicios` (`ClienteRest` with Gson, `SesionServicio` and the per-module services), `config/ConfigServidor` (`URL_BASE`, `TIMEOUT_MS = 15000`, read from a config file so the IP can change without recompiling) and `modelos`. Clients talk **only** to the server, never to MySQL or the external APIs. A 401 response clears the session and returns to login.

## Commands

```bash
# Database: MySQL is not installed locally; use Docker Desktop + the compose file.
# The first start runs init.sql then seed.sql (root password "root", port 3306).
cd "03. BDD" && docker compose up -d        # wait until `docker compose ps` shows healthy
docker compose down -v && docker compose up -d   # reset the DB to the seed data

# Maven projects (server, desktop, console, web)
mvn package
mvn test
mvn test -Dtest=NombreClaseTest#metodo   # single test

# Android client
./gradlew assembleDebug
./gradlew test --tests "ec.edu.monster.*NombreClaseTest"

# Server: deploy to the local Payara 7 (C:\payara7) and run the curl suite (Git Bash)
/c/payara7/bin/asadmin start-domain domain1
/c/payara7/bin/asadmin deploy --force=true target/WS_COUNTRYAPI_JAVA_RESTFul.war
bash pruebas/curl_countryapi.sh            # exits with the number of failed cases
```

Environment gotchas:

- **REST Countries is on v5, not the documented v3.1** (which was retired; see DUDAS.md C1).
  - Base URL: `https://api.restcountries.com/countries/v5`, with `Authorization: Bearer <key>`.
  - Responses come in `data.objects`, paginated at 100 per page.
  - The key lives in the gitignored `src/main/resources/api.properties`. Never commit it and never hard-code it.
  - The free plan allows 1000 requests a month: each import uses 3 and each name lookup uses 1. Avoid needless live calls; use the fake server in the tests instead.
- **Pointing the external APIs elsewhere:** set the system properties `countryapi.restcountries.url` and `countryapi.universidades.url`; they are read on every request.
- **Escaping in Git Bash:** pass `asadmin` arguments that contain `:` with `MSYS2_ARG_CONV_EXCL="countryapi"`, and escape each `:` as `\:`.
- **Payara and port 5900:** TightVNC listens on 5900, and that breaks Hazelcast discovery. `domain1` therefore uses `discovery-mode="tcpip" tcpip-members="127.0.0.1:4900"`.
- **MySQL TLS from Payara:** it fails with `unknown_ca`, so `db.properties` uses `sslMode=DISABLED&allowPublicKeyRetrieval=true`.

Before calling the server phase done: `mvn package` must succeed, and `04. SERVIDOR/WS_COUNTRYAPI_JAVA_RESTFul/pruebas/` must contain `curl` commands for every endpoint, including the 400, 401, 404, 409 and 502 cases. Tests use JUnit 5. Clients test their controllers against a fake server. Each project gets a short `README.md` covering configuration, how to run it and how to run its tests.

## Reference projects (visual design and code layout only, not business logic)

- UI style: `..\..\PARCIAL_1\PAO202651_EXAMEN_CELULARES_RESTFUL_DOTNET_GR04\04. CLI*`: blue top bar, card menu, light-blue table headers, green/red alerts, server-status indicator. Palette: azul `#1F4E79`, azul oscuro `#183D60`, azul claro `#E8EFF7`, fondo `#F3F4F6`, texto `#1F2933`, texto suave `#4B5563`, borde `#D1D5DB`, verde `#22C55E`, rojo `#B91C1C`, rojo claro `#FDECEC`.
- Java structure, `pom.xml` and `build.gradle`, Swing `Estilos.java`, `AuthFilter`, Android fragments, fake-server tests: `..\..\PARCIAL_1\CONBDD\TG.1.3. PAO202651_RESTFUL_JAVA_CONBDD_NRC36883_GR04\04. *`.
- App title is **CountryAPI**. Do not carry over text, class names or data from EurekaBank or Celulares.
