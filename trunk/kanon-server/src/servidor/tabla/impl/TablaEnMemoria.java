/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;

import servidor.buffer.Bloque;
import servidor.buffer.FabricaBufferManager;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Tipo;
import servidor.excepciones.RegistroExistenteException;
import servidor.fisico.impl.BloqueImpl;
import servidor.tabla.Columna;
import servidor.tabla.FabricaPagina;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;

/**
 * Implementación de tabla que guarda los registros 
 * en memoria (en un bloque, maximo 2048 entradas)
 * @author lleggieri
 *
 */
public final class TablaEnMemoria implements Tabla {
    
    private Pagina contenido;
    
    private Tabla.ID id;
    
    private Columna[] columnas;
    
    /**
     * 
     */
    public TablaEnMemoria(Tabla.ID id, Columna ... cols) {
        this.id = id;
        Pagina.ID paginaID = Pagina.ID.nuevoID(id, 0);
        this.columnas = new Columna[cols.length];
        System.arraycopy(cols, 0, this.columnas, 0, cols.length);
        this.contenido = FabricaPagina.damePagina(FabricaBufferManager.dameInstancia(), this.columnas(), paginaID, new BloqueImpl(Bloque.TAMANIO * 8));
    }

    /**
     * @see servidor.tabla.Tabla#id()
     */
    public ID id() {
        return this.id;
    }

    /**
     * @see servidor.tabla.OperaRegistros#registros()
     */
    public Iterador<Registro.ID> registros() {
        return this.contenido.registros();
    }

	public Iterador<servidor.tabla.Registro.ID> registrosDesde(servidor.tabla.Registro.ID idRegistro) {
		return this.contenido.registrosDesde(idRegistro);
	}

    /**
     * @see servidor.tabla.OperaRegistros#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
     */
    public void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores) {
        this.chequeoDeTipos(valores);
        this.contenido.actualizarRegistro(idRegistro, valores);
    }

    
    /**
     * @see servidor.tabla.OperaRegistros#borrarRegistro(servidor.tabla.Registro.ID)
     */
    public boolean borrarRegistro(Registro.ID idRegistro) {
        return this.contenido.borrarRegistro(idRegistro);
    }
    
    /**
     * @see servidor.tabla.OperaRegistros#liberarRegistro(servidor.tabla.Registro.ID)
     */
    public void liberarRegistro(Registro.ID idRegistro) {
    	this.contenido.liberarRegistro(idRegistro);
    }

    /**
     * @see servidor.tabla.Tabla#columnas()
     */
    public Columna[] columnas() {
        return this.columnas;
    }
    
    /**
     * @see servidor.tabla.OperaRegistros#dameIdRegistroLibre()
     */
    public Registro.ID dameIdRegistroLibre() {
    	return this.contenido.dameIdRegistroLibre();
    }

    /**
     * @see servidor.tabla.OperaRegistros#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
     */
    public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
        this.chequeoDeTipos(valores);
        this.contenido.insertarRegistro(idRegistro, valores);
    }

    /**
     * @see servidor.tabla.OperaRegistros#registro(servidor.tabla.Registro.ID)
     */
    public Registro registro(Registro.ID idRegistro) {
        return this.contenido.registro(idRegistro);
    }

    private void chequeoDeTipos(Collection<Valor> valores) throws IllegalArgumentException {
        for (Valor valor : valores) {
            int pos = valor.posicion();
            if (pos > this.columnas.length) {
                throw new IllegalArgumentException("posicion invalida " + pos);
            }
            Tipo tipo = this.columnas[pos].campo().tipo();
            if (!valor.contenido().getClass().equals(Tipo.dameClase(tipo))) {
                throw new IllegalArgumentException("tipo invalido " + pos);
            }
        }
    }

    /**
     * @return Returns the contenido.
     */
    public final Pagina getContenido() {
        return this.contenido;
    }

	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		return this.contenido.insertarRegistro(valores);
	}
}
