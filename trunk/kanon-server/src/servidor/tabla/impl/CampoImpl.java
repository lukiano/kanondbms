/**
 * 
 */
package servidor.tabla.impl;

import servidor.catalog.tipo.Tipo;
import servidor.tabla.Campo;

/**
 * Implementacion basica de la Interfaz Campo.
 */
public final class CampoImpl implements Campo {

	/**
	 * el tipo de datos de este campo.
	 */
	private Tipo tipo;
	
	/**
	 * la longitud para los tipos de longitud variable.
	 */
	private int longitud;
	
	/**
	 * Constructor de la clase.
	 * @param tipo el tipo de datos de este campo.
	 * @param longitud la longitud para los tipos de longitud variable.
	 */
	public CampoImpl(Tipo tipo, int longitud) {
		this.tipo = tipo;
		this.longitud = longitud;
	}

	/**
	 * @see servidor.tabla.Campo#longitud()
	 */
	public int longitud() {
		return this.longitud;
	}

	/**
	 * @see servidor.tabla.Campo#tipo()
	 */
	public Tipo tipo() {
		return this.tipo;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Campo) {
			Campo otroCampo = (Campo) obj;
			return this.tipo().equals(otroCampo.tipo()) && this.longitud() == otroCampo.longitud();
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.tipo().hashCode() * this.longitud();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.tipo() + ", " + this.longitud() + "]";
	}

}
