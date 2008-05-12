/**
 * 
 */
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

public class EventoChildComitted extends EventoTransaccion {
	
	protected Transaccion.ID idTransaccionHija;
	
	protected LSN lastLSNHija;

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarAnalisis(servidor.log.impl.estructuras.OutputAnalisis, servidor.log.LSN)
	 */
	@Override
	public void realizarAnalisis(OutputAnalisis outputAnalisis, LSN lsnActual) {
		super.realizarAnalisis(outputAnalisis, lsnActual);
		// termino la Tx hija, se remueve de la tabla de transacciones
		outputAnalisis.transTable.remove(this.idTransaccionHija);

	}

	@Override
	public void realizarUndo(LSN undoLSNActual, DatoTransaccion datoTransaccion, Log log) {
		datoTransaccion.undoNextLSN.remove(undoLSNActual);
		datoTransaccion.undoNextLSN.add(this.prevLSN);
		datoTransaccion.undoNextLSN.add(this.lastLSNHija);
	}

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarRollback(servidor.transaccion.Transaccion, java.util.SortedSet, servidor.log.Log)
	 */
	@Override
	public void realizarRollback(Transaccion transaccion, SortedSet<LSN> undoNextLSNs, Log log) {
		undoNextLSNs.add(this.prevLSN);
		undoNextLSNs.add(this.lastLSNHija);
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
		
		// leo el ID de la TX hija
		byte[] idTxHijaBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(idTxHijaBytes);
		this.idTransaccionHija = LogHelper.byteArrayAIdTransaccion(idTxHijaBytes);
		
		// leo el ultimo LSN de la TX hija
		byte[] lastLSNHijaBytes = new byte[Catalogo.LONGITUD_LONG];
		lector.readFully(lastLSNHijaBytes);
		this.lastLSNHija = LogHelper.byteArrayALSN(lastLSNHijaBytes);
		
	}

	@Override
	public String toString() {
		return super.toString() + "|TXHija:" + this.idTransaccionHija + "|LastLSN:" + this.lastLSNHija;
	}

}