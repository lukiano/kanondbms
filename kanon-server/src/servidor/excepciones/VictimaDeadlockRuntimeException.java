/**
 * 
 */
package servidor.excepciones;

/**
 * Excepcion lanzada cuando una transaccion es elegida como victima del Algoritmo de Prevencion de Dead Lock.
 */
public final class VictimaDeadlockRuntimeException extends RuntimeException {
	
	/**
	 * Constante con el mensaje de error asociado a esta excepcion.
	 */
	private static final String MENSAJE = "Transaction chosen like victim of DeadLock";

    /**
     * Constante usada para la serializacion de una instancia de esta clase.
     */
    private static final long serialVersionUID = -5856179871253494295L;

    /**
     * Constructor de la clase. Crea la excepcion con el mensaje por omision.
     * @see #MENSAJE
     */
    public VictimaDeadlockRuntimeException() {
        super(MENSAJE);
    }

    /**
     * Constructor de la clase. Crea la excepcion con el mensaje por omision.
     * @see #MENSAJE
     * @param cause el error ocurrido que provoca esta excepcion.
     */
    public VictimaDeadlockRuntimeException(Throwable cause) {
        super(MENSAJE, cause);
    }

}
