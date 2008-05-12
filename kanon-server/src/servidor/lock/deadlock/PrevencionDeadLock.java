/**
 * 
 */
package servidor.lock.deadlock;

import java.util.Set;

import servidor.transaccion.Transaccion;

/**
 * Interfaz para los Algoritmos de Prevencion de Dead Lock.
 */
public interface PrevencionDeadLock {

    /**
     * Realiza las comprobaciones seg�n el algoritmo implementado y devuelve
     * un thread si �ste ha sido elegido como victima o null si no hay
     * ninguna v�ctima.
     * @param idActual la transaccion actual.
     * @param conjuntoTransacciones las transacciones que poseen locks sobre el recurso deseado.
     */
    Transaccion.ID elegirVictima(Transaccion.ID idActual, Set<Transaccion.ID> conjuntoTransacciones);

}
