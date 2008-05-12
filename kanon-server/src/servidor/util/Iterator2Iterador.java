package servidor.util;

import java.util.Iterator;

/**
 * Adaptador de la interfaz Iterator a la interfaz Iterador.
 * @see Iterador
 * @see Iterator
 */
public class Iterator2Iterador<E> implements Iterador<E> {

	/**
	 * Iterator que sera adaptado.
	 */
	private Iterator<E> iterator;
	
	/**
	 * Constructor de la clase.
	 * @param iterator el Iterator que sera adaptado.
	 */
	public Iterator2Iterador(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	/**
	 * @see servidor.util.Iterador#hayProximo()
	 */
	public boolean hayProximo() {
		return this.iterator.hasNext();
	}

	/**
	 * @see servidor.util.Iterador#proximo()
	 */
	public E proximo() {
		return this.iterator.next();
	}

	/**
	 * @see servidor.util.Iterador#cerrar()
	 */
	public void cerrar() {
		// iterator no tiene un metodo close(). 
	}

}
