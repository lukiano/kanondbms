/**
 * 
 */
package servidor.buffer.politica.impl;

import java.util.LinkedList;
import java.util.List;

import servidor.buffer.Bloque.ID;
import servidor.buffer.politica.PoliticaReemplazo;

/**
 * Politica de Reemplazo basada en un metodo FIFO.
 * Se encolan a medida que son creados o accedidos (si no se encontraban).
 * Se van removiendo segun el orden de la lista.
 *
 */
public class PR_FIFO implements PoliticaReemplazo {
    
    /**
     * La lista que guarda los identificadores de los bloques que van siendo accedidos.
     */
    protected List<ID> ids;
    
    /**
     * Constructor de la clase. Inicializa la lista.
     */
    public PR_FIFO() {
        this.ids = new LinkedList<ID>();
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#accedido(servidor.buffer.Bloque.ID)
     */
    public void accedido(ID id) {
    	if (!this.ids.contains(id)) {
    		this.creado(id);	
    	}
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#creado(servidor.buffer.Bloque.ID)
     */
    public synchronized void creado(ID id) {
        this.ids.add(id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#removido(servidor.buffer.Bloque.ID)
     */
    public synchronized void removido(ID idPagina) {
        this.ids.remove(idPagina);
    }

    /**
     * Se devuelve el primer elemento de la lista.
     * @see servidor.buffer.politica.PoliticaReemplazo#aRemover()
     */
    public synchronized ID aRemover() {
        return this.ids.get(0);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#proximoARemover(servidor.buffer.Bloque.ID)
     */
    public ID proximoARemover(ID idPagina) {
        int indiceParametro = this.ids.indexOf(idPagina);
        if (indiceParametro < this.ids.size()) {
            return this.ids.get(indiceParametro + 1);
        }
        return null;
    }

}
