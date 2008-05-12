/**
 * 
 */
package servidor.util;

import java.util.NoSuchElementException;

/**
 * Implementacion basica de un iterador que devuelve que no hay elementos para iterar.
 */
public final class IteradorVacio<E> implements Iterador<E> {

	/**
	 * Constructor privado de la clase para evitar instanciamiento.
	 */
	private IteradorVacio() {
	}

	/**
	 * @see servidor.util.Iterador#hayProximo()
	 */
	public boolean hayProximo() {
		return false;
	}

	/**
	 * @see servidor.util.Iterador#proximo()
	 */
	public E proximo() {
		throw new NoSuchElementException("There are no more elements.");
	}

	/**
	 * @see servidor.util.Iterador#cerrar()
	 */
	public void cerrar() {
	}
	
	/**
	 * Instancia unica del iterador vacio.
	 */
	private static final Iterador iteradorVacio = new IteradorVacio();

	/**
	 * @return la instancia del iterador vacio.
	 */
	@SuppressWarnings("unchecked")
	public static final <E> Iterador<E> dameIteradorVacio() {
		return iteradorVacio;
		
	}
}
