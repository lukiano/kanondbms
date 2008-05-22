/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;
import java.util.NoSuchElementException;

import servidor.catalog.Valor;
import servidor.excepciones.ObjetoBloqueadoException;
import servidor.excepciones.RegistroExistenteException;
import servidor.lock.LockManager;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.transaccion.Aislamiento;
import servidor.transaccion.Estado;
import servidor.transaccion.TransactionManager;
import servidor.util.AbstractIteradorDecorador;
import servidor.util.Iterador;

/**
 * @author lleggieri
 *
 */
final class TablaDecoradoraConLock extends AbstractTablaDecorador {

	private final class LockIteradorDecorador extends
			AbstractIteradorDecorador<Registro.ID> {
		private Registro.ID idActual;
		private boolean consumido = true;

		private LockIteradorDecorador(Iterador<Registro.ID> iterador) {
			super(iterador);
		}

		/**
		 * @see servidor.util.AbstractIteradorDecorador#hayProximo()
		 */
		@Override
		public boolean hayProximo() {
		    if (!this.consumido) {
		        //no pasar al siguiente hasta que no se consuma el actual
		        return true;
		    }
		    if (this.idActual != null) {
		    	TablaDecoradoraConLock.super.liberarRegistro(idActual);
		    	if (TablaDecoradoraConLock.this.esAislamientoReadCommitted()) {
		    		if (TablaDecoradoraConLock.this.lockManager.estaBloqueado(this.idActual, false)) {
		    			TablaDecoradoraConLock.this.lockManager.desbloquear(this.idActual);
		    		}
		    	}
		    }
		    while (super.hayProximo()) {
		        this.idActual = super.proximo();
		        if (TablaDecoradoraConLock.this.esMasQueAislamientoReadUnCommitted()) {
		        	TablaDecoradoraConLock.this.lockManager.bloquear(this.idActual, false);
		        }
		        Registro registro = TablaDecoradoraConLock.super.registro(this.idActual); 
		        if (registro != null && registro.esValido()) {
		            this.consumido = false;
		            return true;
		        }
		        TablaDecoradoraConLock.this.lockManager.desbloquear(this.idActual);
		    }
		    this.idActual = null;
		    return false;
		}

		/**
		 * @see servidor.util.AbstractIteradorDecorador#proximo()
		 */
		@Override
		public Registro.ID proximo() {
		    if (this.consumido) {
		        if (this.hayProximo()) {
		            this.consumido = true;
		            return this.idActual;
		        }
		        throw new NoSuchElementException("Ya no quedan elementos.");
		    }
		    this.consumido = true;
		    return this.idActual;
		}

		/**
		 * @see servidor.util.AbstractIteradorDecorador#cerrar()
		 */
		@Override
		public void cerrar() {
		    try {
		        super.cerrar();
		    } finally {
		        if (this.idActual != null) {
		        	TablaDecoradoraConLock.super.liberarRegistro(idActual);
		        	if (TablaDecoradoraConLock.this.esAislamientoReadCommitted()) {
		        		if (TablaDecoradoraConLock.this.lockManager.estaBloqueado(this.idActual, false)) {
		        			TablaDecoradoraConLock.this.lockManager.desbloquear(this.idActual);
		        		}
		        	}
		        }
		    }
		}
	}

	private LockManager lockManager;

    private TransactionManager transactionManager;
    
	/**
	 * @param tablaDecorada
	 */
	public TablaDecoradoraConLock(Tabla tablaDecorada, 
            LockManager lockManager, 
            TransactionManager transactionManager) {
		super(tablaDecorada);
		this.lockManager = lockManager;
        this.transactionManager = transactionManager;
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores) {
		this.verificarDentroTransaccion();
		this.lockManager.bloquear(idRegistro, true);
		super.actualizarRegistro(idRegistro, valores);
	}

