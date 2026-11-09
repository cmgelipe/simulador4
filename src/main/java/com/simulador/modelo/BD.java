package com.simulador.modelo;

import com.simulador.datos.AccesoDatosMySQL;
import com.simulador.datos.AccesoDatosMongoDB;
import com.simulador.datos.AccesoDatosPostgreSQL;
import com.simulador.simulacion.GeneradorLogMongoDB;
import com.simulador.simulacion.GeneradorLogMySQL;
import com.simulador.simulacion.GeneradorLogPostgreSQL;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.faces.model.SelectItem;

/**
 * Estado de la configuración de la simulación (bean de respaldo del
 * formulario) y punto de entrada para disparar la generación del log.
 * La generación de cada log en sí vive en com.simulador.simulacion.
 *
 * @author felipe
 */
public class BD implements Serializable {

    private int nSitios = 2;
    private int nConsultas = 3;
    private String cadena;
    private String TipoBD;
    AccesoDatosMySQL accexternomysql;
    AccesoDatosMongoDB accexternomongo;
    AccesoDatosPostgreSQL accexternopost;
    private String dirbd;
    private String nombd;
    private String usubd = "";
    private String passbd = "";
    private List<SelectItem> tablas;
    private Tabla tabla = new Tabla();
    private HashMap<String, String> NombreIp;

    private final GeneradorLogMySQL generadorLogMySQL = new GeneradorLogMySQL();
    private final GeneradorLogPostgreSQL generadorLogPostgreSQL = new GeneradorLogPostgreSQL();
    private final GeneradorLogMongoDB generadorLogMongoDB = new GeneradorLogMongoDB();

    public boolean conexionexterna() {
        boolean res = false;

        if (TipoBD.compareTo("a") == 0) {
            try {
                accexternomysql = new AccesoDatosMySQL(dirbd, nombd, usubd, passbd);
                System.out.println(res);
                if (accexternomysql.conectar()) {
                    res = true;
                    System.out.println(res);
                    accexternomysql.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
            try {
                accexternopost = new AccesoDatosPostgreSQL(dirbd, nombd, usubd, passbd);
                if (accexternopost.conectar()) {
                    res = true;
                    accexternopost.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("d") == 0) {
            try {
                accexternomongo = new AccesoDatosMongoDB(dirbd, nombd, this.getTabla().getNombre(), usubd, passbd);
                if (accexternomongo.conectar()) {
                    res = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return res;
    }

    public void generar(String[] arr) {
        if (this.TipoBD.compareTo("a") == 0) {
            cadena = generadorLogMySQL.generar(dirbd, nombd, usubd, passbd, tabla, nSitios, nConsultas, arr);
        } else if (this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
            cadena = generadorLogPostgreSQL.generar(dirbd, nombd, usubd, passbd, tabla, nSitios, nConsultas, arr);
        } else if (this.TipoBD.compareTo("d") == 0) {

            ArrayList<String> evitados = new ArrayList<>();
            evitados.add("imagen");
            cadena = generadorLogMongoDB.generar(dirbd, nombd, usubd, passbd, tabla, nSitios, nConsultas, arr, evitados);
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
}
