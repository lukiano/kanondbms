package servidor.indice.hash;

import servidor.Id;
import servidor.tabla.Registro;

public interface RegistroIndice {
	
	final class ID implements Id {
		
		private Bucket.ID propietario;
		
		private int numeroRegistroIndice;
		
        private ID(Bucket.ID propietario, int numeroRegistroIndice) {
            this.propietario = propietario;
            this.numeroRegistroIndice = numeroRegistroIndice;
        }
        
        public static final ID nuevoID(Bucket.ID propietario, int numeroRegistroIndice) {
            return new ID(propietario, numeroRegistroIndice);
        }
        
        public Bucket.ID propietario() {
            return this.propietario;
        }
        
        public int numeroRegistroIndice() {
        	return this.numeroRegistroIndice;
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
            return otroID.numeroRegistroIndice == this.numeroRegistroIndice && 
            	otroID.propietario.equals(this.propietario);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.propietario.hashCode() ^ this.numeroRegistroIndice;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "RI(" + this.propietario + " - " + this.numeroRegistroIndice + ")";
        }
	
	}
	
	RegistroIndice.ID id();

	Registro.ID registroReferenciado();
	
	boolean esValido();
	
}
