/**
 * 
 */
package servidor.tabla.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.excepciones.RegistroExistenteException;
import servidor.inspector.Inspector;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;

/**
 *
 */
public class InspectorTabla extends AbstractTablaDecorador {

	private Inspector inspector;
	
	/**
	 * @param tablaDecorada
	 */
	public InspectorTabla(Tabla tablaDecorada) {
		super(tablaDecorada);
		this.inspector = new Inspector(tablaDecorada.id().toString());
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void actualizarRegistro(servidor.tabla.Registro.ID idRegistro, Collection<Valor> valores) {
		this.inspector.agregarEvento("Update", idRegistro.toString());
		this.inspector.agregarEvento(this.valores(valores));
		super.actualizarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	@Override
	public boolean borrarRegistro(servidor.tabla.Registro.ID idRegistro) {
		this.inspector.agregarEvento("Delete", idRegistro.toString());
		return super.borrarRegistro(idRegistro);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void insertarRegistro(servidor.tabla.Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		this.inspector.agregarEvento("Insert", idRegistro.toString());
		this.inspector.agregarEvento(this.valores(valores));
		super.insertarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(java.util.Collection)
	 */
	@Override
	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		Registro.ID idRegistro = super.insertarRegistro(valores);
		this.inspector.agregarEvento("Insert", idRegistro.toString());
		this.inspector.agregarEvento(this.valores(valores));
		return idRegistro;
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#registro(servidor.tabla.Registro.ID)
	 */
	@Override
	public Registro registro(servidor.tabla.Registro.ID idRegistro) {
		this.inspector.agregarEvento("Registro", idRegistro.toString());
		Registro registro = super.registro(idRegistro);
		this.inspector.agregarEvento(this.valores(registro));
		return registro;
	
	}
	
	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		this.inspector.parar();
	}

	private String[] valores(Registro registro) {
		List<Valor> valores = registro.getValores(); 
		return this.valores(valores);
	}
	
	private String[] valores(Collection<Valor> valores) {
		List<String> ret = new ArrayList<String>(2 * valores.size());
		for (Valor valor : valores) {
			ret.add(String.valueOf(valor.posicion()));
			ret.add((String) Conversor.conversorATexto().convertir(valor.campo(), valor.contenido()));
		}
		return ret.toArray(new String[ret.size()]);
	}

}
