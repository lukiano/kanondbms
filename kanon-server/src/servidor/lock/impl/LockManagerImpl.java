package servidor.lock.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

import servidor.Id;
import servidor.excepciones.ObjetoBloqueadoException;
import servidor.excepciones.VictimaDeadlockRuntimeException;
import servidor.lock.Lock;
import servidor.lock.LockManager;
import servidor.lock.deadlock.PrevencionDeadLock;
import servidor.log.LSN;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;

public class LockManagerImpl implements LockManager {
	
	/**
	 * Constante con el mensaje de error para la excepcion lanzada 
	 * cuando el sistema de bloqueo no actua como corresponde (no deberia pasar). 
	 */
    private static final String INCONSISTENCY_SYSTEM_LOCKS = "Inconsistency in the system of locks.";

	/**
	 * Estructura que guarda un pedido de bloqueo el cual es encolado hasta que el lock actual sea liberado.
	 */
	private static final class PedidoEncolado {
		
		/**
		 * El Id de la Transaccion que pide el bloqueo.
		 */
		public Transaccion.ID idTransaccion;
		
		/**
		 * El thread al cual pertenece la transaccion que pide el bloqueo.
		 */
		public Thread thread;
		
		/**
		 * Variable que indica si el bloqueo es exclusivo.
		 */
		public boolean exclusivo;
	}
	
    /**
     * Aparentemente el uso de LockSupport para bloquear el thread no libera los monitores
     * que este tenga, asi que se reemplaza el uso de metodos sincronizados con un semaforo.
     */
    private Semaphore semaphore = new Semaphore(1);
    
    /**
     * El Algoritmo de Prevencion de Deadlock utilizado por este administrador.
     */
    private PrevencionDeadLock prevencionDeadLock;
    
	/**
	 * Mapa que guarda el conjunto de locks tomados para cada elemento.
	 * Puede haber varios locks compartidos pero uno solo exclusivo.
	 */
	private Map<Id, Set<Lock> > locks;
    
    /**
     * Mapa que guarda la cola de pedidos de bloqueo para cada elemento.
     */
    private Map<Id, LinkedList<PedidoEncolado> > threadsEncolados;
    
	/**
	 * Mapa que guarda que elementos tiene bloqueado cada transaccion 
	 * y que clase de lock posee sobre los mismos.
	 */
	private Map<Transaccion.ID, Map<Id, Lock> > transaccionLocks;
    
    /**
     * Constante con la cantidad de veces que se va a encolar un pedido de bloqueo.
     */
    private static final int MAX_ITERACIONES = 100;
    
    /**
     * Conjunto con los threads suspendidos. Es usado para liberarlos cuando
     * se cierra el administrador.
     */
    private Set<Thread> threadsBloqueados;
    
	/**
	 * Constructor de la clase. Inicializa las estructuras.
	 * @param prevencionDeadLock el Algoritmo de Prevencion de Deadlock elegido.
	 */
	public LockManagerImpl(PrevencionDeadLock prevencionDeadLock) {
		this.locks = new HashMap<Id, Set<Lock>>();
        this.threadsEncolados = new HashMap<Id, LinkedList<PedidoEncolado>>();
		this.transaccionLocks = new HashMap<Transaccion.ID, Map<Id, Lock>>();
		this.threadsBloqueados = new HashSet<Thread>();
        this.prevencionDeadLock = prevencionDeadLock;
	}

