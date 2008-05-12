package servidor.indice.hash.impl;

import servidor.buffer.latch.LatchManager;
import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;

class BucketDecoradorConLatch extends AbstractBucketDecorador {

	private LatchManager latchManager;

	public BucketDecoradorConLatch(Bucket bucket, LatchManager latchManager) {
		super(bucket);
		this.latchManager = latchManager;
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	@Override
	public void agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID idRegistroIndice, servidor.tabla.Registro.ID idRegistro) throws RegistroExistenteException {
		Bucket.ID idBucket = idRegistroIndice.propietario();
		this.latchManager.latch(idBucket);
		try {
			super.agregarRegistroIndice(idRegistroIndice, idRegistro);
		} finally {
			this.latchManager.unLatch(idBucket);	
		}
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.tabla.Registro.ID)
	 */
	@Override
	public servidor.indice.hash.RegistroIndice.ID agregarRegistroIndice(servidor.tabla.Registro.ID idRegistro) {
		RegistroIndice.ID idRegistroIndice = this.dameIDRegistroIndiceLibre();
		Bucket.ID idBucket = idRegistroIndice.propietario();
		this.latchManager.latch(idBucket);
		try {
			super.agregarRegistroIndice(idRegistroIndice, idRegistro);
		} catch (RegistroExistenteException e) {
			throw new RuntimeException(e);
		} finally {
			this.latchManager.unLatch(idBucket);	
		}
		return idRegistroIndice;
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	@Override
	public boolean borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID idRegistro) {
		Bucket.ID idBucket = idRegistro.propietario();
		this.latchManager.latch(idBucket);
		try {
			return super.borrarRegistroIndice(idRegistro);
		} finally {
			this.latchManager.unLatch(idBucket);	
		}
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#dameIDRegistroIndiceLibre()
	 */
	@Override
	public servidor.indice.hash.RegistroIndice.ID dameIDRegistroIndiceLibre() {
		RegistroIndice.ID idRegistroIndice = super.dameIDRegistroIndiceLibre();
		if (idRegistroIndice != null) {
			//this.latchManager.latch(idRegistroIndice.propietario());
		}
		return idRegistroIndice;
	}

}
