package servidor.transaccion;

/**
 * Enumeraci�n con los estados posibles de una transacci�n.
 */
public enum Estado {

	/**
	 * hay una transaccion en curso. 
	 */
	EN_CURSO, 
    
    /**
     * no hay ninguna transaccion activa.
     */
    NINGUNA, 
    
    /**
     * la transaccion en curso ha abortado.
     */
    ABORTADA, 
    
    /**
     * la transaccion en curso ha hecho commit.
     */
    COMMIT;
	
}
