package servidor.fisico;

import servidor.buffer.Bloque;
import servidor.buffer.Bloque.ID;

/**
 * Administrador encargado de la persistencia de los bloques con datos en el disco.
 * @see Bloque
 */
public interface DiskSpaceManager {

	/**
	 * Obtiene del disco un bloque con los datos correspondiente al identificador. 
	 * @param id el identificador de un bloque
	 * @return un bloque con los datos correspondiente al identificador o NULL si no hay ninguno que corresponda.
	 */
	Bloque leerBloque(ID id);
	
	/**
	 * Crea en disco un nuevo bloque con un identificador determinado.
	 * @param id el identificador del nuevo bloque.
	 * @return un bloque vacio o lleno con los datos correspondiente si ya existia un bloque con ese identificador en el disco.
	 */
	Bloque nuevoBloque(ID id);
	
	/**
	 * Persiste un bloque. 
	 * @param id el identificador del bloque.
	 * @param bloque el bloque a guardar en el disco.
	 */
	void guardarBloque(ID id, Bloque bloque);
    
    /**
     * Elimina de la persistencia a un bloque determinado.
     * @param id el bloque a borrar. No hace nada si no existe ningun bloque con ese identificador en el disco.
     */
    void borrarBloque(ID id);
	
}
