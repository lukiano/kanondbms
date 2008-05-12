/**
 * 
 */
package servidor.log.impl.eventos;


import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import servidor.buffer.Bloque;
import servidor.catalog.Catalogo;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.Operacion;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.transaccion.Transaccion;

public class Evento {
	
	protected Operacion operacion;
	
	protected int longitud;
	
	public void realizarRollback(Transaccion transaccion, SortedSet<LSN> undoNextLSNs, Log log) {
		throw new RuntimeException("Event nonrecognized: " + this.operacion);
	}
	
	public int longitud() {
		return this.longitud;
	}
	
	public Operacion operacion() {
		return this.operacion;
	}
	
	public void realizarUndo(LSN undoLSNActual, DatoTransaccion datoTransaccion, Log log) {
		// implementacion default no hace nada
	}
	
	public void realizarAnalisis(OutputAnalisis outputAnalisis, LSN lsnActual) {
		// implementacion default no hace nada
	}
	
	public void realizarRedo(LSN lsnActual, Map<Bloque.ID, DatoBloqueSucio> dirtyBloques) {
		// implementacion default no hace nada		
	}
	
	public void leerEvento(DataInput lector) throws IOException {
		this.longitud = Catalogo.LONGITUD_INT;
	}
	
	public static final Evento dameEvento(Operacion operacion) {
		Class<? extends Evento> clazz = eventos.get(operacion);
		Evento evento;
		try {
			evento = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		evento.operacion = operacion;
		return evento;
	}
	
	@Override
	public String toString() {
		return "Evt:" + this.operacion();
	}
	
	private static Map<Operacion, Class<? extends Evento>> eventos = new HashMap<Operacion, Class<? extends Evento>>();
	
	static {
		eventos.put(Operacion.INSERT, EventoUpdate.class);
		eventos.put(Operacion.UPDATE, EventoUpdate.class);
		eventos.put(Operacion.DELETE, EventoUpdate.class);
		
		eventos.put(Operacion.PREPARE, EventoPrepare.class);
		
		eventos.put(Operacion.ROLLBACK, EventoTransaccion.class);
		eventos.put(Operacion.END, EventoTransaccion.class);
		
		eventos.put(Operacion.CHILD_COMMITTED, EventoChildComitted.class);
		
		eventos.put(Operacion.BEGIN_CHECKPOINT, Evento.class);
		eventos.put(Operacion.END_CHECKPOINT, EventoEndCheckpoint.class);

		eventos.put(Operacion.CLR_INSERT, EventoCLR.class);
		eventos.put(Operacion.CLR_UPDATE, EventoCLR.class);
		eventos.put(Operacion.CLR_DELETE, EventoCLR.class);

		eventos.put(Operacion.DUMMY_CLR, EventoDummyCLR.class);

		eventos.put(Operacion.INSERT_INDEX, EventoUpdateIndex.class);
		eventos.put(Operacion.DELETE_INDEX, EventoUpdateIndex.class);

		eventos.put(Operacion.CLR_INSERT_INDEX, EventoCLRIndex.class);
		eventos.put(Operacion.CLR_DELETE_INDEX, EventoCLRIndex.class);

	}
	
}