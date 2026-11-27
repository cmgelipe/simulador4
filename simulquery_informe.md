# Informe: funcionamiento de SimulQUERY y consumo de sus logs por XAMANA

Informe de solo lectura, generado a partir del código fuente tal como existe hoy en
el equipo. No se modificó, compiló ni compiló ningún proyecto; no se hicieron commits.

## Proyectos localizados

| Proyecto | Ruta | Rol |
|---|---|---|
| **SimulQUERY** | `C:\Users\cmgel\Documents\NetBeansProjects\simulador_v4` | Generador del log simulado (es el proyecto en el que está abierto este informe) |
| **XAMANA** | `C:\Users\cmgel\Documents\NetBeansProjects\xamana` | Lee y filtra el log para hacer fragmentación horizontal dirigida por datos (incluye los paquetes `org.xamana.log` y `org.xamana.filter`, que hacen de "observer"/lector de log) |
| **FRAGMENT** | `C:\Users\cmgel\Documents\NetBeansProjects\FRAGMENT` | Proyecto NetBeans/MySQL independiente, **no** lee logs (ver nota al final de la sección "Versiones") |
| **verticalDin** (observer-fragmenter, confirmado por el usuario) | `C:\Users\cmgel\Documents\NetBeansProjects\verticalDin` | Proceso observador independiente (jar ejecutable, clase `Vigilante`) que vigila en continuo el directorio de logs de un sitio y dispara la fragmentación **vertical** dinámica contra la base de registro de XAMANA (ver sección 6) |

**Corrección respecto a la primera versión de este informe:** el proyecto
`verticalDin` —identificado por el usuario como el "observer-fragmenter"— es un
tercer componente independiente, no parte de `xamana`. A diferencia de los
paquetes `org.xamana.log`/`org.xamana.filter` (que viven dentro de `xamana` y
sirven a un flujo web de fragmentación **horizontal** sobre un archivo de log
subido una vez), `verticalDin` es un **proceso en segundo plano que se ejecuta
por separado en cada sitio**, hecho con `java -jar verticalDin.jar <token>
<directorio_de_logs>` (ver `VerticalDin.java:18-22`), y vigila continuamente
el directorio de logs real del motor en ese sitio para la fragmentación
**vertical**. Las dos secciones siguientes (6 y, más abajo, las correcciones a
las secciones e/j) documentan `verticalDin` con la misma evidencia por
archivo:línea. `FRAGMENT` sigue siendo un prototipo distinto, más antiguo, que
no procesa logs (ver más abajo).

### Estado git de cada repositorio (snapshot, sin modificar nada)

- **simulador_v4** — rama `Felipe`, working tree limpio. `git log -5`:
  `6889a77` Cambio entero a jakarta · `062fb95` uso de servidores ·
  `0874ecb` Ignorar carpeta nbproject · `4434ad4` Generador de ids... ·
  `2120578` Cambio en Update...
- **verticalDin** — rama `master`, working tree limpio. `git log -5`:
  `620ef58` Conversión a proyecto maven · `b2f39d5` actualización 7 de juni 2023 ·
  `5491106` (sin mensaje) · `8571819` Fragmentación Mongo correcciones ·
  `915c2f6` Log y fragmentación Mongo
- **xamana** — rama `master`, working tree limpio. `git log -5`:
  `fe8845a` Corrección de la consideración de varios sitios diferentes en sus
  credenciales · `dbf2c8d` Conversión a proyecto maven · `7e4951f` Consideración
  de credenciales diferentes entre sitios... · `1bd0bca` Actualización del script
  sql... · `ac79808` Atributo multimedia en híbrida no CBIR
- **FRAGMENT** — rama `master`, con cambios sin confirmar (`Master respaldo (13
  jun 2022).sql`, `nbproject/genfiles.properties`, `nbproject/project.properties`
  modificados; `.gitignore` sin seguimiento). `git log` tiene solo 2 commits,
  ambos "FRAGMENT" (13 jun 2022). No se tocó nada de esto.

---

## 1. Generación de operaciones

### a. ¿SimulQUERY ejecuta las operaciones contra el DBMS, solo escribe un log, o ambas? ¿Depende del DBMS?

**Solo escribe un log de texto; nunca ejecuta de verdad el insert/update/delete
simulado contra el DBMS**, para ninguno de los 3 motores. Esto es igual en los
tres generadores:

- MongoDB: [`GeneradorLogMongoDB.java:38-47`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java) solo usa `colec.countDocuments()` y
  `colec.aggregate(...$sample...)` (lecturas) para obtener valores reales de
  muestra; no hay ninguna llamada a `insertOne`/`updateOne`/`deleteOne`/`deleteMany`
  en todo el archivo.
- MySQL: [`GeneradorLogMySQL.java:60,64,78,82,99,103`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java) solo invoca
  `acc.ejecutarConsulta("select ...")` (SELECT); la clase `AccesoDatosMySQL`
  sí tiene un método `ejecutarComando(...)` que hace `executeUpdate` real
  ([`AccesoDatosMySQL.java:99-137`](src/main/java/com/simulador/datos/AccesoDatosMySQL.java)), pero el generador de log **nunca lo llama**.
- PostgreSQL/Postgres-XL: [`GeneradorLogPostgreSQL.java:53,57,71,75,92,96`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java)
  usa exclusivamente `accexternopost.ejecutarConsulta("select ...")`.

