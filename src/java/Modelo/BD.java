package Modelo;

import Datos.AccesoDatos;
import Datos.AccesoDatosMG;
import Datos.AccesoDatosPG;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import org.bson.Document;

/**
 *
 * @author felipe
 */
public class BD implements Serializable {

    AccesoDatos acc = null;
    private int nSitios = 2;
    private int nConsultas = 3;
    private String cadena;
    private String TipoBD;
    AccesoDatos accexternomysql;
    AccesoDatosMG accexternomongo;
    AccesoDatosPG accexternopost;
    private String dirbd;
    private String nombd;
    private String usubd = "";
    private String passbd = "";
    private List<SelectItem> tablas;
    private Tabla tabla = new Tabla();

    public boolean conexionexterna() {
        boolean res = false;

        if (TipoBD.compareTo("a") == 0) {
            try {
                accexternomysql = new AccesoDatos(dirbd, nombd, usubd, passbd);

                if (accexternomysql.conectar()) {
                    res = true;
                    accexternomysql.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
            try {
                accexternopost = new AccesoDatosPG(dirbd, nombd, usubd, passbd);
                if (accexternopost.conectar()) {
                    res = true;
                    accexternopost.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("d") == 0) {
            try {
                accexternomongo = new AccesoDatosMG(dirbd, nombd, this.getTabla().getNombre(), usubd, passbd);
                if (accexternomongo.conectar()) {
                    res = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return res;
    }

    public void generar() {
        if (this.TipoBD.compareTo("a") == 0) {
            cadena = generarMYSQL();
        } else if (this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
            cadena = generarPostgreSQL();
        } else if (this.TipoBD.compareTo("d") == 0) {
            cadena = generarMongoDB();
        }
    }

    public int transTipoDato(String tipo) {
        tipo = tipo.toUpperCase();
        switch (tipo) {
            case "CHAR":
            case "VARCHAR":
            case "BIT":
            case "TINYINT":
            case "STRING":
                return 0;
            case "INTEGER":
            case "SMALLINT":
            case "BIGINT":
            case "NUMERIC":
            case "DECIMAL":
            case "FLOAT":
            case "DOUBLE":
                return 1;
            case "DATE":
            case "TIME":
            case "TIMESTAMP":
            case "DATETIME":
                return 2;
            case "OBJECTID":
                return 4;
            case "ARRAY":
                return 5;
            default:
                return 3;
        }
    }

    /**
     * @return the nSitios
     */
    public int getNSitios() {
        return nSitios;
    }

    /**
     * @param nSitios the nSitios to set
     */
    public void setNSitios(int nSitios) {
        this.nSitios = nSitios;
    }

    /**
     * @return the nConsultas
     */
    public int getNConsultas() {
        return nConsultas;
    }

    /**
     * @param nConsultas the nConsultas to set
     */
    public void setNConsultas(int nConsultas) {
        this.nConsultas = nConsultas;
    }

    String fecha() {
        java.util.Date d = new java.util.Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dt2 = new SimpleDateFormat("HH:mm:ss.SS");
        String ceros = "000000000000000000000000000";
        String x = dt.format(d) + "T" + dt2.format(d);
        ceros = ceros.substring(0, 27 - x.length() - 1);
        return x + ceros + "Z\t";
    }

    /**
     * @return the cadena
     */
    public String getCadena() {
        return cadena;
    }

    /**
     * @param cadena the cadena to set
     */
    public void setCadena(String cadena) {
        this.cadena = cadena;
    }

    /**
     * @return the tipoBD
     */
    public String getTipoBD() {
        return TipoBD;
    }

    /**
     * @param tipoBD the tipoBD to set
     */
    public void setTipoBD(String tipoBD) {
        this.TipoBD = tipoBD;
    }

    /**
     * @return the dirbd
     */
    public String getDirbd() {
        return dirbd;
    }

    /**
     * @param dirbd the dirbd to set
     */
    public void setDirbd(String dirbd) {
        this.dirbd = dirbd;
    }

    /**
     * @return the nombd
     */
    public String getNombd() {
        return nombd;
    }

    /**
     * @param nombd the nombd to set
     */
    public void setNombd(String nombd) {
        this.nombd = nombd;
    }

    /**
     * @return the usubd
     */
    public String getUsubd() {
        return usubd;
    }

    /**
     * @param usubd the usubd to set
     */
    public void setUsubd(String usubd) {
        this.usubd = usubd;
    }

    /**
     * @return the passbd
     */
    public String getPassbd() {
        return passbd;
    }

    /**
     * @param passbd the passbd to set
     */
    public void setPassbd(String passbd) {
        this.passbd = passbd;
    }

    public void onTablas() {
        try {
            if (conexionexterna()) {
                if (TipoBD.compareTo("a") == 0) {
                    ArrayList<String> t = null;
                    if (this.accexternomysql.conectar()) {
                        t = this.accexternomysql.tablas(this.nombd);
                    }
                    if (!this.isNullorEmpty(t)) {
                        this.setTablas(new ArrayList<>());
                        for (String item : t) {
                            SelectItem si = new SelectItem(item, item);
                            this.getTablas().add(si);
                        }
                    }
                } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
                    ArrayList<String> t = null;
                    if (this.accexternopost.conectar()) {
                        t = this.accexternopost.tablas(this.nombd);
                    }
                    if (!this.isNullorEmpty(t)) {
                        this.setTablas(new ArrayList<>());
                        for (String item : t) {
                            SelectItem si = new SelectItem(item, item);
                            this.getTablas().add(si);
                        }
                    }
                } else {
                    ArrayList<String> t = null;
                    if (this.accexternomongo.conectar()) {
                        t = this.accexternomongo.tablas(this.nombd);
                    }
                    if (!this.isNullorEmpty(t)) {
                        this.setTablas(new ArrayList<>());
                        for (String item : t) {
                            SelectItem si = new SelectItem(item, item);
                            this.getTablas().add(si);
                        }
                    }
                }
            } else {
                System.out.println("Sin conexión");
                this.setTablas(new ArrayList<>());
            }
        } catch (Exception e) {
            System.out.println("Sin conexión");
            this.setTablas(new ArrayList<>());
        }
    }

    public boolean isNullorEmpty(ArrayList arr) {
        boolean res = false;
        try {
            if (arr == null || arr.isEmpty()) {
                res = true;
            } else {
                res = false;
            }
        } catch (Exception e) {
            res = true;
        }
        return res;
    }

    /**
     * @return the tabla
     */
    public Tabla getTabla() {
        return tabla;
    }

    /**
     * @param tabla the tabla to set
     */
    public void setTabla(Tabla tabla) {
        this.tabla = tabla;
    }

    /**
     * @return the tablas
     */
    public List<SelectItem> getTablas() {
        return tablas;
    }

    /**
     * @param tablas the tablas to set
     */
    public void setTablas(List<SelectItem> tablas) {
        this.tablas = tablas;
    }

    private String generarMYSQL() {
        String ret = "";
        try {
            acc = new AccesoDatos(dirbd, nombd, usubd, passbd);
            if (acc.conectar()) {
                for (int j = 0; j < nSitios; j++) {
                    String pid = ("     ".substring(0, 5 - (j + "").length())) + j;
                    String ip = ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255));
                    ret += fecha() + pid + " Connect\troot@" + ip + " on  using Socket\n";
                    ret += fecha() + pid + " Query\tselect @@version_comment limit 1\n";
                    ret += fecha() + pid + " Query\tSELECT DATABASE()\n";
                    ret += fecha() + pid + " Init DB\t" + this.nombd + "\n";
                    ret += fecha() + pid + " Query\tshow databases\n";
                    ret += fecha() + pid + " Query\tshow tables\n";
                    ret += fecha() + pid + " Field List\tAccount\n";
                    //int divInsert = (int) (nConsultas / 3);
                    //int nInsert = (int) (Math.random() * divInsert) + 1;

                    ArrayList<String> atributos = new ArrayList<>();
                    for (int i = 0; i < (nConsultas /*- nInsert*/); i++) {
                        /*int numeroT = (int) (Math.random() * tablas.size());
                        tabla = tablas.get(numeroT);*/
                        atributos = acc.atributos(this.tabla.getNombre());
                        int numeroA = (int) (Math.random() * atributos.size());
                        String atributoTipo = atributos.get(numeroA);
                        switch (this.transTipoDato(atributoTipo.split(":")[1])) {
                            case 0:
                                String[] c_operador0 = {"!=", "="};
                                String[] c_operacion0 = {"SELECT", "UPDATE", "DELETE"};
                                String operador0 = c_operador0[(int) (Math.random() * c_operador0.length)];
                                String operacion0 = c_operacion0[0/*(int) (Math.random() * c_operacion0.length)*/];
                                ArrayList c_tamanio0 = acc.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                                int tam0 = ((Double) ((ArrayList) c_tamanio0.get(0)).get(0)).intValue();
                                if (tam0 > 0) {
                                    int ale = (int) (Math.random() * tam0);
                                    ArrayList c_valor = acc.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " LIMIT " + ale + ",1;");
                                    if (operacion0.compareTo("SELECT") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion0 + " " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador0 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion0.compareTo("UPDATE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion0 + " " + tabla.getNombre() + " SET " + atributoTipo.split(":")[0] + "='" + generaCadena() + "' WHERE " + atributoTipo.split(":")[0] + operador0 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion0.compareTo("DELETE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion0 + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador0 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    }
                                }
                                break;
                            case 1:
                                String[] c_operador = {">", "<", "<=", ">=", "!=", "="};
                                String[] c_operacion = {"SELECT", "UPDATE", "DELETE"};
                                String operador = c_operador[(int) (Math.random() * c_operador.length)];
                                String operacion = c_operacion[0/*(int) (Math.random() * c_operacion.length)*/];
                                ArrayList c_tamanio = acc.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                                int tam = ((Double) ((ArrayList) c_tamanio.get(0)).get(0)).intValue();
                                if (tam > 0) {
                                    int ale = (int) (Math.random() * tam);
                                    ArrayList c_valor = acc.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " LIMIT " + ale + ",1;");
                                    DecimalFormat formateador = new DecimalFormat("###############.##########");
                                    String v = "0";
                                    v = formateador.format((Double) ((ArrayList) c_valor.get(0)).get(0));
                                    if (operacion.compareTo("SELECT") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion + " " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    } else if (operacion.compareTo("UPDATE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion + " " + tabla.getNombre() + " SET " + atributoTipo.split(":")[0] + "=" + generaNumero() + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    } else if (operacion.compareTo("DELETE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    }
                                }
                                break;
                            case 2:
                                String[] c_operador2 = {">", "<", "<=", ">=", "!=", "="};
                                String[] c_operacion2 = {"SELECT", "UPDATE", "DELETE"};
                                String operador2 = c_operador2[(int) (Math.random() * c_operador2.length)];
                                String operacion2 = c_operacion2[0/*(int) (Math.random() * c_operacion2.length)*/];
                                ArrayList c_tamanio2 = acc.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                                int tam2 = ((Double) ((ArrayList) c_tamanio2.get(0)).get(0)).intValue();
                                if (tam2 > 0) {
                                    int ale = (int) (Math.random() * tam2);
                                    ArrayList c_valor = acc.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " LIMIT " + ale + ",1;");
                                    if (operacion2.compareTo("SELECT") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion2 + " " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador2 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion2.compareTo("UPDATE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion2 + " " + tabla.getNombre() + " SET " + atributoTipo.split(":")[0] + "=" + atributoTipo.split(":")[0] + " WHERE " + atributoTipo.split(":")[0] + operador2 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion2.compareTo("DELETE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion2 + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador2 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    /*if (!tabla.getNombre().isEmpty()) {
                        ArrayList sr = acc.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                        int s = ((Double) ((ArrayList) sr.get(0)).get(0)).intValue();
                        if (s > 0) {
                            for (int t = 0; t < nInsert; t++) {
                                int ale = (int) (Math.random() * s);
                                ArrayList result = acc.ejecutarConsulta("SELECT * FROM " + tabla + " LIMIT " + ale + ",1;");
                                String valoresComa = "";
                                for (int y = 0; y < ((ArrayList) result.get(0)).size(); y++) {
                                    if (this.transTipoDato(atributos.get(y).split(":")[1]) == 0 || this.transTipoDato(atributos.get(y).split(":")[1]) == 2) {
                                        valoresComa += "\""+((ArrayList) result.get(0)).get(y) + "\", ";
                                    } else {
                                        valoresComa += ((ArrayList) result.get(0)).get(y) + ", ";
                                    }
                                }
                                valoresComa = valoresComa.substring(0, valoresComa.length() - 2);
                                ret += fecha() + pid + " Query\tINSERT INTO " + tabla + " values(" + valoresComa + ")\n";
                            }
                        }
                    }*/
                    ret += fecha() + pid + " Quit\t\n";
                }
                acc.desconectar();
            }
        } catch (Exception ex) {
            Logger.getLogger(BD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    private String generarPostgreSQL() {
        return "";
    }

    private String generarMongoDB() {
        String ret = "";
        try {
            accexternomongo = new AccesoDatosMG(dirbd, nombd, this.tabla.getNombre(), usubd, passbd);
            if (accexternomongo.conectar()) {
                for (int j = 0; j < nSitios; j++) {
                    String ip = ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + ":" + ((int) (Math.random() * 90)) + "" + ((int) (Math.random() * 900));
                    ret += fecha() + " I  NETWORK  [listener] connection accepted from " + ip + " #" + (j + 1) + " (1 connection now open)\n";
                    ret += fecha() + " I  NETWORK  [conn" + (j + 1) + "] received client metadata from " + ip + " conn" + (j + 1) + ": { application: { name: \"MongoDB Shell\" }, driver: { name: \"MongoDB Internal Client\", version: \"4.2.6\" }, os: { type: \"Windows\", name: \"Microsoft Windows 10\", architecture: \"x86_64\", version: \"10.0 (build 19042)\" } }";
                    ArrayList<String> atributos = new ArrayList<>();
                    for (int i = 0; i < (nConsultas); i++) {
                        atributos = accexternomongo.atributos(this.nombd, this.getTabla().getNombre());
                        int numeroA = (int) (Math.random() * atributos.size());
                        String atributoTipo = atributos.get(numeroA);
                        ArrayList<String> c_operador0 = new ArrayList<>();
                        int tipo = this.transTipoDato(atributoTipo.split(":")[1]);
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
                        String[] c_operacion0 = {"find", "update", "deleteOne"};
                        String operador0 = c_operador0.get((int) (Math.random() * c_operador0.size()));
                        String operacion0 = c_operacion0[(int) (Math.random() * c_operacion0.length)];
                        MongoCollection<Document> colec = accexternomongo.getMongoClient().getDatabase(this.nombd).getCollection(this.getTabla().getNombre());
                        double res = (double) colec.countDocuments();
                        if (res > 0) {
                            int ale = 0;
                            ale = (int) (Math.random() * res);
                            Document c_valor=colec.find().skip(ale).limit(1).first();
                            ale = (int) (Math.random() * res);
                            Document c_valor2=colec.find().skip(ale).limit(1).first();
                            String valorString1 = "";
                            String valorString2 = "";
                            String atributoDocInterno1=null;
                            String atributoDocInterno2=null;
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
                                String varray=c_valor.get(atributoTipo.split(":")[0]).toString();
                                varray=varray.replace("]", "");
                                varray=varray.replace("[", "");
                                varray=varray.replace("[{", "");
                                varray=varray.replace("}]", "");
                                varray=varray.replace("{", "");
                                varray=varray.replace("}", "");
                                varray=varray.replace("}", "");
                                System.out.println("ARRAY: "+varray);
                                if(varray.contains(":")){
                                    int nValoresInternos=0;
                                    for(int u=0;u<varray.length();u++){
                                        if(varray.charAt(u)==':'){
                                            nValoresInternos++;
                                        }
                                    }
                                    nValoresInternos++;
                                    String [] arraydearray=varray.split(",");
                                    String linea=arraydearray[(int)(Math.random() * nValoresInternos)];
                                    System.out.println(linea);
                                    String []atributovalor=linea.split(":");
                                    atributoDocInterno1=atributovalor[0];
                                    valorString1=atributovalor[1];
                                    linea=arraydearray[(int)(Math.random() * nValoresInternos)];
                                    System.out.println(linea);
                                    atributovalor=linea.split(":");
                                    atributoDocInterno2=atributovalor[0];
                                    valorString2=atributovalor[1];
                                }else{
                                    varray=varray.replace("[", "");
                                    varray=varray.replace("]", "");
                                    String []valores=varray.split(",");
                                    valorString1=valores[(int)Math.random()*valores.length];
                                    valorString2=valores[(int)Math.random()*valores.length];
                                }   
                            } else {
                                valorString1 = c_valor.getString(atributoTipo.split(":")[0]);
                                valorString2 = c_valor2.getString(atributoTipo.split(":")[0]);
                            }
                            if (operacion0.compareTo("find") == 0) {
                                if(atributoDocInterno1!=null){
                                    ret += fecha() + " I  COMMAND [conn" + (j + 1) + "] command " + nombd + "." + this.tabla.getNombre()
                                        + "appName: \"MongoDB Shell\" command: find { find: \"" + this.tabla.getNombre() + "\", filter: { " + atributoTipo.split(":")[0] + "."+atributoDocInterno1+": {" + operador0 + ":" + valorString1 + "} }, lsid: { id: UUID(\"73bfc0b6-e8de-4504-89d7-7636bfe18ecf\") }, $db: \"" + this.nombd + "\"}\n";
                                    atributoDocInterno1=null;
                                }else{
                                    ret += fecha() + " I  COMMAND [conn" + (j + 1) + "] command " + nombd + "." + this.tabla.getNombre()
                                        + "appName: \"MongoDB Shell\" command: find { find: \"" + this.tabla.getNombre() + "\", filter: { " + atributoTipo.split(":")[0] + ": {" + operador0 + ":" + valorString1 + "} }, lsid: { id: UUID(\"73bfc0b6-e8de-4504-89d7-7636bfe18ecf\") }, $db: \"" + this.nombd + "\"}\n";
                                }
                                
                            } else if (operacion0.compareTo("update") == 0) {
                                if(atributoDocInterno1!=null && atributoDocInterno2!=null){
                                    ret += fecha() + " I  WRITE [conn" + (j + 1) + "] update " + nombd + "." + this.tabla.getNombre()
                                        + "appName: \"MongoDB Shell\" command: { q: { " + atributoTipo.split(":")[0] + "."+atributoDocInterno1+": { " + operador0 + ":" + valorString1 + " } }, u:{ $set: {" + atributoTipo.split(":")[0]+"." +atributoDocInterno2+ ": " + valorString2 + " } }, multi: false, upsert: false } planSummary: COLLSCAN keysExamined:0 docsExamined:10 nMatched:1 nModified:0 numYields:0 queryHash:057916ED planCacheKey:057916ED locks:{ ParallelBatchWriterMode: { acquireCount: { r: 1 } }, ReplicationStateTransition: { acquireCount: { w: 1 } }, Global: { acquireCount: { w: 1 } }, Database: { acquireCount: { w: 1 } }, Collection: { acquireCount: { w: 1 } }, Mutex: { acquireCount: { r: 1 } } } flowControl:{ acquireCount: 1, timeAcquiringMicros: 1 } storage:{} 31ms\n";
                                }else if(atributoDocInterno1==null && atributoDocInterno2==null){
                                    ret += fecha() + " I  WRITE [conn" + (j + 1) + "] update " + nombd + "." + this.tabla.getNombre()
                                        + "appName: \"MongoDB Shell\" command: { q: { " + atributoTipo.split(":")[0] + ": { " + operador0 + ":" + valorString1 + " } }, u:{ $set: {" + atributoTipo.split(":")[0]+ ": " + valorString2 + " } }, multi: false, upsert: false } planSummary: COLLSCAN keysExamined:0 docsExamined:10 nMatched:1 nModified:0 numYields:0 queryHash:057916ED planCacheKey:057916ED locks:{ ParallelBatchWriterMode: { acquireCount: { r: 1 } }, ReplicationStateTransition: { acquireCount: { w: 1 } }, Global: { acquireCount: { w: 1 } }, Database: { acquireCount: { w: 1 } }, Collection: { acquireCount: { w: 1 } }, Mutex: { acquireCount: { r: 1 } } } flowControl:{ acquireCount: 1, timeAcquiringMicros: 1 } storage:{} 31ms\n";
                                }
                                
                            } else if (operacion0.compareTo("deleteOne") == 0) {
                                if(atributoDocInterno1!=null){
                                    ret += fecha() + " I  WRITE [conn" + (j + 1) + "] remove " + nombd + "." + this.tabla.getNombre()
                                        + "appName: \"MongoDB Shell\" command: { q: { " + atributoTipo.split(":")[0] +"."+atributoDocInterno1 +": { " + operador0 + ":" + valorString1 + " } }, limit: 1 }  planSummary: COLLSCAN keysExamined:0 docsExamined:18 ndeleted:1 keysDeleted:1 numYields:1 queryHash:057916ED planCacheKey:057916ED locks:{ ParallelBatchWriterMode: { acquireCount: { r: 2 } }, ReplicationStateTransition: { acquireCount: { w: 2 } }, Global: { acquireCount: { w: 2 } }, Database: { acquireCount: { w: 2 } }, Collection: { acquireCount: { w: 2 } }, Mutex: { acquireCount: { r: 3 } } } flowControl:{ acquireCount: 2, timeAcquiringMicros: 6 } storage:{} 31ms\n";
                                }else{
                                    ret += fecha() + " I  WRITE [conn" + (j + 1) + "] remove " + nombd + "." + this.tabla.getNombre()
                                        + "appName: \"MongoDB Shell\" command: { q: { " + atributoTipo.split(":")[0] + ": { " + operador0 + ":" + valorString1 + " } }, limit: 1 }  planSummary: COLLSCAN keysExamined:0 docsExamined:18 ndeleted:1 keysDeleted:1 numYields:1 queryHash:057916ED planCacheKey:057916ED locks:{ ParallelBatchWriterMode: { acquireCount: { r: 2 } }, ReplicationStateTransition: { acquireCount: { w: 2 } }, Global: { acquireCount: { w: 2 } }, Database: { acquireCount: { w: 2 } }, Collection: { acquireCount: { w: 2 } }, Mutex: { acquireCount: { r: 3 } } } flowControl:{ acquireCount: 2, timeAcquiringMicros: 6 } storage:{} 31ms\n";
                                }
                                
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String generaCadena() {
        return "" + UUID.randomUUID();
    }

    private String generaNumero() {
        return "" + (int) (Math.random() * 25 + 1);
    }
}
