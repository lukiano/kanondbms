package servidor.lock.deadlock.impl;

import java.util.Set;

import servidor.inspector.Inspector;
import servidor.lock.deadlock.PrevencionDeadLock;
import servidor.transaccion.Transaccion;

/**
 * Decorador de un Algoritmo de Prevencion que informa en pantalla los eventos que van ocurriendo.
 */
public class InspectorPDL implements PrevencionDeadLock {
	
    /**
     * El Algoritmo de Prevencion decorado.
     */
	private PrevencionDeadLock pdl;
	
	/**
	 * El inspector que muestra los eventos.
	 */
	private Inspector inspector = new Inspector("PrevencionDeadLock");
	
	/**
	 * Constructor de la clase.
     * @param pdl el Algoritmo de Prevencion a decorar.
     */
	public InspectorPDL(PrevencionDeadLock pdl) {
		this.pdl = pdl;
	}

	/**
	 * @see servidor.lock.deadlock.PrevencionDeadLock#elegirVictima(servidor.transaccion.Transaccion.ID, java.util.Set)
	 */
	public Transaccion.ID elegirVictima(Transaccion.ID idActual, Set<Transaccion.ID> conjuntoTransacciones) {
		Transaccion.ID victima = this.pdl.elegirVictima(idActual, conjuntoTransacciones);
		if (victima == null) {
			this.inspector.agregarEvento(idActual.toString(), conjuntoTransacciones.toString(), "sin victimas");
		} else {
			this.inspector.agregarEvento(idActual.toString(), conjuntoTransacciones.toString(), "victima:", victima.toString());
		}
		return victima;
	}

}
