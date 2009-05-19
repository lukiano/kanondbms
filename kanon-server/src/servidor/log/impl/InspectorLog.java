package servidor.log.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import servidor.Id;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.indice.hash.RegistroIndice;
import servidor.inspector.Inspector;
import servidor.lock.FabricaLockManager;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.eventos.Evento;
import servidor.tabla.Registro;
import servidor.tabla.Registro.ID;
import servidor.transaccion.Transaccion;

public class InspectorLog implements Log {
	
	private Log log;
	
	private Inspector inspector = new Inspector("Log");

	public InspectorLog(Log log) {
		this.log = log;
	}

	public boolean borrarLog() {
		return this.log.borrarLog();
	}

	public void cerrar() {
		this.log.cerrar();
	}
	
	public void escribirMasterRecord(LSN lsn) {
		this.log.escribirMasterRecord(lsn);
		this.inspector.agregarEvento("Master Record", lsn.toString());
	}

	public LSN dameLSNMaestro() {
		return this.log.dameLSNMaestro();
	}

	public LSN escribirCLRDelete(Transaccion transaccion, ID idRegistro,
			Set<LSN> undoNextLSNs, Collection<Valor> valoresViejos) {
		LSN lsn = this.log.escribirCLRDelete(transaccion, idRegistro, undoNextLSNs, valoresViejos);
		this.inspector.agregarEvento(lsn.toString(), "CLRDelete", transaccion.id().toString(),
				idRegistro.toString(), undoNextLSNs.toString());
		this.inspector.agregarEvento(this.valores(valoresViejos));
		return lsn;
	}
	
	public LSN escribirDummyCLR(Transaccion transaccion, LSN undoNextLSN) {
		LSN lsn = this.log.escribirDummyCLR(transaccion, undoNextLSN);
		this.inspector.agregarEvento(lsn.toString(), "DummyCLR", transaccion.id().toString(), 
				undoNextLSN.toString());
		return lsn;
	}


	public LSN escribirCLRInsert(Transaccion transaccion, ID idRegistro,
			Set<LSN> undoNextLSNs) {
		LSN lsn = this.log.escribirCLRInsert(transaccion, idRegistro, undoNextLSNs);
		this.inspector.agregarEvento(lsn.toString(), "CLRInsert", transaccion.id().toString(), 
				idRegistro.toString(), undoNextLSNs.toString());
		return lsn;
	}

	public LSN escribirCLRUpdate(Transaccion transaccion, ID idRegistro,
			Set<LSN> undoNextLSNs, Collection<Valor> valoresViejos) {
		LSN lsn = this.log.escribirCLRUpdate(transaccion, idRegistro, undoNextLSNs, valoresViejos);
		this.inspector.agregarEvento(lsn.toString(), "CLRUpdate", transaccion.id().toString(),
				idRegistro.toString(), undoNextLSNs.toString());
		this.inspector.agregarEvento(this.valores(valoresViejos));
		return lsn;
	}

	public LSN escribirChildCommittedTransaccion(Transaccion transaccion,
			Transaccion transaccionHija) {
		LSN lsn = this.log.escribirChildCommittedTransaccion(transaccion, transaccionHija);
		this.inspector.agregarEvento(lsn.toString(), "ChildCommitted", "TX", transaccion.id().toString(), 
				"TXHija", transaccionHija.id().toString());
		return lsn;
	}

	public LSN escribirDelete(Transaccion transaccion, Registro registro) {
		LSN lsn = this.log.escribirDelete(transaccion, registro);
		this.inspector.agregarEvento(lsn.toString(), "Delete", transaccion.id().toString(), 
				registro.id().toString());
		this.inspector.agregarEvento(this.valores(registro));
		return lsn;
	}

	public LSN escribirFinTransaccion(Transaccion transaccion) {
		LSN lsn = this.log.escribirFinTransaccion(transaccion);
		this.inspector.agregarEvento(lsn.toString(), "Fin", transaccion.id().toString());
		return lsn;
	}

	public LSN escribirInsert(Transaccion transaccion, ID idRegistro,
			Collection<Valor> valoresNuevos) {
		LSN lsn = this.log.escribirInsert(transaccion, idRegistro, valoresNuevos);
		this.inspector.agregarEvento(lsn.toString(), "Insert", transaccion.id().toString(),
				idRegistro.toString());
		this.inspector.agregarEvento(this.valores(valoresNuevos));
		return lsn;
	}

	public LSN escribirPrepareTransaccion(Transaccion transaccion) {
		LSN lsn = this.log.escribirPrepareTransaccion(transaccion);
		this.inspector.agregarEvento(lsn.toString(), "Prepare", transaccion.id().toString());

		// obtengo los locks exclusivos de esta transaccion
		Set<Id> locks = FabricaLockManager.dameInstancia().locksExclusivos(transaccion.id());
		
		// solo escribo los locks que son de un registro
		Iterator<Id> iterator = locks.iterator();
		while (iterator.hasNext()) {
			Id id = iterator.next();
			if (id instanceof Registro.ID) {
				Registro.ID idRegistro = (Registro.ID)id;
				this.inspector.agregarEvento("Lock:", idRegistro.propietario().propietario().nombre(), 
						"Pag:" + idRegistro.propietario().numeroPagina(), "Reg:" + idRegistro.numeroRegistro());
			}
		}

		return lsn;
	}

	public LSN escribirRollbackTransaccion(Transaccion transaccion) {
		LSN lsn = this.log.escribirRollbackTransaccion(transaccion);
		this.inspector.agregarEvento(lsn.toString(), "Rollback", transaccion.id().toString());
		return lsn;
	}

