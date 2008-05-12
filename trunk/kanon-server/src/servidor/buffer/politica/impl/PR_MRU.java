/**
 * 
 */
package servidor.buffer.politica.impl;

import java.util.SortedMap;

import servidor.buffer.Bloque.ID;


/**
 * Politica de reemplazo usando un algoritmo MRU.
 * Las estructuras se heredan del algoritmo LRU.
 */
public class PR_MRU extends PR_LRU {

    /**
     * @see servidor.buffer.politica.impl.PR_LRU#aRemover()
     */
    @Override
    public synchronized ID aRemover() {
    	// Se devuelve el ultimo elemento (mas reciente).
        Long primerClave = this.longs.lastKey();
        return this.longs.get(primerClave);
    }

    /**
     * @see servidor.buffer.politica.impl.PR_LRU#proximoARemover(servidor.buffer.Bloque.ID)
     */
    @Override
    public ID proximoARemover(ID id) {
        Long claveParametro = this.ubicacion.get(id);
        if (claveParametro == null) {
        	return this.aRemover();
        }
        SortedMap<Long, ID> temporalMap = this.longs.headMap(claveParametro);
        if (temporalMap.isEmpty()) {
        	return null;
        }
        Long claveProxima = temporalMap.lastKey();
        return this.longs.get(claveProxima);
    }
}
