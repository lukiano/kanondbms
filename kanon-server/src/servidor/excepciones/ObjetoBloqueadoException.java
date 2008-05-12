/**
 * 
 */
package servidor.excepciones;

/**
 * Exception tirada cuando se intenta bloquear un objeto ya bloqueado
 * y no se desea esperar a que se desbloquee.
 */
public final class ObjetoBloqueadoException extends Exception {

	/**
	 * Constante usada para la serializacion de una instancia de esta clase.
	 */
	private static final long serialVersionUID = -5163710308743642820L;

}
