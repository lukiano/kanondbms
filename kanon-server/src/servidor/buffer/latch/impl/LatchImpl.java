/**
 * 
 */
package servidor.buffer.latch.impl;

import java.util.Date;

import servidor.buffer.Bloque.ID;
import servidor.buffer.latch.Latch;

/**
 * Implementacion basica de un Latch.
 */
public final class LatchImpl implements Latch {

	/**
	 * El ID del bloque sobre el cual este latch se aplica.
	 */
	private ID idElemento;
	
	/**
	 * Fecha de creacion de este Latch.
	 */
	private Date fechaCreacion;
    
    /**
     * Thread propietario del Latch. (Se usa un thread en vez de una transaccion por simplicidad).
     */
    private Thread propietario;
    
	/**
	 * Constructor de la clase.
	 * @param propietario el Thread que crea el Latch.
	 * @param idElemento el bloque al cual se le aplica.
	 */
	public LatchImpl(Thread propietario, ID idElemento) {
		this.idElemento = idElemento;
        this.propietario = propietario;
		this.fechaCreacion = new Date();
	}

	/**
	 * @see servidor.buffer.latch.Latch#idElementoBloqueado()
	 */
	public ID idElementoBloqueado() {
		return this.idElemento;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Latch: " + this.idElementoBloqueado();
	}

	/**
	 * @see servidor.buffer.latch.Latch#fechaCreacion()
	 */
	public Date fechaCreacion() {
		return (Date) this.fechaCreacion.clone();
	}

    /**
     * @see servidor.buffer.latch.Latch#propietario()
     */
    public Thread propietario() {
        return this.propietario;
    }
    
}
