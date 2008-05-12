package servidor.buffer.latch;

import servidor.buffer.Bloque.ID;

/**
 * Administrador del manejo de Latches en el motor.
 * Los latches sirven para manejar el acceso de escritura a los bloques.
 * Evita que dos transacciones escriban en un bloque al mismo tiempo.
 * Su funcion es similar a los locks, pero son mas rapidos.
 * Los latches se usan de tal manera que no pueden entrar en deadlock entre transacciones
 * ni entre latches y locks.
 * @author luciano
 *
 */
public interface LatchManager {
	
	/**
	 * Bloquea un bloque con un latch. El metodo puede poner la transaccion en espera si ya existe
	 * un latch sobre tal bloque.
	 * @param id el identificador del bloque.
	 * @return true si el elemento no se encontraba bloqueado con latch por el thread.
	 */
	boolean latch(ID id);
	
	/**
	 * Libera el latch de un determinado bloque.
	 * Si la transaccion no tenia ninguno sobre el mismo, no hace nada.
	 * @param id el identificador del bloque.
	 */
	void unLatch(ID id);

}
