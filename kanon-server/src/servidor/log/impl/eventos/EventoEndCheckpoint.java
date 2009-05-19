/**
 * 
 */
package servidor.log.impl.eventos;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import servidor.catalog.Catalogo;
import servidor.log.LSN;
import servidor.log.impl.LogHelper;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.transaccion.Estado;

public class EventoEndCheckpoint extends Evento {
	
	protected List<DatoTransaccion> transacciones;
	
	protected List<DatoBloqueSucio> bloquesSucios;

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarAnalisis(servidor.log.impl.estructuras.OutputAnalisis, servidor.log.LSN)
	 */
	@Override
	public void realizarAnalisis(OutputAnalisis outputAnalisis, LSN lsnActual) {
		// se actualizan las tablas con la informacion del End Checkpoint
		for (DatoTransaccion datoTransaccion : this.transacciones) {
			if (!outputAnalisis.transTable.containsKey(datoTransaccion.idTransaccion)) {
				// no existe la transaccion en la tabla => se agrega
				outputAnalisis.transTable.put(datoTransaccion.idTransaccion, datoTransaccion);
			}
		}
		for (DatoBloqueSucio datoBloqueSucio : this.bloquesSucios) {
			if (outputAnalisis.dirtyBloques.containsKey(datoBloqueSucio.idPagina)) {
				// ya existe el bloque en la tabla de bloques sucios => se actualiza
				outputAnalisis.dirtyBloques.get(datoBloqueSucio.idPagina).recLSN = datoBloqueSucio.recLSN; 
			} else {
				// no existe => se agrega
				outputAnalisis.dirtyBloques.put(datoBloqueSucio.idPagina, datoBloqueSucio);
			}
		}
		
	}
	
	@Override
	public void leerEvento(DataInput lector) throws IOException {
		// leo la longitud del evento
		byte[] longitudEventoBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(longitudEventoBytes);
		this.longitud = LogHelper.byteArrayAEntero(longitudEventoBytes) + Catalogo.LONGITUD_LONG;

		// leo la cantidad de Tx
		byte[] cantTxBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(cantTxBytes);
		int cantidadTransacciones = LogHelper.byteArrayAEntero(cantTxBytes);
		this.transacciones = new ArrayList<DatoTransaccion>(cantidadTransacciones);

		for (int i = 0; i < cantidadTransacciones; i++) { // leo cada Tx
			DatoTransaccion datoTransaccion = new DatoTransaccion();
			 
			// leo el ID de la Tx
			byte[] idTxBytes = new byte[Catalogo.LONGITUD_INT];
			lector.readFully(idTxBytes);
			datoTransaccion.idTransaccion = LogHelper.byteArrayAIdTransaccion(idTxBytes);
			
			// leo el Estado de la Tx
			byte[] estadoTxBytes = new byte[Catalogo.LONGITUD_INT];
			lector.readFully(estadoTxBytes);
			datoTransaccion.estado = Estado.values()[LogHelper.byteArrayAEntero(estadoTxBytes)];

			// leo el LastLSN de la Tx
			byte[] lastLSNTxBytes = new byte[Catalogo.LONGITUD_LONG];
			lector.readFully(lastLSNTxBytes);
			datoTransaccion.lastLSN = LogHelper.byteArrayALSN(lastLSNTxBytes);
			
			// leo la cantidad de UndoNextLSN de la Tx
			byte[] cantidadUndoNextLSNTxBytes = new byte[Catalogo.LONGITUD_INT];
			lector.readFully(cantidadUndoNextLSNTxBytes);
			int cantidadUndoNextLSNs = LogHelper.byteArrayAEntero(cantidadUndoNextLSNTxBytes);
			
			for (int j = 0; j < cantidadUndoNextLSNs; j++) { // leo cada UndoNextLSN
				byte[] undoNextLSNTxBytes = new byte[Catalogo.LONGITUD_LONG];
				lector.readFully(undoNextLSNTxBytes);
				datoTransaccion.undoNextLSN.add(LogHelper.byteArrayALSN(undoNextLSNTxBytes));
			}

			// leo la cantidad de Locks de la Tx
			byte[] cantidadLocksTxBytes = new byte[Catalogo.LONGITUD_INT];
			lector.readFully(cantidadLocksTxBytes);
			int cantidadLocks = LogHelper.byteArrayAEntero(cantidadLocksTxBytes);
			
			for (int j = 0; j < cantidadLocks; j++) { // leo cada registro bloqueado
				byte[] lockRegistroTxBytes = new byte[Catalogo.LONGITUD_LONG];
				lector.readFully(lockRegistroTxBytes);
				datoTransaccion.registrosBloqueados.add(LogHelper.byteArrayAIdRegistro(lockRegistroTxBytes));
			}

			this.transacciones.add(datoTransaccion);
		}

		// leo la cantidad de paginas sucias
		byte[] cantDPBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(cantDPBytes);
		int cantidadPaginasSucias = LogHelper.byteArrayAEntero(cantDPBytes);
		this.bloquesSucios = new ArrayList<DatoBloqueSucio>(cantidadPaginasSucias);

		for (int i = 0; i < cantidadPaginasSucias; i++) { // leo cada Dirty Page
			DatoBloqueSucio datoPaginaSucia = new DatoBloqueSucio();
			
			// leo el ID de la Pagina
			byte[] idPaginaBytes = new byte[LogHelper.LONGITUD_PAGINA];
			lector.readFully(idPaginaBytes);
			datoPaginaSucia.idPagina = LogHelper.byteArrayAIdPagina(idPaginaBytes);
			
			// leo el recLSN de la Pagina
			byte[] recLSNBytes = new byte[Catalogo.LONGITUD_LONG];
			lector.readFully(recLSNBytes);
			datoPaginaSucia.recLSN = LogHelper.byteArrayALSN(recLSNBytes);
			
			this.bloquesSucios.add(datoPaginaSucia);
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "\nTransactions:" + this.transacciones + "\nDirtyPages:" + this.bloquesSucios;
	}

}