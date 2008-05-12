/**
 * 
 */
package servidor.fisico;

import java.io.File;

import servidor.fisico.impl.DiskSpaceManagerImpl;

/**
 * @author lleggieri
 *
 */
public final class FabricaDiskSpaceManager {
	
	private static DiskSpaceManager instancia;

	/**
	 * 
	 */
	private FabricaDiskSpaceManager() {
		super();
	}
	
	public static synchronized DiskSpaceManager dameInstancia() {
		if (instancia == null) {
			instancia = new DiskSpaceManagerImpl(new File("paginas"));
			//instancia = new InspectorDiskSpaceManager(instancia);
		}
		return instancia;
	}

}
