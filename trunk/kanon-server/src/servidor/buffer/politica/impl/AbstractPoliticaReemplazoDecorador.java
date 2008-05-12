/**
 * 
 */
package servidor.buffer.politica.impl;

import servidor.buffer.Bloque.ID;
import servidor.buffer.politica.PoliticaReemplazo;

/**
 * Clase abstracta para decorar politicas de reemplazo.
 * Segun el Design Pattern Decorator.
 */
public abstract class AbstractPoliticaReemplazoDecorador implements
        PoliticaReemplazo {

    /**
     * La Politica de Reemplazo decorada.
     */
    private PoliticaReemplazo politicaReemplazo;
    
    /**
     * Constructor de la clase. Esta clase no se puede instanciar.
     * @param politicaReemplazo la politica a decorar.
     */
    public AbstractPoliticaReemplazoDecorador(PoliticaReemplazo politicaReemplazo) {
        this.politicaReemplazo = politicaReemplazo;
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#accedido(servidor.buffer.Bloque.ID)
     */
    public void accedido(ID id) {
        this.politicaReemplazo.accedido(id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#creado(servidor.buffer.Bloque.ID)
     */
    public void creado(ID id) {
        this.politicaReemplazo.creado(id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#removido(servidor.buffer.Bloque.ID)
     */
    public void removido(ID id) {
        this.politicaReemplazo.removido(id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#aRemover()
     */
    public ID aRemover() {
        return this.politicaReemplazo.aRemover();
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#proximoARemover(servidor.buffer.Bloque.ID)
     */
    public ID proximoARemover(ID id) {
        return this.politicaReemplazo.proximoARemover(id);
    }

}
