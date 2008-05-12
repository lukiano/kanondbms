package servidor.buffer.latch.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

import servidor.buffer.Bloque.ID;
import servidor.buffer.latch.Latch;
import servidor.buffer.latch.LatchManager;
import servidor.excepciones.VictimaDeadlockRuntimeException;

/**
 * Implementacion del Latch Manager. Es una version simplificada del Lock Manager.
 */
public class LatchManagerImpl implements LatchManager {
	
    /**
     * Constante con el mensaje de error para la excepcion lanzada 
     * cuando el sistema de bloqueo no actua como corresponde (no deberia pasar). 
     */
    private static final String INCONSISTENCY_SYSTEM_LOCKS = "Inconsistency in the system of locks.";

    private static final String MAXIMUM_NUMBER_ATTEMPTS = "Maximum number of attempts reached when trying to latching: ";
    
    /**
     * Aparentemente el uso de LockSupport para bloquear el thread no libera los monitores
     * que este tenga, asi que se reemplaza el uso de metodos sincronizados con un semaforo.
     */
    private Semaphore semaphore = new Semaphore(1);
    
	/**
	 * Mapa donde se guarda que Latch se encuentra aplicado sobre cada identificador.
	 * Tambien indica cuales bloques se encuentran con un Latch.
	 */
	private Map<ID, Latch > latches;
    
    /**
     * Indica para cada bloque, que threads se encuentran parados intentando obtener un
     * Latch sobre el mismo. Estos van siendo encolados segun una politica FIFO.
     */
    private Map<ID, Queue<Thread> > threadsEncolados;
    
	/**
	 * Para cada thread, indica cuales Latches tiene en su poder.
	 */
	private ThreadLocal<Map<ID, Latch> > localThreadLatches;
    
    /**
     * Constante con la cantidad de veces a intentar obtener un Latch sobre un bloque.
     */
    private static final int MAX_ITERACIONES = 100;
    
	/**
	 * Constructor de la clase. Inicializa los mapas.
	 */
	public LatchManagerImpl() {
		this.latches = new HashMap<ID, Latch>();
        this.threadsEncolados = new HashMap<ID, Queue<Thread>>();
		this.localThreadLatches = new ThreadLocal<Map<ID, Latch>>();
	}

