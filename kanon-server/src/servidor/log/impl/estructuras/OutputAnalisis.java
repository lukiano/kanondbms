/**
 * 
 */
package servidor.log.impl.estructuras;

import java.util.HashMap;
import java.util.Map;

import servidor.buffer.Bloque;
import servidor.log.LSN;
import servidor.transaccion.Transaccion;

public final class OutputAnalisis {
	
	public LSN redoLSN;
	
	public Map<Transaccion.ID, DatoTransaccion> transTable = new HashMap<Transaccion.ID, DatoTransaccion>();
	
	public Map<Bloque.ID, DatoBloqueSucio> dirtyBloques = new HashMap<Bloque.ID, DatoBloqueSucio>();

	@Override
	public String toString() {
		return "RedoLSN:" + redoLSN + "\nTransTable:" + transTable + "\nDirtyPages:" + dirtyBloques;
	}
}