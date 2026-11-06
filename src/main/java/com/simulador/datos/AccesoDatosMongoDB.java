package com.simulador.datos;

import java.io.Serializable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

/**
 * Acceso a datos para MongoDB (antes Datos.AccesoDatosMG).
 *
 * @author pipe
 */
public class AccesoDatosMongoDB implements Serializable {

    private static String sUrl = null;  // sUrl de conexión
    private static String sUsr = null; // Usuario
    private static String sPwd = null;  // Contraseña
    private static String tabla = null;
    private MongoClient mongoClient;  // La conexión

    public AccesoDatosMongoDB(String url, String nombd, String tabla, String usr, String pwd) throws Exception {
        sUrl = url;//"jdbc:mysql://localhost/Master";
        sUsr = usr;//"root";
        sPwd = pwd;//"sunny_4teamo";
        this.setTabla(tabla);
    }

    public boolean conectar() throws Exception {
        String mongoClientURI = "";
        if (sUsr.isEmpty()) {
            mongoClientURI = "mongodb://" + sUrl + "/";
        } else {
            mongoClientURI = "mongodb://" + sUsr + ":" + sPwd + "@" + sUrl + "/";
        }
        setMongoClient(MongoClients.create(mongoClientURI));

        return getMongoClient() != null;
    }

    public void desconectar() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    /**
     * @return the tabla
     */
    public String getTabla() {
        return tabla;
    }

    /**
     * @param tabla the tabla to set
     */
    public void setTabla(String tabla) {
        this.tabla = tabla;
    }

    private static final int TAMANIO_MUESTRA_ESQUEMA = 20;

    /**
     * Infiere el esquema (campo:tipo) de la colección en una sola pasada.
     * Antes: se tomaba un único documento de muestra y se hacía, por cada
     * campo encontrado, una agregación $type aparte contra la base del
     * usuario (1 + N consultas). Ahora se muestrean varios documentos con
     * $sample y el tipo se determina del valor ya traído por el driver, sin
     * round trips adicionales.
     */
    public synchronized ArrayList<String> atributos(String nombreBase, String nombre) {
        MongoDatabase db = this.getMongoClient().getDatabase(nombreBase);
        MongoCollection<Document> collection = db.getCollection(nombre);
        List<Document> muestra = collection.aggregate(
                Arrays.asList(Aggregates.sample(TAMANIO_MUESTRA_ESQUEMA)))
                .into(new ArrayList<>());

        LinkedHashMap<String, String> tipos = new LinkedHashMap<>();
        for (Document doc : muestra) {
            for (String campo : doc.keySet()) {
                if (tipos.containsKey(campo)) {
                    continue;
                }
                Object valor = doc.get(campo);
                if (valor != null) {
                    tipos.put(campo, tipoBson(valor));
                }
            }
        }

        ArrayList<String> res = new ArrayList<>();
        for (Map.Entry<String, String> entrada : tipos.entrySet()) {
            System.out.println(entrada.getKey() + ":" + entrada.getValue());
            res.add(entrada.getKey() + ":" + entrada.getValue());
        }
        return res;
    }

    /**
     * Traduce el valor ya materializado por el driver al mismo nombre de
     * tipo que antes devolvía el operador $type de MongoDB.
     */
    private String tipoBson(Object valor) {
        if (valor instanceof String) {
            return "string";
        } else if (valor instanceof Double) {
            return "double";
        } else if (valor instanceof Integer) {
            return "int";
        } else if (valor instanceof Long) {
            return "long";
        } else if (valor instanceof Decimal128) {
            return "decimal";
        } else if (valor instanceof Boolean) {
            return "bool";
        } else if (valor instanceof ObjectId) {
            return "objectId";
        } else if (valor instanceof java.util.Date) {
            return "date";
        } else if (valor instanceof List) {
            return "array";
        } else if (valor instanceof Document) {
            return "object";
        }
        return "string";
    }

    public synchronized ArrayList<String> tablas(String nombreBase) {
        ArrayList<String> res=new ArrayList<>();
        MongoDatabase db = this.getMongoClient().getDatabase(nombreBase);
        db.listCollectionNames().forEach(x->{res.add(x);});
        return res;
    }

    /**
     * @return the mongoClient
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * @param mongoClient the mongoClient to set
     */
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
}
