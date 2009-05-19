package servidor.log.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import servidor.Id;
import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.buffer.FabricaBufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.inspector.Inspector;
import servidor.lock.FabricaLockManager;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.RecoveryManager;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.estructuras.DatoTransaccion2Transaccion;
import servidor.log.impl.estructuras.OutputAnalisis;
import servidor.log.impl.eventos.Evento;
import servidor.tabla.Columna;
import servidor.tabla.FabricaPagina;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.transaccion.Estado;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.Transaccion;

public class RecoveryManagerImpl implements RecoveryManager {
	
	private Log log;
	
	private Inspector inspector;
	
	private boolean usarInspector;

	public RecoveryManagerImpl(Log log, boolean usarInspector) {
        super();
        this.log = log;
        this.usarInspector = usarInspector;
        if (this.usarInspector) {
        	this.inspector = new Inspector("RecoveryManager");
        }
    }
	
	public Log log() {
		return this.log;
	}
	
    /**
     * @see servidor.log.RecoveryManager#checkpoint()
     */
    public void checkpoint() {
    	if (this.usarInspector) {
    		this.inspector.agregarEvento("Setting Checkpoint...");
    	}
    	LSN lsnBegin = this.log.escribirBeginCheckpoint();
    	this.log.escribirEndCheckpoint(this.dameTransaccionesActivas(), this.dameBloquesSucios());
    	this.log.escribirMasterRecord(lsnBegin);
        FabricaBufferManager.dameInstancia().guardarBloquesModificados();
        if (this.usarInspector) {
        	this.inspector.agregarEvento("Checkpoint set.");
        }
    }
	
    private Collection<DatoTransaccion> dameTransaccionesActivas() {
    	Set<Transaccion> transacciones = FabricaTransactionManager.dameInstancia().dameTransacciones();
    	Set<DatoTransaccion> datos = new HashSet<DatoTransaccion>(transacciones.size());
    	
    	for (Transaccion transaccion : transacciones) {
    		DatoTransaccion datoTransaccion = new DatoTransaccion();
    		datoTransaccion.idTransaccion = transaccion.id();
    		datoTransaccion.estado = transaccion.estado();
    		datoTransaccion.lastLSN = transaccion.ultimoLSN();
    		datoTransaccion.undoNextLSN.addAll(transaccion.undoNextLSN());
    		
    		// obtengo los locks de esta transaccion
    		Set<Id> locks = FabricaLockManager.dameInstancia().locksExclusivos(transaccion.id());
    		// solo dejo los locks que son de un registro
    		Iterator<Id> iterator = locks.iterator();
    		while (iterator.hasNext()) {
    			Id id = iterator.next();
    			if (id instanceof Registro.ID) {
    				datoTransaccion.registrosBloqueados.add((Registro.ID)id);
    			}
    		}
    		
    		datos.add(datoTransaccion);
    	}
		return datos;
	}