Es decir: la conexión real al DBMS se usa únicamente como fuente de datos
reales (esquema, conteos y valores de muestra) para que el log simulado sea
verosímil, nunca para producir el efecto (insertar/actualizar/eliminar) que el
log describe. Esto **no depende del tipo de DBMS**: el patrón se repite
idéntico en los tres generadores.

### b. ¿Cómo se elige el tipo de operación?

El usuario marca uno o más tipos permitidos (checkbox `arrOPERACIONES`,
valores `a`=find/select, `b`=update, `c`=delete, `d`=insert — ver sección g).
Dentro de ese subconjunto habilitado, el tipo de operación **se elige
uniformemente al azar** con `Math.random()`:

- MongoDB: [`GeneradorLogMongoDB.java:100-101`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)
  `c_operacion0.get((int) (Math.random() * c_operacion0.size()))`.
- MySQL: [`GeneradorLogMySQL.java:59,77`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java) mismo patrón.
- PostgreSQL: [`GeneradorLogPostgreSQL.java:52,70`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java) mismo patrón.

No hay proporciones configurables por tipo de operación (no existe ningún
peso o porcentaje en la interfaz ni en el código); es uniforme sobre los tipos
habilitados. (Nota: el commit `4434ad4` del historial de git menciona
"Proporción desigual para generar las consultas y no uniforme", pero esa
proporción desigual —ver `distribuirOperaciones` en la sección h— se aplica a
cómo se **reparten las operaciones entre sitios**, no a qué tipo de operación
se elige dentro de cada una.)

### c. ¿Cómo se eligen los atributos de la proyección y del predicado?

**Atributo del predicado (la condición WHERE/filter):** siempre **un único
atributo**, elegido al azar uniformemente entre todos los atributos de la
tabla/colección:
- MongoDB: [`GeneradorLogMongoDB.java:58-59`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)
  `int numeroA = (int)(Math.random()*atributos.size()); atributoTipo = atributos.get(numeroA);`
- MySQL/PostgreSQL: mismo patrón, [`GeneradorLogMySQL.java:36-37`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java),
  [`GeneradorLogPostgreSQL.java:28-29`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java).

**Proyección:** **solo existe en el generador de MongoDB** (`find.projection`).
MySQL y PostgreSQL no generan ninguna lista de columnas seleccionadas en el
`SELECT` (siempre seleccionan el propio atributo del predicado como si fuera
`SELECT campo FROM tabla WHERE campo...`; no hay concepto de proyección
separada ni de `*`).

Para MongoDB, la proyección se construye en
[`atributosProjectionMongoDB(...)`, `GeneradorLogMongoDB.java:212-231`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java):
- Se baraja la lista de atributos (`Collections.shuffle(arr)`).
- Se eligen `nAtributos = (int)(Math.random()*arr.size())+1` atributos, es
  decir, un número aleatorio **entre 1 y el total de atributos** (ambos
  inclusive). No existe la opción de proyección vacía o de usar `"*"`
  (cuando se devuelve `"{}"` como projection vacía nunca ocurre por
  construcción: el mínimo es 1 atributo).

### d. ¿De dónde salen los valores de los predicados?

**De documentos/filas reales**, leídos en vivo de la conexión al DBMS que el
usuario configuró, no de valores inventados desde cero:

- MongoDB: [`muestraDocumentoConCampo(...)`, `GeneradorLogMongoDB.java:397-404`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)
  usa `$match` (campo existe y no es null) + `$sample(1)` sobre la colección
  real para traer un documento al azar con ese campo poblado, y de ahí se
  toma el valor del filtro (`valorString1`, `c_valor` en
  [`GeneradorLogMongoDB.java:104-160`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)).
- MySQL: `SELECT campo FROM tabla LIMIT <offset_aleatorio>,1` sobre una fila
  real ([`GeneradorLogMySQL.java:64,82,103`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java)), con el offset elegido
  al azar entre 0 y el conteo total de filas.
- PostgreSQL: `SELECT campo FROM tabla LIMIT 1 OFFSET <offset_aleatorio>`
  ([`GeneradorLogPostgreSQL.java:57,75,96`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java)), mismo mecanismo.

**Excepción — el "nuevo valor" de un `insert`/`update` es sintético, no real**,
para evitar duplicar un valor existente:
- MongoDB: `generarValorNuevoSintetico(tipo)` ([`GeneradorLogMongoDB.java:366-377`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java))
  genera un número aleatorio, una fecha actual, un `new ObjectId()` o un UUID
  aleatorio según el tipo de dato, en vez de tomarlo de otro documento real
  muestreado.
- MySQL/PostgreSQL: el `SET campo='...'` de un `update` usa
  `GeneradorLogUtil.generaCadena()` (UUID) o `generaNumero()` (entero
  1-25) en vez de un valor real ([`GeneradorLogMySQL.java:68,89`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java),
  [`GeneradorLogPostgreSQL.java:61,82`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java)). El `insert` de MySQL/PostgreSQL
  **no construye ninguna sentencia de log** (no hay rama `operacion0 ==
  "insert"` en ninguno de los dos generadores SQL — ver nota en la sección i).

### e. ¿Genera joins / $lookup?

**SimulQUERY no genera joins ni `$lookup`.** No se encontró ninguna ocurrencia
de `join`, `JOIN`, `lookup` o `$lookup` en el código fuente de SimulQUERY
(`grep` sin resultados sobre `src/main/java`). Cada operación generada actúa
sobre una sola tabla/colección (la que el usuario configuró), nunca combina
dos.

