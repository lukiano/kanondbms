package servidor.buffer.latch;

import java.util.Date;

import servidor.buffer.Bloque.ID;

/**
 * Interfaz que representa a un Latch aplicado a un bloque del Buffer Manager.
 */
public interface Latch {

	/**
	 * @return el Id del elemento bloqueado por este Latch.
	 */
	ID idElementoBloqueado();
	
	/**
	 * @return la fecha y hora en la que este latch fue creado.
	 */
	Date fechaCreacion();
    
    /**
     * @return el thread en donde se creó el latch.
     */
	Thread propietario();
	
}
