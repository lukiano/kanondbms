/**
 * 
 */
package servidor.log.impl.eventos;

import java.io.DataInput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.buffer.FabricaBufferManager;
import servidor.catalog.Catalogo;
import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.indice.hash.impl.FabricaBucket;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.Operacion;
import servidor.log.impl.LogHelper;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.DatoTransaccion2Transaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.tabla.Registro;
import servidor.transaccion.Transaccion;

public class EventoUpdateIndex extends EventoTransaccion {
	
	protected RegistroIndice.ID idRegistroIndice;
	
	protected Registro.ID registroReferenciado;

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
		Bucket.ID idBucket = this.idRegistroIndice.propietario();
		if (!outputAnalisis.dirtyBloques.containsKey(idBucket)) {
			// se agrega
			DatoBloqueSucio datoPaginaSucia = new DatoBloqueSucio();
			datoPaginaSucia.idBucket = idBucket;
			datoPaginaSucia.recLSN = lsnActual;
			outputAnalisis.dirtyBloques.put(idBucket, datoPaginaSucia);
		}
		
	}

	/**
	 * @see servidor.log.impl.eventos.Evento#realizarRedo(servidor.log.LSN, java.util.Map)
	 */
	@Override
	public void realizarRedo(LSN lsnActual, Map<Bloque.ID, DatoBloqueSucio> dirtyBloques) {
		super.realizarRedo(lsnActual, dirtyBloques);
		
		Bucket.ID idBucket = this.idRegistroIndice.propietario();
		
		if (dirtyBloques.containsKey(idBucket)) { // se encuentra en las paginas sucias
			DatoBloqueSucio datoPaginaSucia = dirtyBloques.get(idBucket); 
			if (lsnActual.compareTo(datoPaginaSucia.recLSN) != -1) { // si el LSN actual es mayor o igual => REDO
				this.actualizarPagina(lsnActual, this.idRegistroIndice, 
						this.operacion, this.registroReferenciado);
				datoPaginaSucia.recLSN = lsnActual.incrementar(this.longitud);
			}
		}
	}
	
	private void actualizarPagina(LSN lsnActual, RegistroIndice.ID idRegistroIndice, Operacion operacion, 
			Registro.ID idRegistroReferenciado) {
		BufferManager bufferManager = this.getBufferManager();
		Bucket.ID idBucket = idRegistroIndice.propietario();
		Bloque bloque = bufferManager.dameBloque(idBucket);
		if (bloque == null) {
			bloque = bufferManager.nuevoBloque(idBucket);
		}
		bufferManager.getLatchManager().latch(idBucket);
		try {
			Bucket bucket = FabricaBucket.dameBucketLimpio(bufferManager, idBucket, bloque);
			if (bucket.recoveryLSN().compareTo(lsnActual) == -1) { // no se encuentra el update en la pagina
				switch (operacion) {
					case INSERT_INDEX: {
						try {
							bucket.agregarRegistroIndice(idRegistroIndice, idRegistroReferenciado);
						} catch (RegistroExistenteException e) {
							bucket.borrarRegistroIndice(idRegistroIndice);
							try {
								bucket.agregarRegistroIndice(idRegistroIndice, idRegistroReferenciado);
							} catch (RegistroExistenteException ignorado) {
							}
						}
						break;
					}
					case DELETE_INDEX: {
						bucket.borrarRegistroIndice(idRegistroIndice);
						break;
					}
					case CLR_INSERT_INDEX: {
						bucket.borrarRegistroIndice(idRegistroIndice);
						break;
					}
					case CLR_DELETE_INDEX: {
						try {
							bucket.agregarRegistroIndice(idRegistroIndice, idRegistroReferenciado); 
						} catch (RegistroExistenteException e) {
							bucket.borrarRegistroIndice(idRegistroIndice);
							try {
								bucket.agregarRegistroIndice(idRegistroIndice, idRegistroReferenciado);
							} catch (RegistroExistenteException ignorado) {
							}
						}
						break;
					}
				}
				bucket.actualizarRecoveryLSN(lsnActual);
			}
		} finally {
			bufferManager.getLatchManager().unLatch(idBucket);
			bufferManager.liberarBloque(idBucket);
		}
	}

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarUndo(servidor.log.LSN, servidor.log.impl.estructuras.DatoTransaccion, servidor.log.Log)
	 */
	@Override
	public void realizarUndo(LSN undoLSNActual, DatoTransaccion datoTransaccion, Log log) {
		Transaccion transaccion = new DatoTransaccion2Transaccion(datoTransaccion); 
		this.restaurarPagina(transaccion, this.idRegistroIndice, this.operacion, this.registroReferenciado, log);
		datoTransaccion.undoNextLSN.remove(undoLSNActual);
		datoTransaccion.undoNextLSN.add(this.prevLSN);
	}

	private void restaurarPagina(Transaccion transaccion, RegistroIndice.ID idRegistroIndice, 
			Operacion operacion, Registro.ID idRegistroViejo, Log log) {
		Set<LSN> undoNextLSNs = transaccion.undoNextLSN();

		
		BufferManager bufferManager = this.getBufferManager();
		Bucket.ID idBucket = idRegistroIndice.propietario();
		Bloque bloque = bufferManager.dameBloque(idBucket);
		if (bloque == null) {
			bloque = bufferManager.nuevoBloque(idBucket);
		}
		bufferManager.getLatchManager().latch(idBucket);
		try {
			Bucket bucket = FabricaBucket.dameBucketLimpio(bufferManager, idBucket, bloque);
			LSN clrLSN;
		
			switch (operacion) {
				case INSERT_INDEX:
					clrLSN = log.escribirCLRInsertIndex(transaccion, 
							idRegistroIndice, 
							undoNextLSNs);
					bucket.borrarRegistroIndice(idRegistroIndice);
					break;
				case DELETE_INDEX:
					clrLSN = log.escribirCLRDeleteIndex(transaccion, 
							idRegistroIndice, 
							undoNextLSNs, 
							idRegistroViejo);
					try {
						bucket.agregarRegistroIndice(idRegistroIndice, idRegistroViejo);
					} catch (RegistroExistenteException e) {
						throw new RuntimeException("Impossible to undo the change of a DELETE because the index register " + idRegistroIndice + " already exists.", e);
					}
					break;
				default:
					// no puede pasar
					throw new RuntimeException("It is wanted to revert an operation nonsupported " + operacion.toString());
			}
			bucket.actualizarRecoveryLSN(clrLSN);
			transaccion.establecerUltimoLSN(clrLSN);
		} finally {
			bufferManager.getLatchManager().unLatch(idBucket);
			bufferManager.liberarBloque(idBucket);
		}
	}

	/**
	 * @see servidor.log.impl.eventos.EventoTransaccion#realizarRollback(servidor.transaccion.Transaccion, java.util.SortedSet, servidor.log.Log)
	 */
	@Override
	public void realizarRollback(Transaccion transaccion, SortedSet<LSN> undoNextLSNs, Log log) {
		undoNextLSNs.add(this.prevLSN);
		this.restaurarPagina(transaccion, this.idRegistroIndice, this.operacion, this.registroReferenciado, log);
	}
	
	private BufferManager getBufferManager() {
		return FabricaBufferManager.dameInstancia();
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
		
		// leo el ID del Registro Indice
		byte[] idRegistroIndiceBytes = new byte[LogHelper.LONGITUD_REGISTRO_INDICE];
		lector.readFully(idRegistroIndiceBytes);
		this.idRegistroIndice = LogHelper.byteArrayAIdRegistroIndice(idRegistroIndiceBytes);
		
		// leo el ID del Registro referenciado
		byte[] idRegistroBytes = new byte[LogHelper.LONGITUD_REGISTRO];
		lector.readFully(idRegistroBytes);
		this.registroReferenciado = LogHelper.byteArrayAIdRegistro(idRegistroBytes);
		
	}

	@Override
	public String toString() {
		return super.toString() + "RecIndex:" + this.idRegistroIndice + "|Reference:" + this.registroReferenciado;
	}

}