package servidor.log.impl.eventos;

import java.io.DataInput;
import java.io.IOException;
import java.util.SortedSet;

import servidor.catalog.Catalogo;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.impl.LogHelper;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.transaccion.Transaccion;

public class EventoDummyCLR extends EventoTransaccion {
	
	protected LSN undoNextLSN;

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarAnalisis(servidor.log.impl.estructuras.OutputAnalisis, servidor.log.LSN)
	 */
	@Override
	public void realizarAnalisis(OutputAnalisis outputAnalisis, LSN lsnActual) {
		super.realizarAnalisis(outputAnalisis, lsnActual);
		
		DatoTransaccion datoTransaccion = outputAnalisis.transTable.get(this.idTransaccion);
		datoTransaccion.lastLSN = lsnActual;
		datoTransaccion.undoNextLSN.remove(lsnActual);
		datoTransaccion.undoNextLSN.add(this.prevLSN);

		datoTransaccion.undoNextLSN.remove(lsnActual);
		datoTransaccion.undoNextLSN.add(this.undoNextLSN);

	}

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarUndo(servidor.log.LSN, servidor.log.impl.estructuras.DatoTransaccion, servidor.log.Log)
	 */
	@Override
	public void realizarUndo(LSN undoLSNActual, DatoTransaccion datoTransaccion, Log log) {
		datoTransaccion.undoNextLSN.remove(undoLSNActual);
		datoTransaccion.undoNextLSN.add(this.undoNextLSN);
	}

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarRollback(servidor.transaccion.Transaccion, java.util.SortedSet, servidor.log.Log)
	 */
	@Override
	public void realizarRollback(Transaccion transaccion, SortedSet<LSN> undoNextLSNs, Log log) {
		undoNextLSNs.add(this.undoNextLSN);
	}

	@Override
	public void leerEvento(DataInput lector) throws IOException {
		// leo la longitud del evento
		byte[] longitudEventoBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(longitudEventoBytes);
		this.longitud = LogHelper.byteArrayAEntero(longitudEventoBytes) + Catalogo.LONGITUD_LONG;
		
		// leo el ID de la TX
		byte[] idTxBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(idTxBytes);
		this.idTransaccion = LogHelper.byteArrayAIdTransaccion(idTxBytes);
		
		// leo el LSN anterior
		byte[] prevLSNBytes = new byte[Catalogo.LONGITUD_LONG];
		lector.readFully(prevLSNBytes);
		this.prevLSN = LogHelper.byteArrayALSN(prevLSNBytes);
		
		// leo el UndoNextLSN
		byte[] undoNextLSNBytes = new byte[Catalogo.LONGITUD_LONG];
		lector.readFully(undoNextLSNBytes);
		this.undoNextLSN = LogHelper.byteArrayALSN(undoNextLSNBytes);
	}
	
	@Override
	public String toString() {
		return super.toString() + "|UndoNextLSN:" + this.undoNextLSN;
	}

}
