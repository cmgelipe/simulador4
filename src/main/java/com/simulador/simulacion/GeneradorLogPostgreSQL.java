package com.simulador.simulacion;

import com.simulador.datos.AccesoDatosPostgreSQL;
import com.simulador.modelo.Tabla;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Genera un log simulado de operaciones sobre PostgreSQL (antes
 * Modelo.BD#generarPostgreSQL).
 */
public class GeneradorLogPostgreSQL {

    public String generar(String dirbd, String nombd, String usubd, String passbd,
            Tabla tabla, int nSitios, int nConsultas, String[] arr) {
        String ret = "";
        try {
            AccesoDatosPostgreSQL accexternopost = new AccesoDatosPostgreSQL(dirbd, nombd, usubd, passbd);
            if (accexternopost.conectar()) {
                for (int j = 0; j < nSitios; j++) {
                    String pid = "[" + (int) ((Math.random() * 8001) + 1000) + "]";
                    String ip = ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255)) + "." + ((int) (Math.random() * 255));
                    ret += pid + "-_-" + ip + "LOG:  conexion autorizada: usuario=" + usubd + " base de datos=" + nombd + " application_name=psql\n";

                    ArrayList<String> atributos = new ArrayList<>();
                    for (int i = 0; i < (nConsultas); i++) {
                        atributos = accexternopost.atributos(nombd, tabla.getNombre());
                        int numeroA = (int) (Math.random() * atributos.size());
                        String atributoTipo = atributos.get(numeroA);
                        ArrayList<String> c_operacion0 = new ArrayList<>();
                        String o = "";
                        for (String t : arr) {
                            o += t;
                        }
                        if (o.contains("a")) {
                            c_operacion0.add("select");
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

                        switch (TraductorTipoDato.traducir(atributoTipo.split(":")[1])) {
                            case 0:
                                String[] c_operador0 = {"!=", "="};
                                String operador0 = c_operador0[(int) (Math.random() * c_operador0.length)];
                                String operacion0 = c_operacion0.get((int) (Math.random() * c_operacion0.size()));
                                ArrayList c_tamanio0 = accexternopost.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                                int tam0 = ((Double) ((ArrayList) c_tamanio0.get(0)).get(0)).intValue();
                                if (tam0 > 0) {
                                    int ale = (int) (Math.random() * tam0);
                                    ArrayList c_valor = accexternopost.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " LIMIT 1 OFFSET " + ale + ";");
                                    if (operacion0.compareTo("select") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion0 + " " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador0 + "'" + ((ArrayList) c_valor.get(0)).get(0) + "'\n";
                                    } else if (operacion0.compareTo("update") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion0 + " " + tabla.getNombre() + " SET " + atributoTipo.split(":")[0] + "='" + GeneradorLogUtil.generaCadena() + "' WHERE " + atributoTipo.split(":")[0] + operador0 + "'" + ((ArrayList) c_valor.get(0)).get(0) + "'\n";
                                    } else if (operacion0.compareTo("delete") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion0 + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador0 + "'" + ((ArrayList) c_valor.get(0)).get(0) + "'\n";
                                    }
                                }
                                break;
                            case 1:
                                String[] c_operador = {">", "<", "<=", ">=", "!=", "="};
                                String operador = c_operador[(int) (Math.random() * c_operador.length)];
                                String operacion = c_operacion0.get((int) (Math.random() * c_operacion0.size()));
                                ArrayList c_tamanio = accexternopost.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                                int tam = ((Double) ((ArrayList) c_tamanio.get(0)).get(0)).intValue();
                                if (tam > 0) {
                                    int ale = (int) (Math.random() * tam);
                                    ArrayList c_valor = accexternopost.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " LIMIT 1 OFFSET " + ale + ";");
                                    DecimalFormat formateador = new DecimalFormat("###############.##########");
                                    String v = "0";
                                    v = formateador.format((Double) ((ArrayList) c_valor.get(0)).get(0));
                                    if (operacion.compareTo("select") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion + " " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    } else if (operacion.compareTo("update") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion + " " + tabla.getNombre() + " SET " + atributoTipo.split(":")[0] + "=" + GeneradorLogUtil.generaNumero() + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    } else if (operacion.compareTo("delete") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador + v + "\n";
                                    }
                                }
                                break;
                            case 2:
                                String[] c_operador2 = {">", "<", "<=", ">=", "!=", "="};
                                String operador2 = c_operador2[(int) (Math.random() * c_operador2.length)];
                                String operacion2 = c_operacion0.get((int) (Math.random() * c_operacion0.size()));
                                ArrayList c_tamanio2 = accexternopost.ejecutarConsulta("select count(*) from " + tabla.getNombre());
                                int tam2 = ((Double) ((ArrayList) c_tamanio2.get(0)).get(0)).intValue();
                                if (tam2 > 0) {
                                    int ale = (int) (Math.random() * tam2);
                                    ArrayList c_valor = accexternopost.ejecutarConsulta("SELECT " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " LIMIT 1 OFFSET " + ale + ";");
                                    if (operacion2.compareTo("select") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion2 + " " + atributoTipo.split(":")[0] + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador2 + "'" + ((ArrayList) c_valor.get(0)).get(0) + "'\n";
                                    } else if (operacion2.compareTo("update") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion2 + " " + tabla.getNombre() + " SET " + atributoTipo.split(":")[0] + "=" + atributoTipo.split(":")[0] + " WHERE " + atributoTipo.split(":")[0] + operador2 + "'" + ((ArrayList) c_valor.get(0)).get(0) + "'\n";
                                    } else if (operacion2.compareTo("delete") == 0) {
                                        ret += pid + "-_-" + ip + "LOG:  sentencia: " + operacion2 + " FROM " + tabla.getNombre() + " WHERE " + atributoTipo.split(":")[0] + operador2 + "'" + ((ArrayList) c_valor.get(0)).get(0) + "'\n";
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    ret += pid + "-_-" + ip + "LOG:  desconexión: duración de sesión 0:53:22.385 usuario=" + usubd + " base=" + nombd + " host=" + dirbd + "\n";
                }
                accexternopost.desconectar();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }
}
