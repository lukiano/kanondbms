/**
 * 
 */
package servidor.indice.hash.impl;

import servidor.buffer.BufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.indice.hash.HashColumna;
import servidor.indice.hash.HashManager;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;

public class HashManagerImpl implements HashManager {

	private BufferManager bufferManager;
	
	private Catalogo catalogo;
	
	/**
	 * 
	 */
	public HashManagerImpl(BufferManager bufferManager) {
		this.bufferManager = bufferManager;
		this.catalogo = FabricaCatalogo.dameInstancia();
		
	}

	/**
	 * @see servidor.indice.hash.HashManager#actualizarRegistro(servidor.tabla.Registro, servidor.tabla.Registro)
	 */
	public void actualizarRegistro(Registro viejoRegistro,
			Registro nuevoRegistro) {
		if (!viejoRegistro.id().equals(nuevoRegistro.id())) {
			throw new RuntimeException("No se pueden actualizar los indices de dos registros en distinta ubicacion.");
		}
		Tabla.ID idTabla = viejoRegistro.id().propietario().propietario();
		Tabla tabla = this.catalogo.dameTabla(idTabla.nombre());
		if (tabla == null) {
			throw new RuntimeException("Inconsistencia de datos. No exista la tabla con ID " + idTabla);
		}
		Columna[] columnas = tabla.columnas();
		for (int i = 0; i < columnas.length; i++) {
			HashColumna hashColumna = this.dameHashColumna(idTabla, columnas[i].campo(), i);
			hashColumna.actualizarRegistro(viejoRegistro.id(), viejoRegistro.valor(i), nuevoRegistro.valor(i));
		}
	}

	/**
	 * @see servidor.indice.hash.HashManager#agregarRegistro(servidor.tabla.Registro)
	 */
	public void agregarRegistro(Registro registro) {
		Tabla.ID idTabla = registro.id().propietario().propietario();
		Tabla tabla = this.catalogo.dameTabla(idTabla.nombre());
		if (tabla == null) {
			throw new RuntimeException("Inconsistencia de datos. No exista la tabla con ID " + idTabla);
		}
		Columna[] columnas = tabla.columnas();
		for (int i = 0; i < columnas.length; i++) {
			HashColumna hashColumna = this.dameHashColumna(idTabla, columnas[i].campo(), i);
			hashColumna.agregarRegistro(registro.id(), registro.valor(i));
		}
	}

	private HashColumna dameHashColumna(servidor.tabla.Tabla.ID idTabla, Campo campo, int i) {
		HashColumna.ID idHashColumna = HashColumna.ID.nuevoID(idTabla, i);
		HashColumna hashColumna = new HashColumnaImpl(idHashColumna, this.bufferManager, campo);
		return hashColumna;
	}

	/**
	 * @see servidor.indice.hash.HashManager#borrarRegistro(servidor.tabla.Registro)
	 */
	public boolean borrarRegistro(Registro registro) {
		Tabla.ID idTabla = registro.id().propietario().propietario();
		Tabla tabla = this.catalogo.dameTabla(idTabla.nombre());
		if (tabla == null) {
			throw new RuntimeException("Inconsistencia de datos. No exista la tabla con ID " + idTabla);
		}
		boolean borrado = false; // devuelve true si hubo al menos un borrado en las HashColumna
		Columna[] columnas = tabla.columnas();
		for (int i = 0; i < columnas.length; i++) {
			HashColumna hashColumna = this.dameHashColumna(idTabla, columnas[i].campo(), i);
			borrado |= hashColumna.borrarRegistro(registro.id(), registro.valor(i));
		}
		return borrado;
	}

	/**
	 * @see servidor.indice.hash.HashManager#dameRegistros(servidor.tabla.Tabla.ID, int, java.lang.Object)
	 */
	public Iterador<Registro.ID> dameRegistros(Tabla.ID idTabla, int columna, Object valor) {
		Tabla tabla = this.catalogo.dameTabla(idTabla.nombre());
		if (tabla == null) {
			throw new RuntimeException("Inconsistencia de datos. No existe la tabla con ID " + idTabla);
		}
		HashColumna hashColumna = this.dameHashColumna(idTabla, tabla.columnas()[columna].campo(), columna);
		return hashColumna.dameRegistros(valor);
	}

}
