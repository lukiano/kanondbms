package servidor.inspector;

public final class VisorEnPantalla implements Visor {
	
	private String nombre;
	
	/**
	 * @param nombre
	 */
	public VisorEnPantalla(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @see servidor.inspector.Visor#mostrarMensaje(java.lang.String[])
	 */
	public void mostrarMensaje(String[] mensaje) {
		StringBuilder mensajeBuilder = new StringBuilder();
		mensajeBuilder.append(this.nombre);
		mensajeBuilder.append(": ");
		for (String mensajeEvento : mensaje) {
			mensajeBuilder.append(mensajeEvento);
			mensajeBuilder.append(", ");
		}
		System.out.println(mensajeBuilder.toString());
	}

	public String nombre() {
		return this.nombre;
	}

	public void cerrar() {
	}

}
