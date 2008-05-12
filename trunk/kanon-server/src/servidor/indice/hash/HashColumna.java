/**
 * 
 */
package servidor.indice.hash;

import servidor.Id;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;

/**
 * Encapsula las operaciones sobre una columna.
 * Redirige la query al Bucket que corresponda.
 * 
 * La interfaz HashColumna no tiene estado.
 * El estado se guarda en los Buckets
 */
public interface HashColumna {

	final class ID implements Id {
		
		private Tabla.ID propietario;
		
		/**
		 * Numero de la columna en la tabla. Empiezan en 0.
		 */
		private int columna;
		
        private ID(Tabla.ID propietario, int columna) {
            this.propietario = propietario;
            this.columna = columna;
        }
        
        public static final ID nuevoID(Tabla.ID propietario, int columna) {
            return new ID(propietario, columna);
        }
        
        public Tabla.ID propietario() {
            return this.propietario;
        }
        
        public int columna() {
        	return this.columna;
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
            return otroID.columna == this.columna && 
            	otroID.propietario.equals(this.propietario);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.propietario.hashCode() ^ this.columna;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "H(" + this.propietario + " - " + this.columna + ")";
        }
	
	}

	HashColumna.ID id();
	
	/**
	 * @param idRegistro el id del registro a agregar.
	 * @param valor sirve para obtener el hash y saber en cual Bucket va.
	 */
	void agregarRegistro(Registro.ID idRegistro, Object valor);
	
	/**
	 * @param idRegistro el id del registro a actualizar.
	 * @param viejoValor sirve para obtener el Bucket donde se encuentra el registro actualmente.
	 * @param nuevoValor sirve para obtener el nuevo Bucket destino del registro.
	 * Si ambos resultan ser el mismo hash, no se realiza ninguna operacion.
	 */
	void actualizarRegistro(Registro.ID idRegistro, Object viejoValor, Object nuevoValor);
	
	/**
	 * @param idRegistro el id del registro a borrar.
	 * @param valor sirve para obtener el hash y saber en cual Bucket va.
	 */
	boolean borrarRegistro(Registro.ID idRegistro, Object valor);
	
	/**
	 * Acordarse que puede devolver registros cuyo valor sea distinto en esa
	 * columna, pero el hash concuerde.
	 */
	Iterador<Registro.ID> dameRegistros(Object valor);

}