	/**
	 * @see servidor.lock.LockManager#bloquear(servidor.Id, boolean)
	 */
	public boolean bloquear(Id idElemento, boolean exclusivo) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
	        if (this.dameTransaccionActual().threadPropietario().isInterrupted()) {
	            // el thread habia sido marcado como victima
	            // => no puede bloquear mas objetos. Ademas tiene que abortar.
                throw new VictimaDeadlockRuntimeException();
	        }
			if (this.estaBloqueadoPorMiThread(idElemento)) {
				return exclusivo && this.actualizarBloqueo(idElemento);
			} else {
				// el objeto no se encuentra bloqueado por mi thread.
				this.intentarBloquear(idElemento, exclusivo, false);
				return true;
			}
        } finally {
        	this.semaphore.release();
        }
	}

	/**
	 * @see servidor.lock.LockManager#bloquearCondicional(servidor.Id, boolean)
	 */
	public boolean bloquearCondicional(Id idElemento, boolean exclusivo) throws ObjetoBloqueadoException {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
	        if (this.dameTransaccionActual().threadPropietario().isInterrupted()) {
	            // el thread habia sido marcado como victima
	            // => no puede bloquear mas objetos. Ademas tiene que abortar.
                throw new VictimaDeadlockRuntimeException();
	        }
			if (this.estaBloqueadoPorMiThread(idElemento)) {
				return exclusivo && this.actualizarBloqueo(idElemento);
			} else {
				// el objeto no se encuentra bloqueado por mi thread.
				this.intentarBloquearCondicional(idElemento, exclusivo);
				return true;
			}
        } finally {
        	this.semaphore.release();
        }
	}

	/**
	 * Intenta bloquear un elemento, y en caso de no poder, encola el pedido de bloqueo
	 * y suspende el thread.
	 * @param idElemento el Id del elemento que se desea bloquear.
	 * @param exclusivo true si se desea bloquear al objeto de manera exclusiva.
	 * @param actualizacion true si es un pedido de actualizacion de bloqueo existente.
	 * @see #encolarEIntentarBloquear(Id, boolean, boolean)
	 * @see #realizarBloqueo(Id, boolean)
	 */
	private void intentarBloquear(Id idElemento, boolean exclusivo, boolean actualizacion) {
        LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
        if (colaThreadsEnEspera == null || colaThreadsEnEspera.isEmpty()) {
            // no hay threads en la cola de espera
            Set<Lock> conjuntoLocks = this.locks.get(idElemento);
            if (conjuntoLocks != null && !conjuntoLocks.isEmpty()) {
                // el objeto se encuentra bloqueado
                if (this.hayLockExclusivo(conjuntoLocks)) {
                    //Ya existe un lock exclusivo sobre el objeto (y si llegue aca no es mio) => hay que esperar => encolar
                    if (colaThreadsEnEspera == null) {
                        // crear una nueva cola pues no existía
                        colaThreadsEnEspera = new LinkedList<PedidoEncolado>();
                        this.threadsEncolados.put(idElemento, colaThreadsEnEspera);
                    }
                    this.encolarEIntentarBloquear(idElemento, exclusivo, actualizacion);
                } else if (exclusivo) { // quiero bloquear de manera exclusiva
                	// el objeto se encuentra bloqueado en modo compartido
                	// Verifico si el lock es de mi transaccion o algun ancestro y no hay ningun otro lock.
                	Transaccion txActual = this.getTransactionManager().dameTransaccion();
                	if (this.sonTodosDeLaTransaccion(txActual, conjuntoLocks)) {
                		// es de mi transaccion (hay uno solo)
                        this.realizarBloqueo(idElemento, exclusivo);
                	} else if (this.sonTodosDelThread(txActual.threadPropietario(), conjuntoLocks)) {
                		// son de mi thread (de alguna/s transaccion/es ancestra/s)
                        this.realizarBloqueo(idElemento, exclusivo);
                	} else {
                		// es de otra transaccion => hay que esperar => encolar
                        if (colaThreadsEnEspera == null) {
                            // crear una nueva cola pues no existía
                            colaThreadsEnEspera = new LinkedList<PedidoEncolado>();
                            this.threadsEncolados.put(idElemento, colaThreadsEnEspera);
                        }
                        this.encolarEIntentarBloquear(idElemento, exclusivo, actualizacion);
                	}
                	
                } else {
                    // el objeto se encuentra bloqueado en modo compartido
                    // y el nuevo lock tambien es compartido
                    this.realizarBloqueo(idElemento, exclusivo);
                }
            } else {
                // el objeto no se encuentra bloqueado
                this.realizarBloqueo(idElemento, exclusivo);
            }
        } else {
            // hay threads en la cola de espera => encolar
            this.encolarEIntentarBloquear(idElemento, exclusivo, actualizacion);
        }
	}

	/**
	 * Intenta bloquear un elemento, pero no encola el pedido (y suspende el thread) en caso de fallar el intento. 
	 * @param idElemento el Id del elemento que se desea bloquear.
	 * @param exclusivo true si se desea bloquear al objeto de manera exclusiva.
	 * @throws ObjetoBloqueadoException si el objeto se encontraba bloqueado por una transaccion de otro thread.
	 * @see #realizarBloqueo(Id, boolean) 
	 */
	private void intentarBloquearCondicional(Id idElemento, boolean exclusivo) throws ObjetoBloqueadoException {
        LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
        if (colaThreadsEnEspera == null || colaThreadsEnEspera.isEmpty()) {
            // no hay threads en la cola de espera
            Set<Lock> conjuntoLocks = this.locks.get(idElemento);
            if (conjuntoLocks != null && !conjuntoLocks.isEmpty()) {
                // el objeto se encuentra bloqueado
                if (exclusivo || this.hayLockExclusivo(conjuntoLocks)) {
                    // uno de los 2 locks es exclusivo => exception
                	throw new ObjetoBloqueadoException();
                } else {
                    // el objeto se encuentra bloqueado en modo compartido
                    // y el nuevo lock tambien es compartido
                    this.realizarBloqueo(idElemento, exclusivo);
                }
            } else {
                // el objeto no se encuentra bloqueado
                this.realizarBloqueo(idElemento, exclusivo);
            }
        } else {
            // hay threads en la cola de espera => exception
        	throw new ObjetoBloqueadoException();
        }
	}

    /**
     * Encola el pedido de bloqueo y suspende el thread. 
     * Cuando se despierte se comprobara si se puede realizar el bloqueo y se hara en caso positivo
     * y en caso negativo se volvera a encolar el pedido (esto se repite una cantidad fija de veces).
     * Si es un pedido de actualizacion de bloqueo compartido a exclusivo, el pedido toma prioridad 
     * (se mete en la cabeza de la cola).
     * @param idElemento el Id del elemento que se desea bloquear.
     * @param exclusivo true si es un pedido de bloqueo exclusivo.
     * @param actualizacion true si es un pedido de actualizacion de bloqueo existente.
     * @see #MAX_ITERACIONES
     * @see #comprobarSiSePuedeBloquear(servidor.lock.impl.LockManagerImpl.PedidoEncolado, Id, boolean)
     * @see #realizarBloqueo(Id, boolean)
     */
    private void encolarEIntentarBloquear(Id idElemento, boolean exclusivo, boolean actualizacion) {
        this.comprobarDeadLock(idElemento);
        LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
        
        // la primera vez hay que agregar al final de la cola.
        // Para la próxima ya estará en la cabeza y se quedará ahí.
    	PedidoEncolado pedidoEncolado = new PedidoEncolado();
    	pedidoEncolado.thread = this.dameTransaccionActual().threadPropietario();
    	pedidoEncolado.idTransaccion = this.getTransactionManager().dameTransaccion().id();
    	pedidoEncolado.exclusivo = exclusivo;
    	if (actualizacion) {
    		// tiene prioridad cuando es actualizacion de compartido a exclusivo
    		colaThreadsEnEspera.addFirst(pedidoEncolado);        // se guarda el lock en el mapa global
    	} else {
    		colaThreadsEnEspera.addLast(pedidoEncolado);        // se guarda el lock en el mapa global
    	}
    	
        for (int iteraciones = 0; iteraciones < MAX_ITERACIONES; iteraciones++) {
            this.semaphore.release();
            // XXX: deberia suspender el thread de la transaccion actual, que podria ser distinto al thread actual.
            this.threadsBloqueados.add(Thread.currentThread());
            LockSupport.park();
            // el thread fue despertado
            if (this.comprobarSiSePuedeBloquear(colaThreadsEnEspera.element(), idElemento, exclusivo)) {
                this.realizarBloqueo(idElemento, exclusivo);
                return;
            }
        }
        // se ha llegado al maximo de iteraciones sin poder bloquear al objeto
        throw new RuntimeException("Numero maximo de reintentos alcanzados al intentar bloquear " + idElemento);
    }

	/**
	 * Comprueba si el primer pedido en la cola corresponde a la transaccion actual
	 * y si el objeto se encuentra desbloquedo para poder bloquearlo
	 * @param primerPedidoDeLaCola datos del pedido que encabeza la cola de pedidos para el elemento.
	 * @param idElemento el Id del elemento que se desea bloquear.
	 * @param exclusivo true si es un pedido de bloqueo exclusivo.
	 * @return true si los requisitos se cumplen y el objeto puede ser bloqueado.
	 * @see #removerPrimerElementoDeLaCola(Id)
	 */
	private boolean comprobarSiSePuedeBloquear(PedidoEncolado primerPedidoDeLaCola, Id idElemento, boolean exclusivo) {
	    try {
	        this.semaphore.acquire();
	        this.threadsBloqueados.remove(Thread.currentThread());
	    } catch (InterruptedException e) {
	        // el thread fue marcado como victima
	    	this.threadsBloqueados.remove(Thread.currentThread());
	        this.removerPrimerElementoDeLaCola(idElemento);
	        throw new VictimaDeadlockRuntimeException();
	    }
	    if (this.dameTransaccionActual().threadPropietario().isInterrupted()) {
	        // el thread fue marcado como victima
	    	this.removerPrimerElementoDeLaCola(idElemento);
	        throw new VictimaDeadlockRuntimeException();
	    }
	    if (!primerPedidoDeLaCola.idTransaccion.equals(this.dameTransaccionActual().id())) {
	    	// estaba primero pero llego un pedido mas importante (ej, actualizacion de shared a exclusive) => hay que esperar => encolar
	    	return false;
	    }
	    Set<Lock> conjuntoLocks = this.locks.get(idElemento);
	    if (conjuntoLocks != null && !conjuntoLocks.isEmpty()) {
	        // el objeto se encuentra bloqueado
	        if (this.hayLockExclusivo(conjuntoLocks)) {
	            //Ya existe un lock exclusivo sobre el objeto (y si llegue aca no es mio) => hay que esperar => encolar
	        	return false;
	        } else if (exclusivo) {
	        	// el objeto se encuentra bloqueado en modo compartido
	        	// Verifico si el lock es de mi transaccion o algun ancestro y no hay ningun otro lock.
	        	Transaccion txActual = this.getTransactionManager().dameTransaccion();
	        	if (this.sonTodosDeLaTransaccion(txActual, conjuntoLocks)) {
	        		// es de mi transaccion (deberia llegar aca?)
	        		this.removerPrimerElementoDeLaCola(idElemento);
	                return true;
	        	} else if (this.sonTodosDelThread(txActual.threadPropietario(), conjuntoLocks)) {
	        		// son de mi thread (de alguna/s transaccion/es ancestra/s)
	        		this.removerPrimerElementoDeLaCola(idElemento);
	                return true;
	        	} else {
	        		// son de otra/s transaccion/es => hay que esperar => encolar
	        		return false;
	        	}
	        } else {
	            // el objeto se encuentra bloqueado en modo compartido
	            // y el nuevo lock tambien es compartido
	        	this.removerPrimerElementoDeLaCola(idElemento);
	            return true;
	        }
	    } else {
	        // el objeto no se encuentra bloqueado
	    	this.removerPrimerElementoDeLaCola(idElemento);
	        return true;
	    }
	}

	/**
	 * Remueve la cabeza de la cola de pedidos encolados para un determinado elemento.
	 * @param idElemento el Id de un elemento.
	 */
	private void removerPrimerElementoDeLaCola(Id idElemento) {
		LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
		if (colaThreadsEnEspera != null) {
			colaThreadsEnEspera.remove(); // se remueve el primer elemento de la lista.
			if (colaThreadsEnEspera.isEmpty()) {
			    this.threadsEncolados.remove(idElemento);
			}
		}
	}

	/**
	 * @return la transaccion actual del thread que llama a este metodo.
	 */
	private Transaccion dameTransaccionActual() {
		TransactionManager transactionManager = this.getTransactionManager();
		return transactionManager.dameTransaccion();
	}

    /**
     * Comprueba si puede ocurrir un dead lock entre la transaccion actual y aquellas
     * que tienen ya un lock sobre el elemento en disputa.
     * @param idElemento el Id de un elemento que se desea bloquear.
     * @throws VictimaDeadlockRuntimeException si la transaccion actual fue elegida como victima
     * por el Algoritmo de Prevencion.
     */
    private void comprobarDeadLock(Id idElemento) {
    	boolean soloExclusivos = false;
    	
        Set<Lock> conjuntoLocks = this.locks.get(idElemento);
        if (conjuntoLocks == null || conjuntoLocks.isEmpty()) {
            return;
        }
        Set<Transaccion.ID> conjuntoTransacciones = new HashSet<Transaccion.ID>(conjuntoLocks.size());
        for (Lock lock : conjuntoLocks) {
        	// Solo los exclusivos? (que va a haber a lo sumo uno solo).
            if (!soloExclusivos || lock.exclusivo()) {
                conjuntoTransacciones.add(lock.propietario().id());
            }
        }
        
        Transaccion.ID idActual = this.dameTransaccionActual().id(); 
        
        // ahora se llama al algoritmo para elegir una victima
        Transaccion.ID victima = this.prevencionDeadLock.elegirVictima(
                idActual,
                conjuntoTransacciones
                );
        
        if (victima != null) {
            if (victima.equals(idActual)) {
                // el thread actual ha sido elegido como victima
                throw new VictimaDeadlockRuntimeException();
            } else {
                // se marca al thread elegido
            	this.getTransactionManager().dameTransaccion(victima).threadPropietario().interrupt();
            }
        }
    }

    /**
     * Crea una implementacion de Lock y la guarda en las estructuras utilizando las claves correspondientes.
     * @param idElemento el Id del elemento que esta siendo bloqueado.
     * @param exclusivo true si el bloqueo es exclusivo. False si es compartido.
     */
    private void realizarBloqueo(Id idElemento, boolean exclusivo) {
        Lock lock = new LockImpl(this.dameTransaccionActual(), 
        		idElemento, 
        		exclusivo); // se crea un nuevo Lock
        
		Set<Lock> conjuntoLocks = this.locks.get(idElemento);
		if (conjuntoLocks == null) {
			conjuntoLocks = new HashSet<Lock>();
			this.locks.put(idElemento, conjuntoLocks);
		}
		conjuntoLocks.add(lock); 		// se guarda el lock en el mapa global
		
		// se guarda el lock en el mapa local
		Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(this.dameTransaccionActual().id());
		if (conjuntoLocalLocks == null) {
			conjuntoLocalLocks = new HashMap<Id, Lock>();
			this.transaccionLocks.put(this.dameTransaccionActual().id(), conjuntoLocalLocks);
		}
		conjuntoLocalLocks.put(lock.idElementoBloqueado(), lock);
        if (!exclusivo) {
            // como el lock no es exclusivo => se puede desbloquear al siguiente thread encolado
            LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
            if (colaThreadsEnEspera != null) {
            	PedidoEncolado pedidoEncolado = colaThreadsEnEspera.peek();
            	if (pedidoEncolado != null) {
            		LockSupport.unpark(pedidoEncolado.thread);	
            	}
            }
        }
    }
    
    /**
     * Permite saber si en el conjunto hay algun bloqueo exclusivo.
     * Si hay un bloqueo exclusivo, entonces es unico en el conjunto, o
     * existen bloqueos compartidos de transacciones ancestras a la propietaria
     * del bloqueo compartido.
     * @param conjuntoLocks un conjunto de locks.
     * @return true si en el conjunto hay algun bloqueo exclusivo.
     */
    private boolean hayLockExclusivo(Set<Lock> conjuntoLocks) {
    	for (Lock lock : conjuntoLocks) {
    		if (lock.exclusivo()) {
    			return true;
    		}
    	}
        return false;
    }

    /**
     * Verifica si todos los locks del conjunto pertenecen a una transaccion.
     * @param transaccion una transaccion.
     * @param conjuntoLocks un conjunto de locks.
     * @return true si todos los locks del conjunto pertenecen a una transaccion.
     */
    private boolean sonTodosDeLaTransaccion(Transaccion transaccion, Set<Lock> conjuntoLocks) {
    	for (Lock lock : conjuntoLocks) {
    		if (!lock.propietario().equals(transaccion)) {
    			return false;
    		}
    	}
        return true;
    }

    /**
     * Verifica si todos los locks del conjunto pertenecen a un thread.
     * @param thread un thread.
     * @param conjuntoLocks un conjunto de locks.
     * @return true si todos los locks del conjunto pertenecen a un thread.
     */
    private boolean sonTodosDelThread(Thread thread, Set<Lock> conjuntoLocks) {
    	for (Lock lock : conjuntoLocks) {
    		if (!lock.propietario().threadPropietario().equals(thread)) {
    			return false;
    		}
    	}
        return true;
    }

	/**
	 * Metodo que se llama cuando se intenta actualizar el lock de una transaccion de
	 * compartido a exclusivo.
	 * @param idElemento el Id del elemento cuyo lock se desea actualizar.
	 * @return true si se realizo la actualizacion. False si el lock anterior ya era exclusivo.
	 */
	private boolean actualizarBloqueo(Id idElemento) {
		Transaccion transaccion = this.dameTransaccionActual();
		Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(transaccion.id());
		if (conjuntoLocalLocks == null || !conjuntoLocalLocks.containsKey(idElemento)) {
			// el lock a actualizar se encuentra en alguna transaccion ancestral.
			// Esa se deja y se guarda la actualizacion en la transaccion actual.
			while (transaccion != null) {
				conjuntoLocalLocks = this.transaccionLocks.get(transaccion.id());
				if (conjuntoLocalLocks != null) {
					Lock lockAncestral = conjuntoLocalLocks.get(idElemento);
					if (lockAncestral != null) {
						if (lockAncestral.exclusivo()) {
							//no se cambia nada
							return false;
						}
						this.intentarBloquear(idElemento, true, true); // se encola el pedido de exclusivo pero con prioridad
						return true;
					} 

				}
				transaccion = transaccion.padre();
			}
			// no se encontro el elemento en ningun ancestro!
			throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
		} else {
			Lock lockActual = conjuntoLocalLocks.get(idElemento);
			if (lockActual.exclusivo()) {
				//no se cambia nada
				return false;
			}
			Set<Lock> conjuntoLocks = this.locks.get(idElemento);
			//conjuntoLocalLocks.remove(idElemento);
			this.intentarBloquear(idElemento, true, true); // se encola el pedido de exclusivo pero con prioridad
			conjuntoLocks.remove(lockActual); // se remueve el viejo lock compartido
			return true;
		}
	}

	/**
	 * @see servidor.lock.LockManager#desbloquear(servidor.Id)
	 */
	public void desbloquear(Id idElemento) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
            Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(this.dameTransaccionActual().id());
            if (conjuntoLocalLocks == null) {
                // el conjunto de locks locales estaba vacio => elemento bloqueado por otra transaccion
                return;
            }

            Lock lock = conjuntoLocalLocks.remove(idElemento);
            if (lock == null) {
                // el elemento no se encontraba bloqueado.
                return;
            }
            if (conjuntoLocalLocks.isEmpty()) {
            	this.transaccionLocks.remove(this.dameTransaccionActual().id());
            }
            // se obtienen los locks del elemento asociado.
            Set<Lock> conjuntoLocks = this.locks.get(idElemento);
            if (conjuntoLocks == null) {
                // el elemento no estaba bloqueado
                throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
            }
            // el lock que corresponde se remueve
            if (!conjuntoLocks.remove(lock)) {
                // si llega acá => no estaba el lock en el conjunto => inconsistencia
                throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
            }
            
            // si el conjunto queda vacío, se quita del mapa
            if (conjuntoLocks.isEmpty()) {
                this.locks.remove(idElemento);
            }
            
            // el objeto desbloqueado puede darle paso a otro thread (salvo que el desbloqueo fuera compartido y no fuera el unico)
            LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
            if (colaThreadsEnEspera != null) {
            	if (colaThreadsEnEspera.isEmpty()) {
            		//XXX: hack
            		this.threadsEncolados.remove(idElemento);
            	} else {
                    Thread thread = colaThreadsEnEspera.peek().thread;
                    if (thread != null) {
                        LockSupport.unpark(thread);
                    }
            	}
            }
            
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * Libera aquellos locks de la transaccion actual que fueron creados luego de un LSN determinado.
	 * Metodo llamado cuando se abortaba hasta un savepoint.
	 * @param lsn
	 * @deprecated ahora se llama a {@link #locksDesde(servidor.transaccion.Transaccion.ID, LSN)} para obtener los locks
	 * correspondientes y luego se van liberando. 
	 */
	@Deprecated
	public void desbloquear(LSN lsn) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        Transaccion transaccion = this.dameTransaccionActual();
        try {
            Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(this.dameTransaccionActual().id());

            if (conjuntoLocalLocks == null) {
                // el conjunto de locks locales estaba vacio => elemento bloqueado por otra transaccion
                return;
            }

        	for (Map.Entry<Id, Lock> entry: conjuntoLocalLocks.entrySet()) {
        		Id idElemento = entry.getKey();
        		Lock lock = entry.getValue();
        		if (this.lockPerteneceATransaccion(transaccion, lock) && //pertenece a la transaccion
        				lock.ultimoLSN().compareTo(lsn) != -1) { // es mayor o igual al saveLSN
                    // se obtienen los locks del elemento asociado.
                    Set<Lock> conjuntoLocks = this.locks.get(idElemento);
                    if (conjuntoLocks == null) {
                    	// el elemento no estaba bloqueado
                        throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
                    }
                    // el lock que corresponde se remueve
                    if (!conjuntoLocks.remove(lock)) {
                        // si llega acá => no estaba el lock en el conjunto => inconsistencia
                        throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
                    }
                    
                    // si el conjunto queda vacío, se quita del mapa
                    if (conjuntoLocks.isEmpty()) {
                        this.locks.remove(idElemento);
                    }
                    
                    // el objeto desbloqueado puede darle paso a otro thread (salvo que el desbloqueo fuera compartido y no fuera el unico)
                    LinkedList<PedidoEncolado> colaThreadsEnEspera = this.threadsEncolados.get(idElemento);
                    if (colaThreadsEnEspera != null) {
                    	PedidoEncolado pedidoEncolado = colaThreadsEnEspera.peek();
                        if (pedidoEncolado != null) {
                            LockSupport.unpark(pedidoEncolado.thread);
                        }
                    }
        		}
        	}
        	
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * El lock pertenece a una transaccion si fue creado en esa transaccion o en una transaccion hija (descendiente).
	 * @param transaccion la transaccion a la cual se desea saber si pertenece un lock.
	 * @param lock el lock en cuestion.
	 * @return true si el lock pertenece a la transaccion. False en caso contrario.
	 */
	private boolean lockPerteneceATransaccion(Transaccion transaccion, Lock lock) {
		Transaccion propietario = lock.propietario();
		while (propietario != null) {
			if (propietario.equals(transaccion)) {
				return true;
			}
			propietario = propietario.padre();
		}
		return false;
	}

	/**
	 * @see servidor.lock.LockManager#estaBloqueado(servidor.Id, boolean)
	 */
	public boolean estaBloqueado(Id idElemento, boolean exclusivo) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
            Set<Lock> conjunto = this.locks.get(idElemento);
            if (conjunto == null || conjunto.isEmpty()) {
                // no hay locks para ese registro
                return false;
            }
            boolean bloqueadoExclusivo = this.hayLockExclusivo(conjunto);
            // retorna true si el objeto esta bloqueado de la misma manera que se desea saberlo (en el parametro)
            return exclusivo == bloqueadoExclusivo;
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * @param idElemento el Id de un elemento que puede ser bloqueado.
	 * @return true si el elemento representado por el Id pasado por parametro
	 * se encuentra ya bloqueado por alguna transaccion del thread que llama a
	 * este metodo.
	 */
	private boolean estaBloqueadoPorMiThread(Id idElemento) {
		Transaccion transaccionActual = this.dameTransaccionActual();
		while (transaccionActual != null) {
			Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(transaccionActual.id());
			if (conjuntoLocalLocks != null && conjuntoLocalLocks.containsKey(idElemento)) {
				return true;
			}
			transaccionActual = transaccionActual.padre();
		}
		// no se encuentra bloqueado por la transaccion actual del thread ni ningun ancestro
		return false;
	}
	
	/**
	 * @return el administrador de transacciones de la base de datos.
	 */
	private TransactionManager getTransactionManager() {
        return FabricaTransactionManager.dameInstancia();
    }

	/**
	 * @see servidor.lock.LockManager#locks(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locks(Transaccion.ID idTransaccion) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
            Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(idTransaccion);
            if (conjuntoLocalLocks == null) {
            	return java.util.Collections.emptySet();
            } else {
            	return new HashSet<Id>(conjuntoLocalLocks.keySet());
            }
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * @see servidor.lock.LockManager#locksDesde(servidor.transaccion.Transaccion.ID, servidor.log.LSN)
	 */
	public Set<Id> locksDesde(Transaccion.ID idTransaccion, LSN lsn) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
            Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(idTransaccion);
            if (conjuntoLocalLocks == null) {
            	return java.util.Collections.emptySet();
            } else {
            	Set<Id> locks = new HashSet<Id>();
            	for (Map.Entry<Id, Lock> lockEntry : conjuntoLocalLocks.entrySet()) {
            		if (lsn.compareTo(lockEntry.getValue().ultimoLSN()) == -1) {
            			locks.add(lockEntry.getKey());
            		}
            	}
            	return locks;
            }
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * @see servidor.lock.LockManager#locksExclusivos(servidor.transaccion.Transaccion.ID)
	 */
	public Set<Id> locksExclusivos(Transaccion.ID idTransaccion) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
            Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.get(idTransaccion);
            if (conjuntoLocalLocks == null) {
            	return java.util.Collections.emptySet();
            } else {
            	Set<Id> locks = new HashSet<Id>();
            	for (Map.Entry<Id, Lock> lockEntry : conjuntoLocalLocks.entrySet()) {
            		if (lockEntry.getValue().exclusivo()) {
            			locks.add(lockEntry.getKey());
            		}
            	}
            	return locks;
            }
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * @see servidor.lock.LockManager#delegarLocksATransaccionPadre()
	 */
	public void delegarLocksATransaccionPadre() {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            // el thread fue marcado como victima
        	this.semaphore.release();
            throw new VictimaDeadlockRuntimeException();
        }
        try {
           	Transaccion transaccion = this.dameTransaccionActual();
           	if (transaccion.padre() == null) {
           		// si no hay padre, no hay nada para hacer
           		return;
           	}
           	
            Map<Id, Lock> conjuntoLocalLocks = this.transaccionLocks.remove(transaccion.id());
            if (conjuntoLocalLocks == null) {
            	// si no hay locks obtenidos por la transaccion, no hay nada para hacer
            	return;
            }
            
            // obtengo el conjunto de locks del padre
            Map<Id, Lock> conjuntoPadreLocks = this.transaccionLocks.get(transaccion.padre().id());
            if (conjuntoPadreLocks == null) {
    			conjuntoPadreLocks = new HashMap<Id, Lock>();
    			this.transaccionLocks.put(transaccion.padre().id(), conjuntoPadreLocks);

            }
            
            // paso los locks del hijo al padre
           	for (Lock lockHijo : conjuntoLocalLocks.values()) {
           		Set<Lock> locksId = this.locks.get(lockHijo.idElementoBloqueado());
           		if (locksId == null) {
           			throw new IllegalStateException(INCONSISTENCY_SYSTEM_LOCKS);
           		}
           		locksId.remove(lockHijo);
           		
           		Lock lockPadre = new LockImpl(transaccion.padre(), lockHijo.idElementoBloqueado(), lockHijo.exclusivo());
           		Lock lockPadreViejo = conjuntoPadreLocks.get(lockPadre.idElementoBloqueado()); // ya existia, se elimina para reemplazo.
           		if (lockPadreViejo != null) {
           			locksId.remove(lockPadreViejo);
           		}
           		locksId.add(lockPadre);
           		conjuntoPadreLocks.put(lockPadre.idElementoBloqueado(), lockPadre);
           	}
           	
        } finally {
            this.semaphore.release();
        }
	}

	/**
	 * @see servidor.lock.LockManager#cerrar()
	 */
	public void cerrar() {
		for (Thread bloqueado : this.threadsBloqueados) {
			bloqueado.interrupt();
		}
	}
	
}
