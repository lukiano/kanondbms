package servidor.tabla.impl;

import servidor.buffer.BufferManager;
import servidor.buffer.FabricaBufferManager;
import servidor.buffer.latch.impl.TablaDecoradoraConLatch;
import servidor.indice.hash.impl.TablaDecoradoraConIndices;
import servidor.lock.FabricaLockManager;
import servidor.lock.LockManager;
import servidor.log.FabricaRecoveryManager;
import servidor.log.RecoveryManager;
import servidor.tabla.Columna;
import servidor.tabla.FabricaTabla;
import servidor.tabla.Tabla;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.TransactionManager;

public class FabricaTablaNormal implements FabricaTabla {
	
	private BufferManager bufferManager;
	
	private TransactionManager transactionManager;
	
	private LockManager lockManager;
	
	private RecoveryManager recoveryManager;

	public Tabla dameTabla(int id, String nombreTabla, Columna[] columnas) {
        Tabla.ID idTabla = Tabla.ID.nuevoID(nombreTabla, id);
        Tabla tabla = new TablaImpl(idTabla, this.getBufferManager(),
        		this.getRecoveryManager().log(),
        		this.getTransactionManager(), 
        		columnas);
         
    	tabla = new TablaDecoradoraConLatch(tabla, this.getBufferManager().getLatchManager());
    	tabla = new TablaDecoradoraConIndices(tabla); // manejo de indices dentro del lock, por eso va antes
    	tabla = new TablaDecoradoraConLock(tabla, this.getLockManager(), this.getTransactionManager());
        return tabla;
	}
	
	protected TransactionManager getTransactionManager() {
		if (this.transactionManager == null) {
			this.transactionManager = FabricaTransactionManager.dameInstancia(); 
		}
		return this.transactionManager;
	}
	
	protected LockManager getLockManager() {
		if (this.lockManager == null) {
			this.lockManager = FabricaLockManager.dameInstancia(); 
		}
		return this.lockManager;
	}

	protected BufferManager getBufferManager() {
		if (this.bufferManager == null) {
			this.bufferManager = FabricaBufferManager.dameInstancia(); 
		}
		return this.bufferManager;
	}

	protected RecoveryManager getRecoveryManager() {
		if (this.recoveryManager == null) {
			this.recoveryManager = FabricaRecoveryManager.dameInstancia(); 
		}
		return this.recoveryManager;
	}

}
