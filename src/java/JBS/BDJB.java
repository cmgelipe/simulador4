package JBS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean(name = "arch")
@RequestScoped
public class BDJB implements Serializable {


    private StreamedContent file;
    String cadenaArchivo = "";
    
    public BDJB() {
        try {
            inicio objeto = (inicio) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("bd");
            cadenaArchivo = objeto.getObj().getCadena();
            String ruta = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/");
            File f=new File(ruta+"resources/temṕ.log");
            if(f.exists())
                f.delete();
            RandomAccessFile acc = new RandomAccessFile(ruta + "resources/temṕ.log", "rw");
            acc.writeBytes(cadenaArchivo);
            InputStream stream = new FileInputStream(ruta + "resources/temṕ.log");//((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(f.getAbsolutePath());
            file = new DefaultStreamedContent(stream, "text/plain", "log.log");
            acc.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BDJB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BDJB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the file
     */
    public StreamedContent getFile() {
        return file;
    }

}
