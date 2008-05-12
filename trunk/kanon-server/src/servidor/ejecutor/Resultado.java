package servidor.ejecutor;

import servidor.tabla.Tabla;

/**
 * Clase que guarda el resultado de una peticion para ser mandado de vuelta al cliente.
 * @author victor
 */
public class Resultado {
    
    /**
     * Variable que guarda el resultado en caso que este sea una tabla (por el momento solo si la peticion es un SELECT).
     */
    private Tabla tabla;
    
    /**
     * Variable que guarda el resultado en caso que sea un mensaje, ya sea de informacion o de error. 
     */
    private String mensaje;
    
    /**
     * Constructor de la clase.
     */
    public Resultado() {
        this.tabla = null;
        this.mensaje = null;
    }
    
    /**
     * @return el mensaje de error o de informacion, o NULL si el resultado es una tabla.
     */
    public String getMensaje() {
        return mensaje;
    }
    
    /**
     * Se establece el resultado como un mensaje de error o de informacion.
     * @param mensaje el mensaje que sera enviado de vuelta al cliente.
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    /**
     * @return la tabla resultante de un pedido de SELECT o NULL si el resultado de la peticion es un mensaje.
     */
    public Tabla getTabla() {
        return tabla;
    }
    
    /**
     * Se establece el resultado como una tabla.
     * @param tabla la tabla resultante de un SELECT que sera enviada al cliente.
     */
    public void setTabla(Tabla tabla) {
        this.tabla = tabla;
    }
    
}
