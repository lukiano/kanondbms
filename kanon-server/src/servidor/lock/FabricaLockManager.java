/**
 * 
 */
package servidor.lock;

import servidor.lock.deadlock.FabricaPrevencionDeadLock;
import servidor.lock.deadlock.PrevencionDeadLock;
import servidor.lock.impl.InspectorLockManagerParaCliente;
import servidor.lock.impl.LockManagerImpl;

/**
 * Fabrica que devuelve una instancia unica del Administrador de Locks.
 * 
 */
public final class FabricaLockManager {
	
	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static LockManager instancia;

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaLockManager() {
		super();
	}
	
	/**
	 * Devuelve la instancia unica del Administrador de Locks.
	 * Llama a la FabricaPrevencionDeadLock para obtener el Algoritmo de prevencion correspondiente.
	 * @return la instancia unica del Administrador de Locks.
	 */
	public static synchronized LockManager dameInstancia() {
		if (instancia == null) {
			PrevencionDeadLock prevencionDeadLock = FabricaPrevencionDeadLock.dameInstancia();
			instancia = new LockManagerImpl(prevencionDeadLock);
			instancia = new InspectorLockManagerParaCliente(instancia);
			// instancia = new InspectorLockManager(instancia);
		}
		return instancia;
	}

}
