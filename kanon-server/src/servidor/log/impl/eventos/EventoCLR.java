/**
 * 
 */
package servidor.log.impl.eventos;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import servidor.catalog.Catalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.Operacion;
import servidor.log.impl.LogHelper;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Pagina;
import servidor.transaccion.Transaccion;

public class EventoCLR extends EventoUpdate {
	
	protected Set<LSN> undoNextLSN;

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarAnalisis(servidor.log.impl.estructuras.OutputAnalisis, servidor.log.LSN)
	 */
	@Override
	public void realizarAnalisis(OutputAnalisis outputAnalisis, LSN lsnActual) {
		super.realizarAnalisis(outputAnalisis, lsnActual);
		
		DatoTransaccion datoTransaccion = outputAnalisis.transTable.get(this.idTransaccion);
		datoTransaccion.lastLSN = lsnActual;
		datoTransaccion.undoNextLSN.remove(lsnActual);
		datoTransaccion.undoNextLSN.addAll(this.undoNextLSN);
		
		// si la pagina que modifica este evento no se encuentra dentro de las paginas sucias
		Pagina.ID idPagina = this.idRegistro.propietario();
		if (!outputAnalisis.dirtyBloques.containsKey(idPagina)) {
			// se agrega
			DatoBloqueSucio datoPaginaSucia = new DatoBloqueSucio();
			datoPaginaSucia.idPagina = idPagina;
			datoPaginaSucia.recLSN = lsnActual;
			outputAnalisis.dirtyBloques.put(idPagina, datoPaginaSucia);
		}
		
	}

	/**
	 * @see servidor.log.impl.eventos.EventoUpdate#realizarUndo(servidor.log.LSN, servidor.log.impl.estructuras.DatoTransaccion, servidor.log.Log)
	 */
	@Override
	public void realizarUndo(LSN undoLSNActual, DatoTransaccion datoTransaccion, Log log) {
		datoTransaccion.undoNextLSN.remove(undoLSNActual);
		datoTransaccion.undoNextLSN.addAll(this.undoNextLSN);
	}

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarRollback(servidor.transaccion.Transaccion, java.util.SortedSet, servidor.log.Log)
	 */
	@Override
	public void realizarRollback(Transaccion transaccion, SortedSet<LSN> undoNextLSNs, Log log) {
		undoNextLSNs.addAll(this.undoNextLSN);
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
		
		// leo la cantidad de UndoNextLSN
		byte[] cantUNLSNBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(cantUNLSNBytes);
		int cantidadUndoNextLSNs = LogHelper.byteArrayAEntero(cantUNLSNBytes);
		this.undoNextLSN = new HashSet<LSN>(cantidadUndoNextLSNs);
		
		for (int i = 0; i < cantidadUndoNextLSNs; i++) { // leo cada UndoNextLSN
			byte[] undoNextLSNHijoBytes = new byte[Catalogo.LONGITUD_LONG];
			lector.readFully(undoNextLSNHijoBytes);
			this.undoNextLSN.add(LogHelper.byteArrayALSN(undoNextLSNHijoBytes));
		}
		
		// leo el ID del Registro
		byte[] idRegistroBytes = new byte[LogHelper.LONGITUD_REGISTRO];
		lector.readFully(idRegistroBytes);
		this.idRegistro = LogHelper.byteArrayAIdRegistro(idRegistroBytes);

		if (!operacion.equals(Operacion.CLR_INSERT)) { // solo si es update o delete se leen los campos

			// leo la cantidad de columnas
			byte[] cantColBytes = new byte[Catalogo.LONGITUD_INT];
			lector.readFully(cantColBytes);
			int cantidadColumnas = LogHelper.byteArrayAEntero(cantColBytes);
			this.contenidoNuevo = new ArrayList<Valor>(cantidadColumnas);
			this.contenidoViejo = new ArrayList<Valor>(cantidadColumnas);
			
			Conversor conversor = Conversor.conversorDeBytes();

			// Obtengo los campos de las columnas de la tabla
			Columna[] columnas = this.dameColumnas(this.idRegistro);
			for (int i = 0; i < cantidadColumnas; i++) { // leo cada columna
				
				// leo la posicion de la columna
				byte[] nroColumnaBytes = new byte[Catalogo.LONGITUD_INT];
				lector.readFully(nroColumnaBytes);
				int nroColumna = LogHelper.byteArrayAEntero(nroColumnaBytes);
				Campo campo = columnas[nroColumna].campo();
				switch (operacion) {
					// leo los datos
					case CLR_UPDATE:
					case CLR_DELETE: {
						byte[] datoViejoBytes = new byte[campo.longitud()];
						lector.readFully(datoViejoBytes);
						Object contenidoViejo = conversor.convertir(campo, datoViejoBytes);
						this.contenidoViejo.add(Valor.nuevoValor(nroColumna, campo, contenidoViejo));
						break;
					}
				}
			}

		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "\nUndoNextLSN:" + this.undoNextLSN;
	}

}