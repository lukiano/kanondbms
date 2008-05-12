/**
 * 
 */
package servidor.lock.deadlock.impl;

import java.lang.Thread.State;
import java.util.Set;

import servidor.lock.deadlock.PrevencionDeadLock;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;

/**
 * Algoritmo de prevencion que se fija en la fecha de creacion de las distintas transacciones
 * y sigue el sistema Caution-Waiting para elegir a una victima.
 * Si existe una transaccion que sostiene un lock sobre un elemento deseado por otra, 
 * y la primera ademas se encuentra esperando por algun otro lock, entonces la segunda muere. En caso contrario, la segunda espera.
 */
public final class PDL_CautionWaiting implements PrevencionDeadLock {
	
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
    	
    	// si Tj espera algo de Tk entonces Ti aborta. Si Tj no espera a nadie entonces Ti espera.
    	for (Transaccion.ID idTxConflictiva : conjuntoTransacciones) {
    		Transaccion tj = this.transactionManager.dameTransaccion(idTxConflictiva);

			// caution waiting: solamente muere si la otra esta esperando tambien.
			if (tj.threadPropietario().getState().equals(State.WAITING)) {
				return idActual;
			}
    	}
    	
    	// ninguna Transaccion cumple la condicion => la actual se encola y espera
    	return null;
    }

}
