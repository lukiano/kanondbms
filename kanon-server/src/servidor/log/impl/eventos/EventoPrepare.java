package servidor.log.impl.eventos;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import servidor.catalog.Catalogo;
import servidor.log.LSN;
import servidor.log.impl.LogHelper;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.tabla.Registro;
import servidor.transaccion.Estado;

public class EventoPrepare extends EventoTransaccion {
	
	private Set<Registro.ID> registrosBloqueados;

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarAnalisis(servidor.log.impl.estructuras.OutputAnalisis, servidor.log.LSN)
	 */
	@Override
	public void realizarAnalisis(OutputAnalisis outputAnalisis, LSN lsnActual) {
		super.realizarAnalisis(outputAnalisis, lsnActual);

		DatoTransaccion datoTransaccion = outputAnalisis.transTable.get(this.idTransaccion);
		datoTransaccion.estado = Estado.COMMIT;
		datoTransaccion.lastLSN = lsnActual;
		datoTransaccion.registrosBloqueados.addAll(this.registrosBloqueados);
	}

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#leerEvento(java.io.DataInput)
	 */
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

		// leo la cantidad de Locks de la Tx
		byte[] cantidadLocksTxBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(cantidadLocksTxBytes);
		int cantidadLocks = LogHelper.byteArrayAEntero(cantidadLocksTxBytes);
		this.registrosBloqueados = new HashSet<Registro.ID>();
		
		for (int j = 0; j < cantidadLocks; j++) { // leo cada registro bloqueado
			byte[] lockRegistroTxBytes = new byte[LogHelper.LONGITUD_REGISTRO];
			lector.readFully(lockRegistroTxBytes);
			this.registrosBloqueados.add(LogHelper.byteArrayAIdRegistro(lockRegistroTxBytes));
		}
	}

	@Override
	public String toString() {
		return super.toString() + "\nRegLocked:" + this.registrosBloqueados;
	}

}
