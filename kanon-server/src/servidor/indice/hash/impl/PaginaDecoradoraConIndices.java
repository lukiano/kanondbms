package servidor.indice.hash.impl;

import java.util.Collection;

import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.FabricaHashManager;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.impl.AbstractPaginaDecorador;

public class PaginaDecoradoraConIndices extends AbstractPaginaDecorador {
	
	/**
	 * @param paginaDecorada
	 */
	public PaginaDecoradoraConIndices(Pagina paginaDecorada) {
		super(paginaDecorada);
	}

	@Override
	public void actualizarRegistro(servidor.tabla.Registro.ID idRegistro, Collection<Valor> valores) {
		Registro viejoRegistro = super.registro(idRegistro);
		super.liberarRegistro(idRegistro);
		super.actualizarRegistro(idRegistro, valores);
		// la informacion del viejo registro ya no existe en la pagina, pero se usa para actualizar los indices.
		Registro nuevoRegistro = super.registro(idRegistro);
		try {
			FabricaHashManager.dameInstancia().actualizarRegistro(viejoRegistro, nuevoRegistro);
		} finally {
			super.liberarRegistro(idRegistro);
		}
	}

	@Override
	public boolean borrarRegistro(servidor.tabla.Registro.ID idRegistro) {
		Registro registro = super.registro(idRegistro);
		try {
			FabricaHashManager.dameInstancia().borrarRegistro(registro);
		} finally {
			super.liberarRegistro(idRegistro);
		}
		return super.borrarRegistro(idRegistro);
	}

	@Override
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		super.insertarRegistro(idRegistro, valores);
        Registro nuevoRegistro = super.registro(idRegistro);
        try {
        	FabricaHashManager.dameInstancia().agregarRegistro(nuevoRegistro);
        } finally {
        	super.liberarRegistro(idRegistro);
        }
	}

	/**
	 * @see servidor.tabla.impl.AbstractPaginaDecorador#insertarRegistro(java.util.Collection)
	 */
	@Override
	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		Registro.ID idRegistro = super.insertarRegistro(valores);
        Registro nuevoRegistro = super.registro(idRegistro);
        try {
        	FabricaHashManager.dameInstancia().agregarRegistro(nuevoRegistro);
        } finally {
        	super.liberarRegistro(idRegistro);
        }
        return idRegistro;
	}

}
