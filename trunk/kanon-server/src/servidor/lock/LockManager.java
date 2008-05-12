package servidor.lock;

import java.util.Set;

import servidor.Id;
import servidor.excepciones.ObjetoBloqueadoException;
import servidor.excepciones.VictimaDeadlockRuntimeException;
import servidor.log.LSN;
import servidor.transaccion.Transaccion;

/**
 * Interfaz del administrador de locks.
 * Usuado para control de concurrencia entre distintas sesiones.
 */
public interface LockManager {

	/**
     * Bloquea el ID de un objeto para mantener control sobre el mismo.
     * Si el objeto ya se encuentra bloqueado de una forma incompatible, pone el thread en espera
     * y luego reintenta.
	 * @param idElemento el ID del objeto.
	 * @param exclusivo true si se desea bloquear de forma exclusiva
     * o false para hacerlo de manera compartida.
     * @throws RuntimeException si luego de reintentar una cantidad
     * establecida de veces no puede realizar el bloqueo.
     * @throws VictimaDeadlockRuntimeException si el thread fue elegido como 
     * víctima para evitar DeadLocks.
     * @return true si el elemento no se encontraba bloqueado ya por el thread, o si hubo una actualizacion de Lock.
	 */
	boolean bloquear(Id idElemento, boolean exclusivo);
	
	/**
	 * Bloquea el ID de un objeto para mantener control sobre el mismo.
	 * @param idElemento el ID del objeto.
	 * @param exclusivo true si se desea bloquear de forma exclusiva
     * o false para hacerlo de manera compartida.
	 * @return true si el elemento no se encontraba bloqueado ya por el thread, o si hubo una actualizacion de Lock
	 * @throws ObjetoBloqueadoException si el objeto ya se encontraba bloqueado de una forma de una forma incompatible.
	 */
	boolean bloquearCondicional(Id idElemento, boolean exclusivo) throws ObjetoBloqueadoException;
	
	/**
	 * Desbloquea el ID de un objeto ya bloqueado por la transaccion.
	 * @param idElemento el ID del elemento a desbloquear.
	 * @throws VictimaDeadlockRuntimeException si la transaccion/thread fue elegida como 
	 * víctima para evitar DeadLocks.
	 */
	void desbloquear(Id idElemento);
	
	/**
	 * @param idElemento el ID del elemento sobre el cual se desea saber la condición.
	 * @param exclusivo para saber si existe un Lock exclusivo o compartido.
     * @throws VictimaDeadlockRuntimeException si el thread fue elegido como 
     * víctima para evitar DeadLocks.
	 * @return true si el elemento se encuentra bloqueado de la manera deseada por esta transaccion (incluye ancestros).
	 */
	boolean estaBloqueado(Id idElemento, boolean exclusivo);
	
	/**
	 * Devuelve un conjunto con los locks adquiridos por la transaccion.
	 * @return un conjunto con los locks adquiridos por la transaccion.
	 */
	Set<Id> locks(Transaccion.ID idTransaccion);
	
	/**
	 * Devuelve un conjunto con los locks exclusivos adquiridos por la transaccion.
	 * @return un conjunto con los locks exclusivos adquiridos por la transaccion.
	 */
	Set<Id> locksExclusivos(Transaccion.ID idTransaccion);
	
	/**
	 * Devuelve un conjunto con los locks adquiridos por la transaccion a partir del LSN especificado.
	 * @return un conjunto con los locks adquiridos por la transaccion a partir del LSN especificado.
	 */
	Set<Id> locksDesde(Transaccion.ID idTransaccion, LSN lsn);
	
	/**
	 * Mueve los locks de la transaccion actual a su padre en caso que exista.
	 * No toma la transaccion como parametro, debe hacerse con la actual,
	 * para asegurarse que no se encuentra en espera y/o con intentos de bloqueo encolados.
	 */
	void delegarLocksATransaccionPadre();
	
	/**
	 * Metodo llamado cuando se apaga el servidor.
	 * Libera cualquier thread que se encuentre bloqueado esperando locks.
	 */
	void cerrar();
	
}