**Corrección:** en la primera versión de este informe se dijo que "XAMANA
tampoco extrae número de joins". Eso es cierto para los paquetes
`org.xamana.filter.*` dentro del propio proyecto `xamana` (el modelo
`Operation` no tiene ese campo, ver sección j), **pero no para
`verticalDin`** (el observer-fragmenter real, identificado por el usuario):
ese proceso sí calcula un número de joins/"costo de reuniones" (`nJoins`, 
impreso como `"CR: "+nJoins`) a partir de cada línea de log, aunque por un
mecanismo distinto para SQL que para Mongo — ver sección 6.c para el detalle
con archivo:línea. Como SimulQUERY nunca escribe la palabra `join` ni genera
consultas que combinen tablas, en la práctica ese cálculo sobre un log de
SimulQUERY para MySQL/PostgreSQL siempre da `nJoins = 1` (ver sección 6.c).

### f. ¿Usa semilla aleatoria fija o configurable? ¿Qué generador?

Usa **`Math.random()`** en todos los puntos de aleatoriedad (decenas de
llamadas en los tres generadores y en `GeneradorLogUtil`), **sin ningún
`java.util.Random` explícito ni semilla fija o configurable**. Ejemplos:
[`GeneradorLogUtil.java:30,49`](src/main/java/com/simulador/simulacion/GeneradorLogUtil.java) (`generaNumero`, `enteroEntre`),
[`GeneradorLogMongoDB.java:53-54,58,100-101,140,145,153-155,215`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java),
[`GeneradorLogMySQL.java:24,36,58,63,76,81,97,102`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java),
[`GeneradorLogPostgreSQL.java:21-22,28,51,56,69,74,90,95`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java).
No hay ningún `setSeed` ni parámetro de semilla expuesto en la interfaz ni en
el modelo `BD`. Cada ejecución produce, por tanto, un log distinto e
irreproducible exactamente.

### g. ¿Qué parámetros expone la interfaz y cuáles son sus valores por defecto?

Interfaz definida en [`src/main/webapp/index.xhtml`](src/main/webapp/index.xhtml); valores por defecto en los
campos del bean [`BD.java:24-25`](src/main/java/com/simulador/modelo/BD.java):

| Parámetro (UI) | Campo / variable | Valor por defecto | Notas |
|---|---|---|---|
| Gestor (`tipoBD`) | `BD.TipoBD` | ninguno (hay que elegir) | opciones: MySQL=`a`, Postgres-XL=`b`, PostgreSQL=`c`, MongoDB=`d` ([`index.xhtml:16-20`](src/main/webapp/index.xhtml)) |
| Dirección de la base de datos | `dirbd` | vacío | formato sugerido `#.#.#.#:#` |
| Nombre de la base de datos | `nombd` | vacío | |
| Usuario administrador | `usubd` | `""` ([`BD.java:33`](src/main/java/com/simulador/modelo/BD.java)) | opcional para Mongo sin auth |
| Contraseña | `passbd` | `""` ([`BD.java:34`](src/main/java/com/simulador/modelo/BD.java)) | opcional si la BD no tiene password |
| Nombre de la tabla/colección | `tabla.nombre` | ninguno (se elige de un combo poblado tras conectar) | |
| Número de consultas | `nConsultas` | **3** ([`BD.java:25`](src/main/java/com/simulador/modelo/BD.java)) | en Mongo es el **total** a repartir entre sitios; en MySQL/PostgreSQL es **por sitio** (ver sección h) |
| Número de sitios | `nSitios` | **2** ([`BD.java:24`](src/main/java/com/simulador/modelo/BD.java)) | |
| Tipo de consultas (checkboxes) | `arrOPERACIONES` | ninguno marcado por defecto | Selección=`a`, Actualización=`b`, Eliminación=`c`, Inserción=`d` ([`index.xhtml:57-62`](src/main/webapp/index.xhtml)) |

No hay ningún parámetro de semilla, de proporción por tipo de operación, ni de
tamaño de proyección en la interfaz.

---

## 2. Sitios

### h. ¿Cómo se reparten las operaciones entre sitios? ¿La IP se toma de máquinas reales o se escribe sintéticamente? ¿Hay IPs codificadas?

**Reparto entre sitios:**
- **MongoDB**: reparto **desigual** mediante la técnica "stars and bars":
  `distribuirOperaciones(nSitios, nConsultas)` ([`GeneradorLogMongoDB.java:337-358`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java))
  elige `nSitios-1` cortes aleatorios en `[0, nConsultas]`, los ordena, y la
  distancia entre cortes consecutivos es el número de operaciones de cada
  sitio. El comentario del código (líneas 28-31 y 48-51) es explícito: "antes
  cantidad fija por sitio... ahora reparto desigual del total... para que no
  se distribuya de forma pareja". `nConsultas` es aquí el **total** repartido.
- **MySQL/PostgreSQL**: **no hay reparto**, cada uno de los `nSitios` recibe
  exactamente `nConsultas` operaciones (bucle `for (int i=0;i<nConsultas;i++)`
  dentro de `for (int j=0;j<nSitios;j++)`, [`GeneradorLogMySQL.java:22,34`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java),
  [`GeneradorLogPostgreSQL.java:20,26`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java)). Es decir, para estos dos motores el
  reparto desigual del commit `4434ad4` **no se aplicó**; solo se aplicó al
  generador de Mongo.

