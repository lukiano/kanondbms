/**
 * 
 */
package servidor.indice.hash.impl;

import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.inspector.Inspector;
import servidor.util.AbstractIteradorDecorador;
import servidor.util.Iterador;

/**
 *
 */
public class InspectorBucket extends AbstractBucketDecorador {
	
	private Inspector inspector;
	
	/**
	 * 
	 */
	public InspectorBucket(Bucket bucket) {
		super(bucket);
		this.inspector = new Inspector(bucket.id().toString());
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	@Override
	public void agregarRegistroIndice(RegistroIndice.ID idRegistroIndice, servidor.tabla.Registro.ID idRegistro) throws RegistroExistenteException {
		this.inspector.agregarEvento("AgregarIndice", idRegistroIndice.toString());
		super.agregarRegistroIndice(idRegistroIndice, idRegistro);
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.tabla.Registro.ID)
	 */
	@Override
	public RegistroIndice.ID agregarRegistroIndice(servidor.tabla.Registro.ID idRegistro) {
		RegistroIndice.ID idRegistroIndice = super.agregarRegistroIndice(idRegistro);
		this.inspector.agregarEvento("AgregarIndice", idRegistroIndice.toString());
		return idRegistroIndice;
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	@Override
	public boolean borrarRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		this.inspector.agregarEvento("BorrarIndice", idRegistroIndice.toString());
		return super.borrarRegistroIndice(idRegistroIndice);
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#dameRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	@Override
	public RegistroIndice dameRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		this.inspector.agregarEvento("dameIndice", idRegistroIndice.toString());
		return super.dameRegistroIndice(idRegistroIndice);
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#dameRegistrosIndice()
	 */
	@Override
	public Iterador<RegistroIndice.ID> dameRegistrosIndice() {
		Iterador<RegistroIndice.ID> registros = super.dameRegistrosIndice();
		return new AbstractIteradorDecorador<RegistroIndice.ID>(registros) {
		
			@Override
			public RegistroIndice.ID proximo() {
				RegistroIndice.ID idRegistroIndice = super.proximo();
				InspectorBucket.this.inspector.agregarEvento("ObtencionIndice", idRegistroIndice.toString());
				return idRegistroIndice;
			}
		
		};
	}

	@Override
	protected void finalize() {
		this.inspector.parar();
	}

}
