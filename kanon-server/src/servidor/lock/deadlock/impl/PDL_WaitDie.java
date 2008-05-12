/**
 * 
 */
package servidor.lock.deadlock.impl;

import java.util.Set;

import servidor.lock.deadlock.PrevencionDeadLock;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;

/**
 * Algoritmo de prevencion que se fija en la fecha de creacion de las distintas transacciones
 * y sigue el sistema Wait-Die para elegir a una victima.
 * Si existe una transaccion que sostiene un lock sobre un elemento deseado por otra, 
 * y la primera se inicio antes de la segunda, entonces la segunda muere. En caso contrario, la segunda espera.
 */
public final class PDL_WaitDie implements PrevencionDeadLock {
	
	/**
	 * Variable con el administrador de transacciones para obtener las distintas transacciones involucradas.
	 */
	private TransactionManager transactionManager = FabricaTransactionManager.dameInstancia(); 

    /**
     * @see servidor.lock.deadlock.PrevencionDeadLock#elegirVictima(servidor.transaccion.Transaccion.ID, java.util.Set)
     */
    public Transaccion.ID elegirVictima(Transaccion.ID idActual, Set<Transaccion.ID> conjuntoTransacciones) {
    	
    	Transaccion ti = this.transactionManager.dameTransaccion(idActual);
    	if (ti == null) {
    		// no hay transaccion en progreso => imposible comprobar deadlock
    		return null;
    	}
    	
    	// si timestamp(Ti) < timestamp(Tj) entonces Ti espera, sino Ti muere
    	for (Transaccion.ID idTxConflictiva : conjuntoTransacciones) {
    		Transaccion tj = this.transactionManager.dameTransaccion(idTxConflictiva);
        	if (tj != null) {
        		if (ti.fechaInicio().after(tj.fechaInicio())) {
        			// hay una transaccion tj que tiene un lock sobre elemento o esta esperando y comenzo antes que la ti => ti muere
        			return idActual;
        		}
        	}
    	}

    	// ninguna Transaccion cumple la condicion => la actual se encola y espera
        return null;
    }

}
