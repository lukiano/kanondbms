package servidor.log;

import servidor.transaccion.Transaccion;

/**
 * Interfaz del administrador de recuperaci�n.
 * Ser� usado cuando ocurren fatalidades en la base.
 */
public interface RecoveryManager {
	
	void recuperarSistema();

	Log log();
	
	void checkpoint();
	
	void rollback(Transaccion transaccion);
	
	void rollback(Transaccion transaccion, LSN saveLSN);
	
}
