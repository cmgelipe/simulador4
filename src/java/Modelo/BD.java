package Modelo;

import Datos.AccesoDatos;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author felipe
 */
public class BD implements Serializable {

    AccesoDatos acc = null;
    private int nSitios = 2;
    private int nConsultas = 3;
    private String cadena;

    public void generar() {
        String ret = "";
        try {
            acc = new AccesoDatos();
            if (acc.conectar()) {
                ArrayList<String> tablas = acc.tablas();
                for (int j = 0; j < nSitios; j++) {
                    String pid = ("     ".substring(0, 5 - (j + "").length())) + j;
                    String ip = ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255));
                    ret += fecha() + pid + " Connect\troot@" + ip + " on  using Socket\n";
                    ret += fecha() + pid + " Query\tselect @@version_comment limit 1\n";
                    ret += fecha() + pid + " Query\tSELECT DATABASE()\n";
                    ret += fecha() + pid + " Init DB\tTPCE\n";
                    ret += fecha() + pid + " Query\tshow databases\n";
                    ret += fecha() + pid + " Query\tshow tables\n";
                    ret += fecha() + pid + " Field List\tAccount\n";
                    int divInsert = (int) (nConsultas / 3);
                    int nInsert = (int) (Math.random() * divInsert) + 1;
                    String tabla = "";
                    ArrayList<String> atributos = new ArrayList<>();
                    for (int i = 0; i < (nConsultas - nInsert); i++) {
                        int numeroT = (int) (Math.random() * tablas.size());
                        tabla = tablas.get(numeroT);
                        atributos = acc.atributos(tabla);
                        int numeroA = (int) (Math.random() * atributos.size());
                        String atributoTipo = atributos.get(numeroA);
                        switch (this.transTipoDato(atributoTipo.split(":")[1])) {
                            case 0:
                                String[] c_operador0 = {"!=", "="};
                                String[] c_operacion0 = {"SELECT", "UPDATE", "DELETE"};
                                String operador0 = c_operador0[(int) (Math.random() * c_operador0.length)];
                                String operacion0 = c_operacion0[(int) (Math.random() * c_operacion0.length)];
                                ArrayList c_tamanio0 = acc.ejecutarConsulta("select count(*) from " + tabla);
                                int tam0 = ((Double) ((ArrayList) c_tamanio0.get(0)).get(0)).intValue();
                                if (tam0 > 0) {
                                    int ale = (int) (Math.random() * tam0);
                                    ArrayList c_valor = acc.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla + " LIMIT " + ale + ",1;");
                                    if (operacion0.compareTo("SELECT") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion0 + " " + atributoTipo.split(":")[0] + " FROM " + tabla + " WHERE " + atributoTipo.split(":")[0] + operador0 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion0.compareTo("UPDATE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion0 + " " + tabla + " SET " + atributoTipo.split(":")[0] + "=" + atributoTipo.split(":")[0] + " WHERE " + atributoTipo.split(":")[0] + operador0 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion0.compareTo("DELETE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion0 + " FROM " + tabla + " WHERE " + atributoTipo.split(":")[0] + operador0 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    }
                                }
                                break;
                            case 1:
                                String[] c_operador = {">", "<", "<=", ">=", "!=", "="};
                                String[] c_operacion = {"SELECT", "UPDATE", "DELETE"};
                                String operador = c_operador[(int) (Math.random() * c_operador.length)];
                                String operacion = c_operacion[(int) (Math.random() * c_operacion.length)];
                                ArrayList c_tamanio = acc.ejecutarConsulta("select count(*) from " + tabla);
                                int tam = ((Double) ((ArrayList) c_tamanio.get(0)).get(0)).intValue();
                                if (tam > 0) {
                                    int ale = (int) (Math.random() * tam);
                                    ArrayList c_valor = acc.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla + " LIMIT " + ale + ",1;");
                                    DecimalFormat formateador = new DecimalFormat("###############.##########");
                                    String v = "0";
                                    v = formateador.format((Double) ((ArrayList) c_valor.get(0)).get(0));
                                    if (operacion.compareTo("SELECT") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion + " " + atributoTipo.split(":")[0] + " FROM " + tabla + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    } else if (operacion.compareTo("UPDATE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion + " " + tabla + " SET " + atributoTipo.split(":")[0] + "=" + atributoTipo.split(":")[0] + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    } else if (operacion.compareTo("DELETE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion + " FROM " + tabla + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    }
                                }
                                break;
                            case 2:
                                String[] c_operador2 = {">", "<", "<=", ">=", "!=", "="};
                                String[] c_operacion2 = {"SELECT", "UPDATE", "DELETE"};
                                String operador2 = c_operador2[(int) (Math.random() * c_operador2.length)];
                                String operacion2 = c_operacion2[(int) (Math.random() * c_operacion2.length)];
                                ArrayList c_tamanio2 = acc.ejecutarConsulta("select count(*) from " + tabla);
                                int tam2 = ((Double) ((ArrayList) c_tamanio2.get(0)).get(0)).intValue();
                                if (tam2 > 0) {
                                    int ale = (int) (Math.random() * tam2);
                                    ArrayList c_valor = acc.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla + " LIMIT " + ale + ",1;");
                                    if (operacion2.compareTo("SELECT") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion2 + " " + atributoTipo.split(":")[0] + " FROM " + tabla + " WHERE " + atributoTipo.split(":")[0] + operador2 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion2.compareTo("UPDATE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion2 + " " + tabla + " SET " + atributoTipo.split(":")[0] + "=" + atributoTipo.split(":")[0] + " WHERE " + atributoTipo.split(":")[0] + operador2 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    } else if (operacion2.compareTo("DELETE") == 0) {
                                        ret += fecha() + pid + " Query\t" + operacion2 + " FROM " + tabla + " WHERE " + atributoTipo.split(":")[0] + operador2 + "\"" + ((ArrayList) c_valor.get(0)).get(0) + "\"\n";
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (!tabla.isEmpty()) {
                        ArrayList sr = acc.ejecutarConsulta("select count(*) from " + tabla);
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
                    }
                    ret += fecha() + pid + " Quit\t\n";
                }
                acc.desconectar();
            }
        } catch (Exception ex) {
            Logger.getLogger(BD.class.getName()).log(Level.SEVERE, null, ex);
        }
        cadena = ret;
    }

    public int transTipoDato(String tipo) {
        switch (tipo) {
            case "CHAR":
            case "VARCHAR":
            case "BIT":
            case "TINYINT":
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

}
