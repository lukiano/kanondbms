/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;
import java.util.NoSuchElementException;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.tabla.Columna;
import servidor.tabla.FabricaPagina;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;
import servidor.util.Iterador;
import servidor.util.IteradorVacio;

/**
 * @author lleggieri
 *
 */
class TablaImpl implements Tabla {

	// esta implementacion no debe contener ninguna variable de estado.
   
    private Catalogo catalogo;
	
	private Tabla.ID idTabla;
	
	private BufferManager bufferManager;
	
	private Columna[] columnas;
	
	private Log log;
	
	private TransactionManager transactionManager;
	
	/**
	 * 
	 */
	public TablaImpl(Tabla.ID idTabla, BufferManager bufferManager,
			Log log, TransactionManager transactionManager,
			Columna[] columnas) {
        this.catalogo = FabricaCatalogo.dameInstancia();
		this.idTabla = idTabla;
		this.bufferManager = bufferManager;
		this.columnas = columnas;
		this.log = log;
		this.transactionManager = transactionManager;
	}
	
	private Transaccion dameTransaccionActual() {
		return this.transactionManager.dameTransaccion();
	}

	/**
	 * @see servidor.tabla.Tabla#id()
	 */
	public ID id() {
		return this.idTabla;
	}

	/**
	 * @see servidor.tabla.Tabla#columnas()
	 */
	public Columna[] columnas() {
		//return this.catalogo.columnasDeTabla(this.id().propietario(), this.id().nombre());
		return this.columnas.clone();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registros()
	 */
	public Iterador<Registro.ID> registros() {
		return new IteradorTabla(0);
	}
	
	/**
	 * @see servidor.tabla.OperaRegistros#registro(servidor.tabla.Registro.ID)
	 */
	public Registro registro(servidor.tabla.Registro.ID idRegistro) {
		this.chequearIdRegistro(idRegistro);
		Pagina.ID idPagina = idRegistro.propietario();
		Bloque bloque = this.bufferManager.dameBloque(idPagina);
		Pagina pagina = FabricaPagina.damePagina(this.bufferManager, this.columnas(), idPagina, bloque);
		return pagina.registro(idRegistro);
	}

	private void chequearIdRegistro(servidor.tabla.Registro.ID idRegistro) {
		if (!this.id().equals(idRegistro.propietario().propietario())) {
			throw new RuntimeException("Se quiere operar con el registro de otra tabla.");
		}
	}

	/**
	 * @see servidor.tabla.OperaRegistros#liberarRegistro(servidor.tabla.Registro.ID)
	 */
	public void liberarRegistro(servidor.tabla.Registro.ID idRegistro) {
		this.chequearIdRegistro(idRegistro);
		this.bufferManager.liberarBloque(idRegistro.propietario());
	}

	/**
	 * @see servidor.tabla.OperaRegistros#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	public void actualizarRegistro(servidor.tabla.Registro.ID idRegistro,
			Collection<Valor> valores) {
		this.chequearIdRegistro(idRegistro);
		Pagina.ID idPagina = idRegistro.propietario();
		Bloque bloque = this.bufferManager.dameBloque(idPagina);
		try {
			Pagina pagina = FabricaPagina.damePagina(this.bufferManager, this.columnas(), idPagina, bloque);
			Registro registro = pagina.registro(idRegistro);
			LSN lsn;
			try {
				lsn = this.log.escribirUpdate(this.dameTransaccionActual(), registro, valores);
			} finally {
				pagina.liberarRegistro(idRegistro);
			}
			this.actualizarLSN(pagina, lsn);
			pagina.actualizarRegistro(idRegistro, valores);
		} finally {
	        this.bufferManager.liberarBloque(idPagina);
		}
	}

	/**
	 * @see servidor.tabla.OperaRegistros#insertarRegistro(java.util.Collection)
	 */
	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		Registro.ID idRegistro = this.dameIdRegistroLibre();
		try {
			this.insertarRegistro(idRegistro, valores);
		} catch (RegistroExistenteException e) {
			throw new RuntimeException(e);
		}
		return idRegistro;
	}