	private Collection<DatoBloqueSucio> dameBloquesSucios() {
    	BufferManager bufferManager = FabricaBufferManager.dameInstancia();
    	Catalogo catalogo = FabricaCatalogo.dameInstancia();
    	Set<Bloque.ID> sucios = bufferManager.dameBloquesSucios();
    	
    	Set<DatoBloqueSucio> paginasSucias = new HashSet<DatoBloqueSucio>();
    	
    	for (Bloque.ID sucio : sucios) {
    		if (sucio instanceof Pagina.ID) {
    			Pagina.ID idPagina = (Pagina.ID)sucio;
    			Bloque bloque = bufferManager.dameBloqueSoloSiEnMemoria(idPagina);
    			if (bloque != null) {
    				try {
	    				Columna[] columnas = catalogo.columnasDeTabla(idPagina.propietario().nombre());
	    				Pagina pagina = 
	    					FabricaPagina.damePagina(bufferManager, columnas, idPagina, bloque);
	    				
	    				DatoBloqueSucio datoPaginaSucia = new DatoBloqueSucio();
	    				datoPaginaSucia.idPagina = idPagina;
	    				datoPaginaSucia.recLSN = pagina.recoveryLSN();
	    				
	    				paginasSucias.add(datoPaginaSucia);
    				} finally {
    					bufferManager.liberarBloque(idPagina);
    				}
    			}
    		}
    	}
    	return paginasSucias;
    }
    

    
    /**
     * @see servidor.log.RecoveryManager#recuperarSistema()
     */
    public void recuperarSistema() {
    	OutputAnalisis outputAnalisis = this.analisis();
    	if (this.usarInspector) {
        	this.inspector.agregarEvento("Transactions Table: " + outputAnalisis.transTable.keySet());
        	this.inspector.agregarEvento("Dirty Pages Table: " + outputAnalisis.dirtyBloques.keySet());
        }
    	// obtengo el proximo ID de transaccion
    	int proximoIDTransaccion = 0;
    	if (!outputAnalisis.transTable.isEmpty()) {
    		for (DatoTransaccion datoTransaccion : outputAnalisis.transTable.values()) {
    			int nroTransaccion = datoTransaccion.idTransaccion.numeroTransaccion();
    			if (nroTransaccion > proximoIDTransaccion) {
    				proximoIDTransaccion = nroTransaccion;
    			}
    		}
    	}
    	FabricaTransactionManager.dameInstancia().establecerProximoIDTransaccion(Transaccion.ID.nuevoID(proximoIDTransaccion + 1));
    	
    	boolean hacerCheckpoint = false;
    	
    	if (!outputAnalisis.dirtyBloques.isEmpty()) { // optimizacion: si no hay paginas sucias no hace falta hacer REDO
        	this.redo(outputAnalisis.redoLSN, outputAnalisis.dirtyBloques);
        	hacerCheckpoint = true;
    	}
    	if (!outputAnalisis.transTable.isEmpty()) {
    		this.undo(outputAnalisis.transTable);
    		hacerCheckpoint = true;
    	}
    	if (hacerCheckpoint) {
        	this.checkpoint();
    	}
	}
    
    /**
     * @return El resultado de la fase de analisis de ARIES.
     */
    private OutputAnalisis analisis() {
    	if (this.usarInspector) {
    		this.inspector.agregarEvento("ARIES", "Analysis Phase");
    	}
    	OutputAnalisis outputAnalisis = new OutputAnalisis();
    	// obtengo el LSN del ultimo begin checkpoint
    	LSN lsnActual = this.log.dameLSNMaestro();
    	
    	// proceso el log
    	Evento eventoActual = this.log.leerEvento(lsnActual);
    	while (eventoActual != null) {
    		if (this.usarInspector) {
    			this.inspector.agregarEvento("Analysis:" + eventoActual.toString());
    		}
    		eventoActual.realizarAnalisis(outputAnalisis, lsnActual);
    		
    		// leo el proximo evento
        	lsnActual = lsnActual.incrementar(eventoActual.longitud());
        	eventoActual = this.log.leerEvento(lsnActual);
    	}
    	
    	// ahora remuevo de la tabla de transacciones aquellas que hicieron rollback
    	Iterator<DatoTransaccion> iterator = outputAnalisis.transTable.values().iterator();
    	while (iterator.hasNext()) {
    		DatoTransaccion datoTransaccion = iterator.next();
    		if (datoTransaccion.estado.equals(Estado.EN_CURSO)) {
    			SortedSet<LSN> undoNextLSNs = datoTransaccion.undoNextLSN;
    			if (transaccionAbortada(undoNextLSNs)) {
    				// solo queda el LSN Nulo => termino de hacer rollback
    				iterator.remove();
    				// escribo el END log que faltaba
    				this.log.escribirFinTransaccion(new DatoTransaccion2Transaccion(datoTransaccion));
    			}
    		}
    	}
    	
    	// obtengo el minimo RecLSN para empezar la fase REDO
    	for (DatoBloqueSucio datoPaginaSucia : outputAnalisis.dirtyBloques.values()) {
    		if (outputAnalisis.redoLSN == null) {
    			// todavia no existe ninguno => se establece este
    			outputAnalisis.redoLSN = datoPaginaSucia.recLSN; 
    		} else if (outputAnalisis.redoLSN.compareTo(datoPaginaSucia.recLSN) == 1) {
    			// existe uno y era mayor al de este => se establece este
    			outputAnalisis.redoLSN = datoPaginaSucia.recLSN;
    		}
    	}
    	
    	return outputAnalisis;
    }

