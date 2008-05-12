package servidor.buffer.politica;


/**
 * Fabrica que devuelve una instancia unica del Algoritmo de Politica de Reemplazo.
 */
public class FabricaPoliticaReemplazo {
	
	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static PoliticaReemplazo instancia;

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaPoliticaReemplazo() {
		super();
	}
	
	/**
	 * Devuelve la instancia unica del Algoritmo de Politica de Reemplazo.
	 * Por defecto se toma un algoritmo LRU. 
	 * Con la propiedad del sistema "politica" se puede establecer entre las politicas implementadas.
	 * @return la instancia unica del Algoritmo de Politica de Reemplazo.
	 */
	public static synchronized PoliticaReemplazo dameInstancia() {
		if (instancia == null) {
			String politica = System.getProperty("politica", "LRU");
			try {
				String nombrePaquete = PoliticaReemplazo.class.getName();
				nombrePaquete = nombrePaquete.substring(0, nombrePaquete.lastIndexOf('.'));
				Class<?> clazz = Class.forName(nombrePaquete + ".impl.PR_" + politica);
				Class<? extends PoliticaReemplazo> prevencionClass = clazz.asSubclass(PoliticaReemplazo.class);
				instancia = prevencionClass.newInstance();
			} catch (ClassCastException e) {
				throw new RuntimeException("Replacement policy '" + politica + "' not recognized.", e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Replacement policy '" + politica + "' not recognized.", e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Replacement policy '" + politica + "' not recognized.", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Replacement policy '" + politica + "' not recognized.", e);
			}
			//instancia = new InspectorPoliticaReemplazo(instancia);
			
			
		}
		return instancia;
	}

}
