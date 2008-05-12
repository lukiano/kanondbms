package servidor.lock.impl;

import java.util.Set;

import servidor.Id;
import servidor.excepciones.ObjetoBloqueadoException;
import servidor.inspector.Inspector;
import servidor.lock.LockManager;
import servidor.log.LSN;
import servidor.transaccion.Transaccion;

/**
 * Decorador de un Lock Manager que informa en pantalla los eventos que van ocurriendo.
 */
public class InspectorLockManager implements
		LockManager {
	
	/**
	 * El Lock Manager decorado.
	 */
	private LockManager lockManager;
	
	/**
	 * El inspector que muestra los eventos.
	 */
	private Inspector inspector = new Inspector("LockManager");

	/**
	 * Constructor de la clase.
	 * @param lockManager el Lock Manager a decorar.
	 */
	public InspectorLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	/**
	 * @see servidor.lock.LockManager#bloquear(servidor.Id, boolean)
	 */
	public boolean bloquear(Id idElemento, boolean exclusivo) {
		boolean bloqueado = this.lockManager.bloquear(idElemento, exclusivo);
		if (bloqueado) {
			this.inspector.agregarEvento("bloquear", idElemento.toString(), exclusivo?"exclusivo":"compartido");
		}
		return bloqueado;
	}

	/**
	 * @see servidor.lock.LockManager#bloquearCondicional(servidor.Id, boolean)
	 */
	public boolean bloquearCondicional(Id idElemento, boolean exclusivo) throws ObjetoBloqueadoException {
		boolean bloqueado = this.lockManager.bloquearCondicional(idElemento, exclusivo);
		if (bloqueado) {
			this.inspector.agregarEvento("bloquearCondicional", idElemento.toString(), exclusivo?"exclusivo":"compartido");
		}
		return bloqueado;
	}

	/**
	 * @see servidor.lock.LockManager#desbloquear(servidor.Id)
	 */
	public void desbloquear(Id idElemento) {
		this.inspector.agregarEvento("desbloquear", idElemento.toString());
		this.lockManager.desbloquear(idElemento);
	}

	/**
	 * @see servidor.lock.LockManager#estaBloqueado(servidor.Id, boolean)
	 */
	public boolean estaBloqueado(Id idElemento, boolean exclusivo) {
		return this.lockManager.estaBloqueado(idElemento, exclusivo);
	}

	/**
	 * @see servidor.lock.LockManager#locks(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locks(Transaccion.ID idTransaccion) {
		Set<Id> locks = this.lockManager.locks(idTransaccion);
		this.inspector.agregarEvento("locks:", locks.toString());
		return locks;
	}

	/**
	 * @see servidor.lock.LockManager#locksExclusivos(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locksExclusivos(Transaccion.ID idTransaccion) {
		Set<Id> locks = this.lockManager.locksExclusivos(idTransaccion);
		this.inspector.agregarEvento("locks exclusivos:", locks.toString());
		return locks;
	}

	/**
	 * @see servidor.lock.LockManager#locksDesde(servidor.transaccion.Transaccion.ID, servidor.log.LSN)
	 */
	public Set<Id> locksDesde(Transaccion.ID idTransaccion, LSN lsn) {
		Set<Id> locks = this.lockManager.locksDesde(idTransaccion, lsn);
		this.inspector.agregarEvento("locks:", locks.toString());
		return locks;
	}

	/**
	 * @see servidor.lock.LockManager#delegarLocksATransaccionPadre()
	 */
	public void delegarLocksATransaccionPadre() {
		this.lockManager.delegarLocksATransaccionPadre();
	}

	/**
	 * @see servidor.lock.LockManager#cerrar()
	 */
	public void cerrar() {
		this.lockManager.cerrar();
	}
	
}
