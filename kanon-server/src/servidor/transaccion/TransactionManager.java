package servidor.transaccion;

import java.util.Set;


/**
 * Interfaz del administrador de transacciones.
 *
 */
public interface TransactionManager {

	/**
	 * Inicia una nueva transaccion en el thread. Si ya existe una, se crea como hija de la misma.
	 */
	void iniciarTransaccion();
	
	/**
	 * Realiza un commit de la transaccion actual del thread, si hay anidadas lo hace de la mas anidada.
	 * Escribe el LOG de fin de tranasccion o de CHILD_COMMITTED segun corresponda.
	 * @throws RuntimeException si no habia ninguna transaccion activa.
	 */
	void commitTransaccion();
	
	/**
	 * Realiza un rollback de la transaccion actual del thread, si hay anidadas lo hace de la mas anidada.
	 * @throws RuntimeException si no habia ninguna transaccion activa.
	 */
	void abortarUltimaTransaccion();
	
	/**
	 * Realiza un rollback de todas las transacciones del thread, desde la mas anidada hasta la de alto nivel
	 * @throws RuntimeException si no habia ninguna transaccion activa.
	 */
	void abortarTransaccionesDelThread();
	
	/**
	 * Aborta las transacciones del thread sin realizar rollbacks. Es utilizado cuando se simula un crash.
	 */
	void abortarTransaccionesDelThreadSinRollback();
	
	/**
	 * Realiza un rollback de la transaccion hasta el savepoint especificado.
	 * Recordar que abortar hasta un savepoint no borra la transaccion. Esta sigue en curso.
	 * @param savepoint el nombre del savepoint hasta donde se desea abortar.
	 * @throws RuntimeException si no habia ninguna transaccion activa.
	 */
	void abortarUltimaTransaccionHasta(String savepoint);
	
	/**
	 * @return el estado actual de transacciones en el thread.
	 */
	Estado estadoActual();
	
	/**
	 * @return la tranasccion actual del thread. Si hay anidadas retorna la de mas bajo nivel.
	 * @throws RuntimeException si no habia ninguna transaccion activa.
	 */
	Transaccion dameTransaccion();
	
	/**
	 * @return las transacciones activas en el thread. Devuelve un conjunto vacio si no hay ninguna.
	 */
	Set<Transaccion> dameTransacciones();
	
	/**
	 * Devuelve una transaccion especificada segun su identificador.
	 * @param idTransaccion el ID de la transaccion deseada
	 * @return la implementacion de la transaccion o NULL si no hay ninguna con ese ID.
	 */
	Transaccion dameTransaccion(Transaccion.ID idTransaccion);
	
	/**
	 * Devuelve la transaccion de mas bajo nivel de un thread determinado. 
	 * @param thread el thread del cual se desea obtener la transaccion
	 * @return la transaccion de mas bajo nivel del thread pasado por parametro.
	 * @throws RuntimeException si no habia ninguna transaccion activa en ese thread
	 */
	Transaccion dameTransaccion(Thread thread);
	
	/**
	 * Establece el nivel de aislamiento que se va a usar en las futuras transacciones creadas por este administrador.
	 * @param aislamiento el nuevo nivel de aislamiento para las transacciones.
	 */
	void establecerAislamiento(Aislamiento aislamiento);
	
	/**
	 * Establece un identificador a partir del cual se ira incrementando la numeracion para las futuras transacciones.
	 * @param idTransaccion un identificador de transaccion.
	 */
	void establecerProximoIDTransaccion(Transaccion.ID idTransaccion);

}
