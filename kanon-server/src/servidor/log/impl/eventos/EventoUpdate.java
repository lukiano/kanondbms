/**
 * 
 */
package servidor.log.impl.eventos;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.buffer.FabricaBufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.excepciones.RegistroExistenteException;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.Operacion;
import servidor.log.impl.LogHelper;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.DatoTransaccion2Transaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.FabricaPagina;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.transaccion.Transaccion;

public class EventoUpdate extends EventoTransaccion {
	
	protected Registro.ID idRegistro;
	
	protected Collection<Valor> contenidoViejo;
	
	protected Collection<Valor> contenidoNuevo;

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
		
		// si la pagina que modifica este this no se encuentra dentro de las paginas sucias
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
	 * @see servidor.log.impl.eventos.Evento#realizarRedo(servidor.log.LSN, java.util.Map)
	 */
	@Override
	public void realizarRedo(LSN lsnActual, Map<Bloque.ID, DatoBloqueSucio> dirtyBloques) {
		super.realizarRedo(lsnActual, dirtyBloques);
		
		Pagina.ID idPagina = this.idRegistro.propietario();
		
		if (dirtyBloques.containsKey(idPagina)) { // se encuentra en las paginas sucias
			DatoBloqueSucio datoBloqueSucio = dirtyBloques.get(idPagina); 
			if (lsnActual.compareTo(datoBloqueSucio.recLSN) != -1) { // si el LSN actual es mayor o igual => REDO
				this.actualizarPagina(lsnActual, this.idRegistro, 
						this.operacion, this.contenidoViejo, this.contenidoNuevo);
				datoBloqueSucio.recLSN = lsnActual.incrementar(this.longitud);
			}
		}
	}
	
	private void actualizarPagina(LSN lsnActual, Registro.ID idRegistro, Operacion operacion, 
			Collection<Valor> valoresViejos, Collection<Valor> valoresNuevos) {
		BufferManager bufferManager = this.getBufferManager();
		Pagina.ID idPagina = idRegistro.propietario();
		Bloque bloque = bufferManager.dameBloque(idPagina);
		if (bloque == null) {
			bloque = bufferManager.nuevoBloque(idPagina);
		}
		bufferManager.getLatchManager().latch(idPagina);
		try {
			Tabla.ID idTabla = idPagina.propietario();
			Columna[] columnas = FabricaCatalogo.dameInstancia().columnasDeTabla(idTabla.nombre());
			Pagina pagina = FabricaPagina.damePagina(bufferManager, columnas, idPagina, bloque);
			if (pagina.recoveryLSN().compareTo(lsnActual) == -1) { // no se encuentra el update en la pagina
				//pagina = new PaginaDecoradoraConIndices(pagina);
				switch (operacion) {
					case INSERT: {
						try {
							pagina.insertarRegistro(idRegistro, valoresNuevos); 
						} catch (RegistroExistenteException e) {
							// ya existe el registro => actualizo
							pagina.actualizarRegistro(idRegistro, valoresNuevos);
						}
						break;
					}
					case UPDATE: {
						pagina.actualizarRegistro(idRegistro, valoresNuevos);
						break;
					}
					case DELETE: {
						pagina.borrarRegistro(idRegistro);
						break;
					}
					case CLR_INSERT: {
						pagina.borrarRegistro(idRegistro);
						break;
					}
					case CLR_UPDATE: {
						pagina.actualizarRegistro(idRegistro, valoresViejos);
						break;
					}
					case CLR_DELETE: {
						try {
							pagina.insertarRegistro(idRegistro, valoresViejos);
						} catch (RegistroExistenteException e) {
							// ya existe el registro => actualizo
							pagina.actualizarRegistro(idRegistro, valoresViejos);
						}
						break;
					}
				}
				pagina.actualizarRecoveryLSN(lsnActual);
			}
		} finally {
			bufferManager.getLatchManager().unLatch(idPagina);
			bufferManager.liberarBloque(idPagina);
		}
	}

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarUndo(servidor.log.LSN, servidor.log.impl.estructuras.DatoTransaccion, servidor.log.Log)
	 */
	@Override
	public void realizarUndo(LSN undoLSNActual, DatoTransaccion datoTransaccion, Log log) {
		Transaccion transaccion = new DatoTransaccion2Transaccion(datoTransaccion); 
		this.restaurarPagina(transaccion, this.idRegistro, this.operacion, this.contenidoViejo, log);
		datoTransaccion.undoNextLSN.remove(undoLSNActual);
		datoTransaccion.undoNextLSN.add(this.prevLSN);
	}

