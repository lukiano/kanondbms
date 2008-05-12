/**
 * 
 */
package servidor.indice.hash.impl;

import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;

/**
 * @author luciano
 *
 */
final class RegistroIndiceImpl implements RegistroIndice {
	
	private ID id;
	
	private servidor.tabla.Registro.ID idRegistroReferenciado;
	
	private final Bucket bucketPropietario;

	/**
	 * 
	 */
	public RegistroIndiceImpl(ID id, servidor.tabla.Registro.ID idRegistroReferenciado, Bucket bucketPropietario) {
		this.id = id;
		this.idRegistroReferenciado = idRegistroReferenciado;
		this.bucketPropietario = bucketPropietario;
	}
	
	public RegistroIndice.ID id() {
		return this.id;
	}

	/**
	 * @see servidor.indice.hash.RegistroIndice#registroReferenciado()
	 */
	public servidor.tabla.Registro.ID registroReferenciado() {
		return this.idRegistroReferenciado;
	}
	
	public boolean esValido() {
		return this.bucketPropietario.esValido();
	}

}
