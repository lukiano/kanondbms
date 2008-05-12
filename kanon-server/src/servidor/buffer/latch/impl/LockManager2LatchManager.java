package servidor.buffer.latch.impl;

import servidor.buffer.Bloque.ID;
import servidor.buffer.latch.LatchManager;
import servidor.lock.LockManager;

/**
 * Implementacion de Latch Manager que deriva los pedidos de Latch a un Lock Manager.
 */
public class LockManager2LatchManager implements LatchManager {
	
	/**
	 * El Lock Manager al cual se le derivan los pedidos.
	 */
	private LockManager lockManager;

	/**
	 * Constructor de la clase.
	 * @param lockManager el Lock Manager al cual se le derivan los pedidos.
	 */
	public LockManager2LatchManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	/**
	 * @see servidor.buffer.latch.LatchManager#latch(servidor.buffer.Bloque.ID)
	 */
	public boolean latch(ID id) {
		return this.lockManager.bloquear(id, true);
	}

	/**
	 * @see servidor.buffer.latch.LatchManager#unLatch(servidor.buffer.Bloque.ID)
	 */
	public void unLatch(ID id) {
		this.lockManager.desbloquear(id);
	}

}
