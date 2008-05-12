/**
 * 
 */
package servidor.buffer;

import servidor.buffer.impl.BufferManagerImpl;
import servidor.buffer.latch.LatchManager;
import servidor.buffer.latch.impl.LatchManagerImpl;
import servidor.buffer.pin.PinManager;
import servidor.buffer.pin.impl.PinManagerImpl;
import servidor.buffer.politica.FabricaPoliticaReemplazo;
import servidor.buffer.politica.PoliticaReemplazo;

/**
 * Fabrica que devuelve una instancia unica del Buffer Manager. 
 *
 */
public final class FabricaBufferManager {

	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static BufferManager instancia;
	
	
	/**
	 * Tamanio por omision del pool del Buffer Manager.
	 */
	private static final int TAMANIO_POOL = 8;

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaBufferManager() {
		super();
	}
	
	/**
	 * Devuelve la instancia unica del Buffer Manager.
	 * Se establece el tamanio del pool del BM. Con la propiedad del sistema "pool" se puede especificar el tamanio del pool.
	 * Se crean instancias del Latch Manager, Pin Manager y el Algoritmo de Politica de Reemplazo de bloques.
	 * @return la instancia unica del Buffer Manager.
	 */
	public static synchronized BufferManager dameInstancia() {
		if (instancia == null) {
			PoliticaReemplazo politicaReemplazo = FabricaPoliticaReemplazo.dameInstancia();
			
			LatchManager latchManager = new LatchManagerImpl();
			// latchManager = new InspectorLatchManager(latchManager);
			
			PinManager pinManager = new PinManagerImpl();
			// pinManager = new InspectorPinManager(pinManager);
			
			int tamanioPool = Integer.getInteger("pool", TAMANIO_POOL);
			
			instancia = new BufferManagerImpl(
					politicaReemplazo,
					latchManager,
					pinManager,
					tamanioPool);
		}
		return instancia;
	}

}
