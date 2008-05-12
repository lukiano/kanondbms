/**
 * 
 */
package servidor.log.impl.estructuras;

import servidor.indice.hash.Bucket;
import servidor.log.LSN;
import servidor.tabla.Pagina;

public class DatoBloqueSucio {
	
	public Pagina.ID idPagina;
	
	public Bucket.ID idBucket;
	
	public LSN recLSN;
	
	@Override
	public String toString() {
		return "Pag:" + idPagina + "-Buc:" + idBucket + "-RecLSN:" + recLSN; 
	}
}