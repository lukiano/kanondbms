package servidor.buffer;

/**
 * Interfaz para saber si un objeto es valido (se encuentra en memoria).
 */
public interface Validable {
	
    /**
     * @return true si el objeto es valido.
     */
    boolean valido();
    
    /**
     * invalida el objeto.
     */
    void invalidar();

}
