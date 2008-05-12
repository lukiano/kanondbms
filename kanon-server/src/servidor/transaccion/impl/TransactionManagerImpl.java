package servidor.transaccion.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import servidor.Id;
import servidor.lock.FabricaLockManager;
import servidor.lock.LockManager;
import servidor.log.FabricaRecoveryManager;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.RecoveryManager;
import servidor.transaccion.Aislamiento;
import servidor.transaccion.Estado;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;
import servidor.transaccion.Transaccion.ID;

public class TransactionManagerImpl implements TransactionManager {
	
	/**
	 * Mensaje de error que indica que no hay ninguna transaccion activa en el thread.
	 */
	private static final String NO_TX = "No active transaction exists.";

	/**
	 * Mensaje de error que indica que no hay ninguna transaccion activa en el thread para confirmar.
	 */
	private static final String NO_TX_TO_COMMIT = "There is no active transaction to confirm.";

	/**
	 * Mensaje de error que indica que no hay ninguna transaccion activa en el thread para abortar.
	 */
	private static final String NO_TX_TO_ROLLBACK = "There is no active transaction to abort.";

	/**
	 * Variable con la implementacion del Log del motor.
	 */
	private Log log;
	
    /**
     * Variable con el Administrador de Locks.
     */
    private LockManager lockManager;
    
    /**
     * Variable con el Administrador de Recuperacion.
     */
    private RecoveryManager recoveryManager;
    
    /**
     * Contador incremental para proveer de unicidad a los identificadores de transaccion.
     */
    private AtomicInteger idActual;
    
    /**
     * Variable con el aislamiento que se crearan las nuevas transacciones de alto nivel.
     */
    private Aislamiento aislamientoActual;
    
	/**
	 * Mapa que guarda en una lista encadenada a las transacciones de un thread, ordenadas de alto nivel a la mas anidada.
	 */
    // linked list para soporte de transacciones anidadas
	private Map<Thread, LinkedList<Transaccion> > transaccionDelThread;
	
	/**
	 * Mapa que relaciona a las transacciones con su identificador para una obtencion rapida de las mismas.
	 */
	private Map<Transaccion.ID, Transaccion > transacciones;
	
	/**
	 * Constructor de la clase. Inicializa las estructuras.
	 * El aislamiento por omision es READ_COMMITTED.
	 * @param log el Log donde se registraran los eventos de Commit, Fin y Rollback de las transacciones.
	 * @see Aislamiento#READ_COMMITTED
	 */
	public TransactionManagerImpl(Log log) {
		this.log = log;
		this.aislamientoActual = Aislamiento.READ_COMMITTED;
		this.transaccionDelThread = new ConcurrentHashMap<Thread, LinkedList<Transaccion> >();
		this.transacciones = new ConcurrentHashMap<Transaccion.ID, Transaccion>();
	}


	/**
	 * @see servidor.transaccion.TransactionManager#iniciarTransaccion()
	 */
	public void iniciarTransaccion() {
		Thread thread = Thread.currentThread();
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		if (listaDeTransaccionesEnCurso == null) {
			listaDeTransaccionesEnCurso = new LinkedList<Transaccion>();
			this.transaccionDelThread.put(thread, listaDeTransaccionesEnCurso);
			Transaccion transaccion = new TransaccionImpl(
					this.dameNuevoID(),
					thread,
					this.aislamientoActual);
			this.transacciones.put(transaccion.id(), transaccion);
			listaDeTransaccionesEnCurso.addLast(transaccion);
		} else {
			Transaccion transaccion = new TransaccionImpl(
					this.dameNuevoID(),
					listaDeTransaccionesEnCurso.getLast());
			this.transacciones.put(transaccion.id(), transaccion);
			listaDeTransaccionesEnCurso.addLast(transaccion);
		}
	}
	
	/**
	 * @return un nuevo numero unico a partir de un contador incremental.
	 */
	private int dameNuevoID() {
		if (this.idActual == null) {
			throw new RuntimeException("The recovery of the system was not verified.");
		}
		return this.idActual.incrementAndGet();
	}

