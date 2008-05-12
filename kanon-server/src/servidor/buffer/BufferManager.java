package servidor.buffer;

import java.util.Set;

import servidor.buffer.Bloque.ID;
import servidor.buffer.latch.LatchManager;

/**
 * Interfaz para administrar el alojamiento de paginas en memoria.
 * @see Bloque
 */
public interface BufferManager {

	/**
	 * Metodo para obtener un bloque a partir de su identificacion.
	 * Si el bloque no se encuentra en memoria, se trae de disco.
	 * En ese caso, si el pool de bloques estaba lleno, se liberara uno para dar lugar a este bloque.
	 * @param id el identificador del bloque.
	 * @return una implementacion del bloque para trabajar con el mismo o NULL si no existe ningun bloque con ese ID.
	 * @throws RuntimeException si la politica de reemplazo no pudo hacer lugar para el bloque (en caso que haya sido traido de disco).
	 */
	Bloque dameBloque(ID id);
	
	/**
	 * Metodo para obtener un bloque a partir de su identificacion. Devuelve el bloque solo si se encuentra en memoria.
	 * @param id el identificador del bloque.
	 * @return una implementacion del bloque para trabajar con el mismo o NULL si el bloque no se encuentra en el pool de bloques
	 * o no existe ninguno con ese ID.
	 */
	Bloque dameBloqueSoloSiEnMemoria(ID id);
	
	/**
	 * Le comunica al Buffer Manager que puede desprenderse del bloque en memoria 
	 * (a traves de la politica de reemplazo) en caso de ser necesario.
	 * @param id el identificador del bloque que ya no es usado.
	 */
	void liberarBloque(ID id);
	
	/**
	 * Metodo para la eliminacion de un bloque.
	 * El Buffer Manager borra dicho bloque del pool de bloques, 
	 * y le informa al Disk Space Manager que borre su version fisica del disco. 
	 * @param id el identificador del bloque a borrar.
	 */
	void borrarBloque(ID id);
	
	/**
	 * Metodo para conocer la existencia de un bloque en el pool.
	 * @param id el identificador del bloque.
	 * @return true si el bloque se encuentra en el pool de bloques.
	 */
	boolean contieneBloque(ID id);
	
	/**
	 * Metodo para obtener un nuevo bloque con un identificador determinado.
	 * Si ya existe un bloque con dicho ID, se devuelve ese.
	 * @param id el identificador del nuevo bloque.
	 * @return una implementacion del bloque para trabajar con el mismo. Puede no estar vacio.
	 */
	Bloque nuevoBloque(ID id);
	
    /**
     * Le pide al Buffer Manager que guarde en disco (con llamadas al Disk Space Manager) aquellos
     * bloques del pool que hayan sido modificados desde la ultima vez que fueron guardados.
     */
    void guardarBloquesModificados();
    
    /**
     * Le pide al Buffer Manager los identificadores de aquellos bloques del pool que fueron
     * modificados desde la ultima vez que fueron guardados.
     * Metodo usado para realizar el Checkpoint.
     * @return un conjunto de identificadores.
     */
    Set<ID> dameBloquesSucios();
    
    /**
     * Retorna el Latch Manager usado por este sistema.
     * @return una implementacion de un Latch Manager.
     */
    LatchManager getLatchManager();
    
}
