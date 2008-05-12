package servidor.log.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import servidor.Id;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.indice.hash.RegistroIndice;
import servidor.inspector.Inspector;
import servidor.inspector.VisorPorConexion;
import servidor.lock.FabricaLockManager;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.log.Operacion;
import servidor.log.impl.estructuras.DatoBloqueSucio;
import servidor.log.impl.estructuras.DatoTransaccion;
import servidor.log.impl.eventos.Evento;
import servidor.tabla.Campo;
import servidor.tabla.Registro;
import servidor.tabla.Registro.ID;
import servidor.transaccion.Transaccion;

public class InspectorLogParaCliente implements Log {
	
	private Log log;
	
	private Inspector inspector = new Inspector(new VisorPorConexion(4447, "Log"));

	public InspectorLogParaCliente(Log log) {
		this.log = log;
	}

	public boolean borrarLog() {
		return this.log.borrarLog();
	}

	public void cerrar() {
		this.log.cerrar();
	}

	public LSN dameLSNMaestro() {
		return this.log.dameLSNMaestro();
	}
	
	public void escribirMasterRecord(LSN lsn) {
		this.log.escribirMasterRecord(lsn);
	}

	public LSN escribirCLRDelete(Transaccion transaccion, ID idRegistro,
			Set<LSN> undoNextLSNs, Collection<Valor> valoresViejos) {
		LSN lsn = this.log.escribirCLRDelete(transaccion, idRegistro, undoNextLSNs, valoresViejos);
		this.inspector.agregarEvento(this.eventoAString(Operacion.CLR_DELETE, lsn, transaccion));
		this.inspector.agregarEvento(this.undoNextLSN_AString(transaccion.undoNextLSN()));
		this.inspector.agregarEvento(this.registroAString(idRegistro));
		for (Valor valor : valoresViejos) {
			this.inspector.agregarEvento(this.valores(valor.posicion(), valor.campo(), valor.contenido(), null));
		}
		return lsn;
	}
	
	public LSN escribirDummyCLR(Transaccion transaccion, LSN undoNextLSN) {
		LSN lsn = this.log.escribirDummyCLR(transaccion, undoNextLSN);
		this.inspector.agregarEvento(this.eventoAString(Operacion.DUMMY_CLR, lsn, transaccion));
		this.inspector.agregarEvento(this.undoNextLSN_AString(java.util.Collections.singleton(undoNextLSN)));
		return lsn;
	}

	public LSN escribirCLRInsert(Transaccion transaccion, ID idRegistro,
			Set<LSN> undoNextLSNs) {
		LSN lsn = this.log.escribirCLRInsert(transaccion, idRegistro, undoNextLSNs);
		this.inspector.agregarEvento(this.eventoAString(Operacion.CLR_INSERT, lsn, transaccion));
		this.inspector.agregarEvento(this.undoNextLSN_AString(transaccion.undoNextLSN()));
		this.inspector.agregarEvento(this.registroAString(idRegistro));
		return lsn;
	}

	public LSN escribirCLRUpdate(Transaccion transaccion, ID idRegistro,
			Set<LSN> undoNextLSNs, Collection<Valor> valoresViejos) {
		LSN lsn = this.log.escribirCLRUpdate(transaccion, idRegistro, undoNextLSNs, valoresViejos);
		this.inspector.agregarEvento(this.eventoAString(Operacion.CLR_UPDATE, lsn, transaccion));
		this.inspector.agregarEvento(this.undoNextLSN_AString(transaccion.undoNextLSN()));
		this.inspector.agregarEvento(this.registroAString(idRegistro));
		for (Valor valor : valoresViejos) {
			this.inspector.agregarEvento(this.valores(valor.posicion(), valor.campo(), valor.contenido(), null));
		}
		return lsn;
	}

	public LSN escribirChildCommittedTransaccion(Transaccion transaccion,
			Transaccion transaccionHija) {
		LSN lsn = this.log.escribirChildCommittedTransaccion(transaccion, transaccionHija);
		this.inspector.agregarEvento(this.eventoAString(Operacion.CHILD_COMMITTED, lsn, transaccion));
		this.inspector.agregarEvento(
				"", 
				"",
				"IdChildTx:" + String.valueOf(transaccionHija.id().numeroTransaccion()),
				"LastLSN:" + String.valueOf(transaccionHija.ultimoLSN().lsn()),
				"");

		return lsn;
	}