**Origen de la IP:** en los tres generadores la IP (y, para Mongo, también el
puerto y el PID de conexión) se genera **sintéticamente con `Math.random()`**,
una vez por sitio, **no se captura de ninguna máquina real ni de la conexión
JDBC/Mongo real**:
- MongoDB: [`GeneradorLogMongoDB.java:53-54`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)
  `ip = ((int)(Math.random()*255))+"."+...+":"+ ((int)(Math.random()*90))+""+((int)(Math.random()*900))`
  (IP + puerto aleatorios), y `pid = ""+(int)((Math.random()*8001)+1000)`.
- MySQL: [`GeneradorLogMySQL.java:24`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java) mismo patrón de 4 octetos aleatorios (sin
  puerto).
- PostgreSQL: [`GeneradorLogPostgreSQL.java:22`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java) mismo patrón.

**No hay ninguna lista de IPs codificada en el código** (no se encontró ningún
arreglo/constante de direcciones IP en `src/main/java`); cada sitio obtiene una
IP aleatoria nueva en cada ejecución.

---

## 3. Formato de log

### i. Formato de log por DBMS (SimulQUERY)

**MongoDB** — imita el formato de log estructurado JSON de `mongod`
(`mongod.log`, nivel "Slow query", un objeto JSON por línea). Construido en
[`construirLogFind/Update/Delete/Insert`, `GeneradorLogMongoDB.java:238-328`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java).
Ejemplo (`find`, línea 245-253):

```json
{"t":{"$date":"2024-01-01T12:00:00.00Z"},"s":"I",  "c":"COMMAND",  "id":12345, "ctx":"conn1","msg":"Slow query","attr":{"type":"command","ns":"midb.mitabla","appName":"mongosh 1.1.9","command":{"find":"mitabla","filter":{"edad": {$gt:30} },"projection":{"nombre":1},"lsid":{"id":{"$uuid":"...uuid..."}},"$db":"midb"},"planSummary":"COLLSCAN","keysExamined":0,"docsExamined":77,"cursorExhausted":true,"numYields":1,"nreturned":1870,"queryHash":"4C06B3EF","planCacheKey":"4FDA4125","reslen":2000,"locks":{...},"storage":{...},"remote":"23.154.90.12:45321","protocol":"op_msg","durationMillis":60}}
```

**MySQL** — imita el formato del `general_log` de MySQL (líneas con
timestamp, un id/hilo con padding de 5 caracteres, y la columna `Query`/
`Connect`/`Init DB`/`Quit`). Construido en [`GeneradorLogMySQL.java:24-31,117`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java).
Ejemplo:

```
2024-01-01T12:00:00.00Z    1 Connect	root@45.12.90.3 on  using Socket
2024-01-01T12:00:00.00Z    1 Query	select @@version_comment limit 1
2024-01-01T12:00:00.00Z    1 Query	select nombre FROM mitabla WHERE nombre="Juan"
2024-01-01T12:00:00.00Z    1 Quit	
```

**PostgreSQL** — imita el formato de `log_statement`/`log_connections` de
PostgreSQL (línea con PID entre corchetes, separador `-_-` seguido de la IP, y
`LOG:  sentencia: ...` / `LOG:  conexion autorizada: ...`). Construido en
[`GeneradorLogPostgreSQL.java:21-23,58-63,110`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java). Ejemplo:

```
[3344]-_-45.12.90.3LOG:  conexion autorizada: usuario=admin base de datos=midb application_name=psql
[3344]-_-45.12.90.3LOG:  sentencia: select nombre FROM mitabla WHERE nombre='Juan'
[3344]-_-45.12.90.3LOG:  desconexión: duración de sesión 0:53:22.385 usuario=admin base=midb host=10.0.0.5
```

**Postgres-XL** — usa exactamente el **mismo generador y el mismo formato**
que PostgreSQL: en [`BD.java:85-86`](src/main/java/com/simulador/modelo/BD.java) tanto `tipoBD=="b"` (Postgres-XL)
como `tipoBD=="c"` (PostgreSQL) llaman a `generadorLogPostgreSQL.generar(...)`.
No existe un formato ni una clase distinta para Postgres-XL; el código lo trata
como un PostgreSQL más.

**Nota sobre `insert` en MySQL/PostgreSQL**: en ambos generadores SQL, el
`switch` por tipo de dato (`case 0/1/2`) construye las líneas de log para
`select`/`update`/`delete`, pero **ninguna rama escribe una sentencia de log
cuando `operacion == "insert"`** — revisando [`GeneradorLogMySQL.java:65-93,104-112`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java)
y [`GeneradorLogPostgreSQL.java:58-85,97-104`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java) no hay ningún
`if (operacion.compareTo("insert")==0)` en ninguno de los dos archivos. Es
decir: si el usuario marca "Inserción" para MySQL o PostgreSQL, esa opción
puede ser elegida al azar como tipo de operación pero **no produce ninguna
línea de log** en esa iteración (solo MongoDB implementa realmente `insert`,
en `construirLogInsert`). Esto es una observación directa del código, no una
suposición.

### j. XAMANA (y "observer-fragmenter"): formato esperado, extracción, y origen del log

Como se indicó arriba, el componente lector/filtrador vive dentro del propio
proyecto `xamana`, en `org.xamana.log.*` (lectura cruda del archivo) y
`org.xamana.filter.*` (extracción de operación/predicado/sitio por regex). No
hay un proyecto "observer-fragmenter" separado.

