package servidor.tabla;

import servidor.Id;


/**
 * Interfaz que representa a una Tabla en la base de datos.
 * Tambien es usada para las tablas del sistema.
 * Se recomienda la manipulacion de las tablas a traves de esta interfaz y no hacia las paginas que esta tabla posee.
 */
public interface Tabla extends OperaRegistros {
    
    /**
     * @return el ID de esta tabla.
     */
    Tabla.ID id();
    
    /**
     * @return un arreglo ordenado con detalles de las columnas que de esta tabla.
     */
    Columna[] columnas();
    
    /**
     * El ID de una tabla esta formado por su nombre y un identificador numerico. (Ambos serian primary key)
     * El identificador fue agregado para guardar las paginas en disco usando el numero de la tabla en vez de su nombre.
     */
    final class ID implements Id {
        
        /**
         * el numero identificador de la tabla. Se corresponde con la entrada en la Tabla de Tablas. 
         */
        private int idTabla;
        
        /**
         * el nombre de la tabla.
         */
        private String nombre;
        
        /**
         * Constructor privado para evitar instanciamiento.
         * @param nombre el nombre de la tabla.
         * @param idTabla el numero identificador de la tabla. 
         */
        private ID(String nombre, int idTabla) {
            this.nombre = nombre;
            this.idTabla = idTabla;
        }
        
        /**
         * Metodo estatico para obtener el ID de una Tabla.
         * @param nombre el nombre de la tabla.
         * @param idTabla el numero identificador de la tabla.
         * @return el ID de una Tabla de acuerdo a los parametros especificados.
         */
        public static final ID nuevoID(String nombre, int idTabla) {
            return new ID(nombre, idTabla);
        }
        
        /**
         * @return el nombre de la tabla.
         */
        public String nombre() {
            return this.nombre;
        }

        /**
         * @return el numero identificador de la tabla.
         */
        public int idTabla() {
            return this.idTabla;
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
            return otroID.idTabla == this.idTabla;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.idTabla;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "T(" + this.nombre + ")";
        }
        
    }
    
}
