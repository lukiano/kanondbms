package servidor.buffer.pin;

import servidor.buffer.Bloque.ID;

/**
 * Lleva el control sobre aquellos bloques del pool que se encuentran siendo utilizados, y
 * por lo tanto no pueden ser removidos por el Algoritmo de Politica de Reemplazo. 
 */
public interface PinManager {
	
	/**
	 * Se marca un bloque para su posterior utilizacion.
	 * @param id el identificador del bloque.
	 */
	void pinnear(ID id);
	
	/**
	 * Se libera el bloque, indicando que ya no va a ser utilizado, y puede
	 * ser removido del pool en caso de ser necesario.
	 * @param id el identificador del bloque.
	 */
	void desPinnear(ID id);
	
	/**
	 * Metodo para saber si un bloque esta siendo utilizado por alguna transaccion.
	 * Es utilizado por la politica de reemplazo para saber cuales bloques del pool
	 * pueden ser removidos.
	 * @param id el identificador del bloque.
	 * @return true si existe un pin sobre el bloque.
	 */
	boolean estaPinneado(ID id);

}
