/**
 * 
 */
package servidor.util;

/**
 * Clase abstracta para decorar un iterador y proveer funcionalidad extra.
 * Se adhiere al Design Pattern Decorator.
 *
 */
public abstract class AbstractIteradorDecorador<E> implements Iterador<E> {
	
	/**
	 * El iterador decorado.
	 */
	private Iterador<E> iteradorDecorado;

	/**
	 * Constructor de la clase. Esta clase no se puede instanciar por si misma.
	 * @param iterador el iterador a decorar.
	 */
	public AbstractIteradorDecorador(Iterador<E> iterador) {
		this.iteradorDecorado = iterador;
	}

	/**
	 * @see servidor.util.Iterador#hayProximo()
	 */
	public boolean hayProximo() {
		return this.iteradorDecorado.hayProximo();
	}

	/**
	 * @see servidor.util.Iterador#proximo()
	 */
	public E proximo() {
		return this.iteradorDecorado.proximo();
	}

	/**
	 * @see servidor.util.Iterador#cerrar()
	 */
	public void cerrar() {
		this.iteradorDecorado.cerrar();
	}

}
