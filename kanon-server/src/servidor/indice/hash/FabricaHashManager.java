package servidor.indice.hash;

import servidor.buffer.BufferManager;
import servidor.buffer.FabricaBufferManager;
import servidor.indice.hash.impl.HashManagerImpl;

public final class FabricaHashManager {

	private static HashManager instancia;

	/**
	 * 
	 */
	private FabricaHashManager() {
		super();
	}
	
	public static synchronized HashManager dameInstancia() {
		if (instancia == null) {
			BufferManager bufferManager = FabricaBufferManager.dameInstancia();
			
			instancia = new HashManagerImpl(bufferManager);
		}
		return instancia;
	}

}
