/**
 * 
 */
package servidor.catalog;

import servidor.catalog.impl.CatalogoImpl;


/**
 * Fabrica que devuelve una instancia unica del catalogo en el motor.
 * 
 */
public final class FabricaCatalogo {
	
	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static Catalogo instancia;

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaCatalogo() {
		super();
	}
	
	/**
	 * Devuelve la instancia unica del Catalogo.
	 * @return la instancia unica del Catalogo.
	 */
	public static synchronized Catalogo dameInstancia() {
		if (instancia == null) {
			instancia = new CatalogoImpl();
		}
		return instancia;
	}

}
