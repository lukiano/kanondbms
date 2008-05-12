package servidor.transaccion;

import java.util.Date;
import java.util.SortedSet;

import servidor.log.LSN;

/**
 * Interfaz que representa una transaccion en el motor.
 */
public interface Transaccion {
	
	/**
	 * Nombre del savepoint automatico entre sentencias dentro de una transaccion.
	 */
	public static final String AUTO_SAVEPOINT = "AUTO_SAVEPOINT";
	
	/**
	 * Devuelva la fecha y hora en la que comenzó la transacción.
	 * @see servidor.lock.deadlock.PrevencionDeadLock
	 */
	Date fechaInicio();
	
	/**
	 * @return el identificador de esta transaccion.
	 */
	ID id();
	
	/**
	 * @return el LSN de la ultima operacion de log correspondiente a esta transaccion.
	 */
	LSN ultimoLSN();
	
	/**
	 * @param lsn el LSN de la ultima operacion de log correspondiente a esta transaccion.
	 */
	void establecerUltimoLSN(LSN lsn);
	
	/**
	 * Establece el proximo LSN a ser procesado durante el UNDO o rollback de la transaccion.
	 * @param lsn un LSN a agregar al conjunto de Undo Next LSN.
	 * @throws RuntimeException si este metodo es llamado durante un rollback o UNDO.  
	 */
	void establecerUndoNextLSN(LSN lsn);
	
	/**
	 * Metodo para obtener el conjunto de Undo Next LSN ordenado para ser procesado durante
	 * un UNDO o rollback. Solo va a haber mas de un elemento si la transaccion tuvo hijos y todavia
	 * no se hizo rollback de los mismos.
	 * @return un conjunto ordenado de LSN.
	 */
	SortedSet<LSN> undoNextLSN();
	
    /**
     * @return el estado actual de esta transaccion.
     */
    Estado estado();
    
    /**
     * @return el nivel de aislamiento de esta transaccion. 
     */
    Aislamiento aislamiento();
    
    /**
     * @return la transaccion padre de esta transaccion anidada o NULL si es una transaccion de alto nivel.
     * @see LSN#LSN_NULO
     */
    Transaccion padre();
    
    /**
     * @return el thread propietario de esta transaccion (usado para el manejo de locks).
     */
    Thread threadPropietario();
    
    /**
     * Establece un savepoint en la transaccion.
     * El savepoint se asocia con el ultimo LSN de la transaccion al momento
     * de llamar a este metodo. Si ya existe un savepoint con el nombre pasado
     * por parametro, se reemplaza con el ultimo LSN.
     * @param nombreSavepoint el nombre del savepoint.
     */
    void establecerSavepoint(String nombreSavepoint);
    
    /**
     * @param nombreSavepoint el nombre de un savepoint.
     * @return el LSN asociado al savepoint, o LSN.LSN_NULO si no hay ningun savepoint con ese nombre en esta transaccion.
     * @see LSN#LSN_NULO
     */
    LSN dameSavepoint(String nombreSavepoint);
    
    /**
     * Clase identificadora de una transaccion.
     */
    public static final class ID {
    	
        /**
         * El numero de la transaccion. Debe ser unico.
         */
        private int numeroTransaccion;
        
        /**
         * Constructor privado de la clase.
         * @param numeroTx el numero de transaccion del identificador.
         */
        private ID(int numeroTx) {
            this.numeroTransaccion = numeroTx;
        }
        
        /**
         * Devuelve un identificador de transaccion a partir de un numero.
         * @param numeroTx el numero correspondiente al nuevo identificador.
         * @return un identificador de transaccion.
         */
        public static final ID nuevoID(int numeroTx) {
            return new ID(numeroTx);
        }
        
        /**
         * @return el numero de transaccion correspondiente a este identificador.
         */
        public int numeroTransaccion() {
            return this.numeroTransaccion;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object arg0) {
            if (!(arg0 instanceof ID)) {
                return false;
            }
            ID otroID = (ID) arg0;
            return otroID.numeroTransaccion == this.numeroTransaccion;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.numeroTransaccion;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Tx(" + this.numeroTransaccion + ")";
        }
    	
    }
    
}
