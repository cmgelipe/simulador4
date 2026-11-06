package com.simulador.simulacion;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.simulador.datos.AccesoDatosMongoDB;
import com.simulador.modelo.Tabla;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Genera un log simulado de operaciones sobre MongoDB (antes
 * Modelo.BD#generarMongoDB). Se mantiene como un único objeto de larga
 * vida (igual que el BD original) porque el mapa "ocupaciones" acumula
 * estado entre llamadas sucesivas a generar(...).
 */
public class GeneradorLogMongoDB {

    private final HashMap<Integer, Integer> ocupaciones = new HashMap<>();

    /**
     * @param nConsultas total exacto de operaciones a repartir entre los
     * nSitios sitios (antes: cantidad fija POR sitio, generando siempre
     * exactamente el mismo número de operaciones en cada uno).
     */
    public String generar(String dirbd, String nombd, String usubd, String passbd,
            Tabla tabla, int nSitios, int nConsultas, String[] arr, ArrayList<String> evitados) {
        String ret = "";
        AccesoDatosMongoDB accexternomongo = null;
        try {
            accexternomongo = new AccesoDatosMongoDB(dirbd, nombd, tabla.getNombre(), usubd, passbd);
            if (accexternomongo.conectar()) {
                ArrayList<String> atributos = accexternomongo.atributos(nombd, tabla.getNombre());
                System.out.println("Antes: "+atributos.size());
                atributos.removeIf(x->evitados.contains(x.split(":")[0]));
                System.out.println("Despues: "+atributos.size());
                // El conteo y el handle de la colección no cambian durante toda
                // la generación: antes se recalculaban en cada una de las
                // nSitios*nConsultas iteraciones.
                MongoCollection<Document> colec = accexternomongo.getMongoClient().getDatabase(nombd).getCollection(tabla.getNombre());
                double res = (double) colec.countDocuments();
                // Reparto desigual del total de operaciones entre los sitios, en vez
                // de exactamente nConsultas por cada uno: la actividad real no suele
                // distribuirse de forma pareja entre sitios.
                int[] operacionesPorSitio = distribuirOperaciones(nSitios, nConsultas);
                for (int j = 0; j < nSitios; j++) {
                    String ip = ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + ":" + ((int) (Math.random() * 90)) + "" + ((int) (Math.random() * 900));
                    String pid = ""+(int) ((Math.random() * 8001) + 1000);
                    consultaMongo:
                    for (int i = 0; i < operacionesPorSitio[j]; i++) {
                        String projection=atributosProjectionMongoDB(atributos);
                        int numeroA = (int) (Math.random() * atributos.size());
                        String atributoTipo = atributos.get(numeroA);
                        ArrayList<String> c_operador0 = new ArrayList<>();
                        int tipo = TraductorTipoDato.traducir(atributoTipo.split(":")[1]);
                        switch (tipo) {
                            case 0:
                            case 4:
                            case 5:
                                c_operador0.add("$ne");
                                c_operador0.add("$eq");
                                break;
                            case 1:
                            case 2:
                                c_operador0.add("$ne");
                                c_operador0.add("$eq");
                                c_operador0.add("$lt");
                                c_operador0.add("$gt");
                                c_operador0.add("$lte");
                                c_operador0.add("$gte");
                                break;
                            default:
                                System.out.println(atributoTipo.split(":")[1]);
                                break;
                        }
                        System.out.println("Tamaño " + c_operador0.size());
                        ArrayList<String> c_operacion0 = new ArrayList<>();
                        String o = "";
                        for (String t : arr) {
                            o += t;
                        }
                        if (o.contains("a")) {
                            c_operacion0.add("find");
                        }
                        if (o.contains("b")) {
                            c_operacion0.add("update");
                        }
                        if (o.contains("c")) {
                            c_operacion0.add("delete");
                        }
                        if (o.contains("d")) {
                            c_operacion0.add("insert");
                        }
                        String operador0 = c_operador0.get((int) (Math.random() * c_operador0.size()));
                        String operacion0 = c_operacion0.get((int) (Math.random() * c_operacion0.size()));
                        if (res > 0) {
                            String campo = atributoTipo.split(":")[0];
                            Document c_valor = muestraDocumentoConCampo(colec, campo);
                            Document c_valor2 = muestraDocumentoConCampo(colec, campo);
                            if (c_valor == null || c_valor2 == null) {
                                // No hay ningún documento con ese campo poblado:
                                // se descarta esta consulta en vez de bloquear la generación.
                                continue consultaMongo;
                            }
                            String valorString1 = "";
                            String valorString2 = "";
                            String atributoDocInterno1 = null;
                            String atributoDocInterno2 = null;
                            if (atributoTipo.split(":")[1].toUpperCase().compareTo("OBJECTID") == 0) {
                                valorString1 = c_valor.getObjectId(atributoTipo.split(":")[0]).toString();
                                valorString2 = c_valor2.getObjectId(atributoTipo.split(":")[0]).toString();
                            } else if (atributoTipo.split(":")[1].toUpperCase().compareTo("DOUBLE") == 0) {
                                valorString1 = c_valor.getDouble(atributoTipo.split(":")[0]).toString();
                                valorString2 = c_valor2.getDouble(atributoTipo.split(":")[0]).toString();
                            } else if (atributoTipo.split(":")[1].toUpperCase().compareTo("ARRAY") == 0) {
                                String varray = c_valor.get(atributoTipo.split(":")[0]).toString();
                                varray = varray.replace("]", "");
                                varray = varray.replace("[", "");
                                varray = varray.replace("[{", "");
                                varray = varray.replace("}]", "");
                                varray = varray.replace("{", "");
                                varray = varray.replace("}", "");
                                varray = varray.replace("}", "");
                                System.out.println("ARRAY: " + varray);
                                if (varray.contains(":")) {
                                    int nValoresInternos = 0;
                                    for (int u = 0; u < varray.length(); u++) {
                                        if (varray.charAt(u) == ':') {
                                            nValoresInternos++;
                                        }
                                    }
                                    nValoresInternos++;
                                    String[] arraydearray = varray.split(",");
                                    String linea = arraydearray[(int) (Math.random() * nValoresInternos)];
                                    System.out.println(linea);
                                    String[] atributovalor = linea.split(":");
                                    atributoDocInterno1 = atributovalor[0];
                                    valorString1 = atributovalor[1];
                                    linea = arraydearray[(int) (Math.random() * nValoresInternos)];
                                    System.out.println(linea);
                                    atributovalor = linea.split(":");
                                    atributoDocInterno2 = atributovalor[0];
                                    valorString2 = atributovalor[1];
                                } else {
                                    varray = varray.replace("[", "");
                                    varray = varray.replace("]", "");
                                    String[] valores = varray.split(",");
                                    valorString1 = valores[(int) Math.random() * valores.length];
                                    valorString2 = valores[(int) Math.random() * valores.length];
                                }
                            } else {
                                valorString1 = extraerValorComoString(c_valor, atributoTipo.split(":")[0]);
                                valorString2 = extraerValorComoString(c_valor2, atributoTipo.split(":")[0]);
                            }
                            if (operacion0.compareTo("insert") == 0 && tipo != 5) {
                                // El valor insertado se genera de forma sintética para
                                // garantizar que no coincide con ningún valor ya existente
                                // en la colección real (en particular, si el atributo
                                // elegido fuera el propio _id, no se duplicaría el de un
                                // documento real: new ObjectId() es prácticamente único).
                                valorString1 = generarValorNuevoSintetico(tipo);
                            } else if (operacion0.compareTo("update") == 0 && tipo != 5) {
                                // El nuevo valor de un update se genera de forma sintética
                                // (igual que en los generadores de MySQL/PostgreSQL) en vez
                                // de tomarlo de un segundo documento real muestreado al azar;
                                // para arrays (tipo 5) se conserva la selección estructural
                                // existente, ya que ahí "el nuevo valor" es otro elemento
                                // del propio array, no un valor de otro documento.
                                valorString2 = generarValorNuevoSintetico(tipo);
                            }
                            if (operacion0.compareTo("find") == 0) {
                                ret += construirLogFind(pid, ip, j + 1, nombd, tabla.getNombre(), campo,
                                        atributoDocInterno1, operador0, valorString1, projection, tipo);
                            } else if (operacion0.compareTo("update") == 0) {
                                ret += construirLogUpdate(pid, ip, j + 1, nombd, tabla.getNombre(), campo,
                                        atributoDocInterno1, atributoDocInterno2, operador0, valorString1, valorString2, tipo);
                            } else if (operacion0.compareTo("delete") == 0) {
                                ret += construirLogDelete(pid, ip, j + 1, nombd, tabla.getNombre(), campo,
                                        atributoDocInterno1, operador0, valorString1, tipo);
                            } else if (operacion0.compareTo("insert") == 0) {
                                ret += construirLogInsert(pid, ip, j + 1, nombd, tabla.getNombre(), campo, valorString1, tipo);
                            }

                        }
                    }
                }
                for(Integer i:ocupaciones.keySet()){
                    System.out.println(i+": "+ocupaciones.get(i));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (accexternomongo != null) {
                try {
                    accexternomongo.desconectar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    private String atributosProjectionMongoDB(ArrayList<String> arr) {
        String ret="";

        int nAtributos=(int) (Math.random() * arr.size())+1;
        Collections.shuffle(arr);
        System.out.println("nAtributos: "+nAtributos);
        for(int i=0;i<nAtributos;i++){
            ret += "\""+arr.get(i).split(":")[0]+"\":1,";
            if(ocupaciones.get(i)==null){
                ocupaciones.put(i, 1);
            }else{
                ocupaciones.put(i, ocupaciones.get(i)+1);
            }

        }
        if(ret.charAt(ret.length()-1)==','){
            ret=ret.substring(0, ret.length()-1);
        }
        return ret;
    }

    /**
     * Construye la línea de log de una operación "find" (antes: dos bloques
     * casi idénticos duplicados dentro de generar(), uno para cuando el
     * valor venía de un subdocumento de un array y otro para cuando no).
     */
    private String construirLogFind(String pid, String ip, int conexion, String nombd, String tabla,
            String campo, String atributoDocInterno, String operador0, String valorString1, String projection, int tipo) {
        String campoFiltro = atributoDocInterno != null ? campo + "." + atributoDocInterno : campo;
        String valorFiltro = formatoValorJson(valorString1, tipo);
        int numYields = GeneradorLogUtil.enteroEntre(0, 2);
        int reslen = GeneradorLogUtil.enteroEntre(800, 5000);
        int durationMillis = GeneradorLogUtil.enteroEntre(5, 120);
        return "{\"t\":{\"$date\":\""+ GeneradorLogUtil.fecha().trim() +"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                +", \"ctx\":\"conn"+conexion+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                +nombd + "." + tabla+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"find\":\""
                +tabla+"\",\"filter\":{\"" + campoFiltro + "\": {" + operador0 + ":" + valorFiltro
                + "} },\"projection\":{"+projection+"},\"lsid\":{\"id\":{\"$uuid\":\""+GeneradorLogUtil.nuevoUuid()+"\"}},\"$db\":\""
                +nombd+"\"},\"planSummary\":\"COLLSCAN\",\"keysExamined\":0,\"docsExamined\":77,\"cursorExhausted\":true,\"numYields\":"+numYields+",\"nreturned\":"
                + "1870,\"queryHash\":\"4C06B3EF\",\"planCacheKey\":\"4FDA4125\",\"reslen\":"+reslen+",\"locks\":{\"Global\":{\"acquireCount\":{\"r\":1}},\"Mutex\":"
                + "{\"acquireCount\":{\"r\":1}}},\"storage\":{\"data\":{\"bytesRead\":27422,\"timeReadingMicros\":105}},\"remote\":"
                + "\""+ip+"\",\"protocol\":\"op_msg\",\"durationMillis\":"+durationMillis+"}}\n";
    }

    /**
     * Construye la línea de log de una operación "update" (antes: dos
     * bloques casi idénticos duplicados, con una inconsistencia de
     * formato entre ellos -espacios distintos tras "s":"I"- que aquí se
     * unifica al formato usado por los demás bloques de log).
     */
    private String construirLogUpdate(String pid, String ip, int conexion, String nombd, String tabla,
            String campo, String atributoDocInterno1, String atributoDocInterno2, String operador0,
            String valorString1, String valorString2, int tipo) {
        String campoFiltro = atributoDocInterno1 != null ? campo + "." + atributoDocInterno1 : campo;
        String campoSet = atributoDocInterno2 != null ? campo + "." + atributoDocInterno2 : campo;
        String valorFiltro = formatoValorJson(valorString1, tipo);
        String valorNuevo = formatoValorJson(valorString2, tipo);
        int numYields = GeneradorLogUtil.enteroEntre(0, 3);
        int reslen = GeneradorLogUtil.enteroEntre(40, 150);
        int durationMillis = GeneradorLogUtil.enteroEntre(5, 60);
        return "{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
              +",\"ctx\":\"conn"+conexion+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
              +nombd + "." + tabla+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"update\":\""
              +tabla+"\",\"updates\":[{\"q\":{\""+campoFiltro
              +"\":{"+operador0+":"+valorFiltro+"}},\"u\":{\"$set\":{\""+ campoSet +"\":"
              +valorNuevo+"}},\"multi\":true}],\"nModified\":1,\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\""+GeneradorLogUtil.nuevoUuid()+"\"}},"
              + "\"$db\":\""+nombd+"\"},\"numYields\":"+numYields+",\"reslen\":"+reslen+",\"locks\":{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":2}},"
              + "\"ReplicationStateTransition\":{\"acquireCount\":{\"w\":3}},\"Global\":{\"acquireCount\":{\"r\":1,\"w\":2}},\"Database\":"
              + "{\"acquireCount\":{\"w\":2}},\"Collection\":{\"acquireCount\":{\"w\":2}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
              + "{\"acquireCount\":2,\"timeAcquiringMicros\":6},\"storage\":{},\"remote\":\""+ip+"\",\"protocol\":\"op_msg\","
              + "\"durationMillis\":"+durationMillis+"}}\n";
    }

    /**
     * Construye la línea de log de una operación "delete" (antes: dos
     * bloques casi idénticos duplicados).
     */
    private String construirLogDelete(String pid, String ip, int conexion, String nombd, String tabla,
            String campo, String atributoDocInterno, String operador0, String valorString1, int tipo) {
        String campoFiltro = atributoDocInterno != null ? campo + "." + atributoDocInterno : campo;
        String valorFiltro = formatoValorJson(valorString1, tipo);
        int numYields = GeneradorLogUtil.enteroEntre(0, 4);
        int reslen = GeneradorLogUtil.enteroEntre(30, 100);
        int durationMillis = GeneradorLogUtil.enteroEntre(20, 400);
        return "{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                +",\"ctx\":\"conn"+conexion+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                +nombd + "." + tabla+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"delete\":\""
                +tabla+"\",\"deletes\":[{\"q\":{\""+campoFiltro
                +"\":{"+ operador0 +":"+valorFiltro+"}},\"limit\":0}],\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\""+GeneradorLogUtil.nuevoUuid()+"\"}},"
                + "\"$db\":\""+nombd+"\"},\"numYields\":"+numYields+",\"reslen\":"+reslen+",\"locks\":{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":3}},"
                + "\"ReplicationStateTransition\":{\"acquireCount\":{\"w\":4}},\"Global\":{\"acquireCount\":{\"r\":1,\"w\":3}},\"Database\":"
                + "{\"acquireCount\":{\"w\":3}},\"Collection\":{\"acquireCount\":{\"w\":3}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                + "{\"acquireCount\":3,\"timeAcquiringMicros\":13},\"storage\":{},\"remote\":\""+ip+"\",\"protocol\":\"op_msg\","
                + "\"durationMillis\":"+durationMillis+"}}\n";
    }

    /**
     * Construye la línea de log de una operación "insert".
     */
    private String construirLogInsert(String pid, String ip, int conexion, String nombd, String tabla,
            String campo, String valorString1, int tipo) {
        String valorDocumento = formatoValorJson(valorString1, tipo);
        int numYields = GeneradorLogUtil.enteroEntre(0, 2);
        int reslen = GeneradorLogUtil.enteroEntre(30, 100);
        int durationMillis = GeneradorLogUtil.enteroEntre(10, 250);
        return "{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                +",\"ctx\":\"conn"+conexion+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                +nombd+"."+tabla+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"insert\":\""
                +tabla+"\",\"documents\":[{\""+campo+"\":"+valorDocumento+"}],"
                + "\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\""+GeneradorLogUtil.nuevoUuid()+"\"}},\"$db\":\""
                +nombd+"\"},\"ninserted\":1,\"keysInserted\":1,\"numYields\":"+numYields+",\"reslen\":"+reslen+",\"locks\":"
                + "{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":1}},\"ReplicationStateTransition\":"
                + "{\"acquireCount\":{\"w\":1}},\"Global\":{\"acquireCount\":{\"w\":1}},\"Database\":{\"acquireCount\":"
                + "{\"w\":1}},\"Collection\":{\"acquireCount\":{\"w\":1}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                + "{\"acquireCount\":1,\"timeAcquiringMicros\":6},\"storage\":{\"data\":{\"bytesRead\":1114,\"timeReadingMicros\":34}},"
                + "\"remote\":\""+ip+"\",\"protocol\":\"op_msg\",\"durationMillis\":"+durationMillis+"}}\n";
    }

    /**
     * Reparte el total de operaciones entre los sitios de forma desigual,
     * usando cortes aleatorios sobre [0, total] (técnica "stars and bars"):
     * se eligen nSitios-1 puntos de corte, se ordenan, y la distancia entre
     * cortes consecutivos es el tamaño de cada sitio. Antes cada sitio
     * recibía siempre exactamente nConsultas operaciones.
     */
    private int[] distribuirOperaciones(int nSitios, int totalOperaciones) {
        int[] resultado = new int[Math.max(nSitios, 0)];
        if (nSitios <= 0 || totalOperaciones <= 0) {
            return resultado;
        }
        if (nSitios == 1) {
            resultado[0] = totalOperaciones;
            return resultado;
        }
        int[] cortes = new int[nSitios - 1];
        for (int k = 0; k < cortes.length; k++) {
            cortes[k] = (int) (Math.random() * (totalOperaciones + 1));
        }
        Arrays.sort(cortes);
        int anterior = 0;
        for (int j = 0; j < nSitios; j++) {
            int actual = (j < cortes.length) ? cortes[j] : totalOperaciones;
            resultado[j] = actual - anterior;
            anterior = actual;
        }
        return resultado;
    }

    /**
     * Genera un valor sintético para el "nuevo valor" de un update, en vez
     * de tomarlo de un segundo documento real muestreado al azar (igual
     * criterio que usan los generadores de MySQL/PostgreSQL con
     * generaCadena()/generaNumero()).
     */
    private String generarValorNuevoSintetico(int tipo) {
        switch (tipo) {
            case 1: // numérico (int/long/double/decimal)
                return GeneradorLogUtil.generaNumero();
            case 2: // fecha
                return GeneradorLogUtil.fecha().trim();
            case 4: // ObjectId
                return new ObjectId().toString();
            default: // string, bool, null, object
                return GeneradorLogUtil.generaCadena();
        }
    }

    /**
     * Da formato JSON a un valor según su categoría de tipo: los
     * numéricos van sin comillas (antes: todo se envolvía en comillas sin
     * importar el tipo, lo que generaba filtros/valores incorrectos como
     * {"edad": {"$gt": "30"}} en vez de {"edad": {"$gt": 30}}).
     */
    private String formatoValorJson(String valor, int tipo) {
        boolean esNumerico = tipo == 1;
        return esNumerico ? valor : "\"" + valor + "\"";
    }

    /**
     * Devuelve un documento al azar que tenga el campo indicado poblado
     * (antes: se pedía un documento al azar con skip/limit -costoso en
     * Mongo- y se reintentaba del lado del cliente hasta encontrar uno con
     * el campo no nulo, sin límite de intentos). Al filtrar directamente en
     * el servidor con $match + $sample ya no hace falta el reintento.
     */
    private Document muestraDocumentoConCampo(MongoCollection<Document> colec, String campo) {
        Bson filtroPoblado = Filters.and(Filters.exists(campo), Filters.ne(campo, null));
        List<Document> muestra = colec.aggregate(Arrays.asList(
                Aggregates.match(filtroPoblado),
                Aggregates.sample(1)
        )).into(new ArrayList<>());
        return muestra.isEmpty() ? null : muestra.get(0);
    }

    /**
     * Obtiene el valor de un campo de un documento como String, sin
     * depender de capturar ClassCastException para distinguir ObjectId de
     * String (antes: try/catch sobre c_valor.getString(...) inspeccionando
     * el mensaje de la excepción).
     */
    private String extraerValorComoString(Document doc, String campo) {
        Object valor = doc.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }
}
