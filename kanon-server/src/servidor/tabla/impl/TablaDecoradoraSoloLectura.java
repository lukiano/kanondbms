package servidor.tabla.impl;

import java.util.Collection;

import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;

public class TablaDecoradoraSoloLectura extends AbstractTablaDecorador {
	
	private static final String READ_ONLY_TABLE = "No se pueden realizar cambios en una tabla de solo lectura.";

	public TablaDecoradoraSoloLectura(Tabla tablaDecorada) {
		super(tablaDecorada);
	}

	@Override
	public void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores) {
		throw new RuntimeException(READ_ONLY_TABLE);
	}

	@Override
	public boolean borrarRegistro(Registro.ID idRegistro) {
		throw new RuntimeException(READ_ONLY_TABLE);
	}

	@Override
	public Registro.ID insertarRegistro(Collection<Valor> valores) {
		throw new RuntimeException(READ_ONLY_TABLE);
	}

	@Override
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		throw new RuntimeException(READ_ONLY_TABLE);
	}

}
