package servidor.inspector;

public interface Visor {
	
	void mostrarMensaje(String[] mensaje);
	
	String nombre();

	void cerrar();
	
}
