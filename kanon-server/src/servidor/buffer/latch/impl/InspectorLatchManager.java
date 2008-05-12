/**
 * 
 */
package servidor.buffer.latch.impl;

import servidor.buffer.Bloque.ID;
import servidor.buffer.latch.LatchManager;
import servidor.inspector.Inspector;

/**
 * Decorador de un Latch Manager que informa en pantalla los eventos que van ocurriendo.
 */
public class InspectorLatchManager implements LatchManager {
	
	/**
	 * El Latch Manager decorado.
	 */
	private LatchManager latchManager;
	
	/**
	 * El inspector que muestra los eventos.
	 */
	private Inspector inspector = new Inspector("LatchManager");

	/**
	 * Constructor de la clase.
	 * @param latchManager el Latch Manager a decorar.
	 */
	public InspectorLatchManager(LatchManager latchManager) {
		this.latchManager = latchManager;
	}

	/**
	 * @see servidor.buffer.latch.LatchManager#latch(servidor.buffer.Bloque.ID)
	 */
	public boolean latch(ID id) {
		this.inspector.agregarEvento("latch", id.toString());
		return this.latchManager.latch(id);
	}

	/**
	 * @see servidor.buffer.latch.LatchManager#unLatch(servidor.buffer.Bloque.ID)
	 */
	public void unLatch(ID id) {
		this.inspector.agregarEvento("unlatch", id.toString());
		this.latchManager.unLatch(id);
	}

}
