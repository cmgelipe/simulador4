package com.simulador.web;

import com.simulador.modelo.BD;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
/**
 * Managed bean "bd" (antes JBS.inicio).
 *
 * @author felipe
 */
@Named("bd")
@SessionScoped
public class SimuladorBean implements Serializable {
    private BD obj = new BD();
    private boolean listo=true;
    private boolean bconfig = false;
    private boolean bbotonfragmentar = false;
    private boolean bdatosincorrectos = false;
    private String arrOPERACIONES[];
    public SimuladorBean() {
    }
    public void sig(){
            obj.generar(arrOPERACIONES);
            listo=false;
        }
//        return "segunda.xhtml";

    public void sig2(){
        obj.generar(arrOPERACIONES);
        if(this.arrOPERACIONES[0] != null ){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Log Generado",""));
            listo=false;
        }else{
            listo=false;
        }
//        return "segunda.xhtml";
    }
    /**
     * @return the obj
     */
    public BD getObj() {
        return obj;
    }

    /**
     * @param obj the obj to set
     */
    public void setObj(BD obj) {
        this.obj = obj;
    }

    /**
     * @return the listo
     */
    public boolean isListo() {
        return listo;
    }

    /**
     * @param listo the listo to set
     */
    public void setListo(boolean listo) {
        this.listo = listo;
    }
    public void configuracion() {
        setBconfig(true);

    }

    /**
     * @return the bconfig
     */
    public boolean isBconfig() {
        return bconfig;
    }

    /**
     * @param bconfig the bconfig to set
     */
    public void setBconfig(boolean bconfig) {
        this.bconfig = bconfig;
    }
    public String probarConexion() {

        if (obj.conexionexterna()) {
            this.setBbotonfragmentar(true);
            this.setBdatosincorrectos(false);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Conexión exitosa",""));
        } else {
            this.setBbotonfragmentar(false);
            this.setBdatosincorrectos(true);
        }
        return "index.xhtml";
    }
    public void cancelarConf() {
        bconfig = false;
    }

    /**
     * @return the bbotonfragmentar
     */
    public boolean isBbotonfragmentar() {
        return bbotonfragmentar;
    }

    /**
     * @param bbotonfragmentar the bbotonfragmentar to set
     */
    public void setBbotonfragmentar(boolean bbotonfragmentar) {
        this.bbotonfragmentar = bbotonfragmentar;
    }

    /**
     * @return the bdatosincorrectos
     */
    public boolean isBdatosincorrectos() {
        return bdatosincorrectos;
    }

    /**
     * @param bdatosincorrectos the bdatosincorrectos to set
     */
    public void setBdatosincorrectos(boolean bdatosincorrectos) {
        this.bdatosincorrectos = bdatosincorrectos;
    }
    public void revisarDatos() {
        this.bdatosincorrectos = false;
    }

    /**
     * @return the arrOPERACIONES
     */
    public String[] getArrOPERACIONES() {
        return arrOPERACIONES;
    }

    /**
     * @param arrOPERACIONES the arrOPERACIONES to set
     */
    public void setArrOPERACIONES(String []arrOPERACIONES) {
        this.arrOPERACIONES = arrOPERACIONES;
    }
}
