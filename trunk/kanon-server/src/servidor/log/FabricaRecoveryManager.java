/**
 * 
 */
package servidor.log;

import servidor.log.impl.InspectorLog;
import servidor.log.impl.InspectorLogParaCliente;
import servidor.log.impl.LogImpl;
import servidor.log.impl.RecoveryManagerImpl;

public final class FabricaRecoveryManager {
	
	private static RecoveryManager instancia;

	/**
	 * 
	 */
	private FabricaRecoveryManager() {
		super();
	}
	
	public static synchronized RecoveryManager dameInstancia() {
		if (instancia == null) {
			Log log = new LogImpl();
			log = new InspectorLogParaCliente(log);
			log = new InspectorLog(log);
			
			instancia = new RecoveryManagerImpl(log, true);
		}
		return instancia;
	}

}
