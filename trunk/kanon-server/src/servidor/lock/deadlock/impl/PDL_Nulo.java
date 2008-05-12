/**
 * 
 */
package servidor.lock.deadlock.impl;

import java.util.Set;

import servidor.lock.deadlock.PrevencionDeadLock;
import servidor.transaccion.Transaccion;

/**
 * Algoritmo de prevencion simple que indica que nunca se producira un deadlock.
 */
public final class PDL_Nulo implements PrevencionDeadLock {
	
    /**
     * @see servidor.lock.deadlock.PrevencionDeadLock#elegirVictima(servidor.transaccion.Transaccion.ID, java.util.Set)
     */
    public Transaccion.ID elegirVictima(Transaccion.ID idActual, Set<Transaccion.ID> conjuntoTransacciones) {
    	// esta politica no comprueba deadlocks
    	return null;
    }

}
