/**
 * 
 */
package servidor.buffer.politica.impl;

import java.util.Random;

import servidor.buffer.Bloque.ID;


/**
 * Politica de Reemplazo que elige un bloque al azar entre los disponibles (sin pin).
 * El azar es un numero que representa una entrada en la lista (heredada de la politica FIFO).
 * @see Random
 */
public class PR_Azar extends PR_FIFO {
    
    /**
     * Generador de numeros al azar.
     */
    private Random random;

    /**
     * Constructor de la clase.
     */
    public PR_Azar() {
    	super();
        this.random = new Random();
    }

    /**
     * @see servidor.buffer.politica.impl.PR_FIFO#creado(servidor.buffer.Bloque.ID)
     */
    @Override
    public synchronized void creado(ID id) {
    	int elegido = this.random.nextInt(this.ids.size());
        this.ids.add(elegido, id);
    }
    
}