**Qué formato esperan leer:** el mismo formato que produce SimulQUERY (no el
log nativo real completo del motor, sino el subconjunto de campos que
SimulQUERY efectivamente escribe), vía expresiones regulares ad-hoc sobre
texto, no un parser JSON/estructurado:

- **MongoDB**: [`LogReaderMongoDB.determineOperations`, `LogReaderMongoDB.java:26-39`](src/main/java/org/xamana/log/LogReaderMongoDB.java)
  lee las líneas del archivo subido, filtra las que contienen
  `"<database>.<table>"` y **quita todas las comillas dobles**
  (`l.replaceAll("\"", "")`) antes de pasarlas al filtro. Luego
  [`FilterMongoDB.java:29-172`](src/main/java/org/xamana/filter/FilterMongoDB.java) busca por substring `"find"`, `"update"`,
  `"remove"` o `"insert"` y aplica regex:
  - tipo de operación: la propia palabra clave buscada en la línea (`find`→READ,
    `update`→UPDATE, `remove`→DELETE, `insert`→CREATE).
  - atributos/predicado: `filter:\{(.*?)\},lsid:` (find), `q:\{(.*?)\},u:`
    (update), `q:\{(.*?)\},limit` (delete); para `insert` no se extrae
    predicado, se usan todos los atributos de la tabla (`this.attributes`,
    poblados desde `FragmentationOperationsMongo.getAtributes()`,
    [`FragmentationOperationsMongo.java:34-38`](src/main/java/org/xamana/dhf/FOP/FragmentationOperationsMongo.java)).
  - número de joins: **no existe ningún campo ni cálculo de "número de
    joins"** en `FilterMongoDB`, en la clase base `Filter`
    ([`Filter.java`](src/main/java/org/xamana/filter/Filter.java)) ni en el modelo `Operation`
    ([`Operation.java:15-26`](src/main/java/org/xamana/dhf/model/Operation.java), que solo tiene `predicate`,
    `valueOperation`, `tuples`, `site`, `cost`, `frequency`, `typeOperation`,
    `performance`). Coherente con que SimulQUERY tampoco genera joins (sección e).
  - IP de origen: `regexSite = "remote:(.*?):"` ([`Filter.java:26`](src/main/java/org/xamana/filter/Filter.java)), que
    coincide exactamente con el campo `"remote":"ip:puerto"` que escribe
    SimulQUERY en cada línea de Mongo (una vez quitadas las comillas).

  **Discrepancia encontrada:** `FilterMongoDB.delete()`
  ([`FilterMongoDB.java:128-151`](src/main/java/org/xamana/filter/FilterMongoDB.java)) busca el patrón
  `"ndeleted:(.*?),"` para determinar cuántas tuplas se eliminaron, pero el
  log que genera SimulQUERY para `delete`
  ([`construirLogDelete`, `GeneradorLogMongoDB.java:289-306`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)) **no incluye
  ningún campo `ndeleted`** en el JSON que construye. Si esa línea
  llegara a XAMANA, el `match.find()` fallaría (lanzaría excepción al no
  encontrar el grupo). También busca `"remove"` como palabra clave
  ([`FilterMongoDB.java:42`](src/main/java/org/xamana/filter/FilterMongoDB.java)) mientras que SimulQUERY escribe el
  comando como `"delete":"tabla"` ([`GeneradorLogMongoDB.java:298`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java)), no
  `"remove"`. Esto es evidencia directa de desalineación entre el generador
  actual y el lector actual para el caso `delete` en Mongo; no es una
  suposición, es lo que muestran ambos archivos.

- **MySQL**: [`LogReaderMySQL.java:28-66`](src/main/java/org/xamana/log/LogReaderMySQL.java) reconstruye, por cada línea, una
  etiqueta `remote:<ip>:` (extraída de la línea `Connect\troot@<ip> on`, regex
  `"\\@(.*?)on"`) y una etiqueta `db:<nombre>;` (de la línea `Init DB`), y las
  concatena a cada línea de log antes de pasarlas al filtro — esto calza con lo
  que escribe `GeneradorLogMySQL` (líneas `Connect` e `Init DB`,
  [`GeneradorLogMySQL.java:25,28`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java)). [`FilterMySQL.java:29-93`](src/main/java/org/xamana/filter/FilterMySQL.java) busca
  por substring `select`/`update`/`delete`/`insert`, extrae el predicado con
  `WHERE(.*?);` y el sitio con el mismo `regexSite` de la clase base
  (`remote:(.*?):`, que aquí lo puso el propio `LogReaderMySQL`, no el log
  original). Tampoco hay ningún cálculo de joins.
- **PostgreSQL**: [`LogReaderPostgreSQL.java:28-61`](src/main/java/org/xamana/log/LogReaderPostgreSQL.java) extrae el nombre de
  base de datos de la línea `"conexion autorizada:"` y reemplaza comillas
  dobles por simples. [`FilterPostgreSQL.java:28-92`](src/main/java/org/xamana/filter/FilterPostgreSQL.java) extrae el predicado
  con `WHERE(.*?);` y el sitio con `"-_-(.*?)LOG:"`, que coincide con el
  separador `-_-` que escribe `GeneradorLogPostgreSQL`
  ([`GeneradorLogPostgreSQL.java:23`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java)).

