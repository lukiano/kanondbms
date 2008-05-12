/**
 * 
 */
package servidor.indice.hash.impl;

import java.util.NoSuchElementException;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.HashColumna;
import servidor.indice.hash.RegistroIndice;
import servidor.tabla.Campo;
import servidor.tabla.Registro;
import servidor.util.Iterador;
import servidor.util.IteradorVacio;

/**
 *
 */
class HashColumnaImpl implements HashColumna {
	
	private HashColumna.ID idHashColumna;

	private BufferManager bufferManager;
	
	private Campo campo; 
	
	/**
	 * 
	 */
	public HashColumnaImpl(HashColumna.ID id, 
			BufferManager bufferManager,
			Campo campo) {
		this.bufferManager = bufferManager;
		this.campo = campo;
		this.idHashColumna = id;
	}

	/**
	 * @see servidor.indice.hash.HashColumna#actualizarRegistro(servidor.tabla.Registro.ID, java.lang.Object, java.lang.Object)
	 */
	public void actualizarRegistro(servidor.tabla.Registro.ID idRegistro,
			Object viejoValor, Object nuevoValor) {
		int viejoHash = AlgoritmoHash.dameHash(this.campo, viejoValor);
		int nuevoHash = AlgoritmoHash.dameHash(this.campo, nuevoValor);
		if (viejoHash == nuevoHash) {
			// nada para hacer
			return;
		}
		this.borrarRegistro(idRegistro, viejoValor);
		this.agregarRegistro(idRegistro, nuevoValor);
	}

	/**
	 * @see servidor.indice.hash.HashColumna#agregarRegistro(servidor.tabla.Registro.ID, java.lang.Object)
	 */
	public void agregarRegistro(servidor.tabla.Registro.ID idRegistro,
			Object valor) {
		int hash = AlgoritmoHash.dameHash(this.campo, valor);
		int primerBucketNoLleno = 0;
		while (true) {
			Bucket.ID idBucket = Bucket.ID.nuevoID(this.id(), primerBucketNoLleno, hash);
			Bloque bloque = this.bufferManager.dameBloque(idBucket);
			Bucket bucket = FabricaBucket.dameBucket(this.bufferManager, idBucket, bloque);
			if (bucket == null) {
	        	bloque = this.bufferManager.nuevoBloque(idBucket);
	        	bucket = FabricaBucket.dameBucket(this.bufferManager, idBucket, bloque);
	    		try {
	    			bucket.agregarRegistroIndice(idRegistro);
	    		} finally {
	    			this.bufferManager.liberarBloque(idBucket);
	    		}
	    		return;
			} else if (bucket.bucketLleno()) {
				primerBucketNoLleno++;
				this.bufferManager.liberarBloque(idBucket);
			} else {
				try {
					bucket.agregarRegistroIndice(idRegistro);
				} finally {
					this.bufferManager.liberarBloque(idBucket);
				}
				return;
			}
		}
	}

	/**
	 * @see servidor.indice.hash.HashColumna#borrarRegistro(servidor.tabla.Registro.ID, java.lang.Object)
	 */
	public boolean borrarRegistro(servidor.tabla.Registro.ID idRegistro,
			Object valor) {
		// Hay que iterar por todos los bucket para ver en cual se borra el registro
		int hash = AlgoritmoHash.dameHash(this.campo, valor);
		int numeroBucket = 0;
		while (true) {
			Bucket.ID idBucket = Bucket.ID.nuevoID(this.id(), numeroBucket, hash);
			Bloque bloque = this.bufferManager.dameBloque(idBucket);
			Bucket bucket = FabricaBucket.dameBucket(this.bufferManager, idBucket, bloque);
			if (bucket == null) {
				return false;
			}
			try {
				if (this.intentarBorrarRegistro(bucket, idRegistro)) {
					return true;
				}
			} finally {
				this.bufferManager.liberarBloque(idBucket);
			}
			numeroBucket++;
		}
	}

