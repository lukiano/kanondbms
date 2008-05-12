package servidor.lock.impl;

import java.util.Set;

import servidor.Id;
import servidor.lock.LockManager;
import servidor.log.LSN;
import servidor.transaccion.Transaccion;

/**
 * Implementacion del Administrador de Locks que no hace nada.
 */
public class DummyLockManager implements LockManager {

    /**
     * @see servidor.lock.LockManager#bloquear(servidor.Id, boolean)
     */
    public boolean bloquear(Id idElemento, boolean exclusivo) {
        return true;
    }

    /**
     * @see servidor.lock.LockManager#bloquearCondicional(servidor.Id, boolean)
     */
    public boolean bloquearCondicional(Id idElemento, boolean exclusivo) {
        return true;
    }

    /**
     * @see servidor.lock.LockManager#desbloquear(servidor.Id)
     */
    public void desbloquear(Id idElemento) {
    }

    /**
     * @see servidor.lock.LockManager#estaBloqueado(servidor.Id, boolean)
     */
    public boolean estaBloqueado(Id idElemento, boolean exclusivo) {
        return false;
    }

	/**
	 * @see servidor.lock.LockManager#locks(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locks(Transaccion.ID idTransaccion) {
		return java.util.Collections.emptySet();
	}

	/**
	 * @see servidor.lock.LockManager#locksExclusivos(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locksExclusivos(Transaccion.ID idTransaccion) {
		return java.util.Collections.emptySet();
	}

	
	/**
	 * @see servidor.lock.LockManager#locksDesde(servidor.transaccion.Transaccion.ID, servidor.log.LSN)
	 */
	public Set<Id> locksDesde(Transaccion.ID idTransaccion, LSN lsn) {
		return this.locks(idTransaccion);
	}

	/**
	 * @see servidor.lock.LockManager#delegarLocksATransaccionPadre()
	 */
	public void delegarLocksATransaccionPadre() {
	}
	
	/**
	 * @see servidor.lock.LockManager#cerrar()
	 */
	public void cerrar() {
	}
}
