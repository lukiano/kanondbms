/**
 * 
 */
package servidor.catalog.tipo;

import java.util.HashMap;
import java.util.Map;

import servidor.tabla.Campo;

/**
 * Conversor entre tipos de datos del motor, asi como entre los tipos de datos y formato comprensible en texto o formato binario.
 */
public abstract class Conversor {
    
    /**
     * Constructor privado para evitar instanciamiento.
     */
    protected Conversor() {}

    /**
     * Convierte un objeto Java de una clase a otra, segun el campo.
     * @param campo datos para saber en que formato se encuentra el valor crudo.
     * @param valor un valor en un formato especificado por el campo.
     * @return un valor en el formato correspondiente segun el conversor.
     */
    public abstract Object convertir(Campo campo, Object valor);
    
    /**
     * Mapa que contiene los diferentes conversores segun los tipos de datos soportados.
     */
    private static final Map<Tipo, Class<? extends Conversor> > mapa;
    
    /**
     * Conversor de un tipo de datos a un String.
     */
    private static final Conversor conversorATexto = new Texto_Conversor();
    
    /**
     * Conversor de un String a un tipo de datos.
     */
    private static final Conversor conversorDeTexto = new DeTexto_Conversor();
    
    /**
     * Conversor de un tipo de datos a un arreglo binario.
     */
    private static final Conversor conversorABytes = new ByteArray_Conversor();
    
    /**
     * Conversor de un arreglo binario a un tipo de datos. 
     */
    private static final Conversor conversorDeBytes = new DeByteArray_Conversor();
    
    // Llenado del mapa.
    static {
        mapa = new HashMap<Tipo, Class<? extends Conversor> >(Tipo.values().length);
        mapa.put(Tipo.INTEGER, NumericConversor.class);
        mapa.put(Tipo.CHAR, CharArrayConversor.class);
    }

    /**
     * Obtiene el conversor de objetos a un tipo determinado.
     * @param destino el tipo al cual se desean convertir los objetos.
     * @return un conversor de objetos al tipo pasado por parametro.
     */
    public static final Conversor conversor(Tipo destino) {
        Class<? extends Conversor> clazz = mapa.get(destino);
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Conversor de un tipo de datos a un String.
     */
    public static final Conversor conversorATexto() {
        return conversorATexto;
    }

    /**
     * Conversor de un String a un tipo de datos.
     */
    public static final Conversor conversorDeTexto() {
        return conversorDeTexto;
    }

    /**
     * Conversor de un tipo de datos a un arreglo binario.
     */
    public static final Conversor conversorABytes() {
        return conversorABytes;
    }

    /**
     * Conversor de un arreglo binario a un tipo de datos. 
     */
    public static final Conversor conversorDeBytes() {
        return conversorDeBytes;
    }

}