	private boolean intentarBorrarRegistro(Bucket bucket, Registro.ID idRegistroABorrar) {
		Iterador<RegistroIndice.ID> iterador = bucket.dameRegistrosIndice();
		try {
			while (iterador.hayProximo()) {
				RegistroIndice.ID idProximoRegIndice = iterador.proximo();
				RegistroIndice registroIndice = bucket.dameRegistroIndice(idProximoRegIndice);
				if (registroIndice != null) {
					Registro.ID idRegistro = registroIndice.registroReferenciado();
					try {
						if (idRegistroABorrar.equals(idRegistro)) {
							bucket.borrarRegistroIndice(idProximoRegIndice);
							return true;
						}
					} finally {
						bucket.liberarRegistroIndice(idProximoRegIndice);
					}
				}
			}
		} finally {
			iterador.cerrar();
		}
		return false;
	}

	/**
	 * @see servidor.indice.hash.HashColumna#dameRegistros(java.lang.Object)
	 */
	public Iterador<servidor.tabla.Registro.ID> dameRegistros(Object valor) {
		int hash = AlgoritmoHash.dameHash(this.campo, valor);
		return new IteradorColumna(0, hash);
	}

	/**
	 * @see servidor.indice.hash.HashColumna#id()
	 */
	public ID id() {
		return this.idHashColumna;
	}

	private final class IteradorColumna implements Iterador<servidor.tabla.Registro.ID> {
		
		private int numeroBucket;
		
		private int hash;
		
		private Iterador<servidor.tabla.Registro.ID> iteradorActual;
		
		private Bucket.ID bucketActual;
		
		private boolean fin;

		/**
		 * @param nroPrimerBucket el numero del primer bucket a iterar.
		 * @param hash el numero de hash de los buckets a iterar.
		 */
		public IteradorColumna(int nroPrimerBucket, int hash) {
			this.numeroBucket = nroPrimerBucket;
			this.hash = hash;
			Bucket.ID idBucket = Bucket.ID.nuevoID(HashColumnaImpl.this.idHashColumna, this.numeroBucket, this.hash);
			Bloque bloque = HashColumnaImpl.this.bufferManager.dameBloque(idBucket);
			Bucket bucket = FabricaBucket.dameBucket(HashColumnaImpl.this.bufferManager, idBucket, bloque);
			if (bucket == null) {
				this.iteradorActual = IteradorVacio.dameIteradorVacio();
				this.fin = true;
			} else {
				this.bucketActual = idBucket;
				this.iteradorActual = this.dameRegistrosDeBucket(bucket);
			}
		}

		private Iterador<servidor.tabla.Registro.ID> dameRegistrosDeBucket(Bucket bucket) {
			Iterador<RegistroIndice.ID> iteradorIndices = bucket.dameRegistrosIndice();
			return new RegistroIndiceIDARegistroIDIterador(iteradorIndices, bucket);
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
				this.numeroBucket++;
				HashColumnaImpl.this.bufferManager.liberarBloque(this.bucketActual);
				this.bucketActual = Bucket.ID.nuevoID(HashColumnaImpl.this.idHashColumna, this.numeroBucket, this.hash);
				Bloque bloque = HashColumnaImpl.this.bufferManager.dameBloque(this.bucketActual);
				Bucket bucket = FabricaBucket.dameBucket(HashColumnaImpl.this.bufferManager, this.bucketActual, bloque);
				if (bucket == null) {
					this.bucketActual = null;
					this.iteradorActual = IteradorVacio.dameIteradorVacio();
					this.fin = true;
				} else {
					this.iteradorActual = this.dameRegistrosDeBucket(bucket);
				}
				return this.hayProximo();
			}
		}

		public servidor.tabla.Registro.ID proximo() {
			if (this.hayProximo()) {
				return this.iteradorActual.proximo();
			}
			throw new NoSuchElementException("No hay mas registros en este indice" + HashColumnaImpl.this.id());
		}

		/**
		 * @see servidor.util.Iterador#cerrar()
		 */
		public void cerrar() {
			if (this.bucketActual != null) {
				HashColumnaImpl.this.bufferManager.liberarBloque(this.bucketActual);
			}
		}
		
	}

}