	/**
	 * @see servidor.tabla.OperaRegistros#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		this.chequearIdRegistro(idRegistro);
		Pagina.ID idPagina = idRegistro.propietario();
		Bloque bloque = this.bufferManager.dameBloque(idPagina);
		Pagina pagina = FabricaPagina.damePagina(this.bufferManager, this.columnas(), idPagina, bloque);
        if (pagina == null) {
        	throw new RuntimeException("La pagina del registro " + idRegistro + " no existe!");
        }
		try {
			LSN lsn = this.log.escribirInsert(this.dameTransaccionActual(), idRegistro, valores);
			this.actualizarLSN(pagina, lsn);
			pagina.insertarRegistro(idRegistro, valores);
		} finally {
			this.bufferManager.liberarBloque(idPagina);
		}
	}

	/**
	 * @see servidor.tabla.OperaRegistros#dameIdRegistroLibre()
	 */
	public servidor.tabla.Registro.ID dameIdRegistroLibre() {
		int primerPaginaNoLlena = 0;
		while (true) {
			Pagina.ID idPagina = Pagina.ID.nuevoID(this.id(), primerPaginaNoLlena);
			Bloque bloque = this.bufferManager.dameBloque(idPagina);
			Pagina pagina = FabricaPagina.damePagina(this.bufferManager, this.columnas(), idPagina, bloque);
			if (pagina == null) {
	        	bloque = this.bufferManager.nuevoBloque(idPagina);
	        	pagina = FabricaPagina.damePagina(this.bufferManager, this.columnas(), idPagina, bloque);
	            this.actualizarTablaPaginas(idPagina.numeroPagina());
	    		try {
	    			Registro.ID idRegistro = pagina.dameIdRegistroLibre();
	    			if (idRegistro != null) {
	    				return idRegistro;
	    			}
	    		} finally {
	    			this.bufferManager.liberarBloque(idPagina);
	    		}
			} else if (pagina.paginaLlena()) {
				primerPaginaNoLlena++;
				this.bufferManager.liberarBloque(idPagina);
			} else {
				try {
	    			Registro.ID idRegistro = pagina.dameIdRegistroLibre();
	    			if (idRegistro != null) {
	    				return idRegistro;
	    			}
				} finally {
					this.bufferManager.liberarBloque(idPagina);
				}
			}
		}
	}

	private void actualizarTablaPaginas(int nroPagina) {
		this.catalogo.actualizarTablaPaginas(this.id().nombre(), nroPagina);
		
	}

	/**
	 * @see servidor.tabla.OperaRegistros#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	public boolean borrarRegistro(servidor.tabla.Registro.ID idRegistro) {
		this.chequearIdRegistro(idRegistro);
		Pagina.ID idPagina = idRegistro.propietario();
		Bloque bloque = this.bufferManager.dameBloque(idPagina);
		Pagina pagina = FabricaPagina.damePagina(this.bufferManager, this.columnas(), idPagina, bloque);
		if (pagina == null) {
			return false;
		}
		try {
			Registro registro = pagina.registro(idRegistro);
			LSN lsn;
			try {
				 lsn = this.log.escribirDelete(this.dameTransaccionActual(), registro);
			} finally {
				pagina.liberarRegistro(idRegistro);
			}
			this.actualizarLSN(pagina, lsn);
			return pagina.borrarRegistro(idRegistro);
		} finally {
			this.bufferManager.liberarBloque(idPagina);
		}
	}
	
	/**
	 * TODO: Documentar esto, segun ARIES (pag 114, en Transaction Table)
	 * @param pagina
	 * @param lsn
	 */
	private void actualizarLSN(Pagina pagina, LSN lsn) {
		pagina.actualizarRecoveryLSN(lsn);
		Transaccion transaccion = this.transactionManager.dameTransaccion(); 
		transaccion.establecerUltimoLSN(lsn);
		transaccion.establecerUndoNextLSN(lsn);
	}

	private final class IteradorTabla implements Iterador<Registro.ID> {
		
		private int nroPagina;
		
		private Iterador<Registro.ID> iteradorActual;
		
		private Pagina.ID paginaActual;
		
		private boolean fin;

		/**
		 * @param nroPrimerPagina
		 */
		public IteradorTabla(int nroPrimerPagina) {
			this.nroPagina = nroPrimerPagina;
			Pagina.ID idPagina = Pagina.ID.nuevoID(TablaImpl.this.idTabla, this.nroPagina);
			Bloque bloque = TablaImpl.this.bufferManager.dameBloque(idPagina);
			Pagina pagina = FabricaPagina.damePagina(TablaImpl.this.bufferManager, TablaImpl.this.columnas(), idPagina, bloque);
			if (pagina == null) {
				this.iteradorActual = IteradorVacio.dameIteradorVacio();
				this.fin = true;
			} else {
				this.paginaActual = idPagina;
				this.iteradorActual = pagina.registros();
			}
		}

		/**
		 * @see servidor.util.Iterador#hayProximo()
		 */
		public boolean hayProximo() {
			if (this.fin) {
				return false;
			} else if (this.iteradorActual.hayProximo()) {
				return true;
			} else {
				this.nroPagina++;
				TablaImpl.this.bufferManager.liberarBloque(this.paginaActual);
				this.paginaActual = Pagina.ID.nuevoID(TablaImpl.this.idTabla, this.nroPagina);
				Bloque bloque = TablaImpl.this.bufferManager.dameBloque(this.paginaActual);
				Pagina pagina = FabricaPagina.damePagina(TablaImpl.this.bufferManager, TablaImpl.this.columnas(), this.paginaActual, bloque);
				if (pagina == null) {
					this.paginaActual = null;
					this.iteradorActual = IteradorVacio.dameIteradorVacio();
					this.fin = true;
				} else {
					this.iteradorActual = pagina.registros();
				}
				return this.hayProximo();
			}
		}

		public Registro.ID proximo() {
			if (this.hayProximo()) {
				return this.iteradorActual.proximo();
			}
			throw new NoSuchElementException("No hay mas registros en esta tabla" + TablaImpl.this.id());
		}

		/**
		 * @see servidor.util.Iterador#cerrar()
		 */
		public void cerrar() {
			if (this.paginaActual != null) {
				TablaImpl.this.bufferManager.liberarBloque(this.paginaActual);
			}
		}
		
	}

}
