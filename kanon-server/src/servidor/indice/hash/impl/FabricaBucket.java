package servidor.indice.hash.impl;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.indice.hash.Bucket;
import servidor.lock.FabricaLockManager;
import servidor.log.FabricaRecoveryManager;
import servidor.transaccion.FabricaTransactionManager;

public final class FabricaBucket {

	private FabricaBucket() {};
	
	public static Bucket dameBucket(BufferManager bufferManager, Bucket.ID idBucket, Bloque bloque) {
		if (bloque == null) {
			// si no hay bloque, no hay Bucket
			return null;
		}
		Bucket bucket = new BucketImpl(bufferManager, idBucket, bloque);
		bucket = new BucketDecoradorConLog(bucket, FabricaRecoveryManager.dameInstancia().log(), FabricaTransactionManager.dameInstancia());
		bucket = new BucketDecoradorConLatch(bucket, bufferManager.getLatchManager());
		bucket = new BucketDecoradorConLock(bucket, FabricaLockManager.dameInstancia(), FabricaTransactionManager.dameInstancia());
		// bucket = new InspectorBucket(bucket);
		return bucket;
	}

	public static Bucket dameBucketLimpio(BufferManager bufferManager, Bucket.ID idBucket, Bloque bloque) {
		if (bloque == null) {
			// si no hay bloque, no hay Bucket
			return null;
		}
		Bucket bucket = new BucketImpl(bufferManager, idBucket, bloque);
		// bucket = new InspectorBucket(bucket);
		return bucket;
	}

}
