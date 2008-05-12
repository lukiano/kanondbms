package servidor.lock.deadlock;

import servidor.lock.deadlock.impl.InspectorPDL;

/**
 * Fabrica que devuelve una instancia unica del Algoritmo de Prevencion de DeadLock.
 * 
 */
public class FabricaPrevencionDeadLock {
	
	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static PrevencionDeadLock instancia;
	
	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaPrevencionDeadLock() {
		super();
	}
	
	/**
	 * Devuelve la instancia unica del Algoritmo de Prevencion de DeadLock.
	 * Por defecto se toma el algoritmo CautionWaiting. 
	 * Con la propiedad del sistema "prevencion" se puede establecer entre los algoritmos implementados.
	 * @return la instancia unica del Algoritmo de Prevencion de DeadLock.
	 */
	public static synchronized PrevencionDeadLock dameInstancia() {
		if (instancia == null) {
			String prevencion = System.getProperty("prevencion", "CautionWaiting");
			try {
				String nombrePaquete = PrevencionDeadLock.class.getName();
				nombrePaquete = nombrePaquete.substring(0, nombrePaquete.lastIndexOf('.'));
				Class<?> clazz = Class.forName(nombrePaquete + ".impl.PDL_" + prevencion);
				Class<? extends PrevencionDeadLock> prevencionClass = clazz.asSubclass(PrevencionDeadLock.class);
				instancia = prevencionClass.newInstance();
			} catch (ClassCastException e) {
				throw new RuntimeException("Policy of prevention of deadlock '" + prevencion + "' not recognized.", e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Policy of prevention of deadlock '" + prevencion + "' not recognized.", e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Policy of prevention of deadlock '" + prevencion + "' not recognized.", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Policy of prevention of deadlock '" + prevencion + "' not recognized.", e);
			}
			instancia = new InspectorPDL(instancia);
		}
		return instancia;
	}

}
