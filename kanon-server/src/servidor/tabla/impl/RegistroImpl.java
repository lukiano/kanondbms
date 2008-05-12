/**
 * 
 */
package servidor.tabla.impl;

import java.util.ArrayList;
import java.util.List;

import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.tabla.Columna;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;

/**
 * @author lleggieri
 *
 */
class RegistroImpl implements Registro {
    
    private final Registro.ID id;
    
    private final Pagina paginaPropietaria;
    
    private List<Valor> valores;
    
    /**
     * 
     */
    public RegistroImpl(Pagina paginaPropietaria, Registro.ID id, int aridad) {
        this.id = id;
        this.paginaPropietaria = paginaPropietaria;
        this.valores = new ArrayList<Valor>(aridad);
        for (int i = 0; i < aridad; i++) {
            this.valores.add(null);
        }
    }

    public RegistroImpl(Pagina paginaPropietaria, Registro registro) {
        this.id = registro.id();
        this.paginaPropietaria = paginaPropietaria;
        this.valores = new ArrayList<Valor>(registro.aridad());
        this.valores.addAll(registro.getValores());
    }

    public RegistroImpl(Pagina paginaPropietaria, Registro.ID id, byte[] datos, Columna[] columnas) {
        this.id = id;
        this.paginaPropietaria = paginaPropietaria;
        this.valores = new ArrayList<Valor>(columnas.length);
        int index = 0;
        Conversor conversor = Conversor.conversorDeBytes();
        for (int i = 0; i < columnas.length; i++) {
        	int longitudCampo = columnas[i].campo().longitud();
        	byte[] campoEnBytes = new byte[longitudCampo];
        	System.arraycopy(datos, index, campoEnBytes, 0, longitudCampo);
        	Object contenido = conversor.convertir(columnas[i].campo(), campoEnBytes);
            this.valores.add(i, Valor.nuevoValor(i, columnas[i].campo(), contenido));
            index += longitudCampo;
        }
    }
    
    /**
     * @see servidor.tabla.Registro#id()
     */
    public Registro.ID id() {
        return this.id;
    }

    /**
     * @see servidor.tabla.Registro#aridad()
     */
    public int aridad() {
        return this.valores.size();
    }

    /**
     * @see servidor.tabla.Registro#valor(int)
     */
    public Object valor(int columna) {
        return this.valores.get(columna).contenido();
    }

    public void establecerValor(Valor valor) {
        this.valores.set(valor.posicion(), valor);
    }

    /**
     * @see servidor.tabla.Registro#esValido()
     */
    public boolean esValido() {
        return this.paginaPropietaria.esValida();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return "ID: " + this.id() + "\nValores " + this.valores; 
    }

	/**
	 * @see servidor.tabla.Registro#getValores()
	 */
	public List<Valor> getValores() {
		return new ArrayList<Valor>(this.valores);
	}

	
}