**¿Leen el log real del motor, el de SimulQUERY, o ambos?** El código de
`LogReader*`/`Filter*` no distingue el origen: simplemente espera encontrar,
en el texto subido, ciertas subcadenas y campos concretos (`remote:`,
`-_-...LOG:`, `WHERE`, `q:{`, `filter:{`, etc.). Esos campos están diseñados
para calzar con el formato que **SimulQUERY** produce (que a su vez imita,
pero no replica al 100%, el log nativo real de cada motor — ver la
discrepancia de `ndeleted`/`remove` arriba). No hay en el código ninguna rama
o bandera que diga "si es log real, parsear así; si es de SimulQUERY, parsear
de otra forma": es un único parser por motor, acoplado al formato de
SimulQUERY. Un log nativo real que no incluyera exactamente esas subcadenas
(por ejemplo, un `mongod.log` real sin el prefijo `remote:` con puerto en ese
formato exacto, o con comillas distintas) no necesariamente sería leído
correctamente por este código; esto no se puede confirmar sin una prueba, así
que se deja explícito como algo no determinable solo con lectura de código.

---

## 4. Versiones

### SimulQUERY (`simulador_v4/pom.xml`)
- Java: `17` (`maven.compiler.source/target`).
- Jakarta EE 10 (provisto por contenedor GlassFish 7): `jakarta.servlet-api 6.0.0`,
  `jakarta.faces-api 4.0.1`, `jakarta.enterprise.cdi-api 4.0.1`, `jakarta.inject-api 2.0.1`.
- PrimeFaces: `14.0.18` (clasificador `jakarta`).
- Driver MySQL: `mysql-connector-java 8.0.21`.
- Driver PostgreSQL: `postgresql 42.2.18`.
- Driver MongoDB: `mongodb-driver-sync 4.1.1`.
- `maven-war-plugin 3.4.0`.

### XAMANA (`xamana/pom.xml`)
- Java: `17` (`maven.compiler.release`).
- Jakarta EE: `jakarta.jakartaee-api 10.0.0` (provisto).
- PrimeFaces: `16.0.1`.
- Driver MySQL: `mysql-connector-java 8.0.21`.
- Driver PostgreSQL: `postgresql 42.2.18`.
- Driver/BSON MongoDB: `mongodb-driver-sync 4.1.1`, `bson 4.1.1`.
- `commons-io 2.11.0` (usado por `LogReader*` para leer los archivos subidos).
- `org.json:json 20231013`.
- `maven-war-plugin 3.4.0`.

### FRAGMENT
No es un proyecto Maven (es un proyecto NetBeans Ant/Web clásico). Según
`nbproject/project.properties`: Java `1.8` (`javac.source`/`javac.target`).
Librerías incluidas como `.jar` en el propio repositorio:
`mysql-connector-java-8.0.21.jar`, `primefaces-8.0.jar` (+ fuentes). Solo tiene
acceso a MySQL (`Datos/AccesoDatos.java`); no contiene ningún lector de log —
trabaja leyendo tablas en vivo (`BDJB.insertar()`/`proceso()`,
[`BDJB.java:31-59`](FRAGMENT/src/java/JB/BDJB.java)), por lo que **no corresponde al "observer-fragmenter"
basado en logs** descrito en el resto de este informe; si el observer-fragmenter
es otro proyecto, no se localizó en este equipo y habría que indicar la ruta.

---

## 5. Logs/cargas ya generados encontrados en el equipo

Se buscó en `NetBeansProjects`, `Documents`, `Desktop` y `Downloads` cualquier
archivo de log o carga generada. **No se encontró ningún log de 8,000, 9,000 o
12,000 operaciones** en ninguna de esas rutas. El único archivo de log
relacionado con este formato que se encontró es un log de prueba pequeño, en
un proyecto distinto (`KAIXAR`, no mencionado explícitamente en la petición
pero cuyo log usa el mismo formato de MongoDB):

- Ruta: `C:\Users\cmgel\Documents\NetBeansProjects\KAIXAR\test-data\log 4 mongo articulo.log`
- Tamaño: 11,703 bytes (~11.4 KB)
- Líneas: 15
- Contenido: 15 líneas de `find` sobre `HITO.multimedia_records`, todas con
  `"remote":"127.0.0.1:34248"` (una sola IP de origen, `127.0.0.1`) — es decir,
  0 delete/update/insert, 15/15 find, 1 única IP.

Otros archivos con "log" en el nombre encontrados en `Documents`/
`NetBeansProjects` (`KAIXAR/docs/log_digest_campos.txt`, logs de la app
`Dicoogle` en `Downloads`, scripts de ejemplo de Python con "log_loss" en el
nombre) no son logs de operaciones de SimulQUERY/XAMANA y no se detallan aquí.
Si existen en otra ubicación (otro disco, una nube, un respaldo), no se pudo
determinar desde este equipo; se recomienda indicar la ruta si se recuerda
dónde se guardaron.

---

## 6. verticalDin — el "observer-fragmenter"

`verticalDin` se compila como un `.jar` ejecutable independiente (empaquetado
con `maven-jar-plugin`, clase principal `verticaldin.VerticalDin`,
[`pom.xml:9-10,15,56`](../verticalDin/pom.xml)) y se arranca por línea de comandos con dos
argumentos: `arg[0]` = token (el id de tabla registrado en la base de XAMANA)
y `arg[1]` = directorio de logs del sitio donde corre
([`VerticalDin.java:18-22`](../verticalDin/src/main/java/verticaldin/VerticalDin.java)). Es decir: se despliega **una instancia por
sitio**, apuntando al directorio donde ese sitio escribe su log real, y queda
corriendo en un bucle infinito con `Thread.sleep(5000)`
([`Vigilante.java:159,177,183,199,205,215`](../verticalDin/src/main/java/verticaldin/Vigilante.java)) vigilando ese directorio — de
ahí el nombre "observer"/"Vigilante" (la clase central se llama literalmente
`Vigilante`). Su objetivo es disparar **fragmentación vertical dinámica**
(separar atributos en fragmentos distintos y moverlos de sitio) cuando el
costo acumulado de un fragmento supera un umbral, a diferencia de la
fragmentación horizontal que hace el flujo web de `xamana`.

