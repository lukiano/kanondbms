/**
 * 
 */
package servidor.buffer.politica.impl;

import servidor.buffer.Bloque.ID;
import servidor.buffer.politica.PoliticaReemplazo;
import servidor.inspector.Inspector;

/**
 * Decorador de una Politica de Reemplazo que informa en pantalla los eventos que van ocurriendo.
 */
public final class InspectorPoliticaReemplazo extends
        AbstractPoliticaReemplazoDecorador {

	/**
	 * El inspector que muestra los eventos.
	 */
    private Inspector inspector = new Inspector("PoliticaReemplazo");
    
	/**
	 * Constructor de la clase.
     * @param politicaReemplazo la Politica de Reemplazo a decorar.
     */
    public InspectorPoliticaReemplazo(PoliticaReemplazo politicaReemplazo) {
        super(politicaReemplazo);
    }

    /**
     * @see servidor.buffer.politica.impl.AbstractPoliticaReemplazoDecorador#accedido(servidor.buffer.Bloque.ID)
     */
    @Override
    public void accedido(ID id) {
        //this.inspector.agregarEvento("accedido", id.toString());
        super.accedido(id);
    }

    /**
     * @see servidor.buffer.politica.impl.AbstractPoliticaReemplazoDecorador#aRemover()
     */
    @Override
    public ID aRemover() {
        ID id = super.aRemover();
        this.inspector.agregarEvento("aRemover", id.toString());
        return id;
    }

    /**
     * @see servidor.buffer.politica.impl.AbstractPoliticaReemplazoDecorador#creado(servidor.buffer.Bloque.ID)
     */
    @Override
    public void creado(ID id) {
    	this.inspector.agregarEvento("creado", id.toString());
        super.creado(id);
    }

    /**
     * @see servidor.buffer.politica.impl.AbstractPoliticaReemplazoDecorador#removido(servidor.buffer.Bloque.ID)
     */
    @Override
    public void removido(ID id) {
    	this.inspector.agregarEvento("removido", id.toString());
        super.removido(id);
    }

    /**
     * @see servidor.buffer.politica.impl.AbstractPoliticaReemplazoDecorador#proximoARemover(servidor.buffer.Bloque.ID)
     */
    @Override
    public ID proximoARemover(ID id) {
        ID id2 = super.proximoARemover(id);
        if (id2 != null) {
            this.inspector.agregarEvento("aRemover", id2.toString(), "proximo de", id.toString());
        }
        return id2;
    }

}
