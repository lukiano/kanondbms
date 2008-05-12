/**
 * 
 */
package servidor.catalog;

import servidor.catalog.tipo.Conversor;
import servidor.tabla.Campo;


/**
 * Clase que representa el valor de un registro en una columna.
 */
public final class Valor {
    
    /**
     * La posicion (columna) dentro del registro.
     */
    private int posicion;
    
    /**
     * El contenido del registro en la columna.
     */
    private Object contenido;
    
    /**
     * El campo de la columna.
     * @see Campo
     */
    private Campo campo;
    
    /**
     * Constructor privado para evitar instanciamiento.
     * @param posicion la posicion dentro del registro.
     * @param campo el campo de la columna.
     * @param contenido el contenido del registro en la posicion especificada.
     */
    private Valor(int posicion, Campo campo, Object contenido) {
        this.posicion = posicion;
        this.campo = campo;
        this.contenido = contenido;
    }
    
    /**
     * Crea un nuevo valor a partir del campo, posicion dentro del registro y el contenido mismo.
     * @param posicion la posicion dentro del registro.
     * @param campo el campo de la columna.
     * @param contenido el contenido del registro en la posicion especificada.
     * @return una implementacion de Valor con los datos especificados.
     */
    public static Valor nuevoValor(int posicion, Campo campo, Object contenido) {
        return new Valor(posicion, campo, contenido);
    }
    
    /**
     * @return la posicion (columna) dentro del registro.
     */
    public int posicion() {
        return this.posicion;
    }
    
    /**
     * @return el contenido del registro en la posicion especificada.
     */
    public Object contenido() {
        return this.contenido;
    }

    /**
     * @return el campo de la columna.
     */
    public Campo campo() {
        return this.campo;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return "(Posicion: " + this.posicion + " - Campo: " + this.campo + " - Contenido: " + Conversor.conversorATexto().convertir(this.campo, this.contenido) + ")";
    }
    
}