	/**
	 * @see servidor.buffer.latch.LatchManager#latch(servidor.buffer.Bloque.ID)
	 */
	public boolean latch(ID idElemento) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
	        if (Thread.currentThread().isInterrupted()) {
	            // el thread habia sido marcado como victima
	            // => no puede bloquear mas objetos. Ademas tiene que abortar.
                throw new VictimaDeadlockRuntimeException();
	        }
	        boolean nuevoLatch = false;
			if (!this.estaBloqueadoPorMiThread(idElemento)) {
				// se encola el pedido de bloqueo.
				this.esperarYBloquear(idElemento);
				nuevoLatch = true;
			}
			return nuevoLatch;
        } finally {
        	this.semaphore.release();
        }
	}

	/**
	 * Si existen pedidos de bloqueo sobre el elemento, este nuevo pedido
	 * se encola en una manera FIFO. Si no existe ninguna cola, se bloquea
	 * el elemento.
	 * @param idElemento el identificador del bloque a obtener un Latch.
	 */
	private void esperarYBloquear(ID idElemento) {
		Queue<Thread> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
        if (colaThreadsEnEspera == null || colaThreadsEnEspera.isEmpty()) {
            // no hay threads en la cola de espera
            Latch latch = this.latches.get(idElemento);
            if (latch == null) {
                // el objeto no se encuentra bloqueado
                this.bloqueoEfectivo(idElemento);
            } else {
                // el objeto se encuentra bloqueado
                if (colaThreadsEnEspera == null) {
                    // crear una nueva cola pues no existía
                    colaThreadsEnEspera = new LinkedList<Thread>();
                    this.threadsEncolados.put(idElemento, colaThreadsEnEspera);
                }
                this.encolarYBloquear(idElemento);
            }
        } else {
            // hay threads en la cola de espera => encolar
            this.encolarYBloquear(idElemento);
        }
	}

    /**
     * Metodo que encola el pedido y suspende el thread hasta que el mismo se encuentre
     * al tope de la cola. Dicho thread puede ser despertado si fue elegido como
     * victima del algoritmo de prevencion de DeadLock del Lock Manager.
     * Puede ser que cuando se despierte el thread no se puede obtener el Latch, y entonces
     * se vuelve a dormir. Esto se repite una cantidad maxima de veces.
     * @param idElemento el identificador del bloque a obtener un Latch.
     */
    private void encolarYBloquear(ID idElemento) {
    	Queue<Thread> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
        // la primera vez hay que agregar al final de la cola.
        // Para la proxima ya estara en la cabeza y se quedara ahi.
		colaThreadsEnEspera.offer(Thread.currentThread());        // se encola el pedido de latch
        for (int iteraciones = 0; iteraciones < MAX_ITERACIONES; iteraciones++) {
            this.semaphore.release();
            LockSupport.park();
            try {
                this.semaphore.acquire();
            } catch (InterruptedException e) {
                // el thread fue marcado como victima
                colaThreadsEnEspera.remove();
                if (colaThreadsEnEspera.isEmpty()) {
                    this.threadsEncolados.remove(idElemento);
                }
                throw new VictimaDeadlockRuntimeException();
            }
            if (Thread.currentThread().isInterrupted()) {
                // el thread fue marcado como victima
                colaThreadsEnEspera.remove();
                if (colaThreadsEnEspera.isEmpty()) {
                    this.threadsEncolados.remove(idElemento);
                }
                throw new VictimaDeadlockRuntimeException();
            }
            if (!colaThreadsEnEspera.element().equals(Thread.currentThread())) {
                colaThreadsEnEspera.remove();
                if (colaThreadsEnEspera.isEmpty()) {
                    this.threadsEncolados.remove(idElemento);
                }
                throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
            }
            Latch latch = this.latches.get(idElemento);
            if (latch == null) {
                // el objeto no se encuentra bloqueado
                colaThreadsEnEspera.remove();
                if (colaThreadsEnEspera.isEmpty()) {
                    this.threadsEncolados.remove(idElemento);
                }
                // se bloquea el objeto
                this.bloqueoEfectivo(idElemento);
                return;
            }
        }
        // se ha llegado al maximo de iteraciones sin poder latchear al objeto
        throw new RuntimeException(MAXIMUM_NUMBER_ATTEMPTS + idElemento);
    }

    /**
     * Metodo llamado cuando ya es factible obtener el Latch sobre un bloque.
     * Se crea el Latch y se guarda en los mapas correspondientes.
     * @param idElemento el identificador del bloque a obtener un Latch.
     */
    private void bloqueoEfectivo(ID idElemento) {
        Latch latch = new LatchImpl(Thread.currentThread(), idElemento); // se crea un nuevo Latch
        
		this.latches.put(idElemento, latch); // se guarda el latch en el mapa global
		
		// se guarda el latch en el mapa local
		Map<ID, Latch> conjuntoLocalLatches = this.localThreadLatches.get();
		if (conjuntoLocalLatches == null) {
			conjuntoLocalLatches = new HashMap<ID, Latch>(4); // no se suelen usar mas 3 de latches a la vez. Por las dudas uno mas.
			this.localThreadLatches.set(conjuntoLocalLatches);
		}
		conjuntoLocalLatches.put(idElemento, latch);
    }
    
	/**
	 * @see servidor.buffer.latch.LatchManager#unLatch(servidor.buffer.Bloque.ID)
	 */
	public void unLatch(ID idElemento) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
            Map<ID, Latch> conjuntoLocalLatches = this.localThreadLatches.get();
            if (conjuntoLocalLatches != null) {
                Latch latchLocal = conjuntoLocalLatches.remove(idElemento);
                if (conjuntoLocalLatches.isEmpty()) {
                	this.localThreadLatches.remove();
                }
                if (latchLocal != null) {
                    // se obtiene el latch del elemento asociado.
                    Latch latchGlobal = this.latches.get(idElemento);
                    if (latchGlobal == null) {
                        // el elemento no estaba latcheado
                        throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
                    }
                }
                
                // el latch se remueve
                this.latches.remove(idElemento);
                
                Queue<Thread> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
                // se desencola el proximo pedido de latch, en caso de haber.
                if (colaThreadsEnEspera != null) {
                    Thread thread = colaThreadsEnEspera.peek();
                    if (thread != null) {
                        LockSupport.unpark(thread);
                    }
                }
            } else {
                // el conjunto de latch locales estaba vacio => elemento latcheado por otro thread
                //throw new IllegalStateException("se quiere desbloquear un elemento no bloqueado por este thread");
            }
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * Metodo para saber si un elemento ya se encuentra bloqueado por el thread actual.
	 * @param idElemento el identificador del bloque.
	 * @return true si el elemento ya se encontraba bloqueado, false en caso contrario.
	 */
	private boolean estaBloqueadoPorMiThread(ID idElemento) {
		Map<ID, Latch> conjuntoLocalLatches = this.localThreadLatches.get();
		return (conjuntoLocalLatches != null) && conjuntoLocalLatches.containsKey(idElemento);
	}
	
}
