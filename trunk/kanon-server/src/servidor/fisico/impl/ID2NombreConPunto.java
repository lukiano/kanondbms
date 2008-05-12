/**
 * 
 */
package servidor.fisico.impl;

import servidor.buffer.Bloque.ID;

/**
 * Implementacion de ID2Nombre que separa las componentes de los identificadores con un punto '.'
 */
final class ID2NombreConPunto implements ID2Nombre {

	/**
	 * @see servidor.fisico.impl.ID2Nombre#dameNombre(servidor.buffer.Bloque.ID)
	 */
	public String dameNombre(ID id) {
		if (id instanceof servidor.tabla.Pagina.ID) {
			return this.dameNombre((servidor.tabla.Pagina.ID)id);
		} else if (id instanceof servidor.indice.hash.Bucket.ID) {
			return this.dameNombre((servidor.indice.hash.Bucket.ID)id);
		} else {
			throw new RuntimeException("Identifier nonrecognized: " + id.toString());
		}
	}

    /**
     * @param idPagina un Id de una Pagina.
     * @return Un string con: {String id Tabla}.{nro pagina}
     */
    private String dameNombre(servidor.tabla.Pagina.ID idPagina) {
        return this.dameNombre(idPagina.propietario()) + '.' + idPagina.numeroPagina();
    }

    /**
     * @param idTabla el Id de una Tabla
     * @return un String con el numero de la tabla.
     */
    private String dameNombre(servidor.tabla.Tabla.ID idTabla) {
        return String.valueOf(idTabla.idTabla());
    }

    /**
     * @param idBucket un Id de un Bucket.
     * @return Un string con: {String id HashColumna}.{nro hash}.{nro bucket}
     */
    private String dameNombre(servidor.indice.hash.Bucket.ID idBucket) {
        return this.dameNombre(idBucket.propietario()) + '.' + idBucket.numeroHash() + '.' + idBucket.numeroBucket();
    }

    /**
     * @param idHash un Id de una HashColumna
     * @return Un string con: {String id Tabla}.{nro columna}
     */
    private String dameNombre(servidor.indice.hash.HashColumna.ID idHash) {
        return this.dameNombre(idHash.propietario()) + '.' + idHash.columna();
    }

}