	public LSN escribirUpdate(Transaccion transaccion, Registro registro,
			Collection<Valor> valoresNuevos) {
		LSN lsn = this.log.escribirUpdate(transaccion, registro, valoresNuevos);
		this.inspector.agregarEvento(lsn.toString(), "Update", transaccion.id().toString(),
				registro.id().toString());
		this.inspector.agregarEvento(this.valores(registro));
		this.inspector.agregarEvento(this.valores(valoresNuevos));
		return lsn;
	}
	
	private String[] valores(Registro registro) {
		List<Valor> valores = registro.getValores(); 
		return this.valores(valores);
	}
	
	private String[] valores(Collection<Valor> valores) {
		List<String> ret = new ArrayList<String>(2 * valores.size());
		for (Valor valor : valores) {
			ret.add(String.valueOf(valor.posicion()));
			ret.add((String) Conversor.conversorATexto().convertir(valor.campo(), valor.contenido()));
		}
		return ret.toArray(new String[ret.size()]);
	}

	public Evento leerEvento(LSN lsn) {
		Evento evento = this.log.leerEvento(lsn);
		if (evento != null) {
			this.inspector.agregarEvento("Read", lsn.toString(), 
					evento.operacion().toString(), 
					"length", 
					String.valueOf(evento.longitud()));
		}
		return evento;
	}

	public LSN escribirBeginCheckpoint() {
		LSN lsn = this.log.escribirBeginCheckpoint();
		this.inspector.agregarEvento(lsn.toString(), "Begin Checkpoint");
		return lsn;
	}

	public LSN escribirEndCheckpoint(Collection<DatoTransaccion> transacciones, Collection<DatoBloqueSucio> paginasSucias) {
		LSN lsn = this.log.escribirEndCheckpoint(transacciones, paginasSucias);
		this.inspector.agregarEvento(lsn.toString(), "End Checkpoint");
		this.inspector.agregarEvento(this.transaccionAString(transacciones));
		this.inspector.agregarEvento(this.paginasAString(paginasSucias));
		return lsn;
	}
	
	private String[] transaccionAString(Collection<DatoTransaccion> transacciones) {
		List<String> strings = new ArrayList<String>();
		for (DatoTransaccion datoTransaccion : transacciones) {
			strings.add(datoTransaccion.idTransaccion.toString());
			strings.add(datoTransaccion.estado.toString());
			strings.add(datoTransaccion.lastLSN.toString());
			strings.add(datoTransaccion.undoNextLSN.toString());
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	private String[] paginasAString(Collection<DatoBloqueSucio> paginas) {
		List<String> strings = new ArrayList<String>();
		for (DatoBloqueSucio paginaSucia : paginas) {
			strings.add(paginaSucia.idPagina.toString());
			strings.add(paginaSucia.recLSN.toString());
		}
		return strings.toArray(new String[strings.size()]);
	}

	public void forzarADisco() {
		this.log.forzarADisco();
	}

	/**
	 * @see servidor.log.Log#escribirCLRDeleteIndex(servidor.transaccion.Transaccion, servidor.indice.hash.RegistroIndice.ID, java.util.Set, servidor.tabla.Registro.ID)
	 */
	public LSN escribirCLRDeleteIndex(Transaccion transaccion, servidor.indice.hash.RegistroIndice.ID idRegistroIndice, Set<LSN> undoNextLSNs, ID idRegistroViejo) {
		LSN lsn = this.log.escribirCLRDeleteIndex(transaccion, idRegistroIndice, undoNextLSNs, idRegistroViejo);
		this.inspector.agregarEvento(lsn.toString(), "CLRDeleteIndex", transaccion.id().toString(),
				idRegistroIndice.toString(), undoNextLSNs.toString());
		this.inspector.agregarEvento(idRegistroViejo.toString());
		return lsn;
	}

	/**
	 * @see servidor.log.Log#escribirCLRInsertIndex(servidor.transaccion.Transaccion, servidor.indice.hash.RegistroIndice.ID, java.util.Set)
	 */
	public LSN escribirCLRInsertIndex(Transaccion transaccion, servidor.indice.hash.RegistroIndice.ID idRegistroIndice, Set<LSN> undoNextLSNs) {
		LSN lsn = this.log.escribirCLRInsertIndex(transaccion, idRegistroIndice, undoNextLSNs);
		this.inspector.agregarEvento(lsn.toString(), "CLRInsertIndex", transaccion.id().toString(), 
				idRegistroIndice.toString(), undoNextLSNs.toString());
		return lsn;
	}

	/**
	 * @see servidor.log.Log#escribirDeleteIndex(servidor.transaccion.Transaccion, servidor.indice.hash.RegistroIndice)
	 */
	public LSN escribirDeleteIndex(Transaccion transaccion, RegistroIndice registroIndice) {
		LSN lsn = this.log.escribirDeleteIndex(transaccion, registroIndice);
		this.inspector.agregarEvento(lsn.toString(), "DeleteIndex", transaccion.id().toString(), 
				registroIndice.id().toString());
		this.inspector.agregarEvento(registroIndice.registroReferenciado().toString());
		return lsn;
	}

	/**
	 * @see servidor.log.Log#escribirInsertIndex(servidor.transaccion.Transaccion, servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	public LSN escribirInsertIndex(Transaccion transaccion, servidor.indice.hash.RegistroIndice.ID idRegistroIndice, ID idRegistro) {
		LSN lsn = this.log.escribirInsertIndex(transaccion, idRegistroIndice, idRegistro);
		this.inspector.agregarEvento(lsn.toString(), "InsertIndex", transaccion.id().toString(),
				idRegistroIndice.toString());
		this.inspector.agregarEvento(idRegistro.toString());
		return lsn;
	}
	
}
 