### a. Arranque y fuente de la configuración

Al arrancar, `Vigilante` se conecta a la base de datos de registro de XAMANA
(vía `AccesoDatosMy`, una conexión MySQL fija a esa base, no a la base de
datos de la aplicación) y hace 3 consultas con `JOIN` reales sobre las tablas
`BD`/`Tabla`/`Fragmento`/`Atributo` de XAMANA para reconstruir, a partir del
`token`, qué base/tabla/sitio le corresponde vigilar, su gestor (`a`=MySQL,
`b`/`c`=Postgres(-XL), cualquier otro valor=Mongo) y sus atributos
([`Vigilante.java:62-150`](../verticalDin/src/main/java/verticaldin/Vigilante.java) — ruta relativa al propio proyecto
`verticalDin/src/main/java/verticaldin/Vigilante.java`, líneas 62-150). Estos
son los únicos `JOIN` reales que aparecen en todo el ecosistema SimulQUERY/XAMANA,
y son consultas de administración contra la base de XAMANA, no operaciones
simuladas ni generadas a partir de un log.

### b. Qué hace con el log: tipo de observación y umbral

Cada 5 segundos ([`Vigilante.java:177,199,215`](../verticalDin/src/main/java/verticaldin/Vigilante.java)), según el gestor:
- Localiza el archivo modificado más recientemente en el directorio de logs
  (`logMySQL`/`logPostgreSQL`/`logMongoDB`, p.ej.
  [`Vigilante.java:271-286`](../verticalDin/src/main/java/verticaldin/Vigilante.java)).
- Lleva un **offset de lectura** (`caracterLog`, en caracteres) para no
  releer lo ya procesado; si el archivo actual es más corto que ese offset
  (rotación de log), reinicia el offset a 0 ([`Vigilante.java:289-291`](../verticalDin/src/main/java/verticaldin/Vigilante.java)).
- Por cada línea nueva, si contiene `select/delete/insert/update` (SQL) o
  `"command"` (Mongo) **y el nombre de la tabla o de alguno de sus fragmentos
  actuales**, calcula un costo por atributo y por sitio y lo acumula en
  `Atributo.costoAtributoxSitio` (mapa IP→costo).
- Cuando el costo acumulado de un fragmento supera
  `desempeño_inicial * (umbralCO/100)` ([`Vigilante.java:318,1000`](../verticalDin/src/main/java/verticaldin/Vigilante.java)), dispara
  una refragmentación: agrupa los atributos por el sitio que más los usó
  (`getMaxEntry`) y llama a `Fragmentador.generarFragmentos()` para mover
  físicamente esos atributos a una tabla/colección nueva en ese sitio.
- Solo en MySQL/PostgreSQL (no en Mongo, por ser NoSQL) también revisa en
  cada ciclo si se agregaron o borraron atributos físicos en la tabla
  (`ALTER TABLE`) comparando contra el catálogo de XAMANA
  (`nuevosAtributosMySQL`/`eliminadoAtributosMySQL` y sus equivalentes
  PostgreSQL, [`Vigilante.java:172,174,550,731`](../verticalDin/src/main/java/verticaldin/Vigilante.java)).

### c. Extracción de tipo de operación, atributos, "número de joins" e IP — por motor

**MySQL** ([`logMySQLextencion`, `Vigilante.java:422-487`](../verticalDin/src/main/java/verticaldin/Vigilante.java)):
- Formato esperado: texto plano tipo `general_log`, con líneas
  `Connect\t<nombre>\troot@<ip> on ...` y líneas con una columna `Query`
  separada por tabulador — el mismo formato que escribe
  `GeneradorLogMySQL` de SimulQUERY ([`GeneradorLogMySQL.java:24-31`](src/main/java/com/simulador/simulacion/GeneradorLogMySQL.java)).
- Tipo de operación: por substring (`select`→desempeño 1, `delete`/`insert`→2,
  `update`→3; [`Vigilante.java:449-454`](../verticalDin/src/main/java/verticaldin/Vigilante.java)).
- IP de origen: se construye un mapa `nombre→ip` a partir de las líneas
  `Connect\t...` (`NombreIp`, [`Vigilante.java:434-441`](../verticalDin/src/main/java/verticaldin/Vigilante.java)), y luego cada línea
  de consulta se asocia a una IP buscando el mismo "nombre" de hilo en ese
  mapa ([`Vigilante.java:456-459`](../verticalDin/src/main/java/verticaldin/Vigilante.java)). Si la IP es `localhost`/`::1` se
  normaliza a `127.0.0.1` ([`Vigilante.java:438-440`](../verticalDin/src/main/java/verticaldin/Vigilante.java)).
