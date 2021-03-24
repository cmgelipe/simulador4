package JBS;

import Modelo.BD;
import javax.enterprise.context.Dependent;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author felipe
 */
@ManagedBean(name = "bd")
@SessionScoped
public class inicio {
    private BD obj = new BD();
    private boolean listo=true;
    private boolean bconfig = false;
    private boolean bbotonfragmentar = false;
    private boolean bdatosincorrectos = false;
    public inicio() {
    }
    
    public void sig(){
        obj.generar();
        listo=false;
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
}
