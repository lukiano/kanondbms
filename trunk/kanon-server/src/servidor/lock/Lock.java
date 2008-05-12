package servidor.lock;

import java.util.Date;

import servidor.Id;
import servidor.log.LSN;
import servidor.transaccion.Transaccion;

/**
 * Interfaz que representa a un Lock aplicado sobre un objeto.
 */
public interface Lock {

	/**
	 * @return el Id del elemento bloqueado por este Lock
	 */
	Id idElementoBloqueado();
	
	/**
	 * Comprueba exclusividad de este lock.
	 * @return true si este lock es exclusivo.
	 */
	boolean exclusivo();
	
	/**
	 * Devuelve la fecha y hora en la que este lock fue creado.
	 * @return la fecha y hora en la que este lock fue creado.
	 */
	Date fechaCreacion();
    
    /**
     * Devuelve la transaccion en donde se creó el lock.
     * @return la transaccion en donde se creó el lock.
     */
	Transaccion propietario();
	
	/**
	 * Devuelve el ultimo LSN de la transaccion antes que creara a este lock.
	 * @return el ultimo LSN de la transaccion antes que creara a este lock.
	 * (Por si se implementan rollbacks con savepoints).
	 */
	LSN ultimoLSN();
	
}
