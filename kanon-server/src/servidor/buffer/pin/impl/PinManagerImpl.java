/**
 * 
 */
package servidor.buffer.pin.impl;

import java.util.HashMap;
import java.util.Map;

import servidor.buffer.Bloque.ID;
import servidor.buffer.pin.PinManager;

/**
 * Implementacion estandar del Pin Manager.
 * Usa un mapa con un contador para saber cuantos pines existen sobre cada bloque.
 */
public class PinManagerImpl implements PinManager {
	
	/**
	 * Un mapa que guarda la cantidad de pines que existen sobre cada bloque.
	 */
	private Map<ID, Integer> pines;

	/**
	 * Constructor de la clase. Inicializa el mapa.
	 */
	public PinManagerImpl() {
		this.pines = new HashMap<ID, Integer>();
	}


	/**
	 * @see servidor.buffer.pin.PinManager#desPinnear(servidor.buffer.Bloque.ID)
	 */
	public synchronized void desPinnear(ID id) {
		if (this.pines.containsKey(id)) {
			// existe al menos un pin sobre el elemento.
			Integer count = this.pines.get(id);
			if (count == 1) {
				// existe un solo pin, asi que se elimina la entrada directamente.
				this.pines.remove(id);
			} else {
				// se actualiza la entrada con un pin menos.
				this.pines.put(id, count - 1);
			}
		}
	}

	/**
	 * @see servidor.buffer.pin.PinManager#estaPinneado(servidor.buffer.Bloque.ID)
	 */
	public boolean estaPinneado(ID id) {
		return this.pines.containsKey(id);
	}

	/**
	 * @see servidor.buffer.pin.PinManager#pinnear(servidor.buffer.Bloque.ID)
	 */
	public synchronized void pinnear(ID id) {
		if (this.pines.containsKey(id)) {
			// se aumenta la cantidad de pines en la entrada en uno.
			Integer count = this.pines.get(id);
			this.pines.put(id, count + 1);
		} else {
			// no existia ningun pin, se crea la entrada con uno.
			this.pines.put(id, 1);
		}
	}

}