	/**
	 * @see servidor.transaccion.TransactionManager#commitTransaccion()
	 */
	public void commitTransaccion() {
		Thread thread = Thread.currentThread();
		if (!this.transaccionDelThread.containsKey(thread)) {
			throw new RuntimeException(NO_TX_TO_COMMIT);
		}
		
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		
		if (listaDeTransaccionesEnCurso.size() == 1) {
			// unica transaccion en el thread
			Transaccion transaccion = listaDeTransaccionesEnCurso.getLast();
			LSN prepareLSN = this.log.escribirPrepareTransaccion(transaccion);
			transaccion.establecerUltimoLSN(prepareLSN);
			transaccion.establecerUndoNextLSN(prepareLSN);
			
			// si hay una sola, la principal, se liberan los locks
			Set<Id> locksObtenidos = this.getLockManager().locks(transaccion.id());
			for (Id idBloqueado : locksObtenidos) {
				this.getLockManager().desbloquear(idBloqueado);
			}
			
			LSN finLSN = this.log.escribirFinTransaccion(transaccion);
			transaccion.establecerUltimoLSN(finLSN);
			transaccion.establecerUndoNextLSN(finLSN);
			
			this.transacciones.remove(transaccion.id());
			this.transaccionDelThread.remove(thread);
			
		} else {
			// es una transaccion hija
			Transaccion transaccion = listaDeTransaccionesEnCurso.getLast();
			Transaccion padre = transaccion.padre();
			
			LSN ccLSN = this.log.escribirChildCommittedTransaccion(padre, transaccion);
			padre.establecerUltimoLSN(ccLSN);
			padre.establecerUndoNextLSN(ccLSN);

			// actualizar los locks del hijo al padre
			this.getLockManager().delegarLocksATransaccionPadre();

			// ahora se puede borrar la transaccion hija
			this.transacciones.remove(transaccion.id());
			listaDeTransaccionesEnCurso.removeLast();
		}
	}
	
	/**
	 * @see servidor.transaccion.TransactionManager#abortarUltimaTransaccionHasta(java.lang.String)
	 */
	public void abortarUltimaTransaccionHasta(String nombreSavepoint) {
		Thread thread = Thread.currentThread();
		if (!this.transaccionDelThread.containsKey(thread)) {
			throw new RuntimeException(NO_TX_TO_ROLLBACK);
		}
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		
		// se aborta hasta el Savepoint definido. Si no existe se rollbackea hasta el principio
		Transaccion transaccion = listaDeTransaccionesEnCurso.getLast();
		LSN saveLSN = transaccion.dameSavepoint(nombreSavepoint); 
		this.getRecoveryManager().rollback(transaccion, saveLSN);
		
		Set<Id> locksObtenidos = this.getLockManager().locksDesde(transaccion.id(), saveLSN);
		for (Id idBloqueado : locksObtenidos) {
			this.getLockManager().desbloquear(idBloqueado);
		}
		
		// no se borra ningun mapa ni se escribe fin de transaccion (sigue activa)
	}

	/**
	 * @see servidor.transaccion.TransactionManager#abortarUltimaTransaccion()
	 */
	public void abortarUltimaTransaccion() {
		Thread thread = Thread.currentThread();
		if (!this.transaccionDelThread.containsKey(thread)) {
			throw new RuntimeException(NO_TX_TO_ROLLBACK);
		}
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		
		// se aborta la ultima transaccion
		Transaccion transaccion = listaDeTransaccionesEnCurso.getLast();
		try {
			this.getRecoveryManager().rollback(transaccion);
		} finally {
			Set<Id> locksObtenidos = this.getLockManager().locks(transaccion.id());
			for (Id idBloqueado : locksObtenidos) {
				this.getLockManager().desbloquear(idBloqueado);
			}
			// recien ahora se pueden borrar los mapas
			listaDeTransaccionesEnCurso.removeLast();
			if (listaDeTransaccionesEnCurso.isEmpty()) {
				this.transaccionDelThread.remove(thread);
			}
			this.transacciones.remove(transaccion.id());
		}
		LSN finLSN = this.log.escribirFinTransaccion(transaccion);
		transaccion.establecerUltimoLSN(finLSN);
		transaccion.establecerUndoNextLSN(finLSN);

	}
	
	/**
	 * @see servidor.transaccion.TransactionManager#abortarTransaccionesDelThread()
	 */
	public void abortarTransaccionesDelThread() {
		Thread thread = Thread.currentThread();
		if (!this.transaccionDelThread.containsKey(thread)) {
			throw new RuntimeException(NO_TX_TO_ROLLBACK);
		}
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		
		// se abortan todas las transacciones
		while (!this.estadoActual().equals(Estado.NINGUNA)) {
			Transaccion transaccion = listaDeTransaccionesEnCurso.getLast();
			try {
				this.getRecoveryManager().rollback(transaccion);
			} finally {
				Set<Id> locksObtenidos = this.getLockManager().locks(transaccion.id());
				for (Id idBloqueado : locksObtenidos) {
					this.getLockManager().desbloquear(idBloqueado);
				}
				// recien ahora se pueden borrar los mapas
				listaDeTransaccionesEnCurso.removeLast();
				if (listaDeTransaccionesEnCurso.isEmpty()) {
					this.transaccionDelThread.remove(thread);
				}
				this.transacciones.remove(transaccion.id());
			}
			LSN finLSN = this.log.escribirFinTransaccion(transaccion);
			transaccion.establecerUltimoLSN(finLSN);
			transaccion.establecerUndoNextLSN(finLSN);

		}
	}

