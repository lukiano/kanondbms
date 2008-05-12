/**
 * 
 */
package servidor.fisico.impl;

import servidor.buffer.Bloque;
import servidor.buffer.Bloque.ID;
import servidor.fisico.DiskSpaceManager;
import servidor.inspector.Inspector;

/**
 * Decorador de un Disk Manager que informa en pantalla los eventos que van ocurriendo.
 */
public class InspectorDiskSpaceManager implements DiskSpaceManager {
	
	/**
	 * El Disk Manager decorado.
	 */
	private DiskSpaceManager diskSpaceManager;
	
	/**
	 * El inspector que muestra los eventos.
	 */
	private Inspector inspector = new Inspector("DiskSpaceManager");

	/**
	 * Constructor de la clase.
	 * @param diskSpaceManager el Disk Manager a decorar.
	 */
	public InspectorDiskSpaceManager(DiskSpaceManager diskSpaceManager) {
		this.diskSpaceManager = diskSpaceManager;
	}

	/**
	 * @see servidor.fisico.DiskSpaceManager#borrarBloque(servidor.buffer.Bloque.ID)
	 */
	public void borrarBloque(ID id) {
		this.inspector.agregarEvento("borrarBloque", id.toString());
		this.diskSpaceManager.borrarBloque(id);
	}

	/**
	 * @see servidor.fisico.DiskSpaceManager#guardarBloque(servidor.buffer.Bloque.ID, servidor.buffer.Bloque)
	 */
	public void guardarBloque(ID id, Bloque bloque) {
		this.inspector.agregarEvento("guardarBloque", id.toString());
		this.diskSpaceManager.guardarBloque(id, bloque);
	}

	/**
	 * @see servidor.fisico.DiskSpaceManager#leerBloque(servidor.buffer.Bloque.ID)
	 */
	public Bloque leerBloque(ID id) {
		this.inspector.agregarEvento("leerBloque", id.toString());
		return this.diskSpaceManager.leerBloque(id);
	}

	/**
	 * @see servidor.fisico.DiskSpaceManager#nuevoBloque(servidor.buffer.Bloque.ID)
	 */
	public Bloque nuevoBloque(ID id) {
		this.inspector.agregarEvento("nuevoBloque", id.toString());
		return this.diskSpaceManager.nuevoBloque(id);
	}

}
