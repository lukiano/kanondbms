/**
 * 
 */
package servidor.buffer.politica.impl;

import servidor.buffer.Bloque.ID;


/**
 * Politica de Reemplazo basada en un metodo LIFO.
 * Utiliza la lista heredada de la politica FIFO.
 * Se van removiendo en orden inverso de la lista.
 */
public class PR_LIFO extends PR_FIFO {
    
    /**
     * Constructor de la clase. Inicializa la lista.
     */
    public PR_LIFO() {
        super();
    }

    /**
     * Se devuelve el ultimo elemento de la lista.
     * @see servidor.buffer.politica.impl.PR_FIFO#aRemover()
     */
    @Override
    public synchronized ID aRemover() {
        return this.ids.get(this.ids.size() - 1);
    }

    /**
     * @see servidor.buffer.politica.impl.PR_FIFO#proximoARemover(servidor.buffer.Bloque.ID)
     */
    @Override
    public ID proximoARemover(ID id) {
        int indiceParametro = this.ids.indexOf(id);
        if (indiceParametro > 0) {
            return this.ids.get(indiceParametro - 1);
        }
        return null;
    }

}
