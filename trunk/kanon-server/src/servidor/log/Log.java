package servidor.log;

import java.util.Collection;
import java.util.Set;

import servidor.catalog.Valor;
import servidor.indice.hash.RegistroIndice;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.eventos.Evento;
import servidor.tabla.Registro;
import servidor.transaccion.Transaccion;


/**
 * En el log se escriben las operaciones de ARIES.
 * En la recuperacion esto es lo que se lee en las 3 fases.
 * Su contenido es un archivo en binario. No utiliza BufferManager.
 * 
 * Habria que crear los objetos Java que representen a cada evento posible.
 * 
 * Ver el manejo de las LSN.
 */
public interface Log {
	
	Evento leerEvento(LSN lsn);
	
	LSN dameLSNMaestro();
	
	void escribirMasterRecord(LSN lsn);

	LSN escribirInsert(Transaccion transaccion, Registro.ID idRegistro, Collection<Valor> valoresNuevos);
	
	LSN escribirUpdate(Transaccion transaccion, Registro registro, Collection<Valor> valoresNuevos);
	
	LSN escribirDelete(Transaccion transaccion, Registro registro);
	
	LSN escribirInsertIndex(Transaccion transaccion, RegistroIndice.ID idRegistroIndice, Registro.ID idRegistro);
	
	LSN escribirDeleteIndex(Transaccion transaccion, RegistroIndice registroIndice);

	LSN escribirCLRInsert(Transaccion transaccion, Registro.ID idRegistro, Set<LSN> undoNextLSNs);
	
	LSN escribirCLRUpdate(Transaccion transaccion, Registro.ID idRegistro, Set<LSN> undoNextLSNs, Collection<Valor> valoresViejos);
	
	LSN escribirCLRDelete(Transaccion transaccion, Registro.ID idRegistro, Set<LSN> undoNextLSNs, Collection<Valor> valoresViejos);
	
	LSN escribirCLRInsertIndex(Transaccion transaccion, RegistroIndice.ID idRegistroIndice, Set<LSN> undoNextLSNs);
	
	LSN escribirCLRDeleteIndex(Transaccion transaccion, RegistroIndice.ID idRegistroIndice, Set<LSN> undoNextLSNs, Registro.ID idRegistroViejo);

	LSN escribirPrepareTransaccion(Transaccion transaccion);
	
	LSN escribirChildCommittedTransaccion(Transaccion transaccion, Transaccion transaccionHija);
	
	LSN escribirRollbackTransaccion(Transaccion transaccion);
	
	LSN escribirFinTransaccion(Transaccion transaccion);
	
	LSN escribirDummyCLR(Transaccion transaccion, LSN undoNextLSN);
	
	LSN escribirBeginCheckpoint();
	
	LSN escribirEndCheckpoint(Collection<DatoTransaccion> transacciones, Collection<DatoBloqueSucio> paginasSucias);
    
	void forzarADisco();
	
    void cerrar();
    
    boolean borrarLog();
    
}