	public LSN escribirDelete(Transaccion transaccion, Registro registro) {
		LSN lsn = this.log.escribirDelete(transaccion, registro);
		this.inspector.agregarEvento(this.eventoAString(Operacion.DELETE, lsn, transaccion));
		this.inspector.agregarEvento(this.registroAString(registro.id()));
		for (Valor valor : registro.getValores()) {
			this.inspector.agregarEvento(this.valores(valor.posicion(), valor.campo(), valor.contenido(), null));
		}
		return lsn;
	}

	public LSN escribirFinTransaccion(Transaccion transaccion) {
		LSN lsn = this.log.escribirFinTransaccion(transaccion);
		this.inspector.agregarEvento(this.eventoAString(Operacion.END, lsn, transaccion));
		return lsn;
	}

	public LSN escribirInsert(Transaccion transaccion, ID idRegistro,
			Collection<Valor> valoresNuevos) {
		LSN lsn = this.log.escribirInsert(transaccion, idRegistro, valoresNuevos);
		this.inspector.agregarEvento(this.eventoAString(Operacion.INSERT, lsn, transaccion));
		this.inspector.agregarEvento(this.registroAString(idRegistro));
		for (Valor valor : valoresNuevos) {
			this.inspector.agregarEvento(this.valores(valor.posicion(), valor.campo(), null, valor.contenido()));
		}
		return lsn;
	}

	public LSN escribirPrepareTransaccion(Transaccion transaccion) {
		LSN lsn = this.log.escribirPrepareTransaccion(transaccion);
		this.inspector.agregarEvento(this.eventoAString(Operacion.PREPARE, lsn, transaccion));
		
		// obtengo los locks exclusivos de esta transaccion
		Set<Id> locks = FabricaLockManager.dameInstancia().locksExclusivos(transaccion.id());
		
		// solo escribo los locks que son de un registro
		Iterator<Id> iterator = locks.iterator();
		while (iterator.hasNext()) {
			Id id = iterator.next();
			if (id instanceof Registro.ID) {
				Registro.ID idRegistro = (Registro.ID)id;
				this.inspector.agregarEvento(this.registroLockAString(idRegistro));
			}
		}
		
		return lsn;
	}

	public LSN escribirRollbackTransaccion(Transaccion transaccion) {
		LSN lsn = this.log.escribirRollbackTransaccion(transaccion);
		this.inspector.agregarEvento(this.eventoAString(Operacion.ROLLBACK, lsn, transaccion));
		return lsn;
	}

	public LSN escribirUpdate(Transaccion transaccion, Registro registro,
			Collection<Valor> valoresNuevos) {
		LSN lsn = this.log.escribirUpdate(transaccion, registro, valoresNuevos);
		this.inspector.agregarEvento(this.eventoAString(Operacion.UPDATE, lsn, transaccion));
		this.inspector.agregarEvento(this.registroAString(registro.id()));
		for (Valor valor : valoresNuevos) {
			this.inspector.agregarEvento(
					this.valores(valor.posicion(), valor.campo(), registro.valor(valor.posicion()), valor.contenido())
					);
		}
		return lsn;
	}
	
	public Evento leerEvento(LSN lsn) {
		Evento evento = this.log.leerEvento(lsn);
		if (evento != null) {
			this.inspector.agregarEvento("Read: " + String.valueOf(lsn.lsn()), 
					evento.operacion().toString(), 
					"nextLSN:" + String.valueOf(lsn.lsn() + evento.longitud())
					);
		}
		return evento;
	}

	public LSN escribirBeginCheckpoint() {
		LSN lsn = this.log.escribirBeginCheckpoint();
		this.inspector.agregarEvento(this.eventoAString(Operacion.BEGIN_CHECKPOINT, lsn));
		return lsn;
	}

