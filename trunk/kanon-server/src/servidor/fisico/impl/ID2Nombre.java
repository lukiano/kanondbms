/**
 * 
 */
package servidor.fisico.impl;

import servidor.buffer.Bloque.ID;

/**
 * Obtiene String a partir de ID.
 * Es usado para obtener el nombre del archivo en disco que
 * contiene a un bloque deseado.
 */
interface ID2Nombre {

    /**
     * Devuelve un String con un nombre que se corresponda con el identificador.
     * @param id el identificador de un bloque.
     * @return un String con un nombre que se corresponda con el identificador.
     * @throws RuntimeException si el identificador no se reconoce.
     */
    String dameNombre(ID id);
    
}
