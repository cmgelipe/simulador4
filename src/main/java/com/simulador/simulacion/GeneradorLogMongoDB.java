package com.simulador.simulacion;

import com.mongodb.client.MongoCollection;
import com.simulador.datos.AccesoDatosMongoDB;
import com.simulador.modelo.Tabla;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.bson.Document;

/**
 * Genera un log simulado de operaciones sobre MongoDB (antes
 * Modelo.BD#generarMongoDB). Se mantiene como un único objeto de larga
 * vida (igual que el BD original) porque el mapa "ocupaciones" acumula
 * estado entre llamadas sucesivas a generar(...).
 */
public class GeneradorLogMongoDB {

    private final HashMap<Integer, Integer> ocupaciones = new HashMap<>();

    public String generar(String dirbd, String nombd, String usubd, String passbd,
            Tabla tabla, int nSitios, int nConsultas, String[] arr, ArrayList<String> evitados) {
        String ret = "";
        try {
            AccesoDatosMongoDB accexternomongo = new AccesoDatosMongoDB(dirbd, nombd, tabla.getNombre(), usubd, passbd);
            if (accexternomongo.conectar()) {
                ArrayList<String> atributos = accexternomongo.atributos(nombd, tabla.getNombre());
                System.out.println("Antes: "+atributos.size());
                atributos.removeIf(x->evitados.contains(x.split(":")[0]));
                System.out.println("Despues: "+atributos.size());
                for (int j = 0; j < nSitios; j++) {
                    String ip = ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + ":" + ((int) (Math.random() * 90)) + "" + ((int) (Math.random() * 900));
                    String pid = ""+(int) ((Math.random() * 8001) + 1000);
                    for (int i = 0; i < (nConsultas); i++) {
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
                        MongoCollection<Document> colec = accexternomongo.getMongoClient().getDatabase(nombd).getCollection(tabla.getNombre());
                        double res = (double) colec.countDocuments();
                        if (res > 0) {
                            int ale = 0;
                            ale = (int) (Math.random() * res);
                            Document c_valor = colec.find().skip(ale).limit(1).first();
                            ale = (int) (Math.random() * res);
                            Document c_valor2 = colec.find().skip(ale).limit(1).first();
                            String valorString1 = "";
                            String valorString2 = "";
                            String atributoDocInterno1 = null;
                            String atributoDocInterno2 = null;
                            while (c_valor.get(atributoTipo.split(":")[0]) == null) {
                                ale = (int) (Math.random() * res);
                                c_valor = colec.find().skip(ale).limit(1).first();
                            }
                            while (c_valor2.get(atributoTipo.split(":")[0]) == null) {
                                ale = (int) (Math.random() * res);
                                c_valor2 = colec.find().skip(ale).limit(1).first();
                            }
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
                                try{
                                    valorString1 = c_valor.getString(atributoTipo.split(":")[0]);
                                }catch(java.lang.ClassCastException e){
                                    String mensaje=e.getMessage();
                                    if(mensaje!=null){
                                        if(mensaje.contains("org.bson.types.ObjectId cannot be cast to java.lang.String")){
                                            valorString1 = c_valor.getObjectId(atributoTipo.split(":")[0]).toString();
                                        }
                                    }else{
                                        e.printStackTrace();
                                    }
                                }
                                try{
                                    valorString2 = c_valor2.getString(atributoTipo.split(":")[0]);
                                }catch(java.lang.ClassCastException e){
                                    String mensaje=e.getMessage();
                                    if(mensaje!=null){
                                        if(mensaje.contains("org.bson.types.ObjectId cannot be cast to java.lang.String")){
                                            valorString2 = c_valor2.getObjectId(atributoTipo.split(":")[0]).toString();
                                        }
                                    }else{
                                        e.printStackTrace();
                                    }

                                }
                            }
                            if (operacion0.compareTo("find") == 0) {
                                if (atributoDocInterno1 != null) {
                                    ret += "{\"t\":{\"$date\":\""+ GeneradorLogUtil.fecha().trim() +"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                                            +", \"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                            +nombd + "." + tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"find\":\""
                                            +tabla.getNombre()+"\",\"filter\":{\"" + atributoTipo.split(":")[0] + "." + atributoDocInterno1 + "\": {" + operador0 + ":\"" + valorString1
                                            + "\"} },\"projection\":{"+projection+"},\"lsid\":{\"id\":{\"$uuid\":\"60e637fc-f267-4840-993c-a3be0d6851ea\"}},\"$db\":\""
                                            +nombd+"\"},\"planSummary\":\"COLLSCAN\",\"keysExamined\":0,\"docsExamined\":77,\"cursorExhausted\":true,\"numYields\":0,\"nreturned\":"
                                            + "1870,\"queryHash\":\"4C06B3EF\",\"planCacheKey\":\"4FDA4125\",\"reslen\":2882,\"locks\":{\"Global\":{\"acquireCount\":{\"r\":1}},\"Mutex\":"
                                            + "{\"acquireCount\":{\"r\":1}}},\"storage\":{\"data\":{\"bytesRead\":27422,\"timeReadingMicros\":105}},\"remote\":"
                                            + "\""+ip+"\",\"protocol\":\"op_msg\",\"durationMillis\":36}}\n";
                                    atributoDocInterno1 = null;
                                } else {
                                    ret += "{\"t\":{\"$date\":\""+ GeneradorLogUtil.fecha().trim() +"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                                            +", \"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                            +nombd + "." + tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"find\":\""
                                            +tabla.getNombre()+"\",\"filter\":{\"" + atributoTipo.split(":")[0] + "\": {" + operador0 + ":\"" + valorString1
                                            + "\"} },\"projection\":{"+projection+"},\"lsid\":{\"id\":{\"$uuid\":\"60e637fc-f267-4840-993c-a3be0d6851ea\"}},\"$db\":\""
                                            +nombd+"\"},\"planSummary\":\"COLLSCAN\",\"keysExamined\":0,\"docsExamined\":77,\"cursorExhausted\":true,\"numYields\":0,\"nreturned\":"
                                            + "1870,\"queryHash\":\"4C06B3EF\",\"planCacheKey\":\"4FDA4125\",\"reslen\":2882,\"locks\":{\"Global\":{\"acquireCount\":{\"r\":1}},\"Mutex\":"
                                            + "{\"acquireCount\":{\"r\":1}}},\"storage\":{\"data\":{\"bytesRead\":27422,\"timeReadingMicros\":105}},\"remote\":"
                                            + "\""+ip+"\",\"protocol\":\"op_msg\",\"durationMillis\":36}}\n";
                                }

                            } else if (operacion0.compareTo("update") == 0) {
                                if (atributoDocInterno1 != null && atributoDocInterno2 != null) {
                                    ret+="{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",\"c\":\"COMMAND\",\"id\":"+pid
                                          +",\"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                          +nombd + "." + tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"update\":\""
                                          +tabla.getNombre()+"\",\"updates\":[{\"q\":{\""+atributoTipo.split(":")[0]+"." + atributoDocInterno1
                                          +"\":{"+operador0+":\""+valorString1+"\"}},\"u\":{\"$set\":{\""+ atributoTipo.split(":")[0] + "." + atributoDocInterno2 +"\":\""
                                          +valorString2+"\"}},\"multi\":true}],\"nModified\":1,\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\"9e8b0ba3-b575-4d29-bc16-52ab6851daab\"}},"
                                          + "\"$db\":\""+nombd+"\"},\"numYields\":1,\"reslen\":60,\"locks\":{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":2}},"
                                          + "\"ReplicationStateTransition\":{\"acquireCount\":{\"w\":3}},\"Global\":{\"acquireCount\":{\"r\":1,\"w\":2}},\"Database\":"
                                          + "{\"acquireCount\":{\"w\":2}},\"Collection\":{\"acquireCount\":{\"w\":2}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                                          + "{\"acquireCount\":2,\"timeAcquiringMicros\":6},\"storage\":{},\"remote\":\""+ip+"\",\"protocol\":\"op_msg\","
                                          + "\"durationMillis\":28}}\n";
                                } else if (atributoDocInterno1 == null && atributoDocInterno2 == null) {
                                    ret+="{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                                          +",\"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                          +nombd + "." + tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"update\":\""
                                          +tabla.getNombre()+"\",\"updates\":[{\"q\":{\""+atributoTipo.split(":")[0]
                                          +"\":{"+operador0+":\""+valorString1+"\"}},\"u\":{\"$set\":{\""+ atributoTipo.split(":")[0] +"\":\""
                                          +valorString2+"\"}},\"multi\":true}],\"nModified\":1,\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\"9e8b0ba3-b575-4d29-bc16-52ab6851daab\"}},"
                                          + "\"$db\":\""+nombd+"\"},\"numYields\":1,\"reslen\":60,\"locks\":{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":2}},"
                                          + "\"ReplicationStateTransition\":{\"acquireCount\":{\"w\":3}},\"Global\":{\"acquireCount\":{\"r\":1,\"w\":2}},\"Database\":"
                                          + "{\"acquireCount\":{\"w\":2}},\"Collection\":{\"acquireCount\":{\"w\":2}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                                          + "{\"acquireCount\":2,\"timeAcquiringMicros\":6},\"storage\":{},\"remote\":\""+ip+"\",\"protocol\":\"op_msg\","
                                          + "\"durationMillis\":28}}\n";
                                }

                            } else if (operacion0.compareTo("delete") == 0) {
                                if (atributoDocInterno1 != null) {
                                    ret += "{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                                            +",\"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                            +nombd + "." + tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"delete\":\""
                                            +tabla.getNombre()+"\",\"deletes\":[{\"q\":{\""+atributoTipo.split(":")[0] + "." + atributoDocInterno1
                                            +"\":{"+ operador0 +":\""+valorString1+"\"}},\"limit\":0}],\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\"9e8b0ba3-b575-4d29-bc16-52ab6851daab\"}},"
                                            + "\"$db\":\""+nombd+"\"},\"numYields\":2,\"reslen\":45,\"locks\":{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":3}},"
                                            + "\"ReplicationStateTransition\":{\"acquireCount\":{\"w\":4}},\"Global\":{\"acquireCount\":{\"r\":1,\"w\":3}},\"Database\":"
                                            + "{\"acquireCount\":{\"w\":3}},\"Collection\":{\"acquireCount\":{\"w\":3}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                                            + "{\"acquireCount\":3,\"timeAcquiringMicros\":13},\"storage\":{},\"remote\":\""+ip+"\",\"protocol\":\"op_msg\","
                                            + "\"durationMillis\":306}}\n";
                                } else {
                                    ret += "{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                                            +",\"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                            +nombd + "." + tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"delete\":\""
                                            +tabla.getNombre()+"\",\"deletes\":[{\"q\":{\""+atributoTipo.split(":")[0]
                                            +"\":{"+ operador0 +":\""+valorString1+"\"}},\"limit\":0}],\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\"9e8b0ba3-b575-4d29-bc16-52ab6851daab\"}},"
                                            + "\"$db\":\""+nombd+"\"},\"numYields\":2,\"reslen\":45,\"locks\":{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":3}},"
                                            + "\"ReplicationStateTransition\":{\"acquireCount\":{\"w\":4}},\"Global\":{\"acquireCount\":{\"r\":1,\"w\":3}},\"Database\":"
                                            + "{\"acquireCount\":{\"w\":3}},\"Collection\":{\"acquireCount\":{\"w\":3}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                                            + "{\"acquireCount\":3,\"timeAcquiringMicros\":13},\"storage\":{},\"remote\":\""+ip+"\",\"protocol\":\"op_msg\","
                                            + "\"durationMillis\":306}}\n";
                                }
                            } else if (operacion0.compareTo("insert") == 0) {
                                ret += "{\"t\":{\"$date\":\""+GeneradorLogUtil.fecha().trim()+"\"},\"s\":\"I\",  \"c\":\"COMMAND\",  \"id\":"+pid
                                        +",\"ctx\":\"conn"+(j+1)+"\",\"msg\":\"Slow query\",\"attr\":{\"type\":\"command\",\"ns\":\""
                                        +nombd+"."+tabla.getNombre()+"\",\"appName\":\"mongosh 1.1.9\",\"command\":{\"insert\":\""
                                        +tabla.getNombre()+"\",\"documents\":[{\""+atributoTipo.split(":")[0]+"\":\""+valorString1+"\"}],"
                                        + "\"ordered\":true,\"lsid\":{\"id\":{\"$uuid\":\"9e8b0ba3-b575-4d29-bc16-52ab6851daab\"}},\"$db\":\""
                                        +nombd+"\"},\"ninserted\":1,\"keysInserted\":1,\"numYields\":0,\"reslen\":45,\"locks\":"
                                        + "{\"ParallelBatchWriterMode\":{\"acquireCount\":{\"r\":1}},\"ReplicationStateTransition\":"
                                        + "{\"acquireCount\":{\"w\":1}},\"Global\":{\"acquireCount\":{\"w\":1}},\"Database\":{\"acquireCount\":"
                                        + "{\"w\":1}},\"Collection\":{\"acquireCount\":{\"w\":1}},\"Mutex\":{\"acquireCount\":{\"r\":1}}},\"flowControl\":"
                                        + "{\"acquireCount\":1,\"timeAcquiringMicros\":6},\"storage\":{\"data\":{\"bytesRead\":1114,\"timeReadingMicros\":34}},"
                                        + "\"remote\":\""+ip+"\",\"protocol\":\"op_msg\",\"durationMillis\":205}}\n";
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
}