	private void restaurarPagina(Transaccion transaccion, Registro.ID idRegistro, 
			Operacion operacion, Collection<Valor> valoresViejos, Log log) {
		Set<LSN> undoNextLSNs = transaccion.undoNextLSN();
		                                                               
		BufferManager bufferManager = this.getBufferManager();
		Pagina.ID idPagina = idRegistro.propietario();
		Bloque bloque = bufferManager.dameBloque(idPagina);
		if (bloque == null) {
			bloque = bufferManager.nuevoBloque(idPagina);
		}
		bufferManager.getLatchManager().latch(idPagina);
		try {
			Tabla.ID idTabla = idPagina.propietario();
			Columna[] columnas = FabricaCatalogo.dameInstancia().columnasDeTabla(idTabla.nombre());
			Pagina pagina = FabricaPagina.damePagina(bufferManager, columnas, idPagina, bloque);
			//pagina = new PaginaDecoradoraConIndices(pagina);
			LSN clrLSN;
		
			switch (operacion) {
				case INSERT:
					clrLSN = log.escribirCLRInsert(transaccion, 
							idRegistro, 
							undoNextLSNs);
					pagina.borrarRegistro(idRegistro);
					break;
				case UPDATE:
					clrLSN = log.escribirCLRUpdate(transaccion, 
							idRegistro, 
							undoNextLSNs, 
							valoresViejos);
					pagina.actualizarRegistro(idRegistro, valoresViejos);
					break;
				case DELETE:
					clrLSN = log.escribirCLRDelete(transaccion, 
							idRegistro, 
							undoNextLSNs, 
							valoresViejos);
					try {
						pagina.insertarRegistro(idRegistro, valoresViejos);
					} catch (RegistroExistenteException e) {
						throw new RuntimeException("Impossible to undo the change of a DELETE because the index register " + idRegistro + " already exists.", e);
					}
					break;
				default:
					// no puede pasar
					throw new RuntimeException("It is wanted to revert an operation nonsupported " + operacion.toString());
			}
			pagina.actualizarRecoveryLSN(clrLSN);
			transaccion.establecerUltimoLSN(clrLSN);
		} finally {
			bufferManager.getLatchManager().unLatch(idPagina);
			bufferManager.liberarBloque(idPagina);
		}
	}

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarRollback(servidor.transaccion.Transaccion, java.util.SortedSet, servidor.log.Log)
	 */
	@Override
	public void realizarRollback(Transaccion transaccion, SortedSet<LSN> undoNextLSNs, Log log) {
		undoNextLSNs.add(this.prevLSN);
		this.restaurarPagina(transaccion, this.idRegistro, this.operacion, this.contenidoViejo, log);
	}
	
	private BufferManager getBufferManager() {
		return FabricaBufferManager.dameInstancia();
	}

	protected final Columna[] dameColumnas(Registro.ID idRegistro) {
		String nombreTabla = idRegistro.propietario().propietario().nombre();
		Tabla tabla = FabricaCatalogo.dameInstancia().dameTabla(nombreTabla);
		if (tabla == null) {
			// throw new RuntimeException("No existe la tabla correspondiente para el registro " + idRegistro);
			// fase de analisis, puede ser que la tabla no se haya creado todavia
			return null;
		}
		return tabla.columnas();
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
		
		// leo el ID del Registro
		byte[] idRegistroBytes = new byte[LogHelper.LONGITUD_REGISTRO];
		lector.readFully(idRegistroBytes);
		this.idRegistro = LogHelper.byteArrayAIdRegistro(idRegistroBytes);
		
		// leo la cantidad de columnas
		byte[] cantColBytes = new byte[Catalogo.LONGITUD_INT];
		lector.readFully(cantColBytes);
		int cantidadColumnas = LogHelper.byteArrayAEntero(cantColBytes);
		this.contenidoNuevo = new ArrayList<Valor>(cantidadColumnas);
		this.contenidoViejo = new ArrayList<Valor>(cantidadColumnas);

		Conversor conversor = Conversor.conversorDeBytes();

		// Obtengo los campos de las columnas de la tabla
		Columna[] columnas = this.dameColumnas(this.idRegistro);
		if (columnas != null) {
			for (int i = 0; i < cantidadColumnas; i++) { // leo cada columna
				
				// leo la posicion de la columna
				byte[] nroColumnaBytes = new byte[Catalogo.LONGITUD_INT];
				lector.readFully(nroColumnaBytes);
				int nroColumna = LogHelper.byteArrayAEntero(nroColumnaBytes);
				Campo campo = columnas[nroColumna].campo();
				switch (operacion) {
					// leo el o los datos
					case INSERT: {
						byte[] datoNuevoBytes = new byte[campo.longitud()];
						lector.readFully(datoNuevoBytes);
						Object contenidoNuevo = conversor.convertir(campo, datoNuevoBytes);
						this.contenidoNuevo.add(Valor.nuevoValor(nroColumna, campo, contenidoNuevo));
						break;
					}
					case UPDATE: {
						byte[] datoViejoBytes = new byte[campo.longitud()];
						byte[] datoNuevoBytes = new byte[campo.longitud()];
						lector.readFully(datoViejoBytes);
						lector.readFully(datoNuevoBytes);
						Object contenidoNuevo = conversor.convertir(campo, datoNuevoBytes);
						Object contenidoViejo = conversor.convertir(campo, datoViejoBytes);
						this.contenidoViejo.add(Valor.nuevoValor(nroColumna, campo, contenidoViejo));
						this.contenidoNuevo.add(Valor.nuevoValor(nroColumna, campo, contenidoNuevo));
						break;
					}
					case DELETE: {
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
		return super.toString() + "Reg:" + this.idRegistro + "\nContenidoViejo:" + this.contenidoViejo + "\nContenidoNuevo:" + this.contenidoNuevo;
	}

}