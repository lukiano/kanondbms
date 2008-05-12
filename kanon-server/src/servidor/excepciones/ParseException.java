package servidor.excepciones;

/**
 * Excepcion lanzada cuando ocurre un error en el analisis de una sentencia.
 */
public final class ParseException extends Exception {

	/**
	 * Constante usada para la serializacion de una instancia de esta clase.
	 */
	private static final long serialVersionUID = 7655799502934138193L;

	/**
	 * Constructor de la clase.
	 * @param message un mensaje de error explicando el origen de la excepcion.
	 * @param cause el error ocurrido que provoca esta excepcion.
	 */
	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor de la clase.
	 * @param message un mensaje de error explicando el origen de la excepcion.
	 */
	public ParseException(String message) {
		super(message);
	}

}
