package servidor.tabla;

import servidor.buffer.Bloque;
import servidor.log.LSN;

/**
 * Interfaz que representa a una Pagina de una Tabla.
 * Las tablas guardan su contenido en paginas, las cuales a su vez guardan una cantidad fija de registros.
 */
public interface Pagina extends OperaRegistros {
	
    /**
     * @return el ID de esta pagina.
     */
    Pagina.ID id();
    
	/**
	 * @return true si la pagina está llena.
	 */
	boolean paginaLlena();

    /**
     * @return la aridad de los registros de esta pagina.
     */
    int aridad();
    
    /**
     * @return true si la pagina se encuentra en el BufferManager.
     */
    boolean esValida();
    
    /**
     * @return el LSN de la ultima modificacion hecha en esta pagina.
     */
    LSN recoveryLSN();
    
    /**
     * Actualiza el puntero al evento en el log que contiene la ultima modificacion hecha a esta pagina.
     * @param nuevoLSN el ultimo LSN con modificaciones a esta pagina.
     */
    void actualizarRecoveryLSN(LSN nuevoLSN);
    
    /**
     * El ID de una pagina está formado por el ID de su tabla asociada
     * (una pagina pertenece exclusivamente a una tabla) y el nro de pagina
     * dentro de tal tabla.
     */
    final class ID implements Bloque.ID {
        
        /**
         * La tabla a la que pertenece esta pagina.
         */
        private Tabla.ID propietario;
        
        /**
         * El numero de pagina dentro de la tabla.
         */
        private int numeroPagina;
        
        /**
         * Constructor privado para evitar instanciamiento.
         * @param propietario la tabla a la que pertenece esta pagina.
         * @param numeroPagina el numero de pagina dentro de la tabla.
         */
        private ID(Tabla.ID propietario, int numeroPagina) {
            this.propietario = propietario;
            this.numeroPagina = numeroPagina;
        }
        
        /**
         * Metodo estatico para obtener el ID de una Pagina.
         * @param propietario la tabla a la que pertenece esta pagina.
         * @param numeroPagina el numero de pagina dentro de la tabla.
         * @return el ID de una Pagina de acuerdo a los parametros especificados.
         */
        public static final ID nuevoID(Tabla.ID propietario, int numeroPagina) {
            return new ID(propietario, numeroPagina);
        }
        
        /**
         * @return la tabla a la que pertenece esta pagina.
         */
        public Tabla.ID propietario() {
            return this.propietario;
        }
        
        /**
         * @return el numero de pagina dentro de la tabla.
         */
        public int numeroPagina() {
            return this.numeroPagina;
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
            return otroID.numeroPagina == this.numeroPagina && otroID.propietario.equals(this.propietario);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.propietario.hashCode() ^ this.numeroPagina;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "P(" + this.propietario + " - " + this.numeroPagina + ")";
        }
        
    }
    
}
