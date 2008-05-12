package servidor.indice.hash.impl;

import java.util.NoSuchElementException;

import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.util.Iterador;

final class RegistroIndiceIDARegistroIDIterador implements Iterador<servidor.tabla.Registro.ID> {
	
	private final Iterador<servidor.indice.hash.RegistroIndice.ID> indices;

	private final Bucket bucket;
	
	private servidor.tabla.Registro.ID idRegistro;
	
	private boolean consumido = true;

	public RegistroIndiceIDARegistroIDIterador(Iterador<servidor.indice.hash.RegistroIndice.ID> indices, Bucket bucket) {
		this.indices = indices;
		this.bucket = bucket;
	}

	public boolean hayProximo() {
		if (this.consumido) {
			while (this.indices.hayProximo()) {
				RegistroIndice.ID idRegistroIndice = this.indices.proximo();
				RegistroIndice registroIndice = this.bucket.dameRegistroIndice(idRegistroIndice);
				try {
					if (registroIndice != null) {
						this.idRegistro = registroIndice.registroReferenciado();
		                if (this.idRegistro == null) {
		                	System.out.println("ss");
		                }

						this.consumido = false;
						return true;
					}
				} finally {
					this.bucket.liberarRegistroIndice(idRegistroIndice);
				}
			}
			return false;
		} else {
			return true;
		}
	}

	public servidor.tabla.Registro.ID proximo() {
		if (this.consumido) {
			if (this.hayProximo()) {
				this.consumido = false;
				return this.idRegistro;
			} else {
				throw new NoSuchElementException("Ya no quedan elementos.");
			}
		} else {
			this.consumido = true;
			return this.idRegistro;
		}
	}

	public void cerrar() {
		this.indices.cerrar();
	}
	
}
