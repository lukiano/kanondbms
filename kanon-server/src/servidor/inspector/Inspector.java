package servidor.inspector;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public final class Inspector {
	
	private final class RunnableVisor implements Runnable {
		
		private Visor visor;
		
		public RunnableVisor(Visor visor) {
			this.visor = visor;
		}
		
		public void run() {
			try {
				while (!Inspector.this.parar) {
					while (!Inspector.this.cola.isEmpty()) {
						String[] mensaje = Inspector.this.cola.poll();
						if (mensaje != null) {
							this.visor.mostrarMensaje(mensaje);
						}
					}
					if (!Inspector.this.parar) {
						LockSupport.park();	
					}
				}
			} finally {
				this.visor.cerrar();
			}
		}
	}

	public static final int capacidadMaxima = 100;
	
	private Queue<String[]> cola = new ConcurrentLinkedQueue<String[]>();
	
	private Thread thread;
	
	private boolean parar = false;
	
	public Inspector(String nombreManager) {
		this(new VisorEnPantalla(nombreManager));
	}

	public Inspector(Visor visor) {
		Runnable runnable = new RunnableVisor(visor);
		this.thread = new Thread(runnable, "Inspector: " + visor.nombre());
		thread.setDaemon(true);
		thread.start();
	}

	public void agregarEvento(String... mensajes) {
		if (this.cola.size() > capacidadMaxima) {
			this.cola.poll();
		}
		this.cola.offer(mensajes);
		LockSupport.unpark(this.thread);
	}
	
	public void parar() {
		this.parar = true;
		LockSupport.unpark(this.thread);
	}
	
}
