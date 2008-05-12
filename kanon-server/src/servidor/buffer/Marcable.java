/**
 * @author lleggieri
 */
package servidor.buffer;

/**
 * Interfaz para saber si el contenido de un objeto ha cambiado
 * y necesita ser guardado en disco antes de desecharlo. 
 *
 */
public interface Marcable {

    /**
     * Metodo para saber si un elemento se encuentra marcado.
     * @return true si el elemento ha sido marcado.
     */
    boolean marcado();
    
    /**
     * Marca el elemento.
     */
    void marcar();
    
    /**
     * Quita la marca del elemento.
     */
    void desMarcar();

}