	public LSN escribirEndCheckpoint(Collection<DatoTransaccion> transacciones, Collection<DatoBloqueSucio> paginasSucias) {
		LSN lsn = this.log.escribirEndCheckpoint(transacciones, paginasSucias);
		this.inspector.agregarEvento(this.eventoAString(Operacion.END_CHECKPOINT, lsn));
		for (DatoTransaccion datoTransaccion : transacciones) {
			this.inspector.agregarEvento(this.transaccionAString(datoTransaccion));
			this.inspector.agregarEvento(this.undoNextLSN_AString(datoTransaccion.undoNextLSN));
			
			// obtengo los locks exclusivos de esta transaccion
			Set<Registro.ID> locks = datoTransaccion.registrosBloqueados;
			
			for (Registro.ID idRegistro : locks) {
				this.inspector.agregarEvento(this.registroLockAString(idRegistro));
			}
			
		}
		for (DatoBloqueSucio datoPaginaSucia : paginasSucias) {
			this.inspector.agregarEvento(this.paginasAString(datoPaginaSucia));
		}
		return lsn;
	}
	
	private String[] eventoAString(Operacion operacion, LSN lsn) {
		return new String[] {
				String.valueOf(lsn.lsn()), 
				operacion.toString(),
				"",
				"",
				""};
	}

	private String[] eventoAString(Operacion operacion, LSN lsn, Transaccion transaccion) {
		return new String[] {
				String.valueOf(lsn.lsn()), 
				operacion.toString(),
				"IdTx: " + String.valueOf(transaccion.id().numeroTransaccion()),
				"PrevLSN:" + String.valueOf(transaccion.ultimoLSN().lsn()),
				""};
	}

	private String[] transaccionAString(DatoTransaccion datoTransaccion) {
		String[] strings = new String[5];
		strings[0] = "";
		strings[1] = "IdTx: " + String.valueOf(datoTransaccion.idTransaccion.numeroTransaccion());
		strings[2] = datoTransaccion.estado.name();
		strings[3] = "LastLSN:" + String.valueOf(datoTransaccion.lastLSN.lsn());
		strings[4] = "";
		return strings;
	}

	private String[] undoNextLSN_AString(Set<LSN> undoNextLSN) {
		String[] strings = new String[5];
		strings[0] = "";
		strings[1] = "";
		Iterator<LSN> iterator = undoNextLSN.iterator();
		if (iterator.hasNext()) {
			strings[2] = String.valueOf(iterator.next().lsn());
		} else {
			strings[2] = "";
		}
		if (iterator.hasNext()) {
			strings[3] = String.valueOf(iterator.next().lsn());
		} else {
			strings[3] = "";
		}
		if (iterator.hasNext()) {
			strings[4] = String.valueOf(iterator.next().lsn());
		} else {
			strings[4] = "";
		}
		return strings;
	}

	private String[] paginasAString(DatoBloqueSucio pagina) {
		String[] strings = new String[5];
		strings[0] = "";
		strings[1] = pagina.idPagina.propietario().nombre();
		strings[2] = "DrtPage:" + String.valueOf(pagina.idPagina.numeroPagina());
		strings[3] = "RecLSN:" + String.valueOf(pagina.recLSN.lsn());
		strings[4] = "";
		return strings;
	}
	
	private String[] registroAString(Registro.ID idRegistro) {
		String[] strings = new String[5];
		strings[0] = "";
		strings[1] = idRegistro.propietario().propietario().nombre();
		strings[2] = "Page: " + String.valueOf(idRegistro.propietario().numeroPagina());
		strings[3] = "Rec: " + String.valueOf(idRegistro.numeroRegistro());
		strings[4] = "";
		return strings;
	}

//	private String[] registro2AString(Registro.ID idRegistro) {
//		String[] strings = new String[5];
//		strings[0] = "";
//		strings[1] = "";
//		strings[2] = idRegistro.propietario().propietario().nombre();
//		strings[3] = "Pag: " + String.valueOf(idRegistro.propietario().numeroPagina());
//		strings[4] = "Reg: " + String.valueOf(idRegistro.numeroRegistro());
//		return strings;
//	}
//
//	private String[] registroIndiceAString(RegistroIndice.ID idRegistroIndice) {
//		String[] strings = new String[5];
//		strings[0] = "";
//		strings[1] = idRegistroIndice.propietario().propietario().propietario().nombre();
//		strings[2] = "Col: " + String.valueOf(idRegistroIndice.propietario().propietario().columna());
//		strings[3] = "Buc: " + String.valueOf(idRegistroIndice.propietario().numeroBucket());
//		strings[4] = "Ind: " + String.valueOf(idRegistroIndice.numeroRegistroIndice());
//		return strings;
//	}

