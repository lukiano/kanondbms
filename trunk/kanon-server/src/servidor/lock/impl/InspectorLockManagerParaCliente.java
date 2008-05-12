package servidor.lock.impl;

import java.util.Set;

import servidor.Id;
import servidor.excepciones.ObjetoBloqueadoException;
import servidor.inspector.Inspector;
import servidor.inspector.VisorPorConexion;
import servidor.lock.LockManager;
import servidor.log.LSN;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.Transaccion;

/**
 * Decorador de un Lock Manager que informa al cliente los eventos que van ocurriendo.
 */
public class InspectorLockManagerParaCliente implements
		LockManager {
	
	/**
	 * El Lock Manager decorado.
	 */
	private LockManager lockManager;
	
	/**
	 * El inspector que muestra los eventos.
	 */
	private Inspector inspector = new Inspector(new VisorPorConexion(4446, "LockManager"));

	/**
	 * Constructor de la clase.
	 * @param lockManager el Lock Manager a decorar.
	 */
	public InspectorLockManagerParaCliente(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	/**
	 * @see servidor.lock.LockManager#bloquear(servidor.Id, boolean)
	 */
	public boolean bloquear(Id idElemento, boolean exclusivo) {
		boolean bloqueado = this.lockManager.bloquear(idElemento, exclusivo);
		String[] elemento = this.parseElemento(idElemento);
		if (elemento != null && bloqueado) {
			Transaccion transaccion = this.dameTransaccion();
			this.inspector.agregarEvento(
					String.valueOf(transaccion.id().numeroTransaccion()),
					elemento[0],
					elemento[1],
					elemento[2],
					exclusivo?"exclusive":"shared",
					"lock");
		}
		return bloqueado;
	}

	/**
	 * Convierte un Id de un elemento en un arreglo de Strings con informacion
	 * del mismo para ser enviada al cliente.
	 * @param idElemento un Id. Soporta Id de Registro, Pagina o Tabla.
	 * @return un arreglo de Strings con informacion del Id.
	 */
	private String[] parseElemento(Id idElemento) {
		String[] strings = new String[3];
		if (idElemento instanceof Registro.ID) {
			Registro.ID idRegistro = (Registro.ID)idElemento;
			strings[0] = idRegistro.propietario().propietario().nombre();
			strings[1] = String.valueOf(idRegistro.propietario().numeroPagina());
			strings[2] = String.valueOf(idRegistro.numeroRegistro());
		} else if (idElemento instanceof Pagina.ID) {
			Pagina.ID idPagina = (Pagina.ID)idElemento;
			strings[0] = idPagina.propietario().nombre();
			strings[1] = String.valueOf(idPagina.numeroPagina());
			strings[2] = "";
		} else if (idElemento instanceof Tabla.ID) {
			Tabla.ID idTabla = (Tabla.ID)idElemento;
			strings[0] = idTabla.nombre();
			strings[1] = "";
			strings[2] = "";
		} else {
			return null;
		}
		return strings;
	}

	/**
	 * @return la transaccion actual del thread que llama a este metodo.
	 */
	private Transaccion dameTransaccion() {
		return FabricaTransactionManager.dameInstancia().dameTransaccion();
	}

	/**
	 * @see servidor.lock.LockManager#bloquearCondicional(servidor.Id, boolean)
	 */
	public boolean bloquearCondicional(Id idElemento, boolean exclusivo) throws ObjetoBloqueadoException {
		boolean bloqueado = this.lockManager.bloquearCondicional(idElemento, exclusivo);
		String[] elemento = this.parseElemento(idElemento);
		if (elemento != null && bloqueado) {
			Transaccion transaccion = this.dameTransaccion();
			this.inspector.agregarEvento(
					String.valueOf(transaccion.id().numeroTransaccion()),
					elemento[0],
					elemento[1],
					elemento[2],
					exclusivo?"exclusive":"shared",
					"lock");
		}
		return bloqueado;
	}

	/**
	 * @see servidor.lock.LockManager#desbloquear(servidor.Id)
	 */
	public void desbloquear(Id idElemento) {
		Boolean exclusivo = null;
		if (this.lockManager.estaBloqueado(idElemento, true)) {
			exclusivo = Boolean.TRUE;
		} else if (this.lockManager.estaBloqueado(idElemento, false)) {
			exclusivo = Boolean.FALSE;
		}
		if (exclusivo != null) {
			String[] elemento = this.parseElemento(idElemento);
			if (elemento != null) {
				Transaccion transaccion = this.dameTransaccion();
				this.inspector.agregarEvento(
						String.valueOf(transaccion.id().numeroTransaccion()),
						elemento[0],
						elemento[1],
						elemento[2],
						exclusivo.booleanValue()?"exclusive":"shared",
						"unlock");
			}
		}
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
		return locks;
	}

	/**
	 * @see servidor.lock.LockManager#locksExclusivos(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locksExclusivos(Transaccion.ID idTransaccion) {
		Set<Id> locks = this.lockManager.locksExclusivos(idTransaccion);
		return locks;
	}

	/**
	 * @see servidor.lock.LockManager#locksDesde(servidor.transaccion.Transaccion.ID, servidor.log.LSN)
	 */
	public Set<Id> locksDesde(Transaccion.ID idTransaccion, LSN lsn) {
		Set<Id> locks = this.lockManager.locksDesde(idTransaccion, lsn);
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
