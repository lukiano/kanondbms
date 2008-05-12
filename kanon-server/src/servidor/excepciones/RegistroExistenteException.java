/**
 * 
 */
package servidor.excepciones;

/**
 * Excepcion lanzada cuando se desea insertar un registro en una posicion ocupada por otro.
 */
public final class RegistroExistenteException extends Exception {

	/**
	 * Constante usada para la serializacion de una instancia de esta clase.
	 */
	private static final long serialVersionUID = 3749373981067297850L;

	/**
	 * Constructor de la clase.
	 * @param message un mensaje de error explicando el origen de la excepcion.
	 */
	public RegistroExistenteException(String message) {
		super(message);
	}

}
