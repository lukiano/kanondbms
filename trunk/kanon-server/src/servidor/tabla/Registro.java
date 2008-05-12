package servidor.tabla;

import java.util.List;

import servidor.Id;
import servidor.catalog.Valor;

/**
 * Interfaz que representa a un Registro dentro de una Pagina.
 * La longitud y valores del registro estan asociados a los campos de las columnas de la tabla correspondiente.
 */
public interface Registro {
	
    /**
     * @return true si la pagina a la que corresponde este registro es valida.
     */
    boolean esValido();
    
    /**
     * @return el ID de este registro.
     */
    Registro.ID id();
    
	/**
	 * @return la cantidad de valores que posee este registro. Se corresponde con la cantidad de columnas de la tabla.
	 */
	int aridad();
	
	/**
	 * Devuelve el valor en este registro para una columna determinada de la tabla.
	 * @param columna un numero de columna (empiezan en 0).
	 * @return el valor que existe en la posicion de este registro para la columna especificada.
	 */
	Object valor(int columna);
	
	/**
	 * @return una lista ordenada segun la posicion de las columnas con los valores de este registro para cada una.
	 */
	public List<Valor> getValores();

    /**
     * El ID de una pagina está formado por el ID de la pagina en la cual se encuentra
     * (un registro pertenece exclusivamente a una pagina) y el nro de registro (la posicion)
     * dentro de tal pagina.
     */
    final class ID implements Id {
        
        /**
         * la pagina a la que pertenece este registro.
         */
        private Pagina.ID propietario;
        
        /**
         * el numero de registro dentro de la pagina.
         */
        private int numeroRegistro;
        
        /**
         * Constructor privado para evitar instanciamiento.
         * @param propietario la pagina a la que pertenece este registro.
         * @param numeroRegistro el numero de registro dentro de la pagina.
         */
        private ID(Pagina.ID propietario, int numeroRegistro) {
            this.propietario = propietario;
            this.numeroRegistro = numeroRegistro;
        }
        
        /**
         * Metodo estatico para obtener el ID de una Pagina.
         * @param propietario la pagina a la que pertenece este registro.
         * @param numeroRegistro el numero de registro dentro de la pagina.
         * @return el ID de un Registro de acuerdo a los parametros especificados.
         */
        public static final ID nuevoID(Pagina.ID propietario, int numeroRegistro) {
            return new ID(propietario, numeroRegistro);
        }
        
        /**
         * @return la pagina a la que pertenece este registro.
         */
        public Pagina.ID propietario() {
            return this.propietario;
        }
        
        /**
         * @return el numero de registro dentro de la pagina.
         */
        public int numeroRegistro() {
            return this.numeroRegistro;
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
            return otroID.numeroRegistro == this.numeroRegistro && otroID.propietario.equals(this.propietario);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.propietario.hashCode() ^ this.numeroRegistro;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "R(" + this.propietario + " - " + this.numeroRegistro + ")";
        }
        
    }
    
}
