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
}
