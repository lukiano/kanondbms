/**
 * 
 */
package servidor.indice.hash;

import servidor.buffer.Bloque;
import servidor.excepciones.RegistroExistenteException;
import servidor.log.LSN;
import servidor.tabla.Registro;
import servidor.util.Iterador;

/**
 * Una entrada en un bucket ocupa:
 * nro pagina + nro registro dentro de pagina. (La tabla a la que pertenece ya la conozco)
 * 4 bytes + 4 bytes = 8 bytes
 * Si un bucket tiene 4096 bytes:
 * 504 entradas: 504 bits del ArregloBits (63 bytes) y 504*8 bytes para las entradas
 * = 63 + 4032 = 4095.
 */
public interface Bucket {
	
	final class ID implements Bloque.ID {
		
		private HashColumna.ID propietario;
		
		private int numeroBucket;
		
		private int numeroHash;
		
        private ID(HashColumna.ID propietario, int numeroBucket, int numeroHash) {
            this.propietario = propietario;
            this.numeroBucket = numeroBucket;
            this.numeroHash = numeroHash;
        }
        
        public static final ID nuevoID(HashColumna.ID propietario, int numeroBucket, int numeroHash) {
            return new ID(propietario, numeroBucket, numeroHash);
        }
        
        public HashColumna.ID propietario() {
            return this.propietario;
        }
        
        public int numeroHash() {
        	return this.numeroHash;
        }

        public int numeroBucket() {
        	return this.numeroBucket;
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
            return otroID.numeroBucket == this.numeroBucket && 
            	otroID.numeroHash == this.numeroHash &&
            	otroID.propietario.equals(this.propietario);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.propietario.hashCode() ^ this.numeroBucket ^ this.numeroHash;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "B(" + this.propietario + " - " + this.numeroHash + " - " + this.numeroBucket + ")";
        }
	
	}

	Bucket.ID id();
	
	boolean bucketLleno();
	
	boolean esValido();
	
	RegistroIndice.ID agregarRegistroIndice(Registro.ID idRegistro);
	
	void agregarRegistroIndice(RegistroIndice.ID idRegistroIndice, Registro.ID idRegistro) throws RegistroExistenteException;
	
	boolean borrarRegistroIndice(RegistroIndice.ID idRegistroIndice);
	
	RegistroIndice.ID dameIDRegistroIndiceLibre();
	
	RegistroIndice dameRegistroIndice(RegistroIndice.ID idRegistroIndice);
	
	void liberarRegistroIndice(RegistroIndice.ID idRegistroIndice);
	
	Iterador<RegistroIndice.ID> dameRegistrosIndice();

    /**
     * @return el LSN de la ultima modificacion hecha en esta pagina.
     */
    LSN recoveryLSN();
    
    /**
     * Actualiza el puntero al evento en el log que contiene la ultima modificacion hecha a esta pagina.
     * @param nuevoLSN el ultimo LSN con modificaciones a esta pagina.
     */
    void actualizarRecoveryLSN(LSN nuevoLSN);
    

}