	private boolean transaccionAbortada(SortedSet<LSN> undoNextLSNs) {
		return undoNextLSNs.size() == 1 && undoNextLSNs.contains(LSN.LSN_NULO);
	}
    
    /**
     * @param redoLSN
     * @param dirtyBloques
     */
    private void redo(LSN redoLSN, Map<Bloque.ID, DatoBloqueSucio> dirtyBloques) {
    	if (this.usarInspector) {
    		this.inspector.agregarEvento("ARIES", "Redo phase");
    	}
    	LSN lsnActual = redoLSN;
       	Evento eventoActual = this.log.leerEvento(lsnActual);
       	// recorro los eventos hasta el fin del log
    	while (eventoActual != null) {
    		if (this.usarInspector) {
    			this.inspector.agregarEvento("Redo:" + eventoActual.toString());
    		}
    		eventoActual.realizarRedo(lsnActual, dirtyBloques);
    		
    		// leo el proximo evento
        	lsnActual = lsnActual.incrementar(eventoActual.longitud());
        	eventoActual = this.log.leerEvento(lsnActual);
    	}
    }
    
    /**
     * @param transTable
     */
    private void undo(Map<Transaccion.ID, DatoTransaccion> transTable) {
    	if (this.usarInspector) {
    		this.inspector.agregarEvento("ARIES", "Undo phase");
    	}
    	while (!transTable.isEmpty()) {
    		DatoTransaccion datoTransaccionConMaximoLSN = this.dameMaximoUndoLSN(transTable.values());
    		LSN undoLSNActual = datoTransaccionConMaximoLSN.undoNextLSN.last();
    		Evento eventoActual = this.log.leerEvento(undoLSNActual);
    		if (this.usarInspector) {
    			this.inspector.agregarEvento("Undo:" + eventoActual.toString());
    		}
    		eventoActual.realizarUndo(undoLSNActual, datoTransaccionConMaximoLSN, this.log);
    		if (transaccionAbortada(datoTransaccionConMaximoLSN.undoNextLSN)) {
    			transTable.remove(datoTransaccionConMaximoLSN.idTransaccion);
    		}
    	}
    }
    
	private DatoTransaccion dameMaximoUndoLSN(Collection<DatoTransaccion> transacciones) {
		DatoTransaccion resultado = null;
		for (DatoTransaccion datoTransaccion : transacciones) {
			if (resultado == null) {
				resultado = datoTransaccion;
			} else {
				LSN LSNAComparar = datoTransaccion.undoNextLSN.last();
				LSN LSNMaximoActual = resultado.undoNextLSN.last(); 
				if (LSNMaximoActual.compareTo(LSNAComparar) == -1) { // el por ahora maximo actual es menor al valor
					resultado = datoTransaccion;
				}
			}
		}
		
		return resultado;
	}

	/**
	 * @see servidor.log.RecoveryManager#rollback(servidor.transaccion.Transaccion)
	 */
	public void rollback(Transaccion transaccion) {
    	this.rollback(transaccion, LSN.LSN_NULO);
    }
    
	/**
	 * @see servidor.log.RecoveryManager#rollback(servidor.transaccion.Transaccion, servidor.log.LSN)
	 */
	public void rollback(Transaccion transaccion, LSN saveLSN) { // segun pagina 119 de ARIES
		if (this.usarInspector) {
			this.inspector.agregarEvento("ARIES", "Rollback");
		}
		LSN lsn = this.log.escribirRollbackTransaccion(transaccion);
		transaccion.establecerUltimoLSN(lsn);
		transaccion.establecerUndoNextLSN(lsn);
		
		SortedSet<LSN> undoNextLSNs = transaccion.undoNextLSN(); 
		LSN undoNextLSN = undoNextLSNs.last();
		undoNextLSNs.remove(undoNextLSN);
		
		while (saveLSN.compareTo(undoNextLSN) == -1) {
			Evento evento = this.log.leerEvento(undoNextLSN);
			
			evento.realizarRollback(transaccion, undoNextLSNs, this.log);
			undoNextLSN = undoNextLSNs.last();
			undoNextLSNs.remove(undoNextLSN);
		}
	}
	
}