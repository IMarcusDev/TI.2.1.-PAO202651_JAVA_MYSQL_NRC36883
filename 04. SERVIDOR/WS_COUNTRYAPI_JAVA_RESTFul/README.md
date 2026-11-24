# WS_COUNTRYAPI_JAVA_RESTFul

Servidor RESTful de CountryAPI (TI.2.1). Expone la API que consumen los cuatro clientes, consume las APIs públicas de países y de universidades, y guarda los datos en MySQL.

- Java 17, Jakarta EE 10 (JAX-RS), Payara 6 o 7.
- Gson 2.10.1 y mysql-connector-j 8.3.0, con JDBC puro y DAO.
- URL base: `http://<servidor>:8080/WS_COUNTRYAPI_JAVA_RESTFul/api`

## 1. Base de datos

Desde la carpeta `03. BDD` del proyecto:

```bash
docker compose up -d      # MySQL 8 con init.sql y seed.sql ya cargados (root / root, puerto 3306)
```

La conexión se configura en `src/main/resources/db.properties`:

| Clave | Por defecto |
| --- | --- |
| `db.url` | `jdbc:mysql://localhost:3306/api_paises?sslMode=DISABLED&allowPublicKeyRetrieval=true&...` |
| `db.usuario` | `root` |
| `db.clave` | `root` |

Si cambia algún valor, vuelva a empaquetar. TLS va desactivado porque Payara usa su propio almacén de certificados y la conexión TLS con MySQL falla («unknown_ca»).

## 1b. Clave de REST Countries v5

La versión v3.1 de REST Countries fue retirada. El servidor usa la **v5**, que exige una clave gratuita (https://restcountries.com/api-keys, plan Free con 1000 peticiones al mes).

1. Copie `src/main/resources/api.properties.ejemplo` como `api.properties`, en la misma carpeta.
2. Ponga su clave en `restcountries.clave=...`.
3. Vuelva a empaquetar.

`api.properties` no se sube a git. Sin clave, la importación y la consulta de países responden 502; el resto del sistema funciona igual.

## 2. Compilar y desplegar

```bash
mvn package                                   # genera target/WS_COUNTRYAPI_JAVA_RESTFul.war y corre las pruebas
asadmin start-domain domain1
asadmin deploy --force=true target/WS_COUNTRYAPI_JAVA_RESTFul.war
```

También se puede abrir el proyecto en NetBeans y ejecutarlo en Payara.

## 3. Pruebas

**Unitarias (JUnit 5, sin base ni internet):**

```bash
mvn test
mvn test -Dtest=ConsumoAPITest                          # una clase
mvn test -Dtest=TokenServiceTest#cadaUsoRenuevaElTiempo # un método
```

Cubren `HashService`, `TokenService`, el mapeo del JSON de `ConsumoAPI` y `ConsumoAPIUniversidades` (con un servidor HTTP falso), los códigos HTTP de `RespuestaDTO` y las reglas de los controladores con DAO en memoria.

**De integración, con el servidor desplegado** (desde Git Bash):

```bash
bash pruebas/curl_countryapi.sh
```

Ejecuta un `curl` por endpoint, incluidos los casos 400, 401, 404, 409 y 502. Para simular una API externa caída (502) usa `asadmin create-system-properties` y después borra las propiedades.

## 4. Propiedades del sistema opcionales

| Propiedad | Uso |
| --- | --- |
| `countryapi.restcountries.url` | Dirección de REST Countries (por defecto `https://api.restcountries.com/countries/v5`) |
| `countryapi.restcountries.clave` | Clave de REST Countries; tiene prioridad sobre `api.properties` |
| `countryapi.universidades.url` | Dirección de la API de universidades (por defecto `http://universities.hipolabs.com`) |

Se leen en cada petición, así que no hace falta reiniciar Payara.

## 5. Problemas conocidos

- **REST Countries v3.1 fue retirada.** El servidor usa la v5 (ver la sección 1b y la duda C1 en `DUDAS.md`).
- **Payara 7 no arranca («Could not join cluster»)** si otro programa escucha en el puerto 5900 (por ejemplo, TightVNC). En `domain1` se configuró Hazelcast para que solo se busque a sí mismo:
  - en `domain.xml`: `<hazelcast-runtime-configuration discovery-mode="tcpip" tcpip-members="127.0.0.1:4900">`;
  - la copia original quedó en `domain.xml.bak-antes-countryapi`.
