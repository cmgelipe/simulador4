package com.simulador.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Managed bean "arch" (antes JBS.BDJB).
 */
@Named("arch")
@RequestScoped
public class DescargaLogBean implements Serializable {


    private StreamedContent file;
    String cadenaArchivo = "";


    public DescargaLogBean() {
        try {
            SimuladorBean objeto = (SimuladorBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("bd");
            cadenaArchivo = objeto.getObj().getCadena();
            String ruta = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/");
            File f=new File(ruta+"resources\\temṕ.log");
            System.out.println(ruta+"resources/temṕ.log");
            if(f.exists())
                f.delete();
            RandomAccessFile acc = new RandomAccessFile(ruta + "resources\\temṕ.log", "rw");
            acc.writeBytes(cadenaArchivo);
            InputStream stream = new FileInputStream(ruta + "resources\\temṕ.log");//((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(f.getAbsolutePath());
            file = DefaultStreamedContent.builder()
                    .contentType("text/plain")
                    .name("log.log")
                    .stream(() -> stream)
                    .build();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {

            ex.printStackTrace();
        }
    }

    /**
     * @return the file
     */
    public StreamedContent getFile() {
        return file;
    }


}