	private void verificarDentroTransaccion() {
		if (!this.transactionManager.estadoActual().equals(Estado.EN_CURSO)) {
			throw new RuntimeException("No se pueden modificar registros fuera de una Transaccion");
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	@Override
	public boolean borrarRegistro(Registro.ID idRegistro) {
		this.verificarDentroTransaccion();
		this.lockManager.bloquear(idRegistro, true);
		return super.borrarRegistro(idRegistro);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		this.verificarDentroTransaccion();
		this.bloquearPorSerializables();
		this.lockManager.bloquear(idRegistro, true);
		super.insertarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(java.util.Collection)
	 */
	@Override
	public Registro.ID insertarRegistro(Collection<Valor> valores) {
		this.verificarDentroTransaccion();
		this.bloquearPorSerializables();
		while (true) { // cuando sea exitoso retorna del metodo
			Registro.ID idRegistro = super.dameIdRegistroLibre(); // aca se latchea la pagina si se encontro uno
			if (idRegistro == null) {
				throw new RuntimeException("Imposible insertar mas registros en la tabla " + this.id());
			}
			boolean pudoBloquear = true;
			try {
				this.lockManager.bloquearCondicional(idRegistro, true);
			} catch (ObjetoBloqueadoException e) {
				pudoBloquear = false;
			}
			if (pudoBloquear) {
				// se pudo bloquear el registro y ademas la pagina esta latcheada
				try {
					super.insertarRegistro(idRegistro, valores); // aca se deslatchea la pagina
				} catch (RegistroExistenteException e) {
					// hay que reintentar
					this.lockManager.desbloquear(idRegistro);
				}
				return idRegistro;
			} else {
				// no pudo bloquear
				super.liberarRegistro(idRegistro); // en este caso libera el latch
				this.lockManager.bloquear(idRegistro, true); // ahora se intenta el bloqueo incondicional
				// se pudo bloquear el registro, pero la pagina no esta latcheada, asi que puede fallar
				try {
					super.insertarRegistro(idRegistro, valores); // aca latchea y deslatchea la pagina
					// fue exitoso
					return idRegistro;
				} catch (RegistroExistenteException e) {
					// hay que reintentar
					this.lockManager.desbloquear(idRegistro);
				}
			}
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#dameIdRegistroLibre()
	 */
	@Override
	public Registro.ID dameIdRegistroLibre() {
		this.lockManager.bloquear(this.id(), true); // no queda otra que bloquear la tabla
		try {
			return super.dameIdRegistroLibre();
		} finally {
			this.lockManager.desbloquear(this.id()); // se desbloquea independientemente del nivel de aislamiento	
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#registro(servidor.tabla.Registro.ID)
	 */
	@Override
	public Registro registro(Registro.ID idRegistro) {
		if (this.esMasQueAislamientoReadUnCommitted()) {
			this.lockManager.bloquear(idRegistro, false);
		}
		return super.registro(idRegistro);
	}

	private boolean esMasQueAislamientoReadUnCommitted() {
		return this.transactionManager.estadoActual().equals(Estado.EN_CURSO) &&
				!this.transactionManager.dameTransaccion().aislamiento().equals(Aislamiento.READ_UNCOMMITTED);
	}

	private boolean esAislamientoReadCommitted() {
		return this.transactionManager.estadoActual().equals(Estado.EN_CURSO) &&
				this.transactionManager.dameTransaccion().aislamiento().equals(Aislamiento.READ_COMMITTED);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#liberarRegistro(servidor.tabla.Registro.ID)
	 */
	@Override
	public void liberarRegistro(Registro.ID idRegistro) {
        try {
            super.liberarRegistro(idRegistro);
        } finally {
        	if (this.esAislamientoReadCommitted() && this.lockManager.estaBloqueado(idRegistro, false)) {
       			this.lockManager.desbloquear(idRegistro);
        	}
        }
	}

	private boolean esAislamientoSerializable() {
		return this.transactionManager.estadoActual().equals(Estado.EN_CURSO) && this.transactionManager.dameTransaccion().aislamiento().equals(Aislamiento.SERIALIZABLE);
	}

	/**
	 * Metodo para mantener el aislamiento serializable en aquellas transacciones que lo son.
	 */
	private void bloquearPorSerializables() {
		if (this.lockManager.estaBloqueado(this.id(), false)) {
			this.lockManager.bloquear(this.id(), true);
		} else if (this.lockManager.estaBloqueado(this.id(), true)) {
			// ya tenemos lock exclusivo. No se hace nada
		} else {
			// no tenemos ningun lock, se pide un exclusivo y se suelta
			this.lockManager.bloquear(this.id(), true);
			this.lockManager.desbloquear(this.id());
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#registros()
	 */
	@Override
	public Iterador<Registro.ID> registros() {
		if (this.esAislamientoSerializable()) {
			this.lockManager.bloquear(this.id(), false);
		}
		Iterador<Registro.ID> iterador = super.registros();
		return new LockIteradorDecorador(iterador);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#registrosDesde(servidor.tabla.Registro.ID)
	 */
	@Override
	public Iterador<Registro.ID> registrosDesde(Registro.ID idRegistro) {
		if (this.esAislamientoSerializable()) {
			this.lockManager.bloquear(this.id(), false);
		}
		Iterador<Registro.ID> iterador = super.registrosDesde(idRegistro);
		return new LockIteradorDecorador(iterador);
	}

}