	/**
	 * @see servidor.transaccion.TransactionManager#abortarTransaccionesDelThreadSinRollback()
	 */
	public void abortarTransaccionesDelThreadSinRollback() {
		Thread thread = Thread.currentThread();
		if (!this.transaccionDelThread.containsKey(thread)) {
			throw new RuntimeException(NO_TX_TO_ROLLBACK);
		}
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		
		// se abortan todas las transacciones
		while (!this.estadoActual().equals(Estado.NINGUNA)) {
			Transaccion transaccion = listaDeTransaccionesEnCurso.getLast();
			
			Set<Id> locksObtenidos = this.getLockManager().locks(transaccion.id());
			for (Id idBloqueado : locksObtenidos) {
				this.getLockManager().desbloquear(idBloqueado);
			}
			
			// recien ahora se pueden borrar los mapas
			listaDeTransaccionesEnCurso.removeLast();
			if (listaDeTransaccionesEnCurso.isEmpty()) {
				this.transaccionDelThread.remove(thread);
			}
			this.transacciones.remove(transaccion.id());
			
		}
	}

	/**
	 * @see servidor.transaccion.TransactionManager#estadoActual()
	 */
	public Estado estadoActual() {
		Thread thread = Thread.currentThread();
		if (this.transaccionDelThread.containsKey(thread)) {
			LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
				this.transaccionDelThread.get(thread);
			return listaDeTransaccionesEnCurso.getLast().estado();
		} else {
			return Estado.NINGUNA ;
		}
	}

	/**
	 * @see servidor.transaccion.TransactionManager#dameTransaccion()
	 */
	public Transaccion dameTransaccion() {
		return this.dameTransaccion(Thread.currentThread());
	}
	
	/**
	 * @see servidor.transaccion.TransactionManager#dameTransaccion(java.lang.Thread)
	 */
	public Transaccion dameTransaccion(Thread thread) {
		LinkedList<Transaccion> listaDeTransaccionesEnCurso = 
			this.transaccionDelThread.get(thread);
		if (listaDeTransaccionesEnCurso == null) {
			throw new RuntimeException(NO_TX);
		}
		return listaDeTransaccionesEnCurso.getLast();
	}

	/**
	 * @see servidor.transaccion.TransactionManager#dameTransaccion(servidor.transaccion.Transaccion.ID)
	 */
	public Transaccion dameTransaccion(Transaccion.ID idTransaccion) {
		return this.transacciones.get(idTransaccion);
	}

	/**
	 * @return es Administador de Lock utilizado para liberar los locks de las transacciones.
	 */
	private LockManager getLockManager() {
		if (this.lockManager == null) {
			this.lockManager = FabricaLockManager.dameInstancia();
		}
        return this.lockManager;
    }

	/**
	 * @return el Administrador de Recuperacion utilizado para el rollback.
	 */
	private RecoveryManager getRecoveryManager() {
		if (this.recoveryManager == null) {
			this.recoveryManager = FabricaRecoveryManager.dameInstancia();
		}
        return this.recoveryManager;
    }

	/**
	 * @see servidor.transaccion.TransactionManager#dameTransacciones()
	 */
	public Set<Transaccion> dameTransacciones() {
		return new HashSet<Transaccion>(this.transacciones.values());
	}

	/**
	 * @see servidor.transaccion.TransactionManager#establecerAislamiento(servidor.transaccion.Aislamiento)
	 */
	public void establecerAislamiento(Aislamiento aislamiento) {
		this.aislamientoActual = aislamiento;
	}


	/**
	 * @see servidor.transaccion.TransactionManager#establecerProximoIDTransaccion(servidor.transaccion.Transaccion.ID)
	 */
	public void establecerProximoIDTransaccion(ID idTransaccion) {
		this.idActual = new AtomicInteger(idTransaccion.numeroTransaccion());
	}
	
}