	private String[] registroLockAString(Registro.ID idRegistro) {
		String[] strings = new String[5];
		strings[0] = "";
		strings[1] = "Lock:";
		strings[2] = idRegistro.propietario().propietario().nombre();
		strings[3] = "Page: " + String.valueOf(idRegistro.propietario().numeroPagina());
		strings[4] = "Rec: " + String.valueOf(idRegistro.numeroRegistro());
		return strings;
	}

	private String[] valores(int posicion, Campo campo, Object contenidoViejo, Object contenidoNuevo) {
		String[] strings = new String[5];
		strings[0] = "";
		strings[1] = "";
		strings[2] = "Col: " + String.valueOf(posicion);
		if (contenidoViejo == null) {
			strings[3] = "";
		} else {
			strings[3] = (String) Conversor.conversorATexto().convertir(campo, contenidoViejo);
		}
		if (contenidoNuevo == null) {
			strings[4] = "";
		} else {
			strings[4] = (String) Conversor.conversorATexto().convertir(campo, contenidoNuevo);
		}
		return strings;
	}

	public void forzarADisco() {
		this.log.forzarADisco();
	}

	public LSN escribirCLRDeleteIndex(Transaccion transaccion, servidor.indice.hash.RegistroIndice.ID idRegistroIndice, Set<LSN> undoNextLSNs, ID idRegistroViejo) {
		LSN lsn = this.log.escribirCLRDeleteIndex(transaccion, idRegistroIndice, undoNextLSNs, idRegistroViejo);
		/*this.inspector.agregarEvento(this.eventoAString(Operacion.CLR_DELETE_INDEX, lsn, transaccion));
		this.inspector.agregarEvento(this.undoNextLSN_AString(transaccion.undoNextLSN()));
		this.inspector.agregarEvento(this.registroIndiceAString(idRegistroIndice));
		this.inspector.agregarEvento(this.registro2AString(idRegistroViejo));*/
		return lsn;
	}

	public LSN escribirCLRInsertIndex(Transaccion transaccion, servidor.indice.hash.RegistroIndice.ID idRegistroIndice, Set<LSN> undoNextLSNs) {
		LSN lsn = this.log.escribirCLRInsertIndex(transaccion, idRegistroIndice, undoNextLSNs);
		/*this.inspector.agregarEvento(this.eventoAString(Operacion.CLR_INSERT_INDEX, lsn, transaccion));
		this.inspector.agregarEvento(this.undoNextLSN_AString(transaccion.undoNextLSN()));
		this.inspector.agregarEvento(this.registroIndiceAString(idRegistroIndice));*/
		return lsn;
	}

	public LSN escribirDeleteIndex(Transaccion transaccion, RegistroIndice registroIndice) {
		LSN lsn = this.log.escribirDeleteIndex(transaccion, registroIndice);
		/*this.inspector.agregarEvento(this.eventoAString(Operacion.DELETE_INDEX, lsn, transaccion));
		this.inspector.agregarEvento(this.registroIndiceAString(registroIndice.id()));
		this.inspector.agregarEvento(this.registro2AString(registroIndice.registroReferenciado()));*/
		return lsn;
	}

	public LSN escribirInsertIndex(Transaccion transaccion, servidor.indice.hash.RegistroIndice.ID idRegistroIndice, ID idRegistro) {
		LSN lsn = this.log.escribirInsertIndex(transaccion, idRegistroIndice, idRegistro);
		/*this.inspector.agregarEvento(this.eventoAString(Operacion.INSERT_INDEX, lsn, transaccion));
		this.inspector.agregarEvento(this.registroIndiceAString(idRegistroIndice));
		this.inspector.agregarEvento(this.registro2AString(idRegistro));*/
		return lsn;
	}
	
}
 