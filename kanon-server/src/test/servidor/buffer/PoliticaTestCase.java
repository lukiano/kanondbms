/**
 * 
 */
package test.servidor.buffer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import servidor.buffer.politica.PoliticaReemplazo;
import servidor.buffer.politica.impl.InspectorPoliticaReemplazo;
import servidor.buffer.politica.impl.PR_FIFO;
import servidor.buffer.politica.impl.PR_LFU;
import servidor.tabla.Pagina;
import servidor.tabla.Tabla;

/**
 * @author lleggieri
 *
 */
public class PoliticaTestCase extends TestCase {

    private Pagina.ID[] idPaginas;

    /**
     * @param name
     */
    public PoliticaTestCase(String name) {
        super(name);
    }

    /**
     * 
     */
    public PoliticaTestCase() throws Exception {
        super();
    }

    @Override
	protected void setUp() {
        this.idPaginas = new Pagina.ID[5];
        Tabla.ID idTabla = Tabla.ID.nuevoID("tabla", 1);
        for (int i = 0; i < this.idPaginas.length; i++) {
            this.idPaginas[i] = Pagina.ID.nuevoID(idTabla, i);
        }
    }
    
    public void testFIFO() throws Exception {
        PoliticaReemplazo politicaReemplazo =
            new InspectorPoliticaReemplazo(
                    new PR_FIFO());
        politicaReemplazo.creado(this.idPaginas[0]);
        politicaReemplazo.creado(this.idPaginas[1]);
        politicaReemplazo.creado(this.idPaginas[2]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[0]);
        
        politicaReemplazo.accedido(this.idPaginas[1]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[0]);
        
        politicaReemplazo.removido(this.idPaginas[1]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[0]);

        politicaReemplazo.creado(this.idPaginas[3]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[0]);

        politicaReemplazo.accedido(this.idPaginas[2]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[0]);

        politicaReemplazo.removido(this.idPaginas[0]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[2]);
        
        politicaReemplazo.accedido(this.idPaginas[3]);
        assertEquals(politicaReemplazo.aRemover(), this.idPaginas[2]);
    }
    
    public void testLFU() throws Exception {
        PoliticaReemplazo politicaReemplazo = new PR_LFU();
        politicaReemplazo.creado(this.idPaginas[0]);
        politicaReemplazo.creado(this.idPaginas[1]);
        politicaReemplazo.creado(this.idPaginas[2]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0], this.idPaginas[1], this.idPaginas[2]);
        
        politicaReemplazo.accedido(this.idPaginas[1]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0], this.idPaginas[2]);
        
        politicaReemplazo.removido(this.idPaginas[1]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0], this.idPaginas[2]);

        politicaReemplazo.creado(this.idPaginas[3]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0], this.idPaginas[2], this.idPaginas[3]);

        politicaReemplazo.accedido(this.idPaginas[2]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0], this.idPaginas[3]);

        politicaReemplazo.accedido(this.idPaginas[3]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0]);

        politicaReemplazo.accedido(this.idPaginas[0]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0], this.idPaginas[2], this.idPaginas[3]);

    }
    
    private static final void assertPaginaARemover(PoliticaReemplazo politicaReemplazo, Pagina.ID ... opciones) throws Exception {
    	Set<Pagina.ID> conjuntoOpciones = new HashSet<Pagina.ID>(opciones.length);
    	Collections.addAll(conjuntoOpciones, opciones);
    	assertTrue(conjuntoOpciones.contains(politicaReemplazo.aRemover()));
    }

    public void testLRU() throws Exception {
        PoliticaReemplazo politicaReemplazo = new PR_LFU();
        politicaReemplazo.creado(this.idPaginas[0]);
        politicaReemplazo.creado(this.idPaginas[1]);
        politicaReemplazo.creado(this.idPaginas[2]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0]);
        
        politicaReemplazo.accedido(this.idPaginas[1]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0]);
        
        politicaReemplazo.removido(this.idPaginas[1]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0]);

        politicaReemplazo.creado(this.idPaginas[3]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[0]);

        politicaReemplazo.accedido(this.idPaginas[0]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[2]);

        politicaReemplazo.removido(this.idPaginas[2]);
        assertPaginaARemover(politicaReemplazo, this.idPaginas[3]);
    }

}
