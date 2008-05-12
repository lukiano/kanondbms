/**
 * 
 */
package servidor.transaccion;

import servidor.log.FabricaRecoveryManager;
import servidor.log.Log;
import servidor.log.RecoveryManager;
import servidor.transaccion.impl.TransactionManagerImpl;

/**
 * Fabrica que devuelve una instancia unica del Administrador de Transacciones.
 * 
 */
public final class FabricaTransactionManager {
	
	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static TransactionManager instancia;

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaTransactionManager() {
		super();
	}
	
	/**
	 * Devuelve la instancia unica del Administrador de Transacciones.
	 * Requiere del Administrador de Recuperacion para obtener el Log.
	 * @return la instancia unica del Administrador de Transacciones.
	 */
	public static synchronized TransactionManager dameInstancia() {
		if (instancia == null) {
			RecoveryManager recoveryManager = FabricaRecoveryManager.dameInstancia();
			Log log = recoveryManager.log();
				
			instancia = new TransactionManagerImpl(log);
		}
		return instancia;
	}

}
