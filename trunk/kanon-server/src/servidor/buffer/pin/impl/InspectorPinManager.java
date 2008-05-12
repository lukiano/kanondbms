/**
 * 
 */
package servidor.buffer.pin.impl;

import servidor.buffer.Bloque.ID;
import servidor.buffer.pin.PinManager;
import servidor.inspector.Inspector;

/**
 * Decorador de un Pin Manager que informa en pantalla los eventos que van ocurriendo.
 */
public class InspectorPinManager implements PinManager {
	
	/**
	 * El Pin Manager decorado.
	 */
	private PinManager pinManager;
	
	/**
	 * El inspector que muestra los eventos.
	 */
	private Inspector inspector = new Inspector("PinManager");

	/**
	 * Constructor de la clase.
	 * @param pinManager el Pin Manager a decorar.
	 */
	public InspectorPinManager(PinManager pinManager) {
		this.pinManager = pinManager;
	}

	/**
	 * @see servidor.buffer.pin.PinManager#desPinnear(servidor.buffer.Bloque.ID)
	 */
	public void desPinnear(ID id) {
		this.inspector.agregarEvento("Despinnear", id.toString());
		this.pinManager.desPinnear(id);
	}

	/**
	 * @see servidor.buffer.pin.PinManager#pinnear(servidor.buffer.Bloque.ID)
	 */
	public void pinnear(ID id) {
		this.inspector.agregarEvento("Pinnear", id.toString());
		this.pinManager.pinnear(id);
	}

	/**
	 * @see servidor.buffer.pin.PinManager#estaPinneado(servidor.buffer.Bloque.ID)
	 */
	public boolean estaPinneado(ID id) {
		return this.pinManager.estaPinneado(id);
	}

}