- **Número de joins**: `int nJoins = incidencias(actual.toLowerCase(), " join ")`
  ([`Vigilante.java:464`](../verticalDin/src/main/java/verticaldin/Vigilante.java)). `incidencias(...)` ([`Vigilante.java:760-768`](../verticalDin/src/main/java/verticaldin/Vigilante.java))
  cuenta cuántas veces aparece literalmente la subcadena `" join "` en el
  texto de la línea y le suma 1 (empieza en `i=1`); es decir, **sí existe** un
  cálculo explícito de "costo de reuniones" (impreso como `"CR: "+nJoins`),
  pero se basa en buscar literalmente la palabra `join` en el texto de la
  consulta logueada. Como SimulQUERY **nunca** escribe esa palabra (sección
  e), al leer un log generado por SimulQUERY este valor siempre sale en
  `nJoins = 1` (ningún "join" encontrado).
- Atributos: se compara cada atributo conocido de la tabla contra el texto de
  la línea (`actual.toLowerCase().contains(atributo)`) o si la línea contiene
  `" * "` ([`Vigilante.java:466-468`](../verticalDin/src/main/java/verticaldin/Vigilante.java)).

**PostgreSQL/Postgres-XL** ([`logPostgreSQLextencion`, `Vigilante.java:1161-1223`](../verticalDin/src/main/java/verticaldin/Vigilante.java)):
mismo esquema que MySQL pero con el formato `[pid]-_-<ip>LOG:  ...` que
escribe `GeneradorLogPostgreSQL` ([`GeneradorLogPostgreSQL.java:21-23`](src/main/java/com/simulador/simulacion/GeneradorLogPostgreSQL.java)): el
"nombre" de sesión es el PID entre corchetes y la IP se extrae entre `-_-` y
`LOG` ([`Vigilante.java:1173-1174`](../verticalDin/src/main/java/verticaldin/Vigilante.java)). El número de joins usa exactamente el mismo
`incidencias(actual.toLowerCase()," join ")` ([`Vigilante.java:1200`](../verticalDin/src/main/java/verticaldin/Vigilante.java)), con la
misma conclusión: con logs de SimulQUERY, siempre `nJoins = 1`.

**MongoDB** ([`logMongoDBextencion`, `Vigilante.java:1395-1495`](../verticalDin/src/main/java/verticaldin/Vigilante.java)): a diferencia de
SQL, aquí sí se **parsea la línea como JSON de verdad** (`new
JSONObject(actual)`, [`Vigilante.java:1408`](../verticalDin/src/main/java/verticaldin/Vigilante.java)), navegando
`attr.command.find/delete/insert/update`, exactamente la estructura que
escribe `GeneradorLogMongoDB` de SimulQUERY. La IP se toma del campo
`"remote":"ip:puerto"` quitando el puerto
(`remote.substring(0, remote.indexOf(":"))`, [`Vigilante.java:1431-1432`](../verticalDin/src/main/java/verticaldin/Vigilante.java)) —
coherente con el campo `remote` que realmente escribe
[`GeneradorLogMongoDB.java:252`](src/main/java/com/simulador/simulacion/GeneradorLogMongoDB.java). **El "número de joins" para Mongo no
busca `$lookup`** (SimulQUERY no lo genera, sección e): en vez de eso,
calcula cuántos **fragmentos horizontales distintos** de la tabla (entre los
que ya existen en ese momento por fragmentación vertical previa) contienen
alguno de los atributos usados por la operación
(`fResuelvenAtributos`, [`Vigilante.java:1441-1454`](../verticalDin/src/main/java/verticaldin/Vigilante.java)), con un mínimo de 1. Es,
en la práctica, un proxy del costo de tener que "reunir" (join lógico) varios
fragmentos para resolver la consulta, no una detección de un operador de
join/`$lookup` real en la consulta.
- Qué atributos participan (para ese cálculo) se determina parseando también
  como JSON la `projection` (para `find`), los `documents` (para `insert`) o
  el `$set` dentro de `updates` (para `update`) —
  [`buscarAtributos`, `Vigilante.java:1512-1569`](../verticalDin/src/main/java/verticaldin/Vigilante.java) —; para `delete` se asume que
  participan todos los atributos de la tabla.

### d. Versiones (verticalDin)

Según [`pom.xml`](../verticalDin/pom.xml): Java 17 (`maven.compiler.release`), sin
framework web (empaqueta como `jar`, no `war`). Mismas versiones de driver que
`xamana` y `simulador_v4`: `mysql-connector-java 8.0.21`, `postgresql 42.2.18`,
`mongodb-driver-sync 4.1.1` + `bson 4.1.1`, y `org.json:json 20231013` (para el
parseo JSON del log de Mongo). `maven-jar-plugin 3.4.1`.

### e. Relación con lo ya reportado

En resumen, el ecosistema tiene **dos consumidores distintos del log**, no
uno:
1. `xamana` (paquetes `org.xamana.log`/`org.xamana.filter`): un flujo web de
   **una sola pasada** sobre un archivo de log subido manualmente, para
   fragmentación **horizontal**; no calcula joins.
2. `verticalDin` (`Vigilante`): un **proceso en segundo plano por sitio** que
   vigila el directorio de logs en continuo, para fragmentación
   **vertical**; sí calcula un número de joins/"costo de reuniones", por
   conteo literal de `" join "` en SQL o por fragmentos horizontales
   involucrados en Mongo.

Ambos esperan el mismo formato de texto que produce SimulQUERY (y, al igual
que con `xamana`, no hay en el código ninguna distinción entre "log real del
motor" y "log generado por SimulQUERY": es el mismo parser para cualquiera de
los dos, acoplado al formato que SimulQUERY efectivamente escribe).
