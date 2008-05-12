/**
 * 
 */
package servidor.catalog.tipo;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase con los distintos tipos de datos soportados por el motor.
 */
public enum Tipo {

    /**
     * Tipo de datos numerico.
     */
    NUMERIC, 
    
    /**
     * Tipo de datos para un arreglo de caracteres de longitud variable.
     */
    CHAR;

    /**
     * Mapeo de tipos de datos a clases Java.
     */
    private static final Map<Tipo, Class> clases;
    
    
    /**
     * Mapeo de clases Java a tipos de datos.
     */
    private static final Map<Class, Tipo> tipos;

    /**
     * Obtiene una Clase Java a partir de un Tipo.
     * @param tipo un tipo de datos.
     * @return la clase Java correspondiente o NULL si no hay ninguna.
     */
    public static final Class dameClase(Tipo tipo) {
        return clases.get(tipo);
    }

    /**
     * Obtiene un Tipo a partir de una Clase Java.
     * @param clazz una clase Java.
     * @return el Tipo de datos correspondiente o NULL si no hay ninguno.
     */
    public static final Tipo dameTipo(Class clazz) {
        return tipos.get(clazz);
    }

    // llenado de los mapas.
    static {
    	clases = new HashMap<Tipo, Class>(Tipo.values().length);
    	tipos = new HashMap<Class, Tipo>(Tipo.values().length);
    	
        clases.put(NUMERIC, Integer.class);
        clases.put(CHAR, String.class);
        tipos.put(Integer.class, NUMERIC);
        tipos.put(String.class, CHAR);
    }
    
}
