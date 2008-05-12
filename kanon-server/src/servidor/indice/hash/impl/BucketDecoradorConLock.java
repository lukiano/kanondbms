/**
 * 
 */
package servidor.indice.hash.impl;

import java.util.NoSuchElementException;

import servidor.Id;
import servidor.excepciones.ObjetoBloqueadoException;
import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.lock.LockManager;
import servidor.transaccion.Aislamiento;
import servidor.transaccion.Estado;
import servidor.transaccion.TransactionManager;
import servidor.util.AbstractIteradorDecorador;
import servidor.util.Iterador;

/**
 *
 */
class BucketDecoradorConLock extends AbstractBucketDecorador {
	
	private TransactionManager transactionManager;
	
	private LockManager lockManager;
	
	public BucketDecoradorConLock(Bucket bucket,
            LockManager lockManager, 
            TransactionManager transactionManager) {
		super(bucket);
		this.transactionManager = transactionManager;
		this.lockManager = lockManager;
	}

	private void verificarDentroTransaccion() {
		if (!this.transactionManager.estadoActual().equals(Estado.EN_CURSO)) {
			throw new RuntimeException("No se pueden modificar registros fuera de una Transaccion");
		}
	}

	/**
	 * Metodo para mantener el aislamiento serializable en aquellas transacciones que lo son.
	 */
	private void bloquearPorSerializables() {
		Id id = this.id().propietario();
		if (this.lockManager.estaBloqueado(id, false)) {
			// tenemos lock compartido. (Alguien esta iterando?)
			this.lockManager.bloquear(id, true); // actualizamos a exclusivo
		} else if (this.lockManager.estaBloqueado(id, true)) {
			// ya tenemos lock exclusivo. No se hace nada.
		} else {
			// no tenemos ningun lock, se pide un exclusivo y se suelta
			this.lockManager.bloquear(id, true);
			this.lockManager.desbloquear(id);
		}
	}

	private boolean esAislamientoSerializable() {
		return this.transactionManager.estadoActual().equals(Estado.EN_CURSO) && this.transactionManager.dameTransaccion().aislamiento().equals(Aislamiento.SERIALIZABLE);
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
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	@Override
	public void agregarRegistroIndice(RegistroIndice.ID idRegistroIndice, servidor.tabla.Registro.ID idRegistro) throws RegistroExistenteException {
		this.verificarDentroTransaccion();
		this.bloquearPorSerializables();
		this.lockManager.bloquear(idRegistroIndice, true);
		super.agregarRegistroIndice(idRegistroIndice, idRegistro);
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.tabla.Registro.ID)
	 */
	@Override
	public servidor.indice.hash.RegistroIndice.ID agregarRegistroIndice(servidor.tabla.Registro.ID idRegistro) {
		this.verificarDentroTransaccion();
		this.bloquearPorSerializables();
		while (true) { // cuando sea exitoso retorna del metodo
			RegistroIndice.ID idRegistroIndice = super.dameIDRegistroIndiceLibre(); // aca se latchea la pagina si se encontro uno
			if (idRegistroIndice == null) {
				throw new RuntimeException("Imposible insertar mas indices en el bucket " + this.id());
			}
			boolean pudoBloquear = true;
			try {
				this.lockManager.bloquearCondicional(idRegistroIndice, true);
			} catch (ObjetoBloqueadoException e) {
				pudoBloquear = false;
			}
			if (pudoBloquear) {
				// se pudo bloquear el registro y ademas la pagina esta latcheada
				try {
					super.agregarRegistroIndice(idRegistroIndice, idRegistro); // aca se deslatchea la pagina
					return idRegistroIndice;
				} catch (RegistroExistenteException e) {
					// hay que reintentar
					this.lockManager.desbloquear(idRegistro);
				}
			} else {
				// no pudo bloquear
				super.liberarRegistroIndice(idRegistroIndice); // en este caso libera el latch
				this.lockManager.bloquear(idRegistroIndice, true); // ahora se intenta el bloqueo incondicional
				// se pudo bloquear el registro, pero la pagina no esta latcheada, asi que puede fallar
				try {
					super.agregarRegistroIndice(idRegistroIndice, idRegistro); // aca latchea y deslatchea la pagina
					// fue exitoso
					return idRegistroIndice;
				} catch (RegistroExistenteException e) {
					// hay que reintentar
					this.lockManager.desbloquear(idRegistroIndice);
				}
			}
		}
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	@Override
	public boolean borrarRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		this.verificarDentroTransaccion();
		this.lockManager.bloquear(idRegistroIndice, true);
		return super.borrarRegistroIndice(idRegistroIndice);
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#liberarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	@Override
	public void liberarRegistroIndice(RegistroIndice.ID idRegistroIndice) {
        try {
            super.liberarRegistroIndice(idRegistroIndice);
        } finally {
        	if (this.esAislamientoReadCommitted()) {
                this.lockManager.desbloquear(idRegistroIndice);
        	}
        }
	}

	@Override
	public Iterador<RegistroIndice.ID> dameRegistrosIndice() {
		if (this.esAislamientoSerializable()) {
			this.lockManager.bloquear(this.id(), false);
		}
		Iterador<RegistroIndice.ID> iterador = super.dameRegistrosIndice();
		return new AbstractIteradorDecorador<RegistroIndice.ID>(iterador) {
			
			private RegistroIndice.ID idActual = null;
            
            private boolean consumido = true;
		
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
                	BucketDecoradorConLock.super.liberarRegistroIndice(idActual);
                	if (BucketDecoradorConLock.this.esAislamientoReadCommitted()) {
                		if (BucketDecoradorConLock.this.lockManager.estaBloqueado(this.idActual, false)) {
                			BucketDecoradorConLock.this.lockManager.desbloquear(this.idActual);
                		}
                	}
                }
                while (super.hayProximo()) {
                    this.idActual = super.proximo();
                    if (BucketDecoradorConLock.this.esMasQueAislamientoReadUnCommitted()) {
                    	BucketDecoradorConLock.this.lockManager.bloquear(this.idActual, false);
                    }
                    RegistroIndice registroIndice = BucketDecoradorConLock.super.dameRegistroIndice(this.idActual);
                    if (registroIndice != null && registroIndice.esValido()) {
                        this.consumido = false;
                        return true;
                    }
                    BucketDecoradorConLock.this.lockManager.desbloquear(this.idActual);
                }
                this.idActual = null;
                return false;
            }

            /**
			 * @see servidor.util.AbstractIteradorDecorador#proximo()
			 */
			@Override
			public RegistroIndice.ID proximo() {
                if (this.consumido) {
                    if (this.hayProximo()) {
                        this.consumido = true;
                        if (this.idActual == null) {
                        	System.out.println("ss");
                        }
                        return this.idActual;
                    }
                    throw new NoSuchElementException("Ya no quedan elementos.");
                }
                this.consumido = true;
                if (this.idActual == null) {
                	System.out.println("ss");
                }
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
                    	BucketDecoradorConLock.super.liberarRegistroIndice(idActual);
                    	if (BucketDecoradorConLock.this.esAislamientoReadCommitted()) {
                    		if (BucketDecoradorConLock.this.lockManager.estaBloqueado(this.idActual, false)) {
                    			BucketDecoradorConLock.this.lockManager.desbloquear(this.idActual);
                    		}
                    	}
                    }
                }
			}
		
		};
	}

}
