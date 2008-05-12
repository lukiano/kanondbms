/**
 * 
 */
package servidor.lock.impl;

import java.util.Date;

import servidor.Id;
import servidor.lock.Lock;
import servidor.log.LSN;
import servidor.transaccion.Transaccion;

/**
 * Implementacion basica de un Lock.
 */
public final class LockImpl implements Lock {

	/**
	 * Indica si este lock es exclusivo.
	 */
	private boolean exclusivo;
	
	/**
	 * El Id del elemento bloqueado por este Lock.
	 */
	private Id idElemento;
	
	/**
	 * La fecha de creacion de este lock.
	 */
	private Date fechaCreacion;
    
    /**
     * La transaccion activa al momento de crear este lock.
     */
    private Transaccion propietario;
    
    /**
     * El ultimo LSN de la transaccion activa al momento de crear este lock.
     */
    private LSN ultimoLSN;
	
	/**
	 * Constructor de la clase. Inicializa las variables.
	 * @param propietario la transaccion activa al momento de crear este lock.
	 * @param idElemento el Id del elemento bloqueado por este Lock.
	 * @param exclusivo true si el elemento se desea bloquear de manera exclusiva.
	 */
	public LockImpl(Transaccion propietario, Id idElemento, boolean exclusivo) {
		this.ultimoLSN = propietario.ultimoLSN();
		this.idElemento = idElemento;
		this.exclusivo = exclusivo;
        this.propietario = propietario;
		this.fechaCreacion = new Date();
	}

	/**
	 * @see servidor.lock.Lock#idElementoBloqueado()
	 */
	public Id idElementoBloqueado() {
		return this.idElemento;
	}

	/**
	 * @see servidor.lock.Lock#exclusivo()
	 */
	public boolean exclusivo() {
		return this.exclusivo;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Lock: " + this.idElementoBloqueado() + " - tipo " + 
			(this.exclusivo?"exclusivo":"compartido") + " " + this.propietario().id();
	}

	/**
	 * @see servidor.lock.Lock#fechaCreacion()
	 */
	public Date fechaCreacion() {
		return (Date) this.fechaCreacion.clone();
	}

    /**
     * @see servidor.lock.Lock#propietario()
     */
    public Transaccion propietario() {
        return this.propietario;
    }
    
    /**
     * @see servidor.lock.Lock#ultimoLSN()
     */
    public LSN ultimoLSN() {
    	return this.ultimoLSN;
    }

}
