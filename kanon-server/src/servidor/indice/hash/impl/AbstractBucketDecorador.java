/**
 * 
 */
package servidor.indice.hash.impl;

import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.log.LSN;
import servidor.util.Iterador;

/**
 *
 */
abstract class AbstractBucketDecorador implements Bucket {
	
	private Bucket bucket;

	/**
	 * 
	 */
	public AbstractBucketDecorador(Bucket bucket) {
		this.bucket = bucket;
	}

	/**
	 * @see servidor.indice.hash.Bucket#agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	public void agregarRegistroIndice(
			servidor.indice.hash.RegistroIndice.ID idRegistroIndice,
			servidor.tabla.Registro.ID idRegistro) throws RegistroExistenteException {
		this.bucket.agregarRegistroIndice(idRegistroIndice, idRegistro);
	}

	/**
	 * @see servidor.indice.hash.Bucket#agregarRegistroIndice(servidor.tabla.Registro.ID)
	 */
	public RegistroIndice.ID  agregarRegistroIndice(servidor.tabla.Registro.ID idRegistro) {
		return this.bucket.agregarRegistroIndice(idRegistro);
	}

	/**
	 * @see servidor.indice.hash.Bucket#borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	public boolean borrarRegistroIndice(
			servidor.indice.hash.RegistroIndice.ID idRegistroIndice) {
		return this.bucket.borrarRegistroIndice(idRegistroIndice);
	}

	/**
	 * @see servidor.indice.hash.Bucket#bucketLleno()
	 */
	public boolean bucketLleno() {
		return this.bucket.bucketLleno();
	}

	/**
	 * @see servidor.indice.hash.Bucket#dameIDRegistroIndiceLibre()
	 */
	public RegistroIndice.ID dameIDRegistroIndiceLibre() {
		return this.bucket.dameIDRegistroIndiceLibre();
	}

	/**
	 * @see servidor.indice.hash.Bucket#dameRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	public RegistroIndice dameRegistroIndice(
			servidor.indice.hash.RegistroIndice.ID idRegistro) {
		return this.bucket.dameRegistroIndice(idRegistro);
	}

	/**
	 * @see servidor.indice.hash.Bucket#dameRegistrosIndice()
	 */
	public Iterador<servidor.indice.hash.RegistroIndice.ID> dameRegistrosIndice() {
		return this.bucket.dameRegistrosIndice();
	}

	/**
	 * @see servidor.indice.hash.Bucket#esValido()
	 */
	public boolean esValido() {
		return this.bucket.esValido();
	}

	/**
	 * @see servidor.indice.hash.Bucket#id()
	 */
	public ID id() {
		return this.bucket.id();
	}

	/**
	 * @see servidor.indice.hash.Bucket#liberarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	public void liberarRegistroIndice(
			servidor.indice.hash.RegistroIndice.ID idRegistro) {
		this.bucket.liberarRegistroIndice(idRegistro);
	}

	/**
	 * @see servidor.indice.hash.Bucket#actualizarRecoveryLSN(servidor.log.LSN)
	 */
	public void actualizarRecoveryLSN(LSN nuevoLSN) {
		this.bucket.actualizarRecoveryLSN(nuevoLSN);
	}

	/**
	 * @see servidor.indice.hash.Bucket#recoveryLSN()
	 */
	public LSN recoveryLSN() {
		return this.bucket.recoveryLSN();
	}

}
