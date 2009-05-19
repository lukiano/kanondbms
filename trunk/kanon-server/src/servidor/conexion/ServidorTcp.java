package servidor.conexion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import servidor.buffer.FabricaBufferManager;
import servidor.catalog.FabricaCatalogo;
import servidor.ejecutor.Ejecutor;
import servidor.fisico.FabricaDiskSpaceManager;
import servidor.indice.hash.FabricaHashManager;
import servidor.lock.FabricaLockManager;
import servidor.log.FabricaRecoveryManager;
import servidor.log.RecoveryManager;
import servidor.transaccion.FabricaTransactionManager;

/**
 * Clase principal del motor que maneja las conexiones con los clientes.
 * Cada nuevo cliente tiene su thread con un Ejecutor que procesa sus peticiones.
 * Tambien se encarga del apagado del motor.
 * @author victor
 * @see Ejecutor
 */
public class ServidorTcp {

    /**
     * Puerto donde se manejan las conexiones con los clientes.
     */
    public static final int PORT = 34444;
    
    /**
     * Puerto donde se escucha la senial para el apagado del motor.
     */
    public static final int STOP_PORT = 34445;
    
    /**
     * Socket que escucha a los clientes.
     */
    private ServerSocket socketServidor;
    
    /**
     * Variable que indica si el servidor esta siendo apagado.
     */
    private boolean apagado = false;
    
    /**
     * Variable que indica si el servidor esta simulando un crash (apagado sin terminar las transacciones activas ni guardar bloques sucios en disco).
     */
    private boolean crash = false;
    
    /**
     * Fabrica que genera los threads que atenderan los pedidos de los clientes.
     */
    private ThreadFactory fabricaThreads;
    
    /**
     * Coleccion que guarda cuales sockets se encuentran activos.
     */
    private Collection<Socket> socketActivos = new HashSet<Socket>();
    
    /**
     * Thread que escucha el pedido de apagado del motor.
     */
    private StopperThread stopperThread;
    
    /**
     * Constructor de la clase.
     */
    public ServidorTcp() {
    }
    
    public void iniciarServidor() throws IOException {
        this.chequearRecovery();
        this.inicializacionManagers();
    	
        // Por defecto se crea un nuevo thread para cada nueva conexion. Esta fabrica podria reemplazarse por una que maneje un pool de threads.
        this.fabricaThreads = Executors.defaultThreadFactory();
        
        // Se abre el puerto de escucha de clientes.
        int port = Integer.getInteger("port", PORT);
        this.socketServidor = new ServerSocket(port);
        
        // Se abre el puerto de escucha del apagado del motor.
        int stopPort = Integer.getInteger("stop-port", STOP_PORT);
        this.stopperThread = new StopperThread(stopPort, this);
        Thread stopDaemon = new Thread(this.stopperThread, "StopDaemon");
        stopDaemon.setDaemon(false);
        stopDaemon.start();
        
        System.out.println("Initiated server.");
        
        // Escuchar pedidos...
        while(!this.apagado) {
            this.escucharPedidos();
        }
    }

	/**
	 * Metodo que inicializa los distintos administradores del motor.
	 */
	private void inicializacionManagers() {
		FabricaRecoveryManager.dameInstancia();
        FabricaLockManager.dameInstancia();
        FabricaCatalogo.dameInstancia();
        FabricaTransactionManager.dameInstancia();
        FabricaBufferManager.dameInstancia();
        FabricaDiskSpaceManager.dameInstancia();
        FabricaHashManager.dameInstancia();
	}
    
    /**
     * Metodo que se ejecuta al comienzo del servidor para devolver en caso que haga falta los datos de la base a un estado consistente.
     * @see RecoveryManager
     */
    private void chequearRecovery() {
        System.out.println("Checking need to recover the system...");
        RecoveryManager recoveryManager =
            FabricaRecoveryManager.dameInstancia();
        recoveryManager.recuperarSistema();
        System.out.println("System is stable.");
    }
    
    /**
     * Metodo para apagar el servidor.
     * @param crash true si se desea simular un crash. False si se desea apagar el servidor suavemente.
     * @throws IOException si ocurre algun error de I/O.
     */
    public void apagar(boolean crash) throws IOException {
    	this.apagado = true;
        this.crash = crash;
        FabricaLockManager.dameInstancia().cerrar(); // se liberan todos los threads bloqueados.
        
		RecoveryManager recoveryManager = FabricaRecoveryManager.dameInstancia(); 
        if (!crash) {
            recoveryManager.checkpoint();
        }
        recoveryManager.log().cerrar();
        
        // se cierran los sockets activos.
        if(this.socketServidor != null) {
        	this.socketServidor.close();
        }
        for (Socket socketActivo : this.socketActivos) {
        	socketActivo.close();
        }
        
        // se termina el thread que escucha el apagado.
        if (this.stopperThread != null) {
            this.stopperThread.close();
        }
    }
        
    /**
     * Espera una nueva conexion de un cliente y le pide a la fabrica de Threads un nuevo thread para que maneje dicha conexion.
     * @throws IOException Si ocurre un error de I/O.
     * @see Ejecutor
     */
    private void escucharPedidos() throws IOException {
    	try {
    		Socket cliente = socketServidor.accept();
    		
            // ahora creamos un thread que maneje esa conexion
            Runnable ejecutor = new Ejecutor(cliente);
            this.fabricaThreads.newThread(ejecutor).start();
    	} catch (SocketException ignorada) {
    		// los errores de socket (ej, cierre) no son tomados en cuenta.
    	}
    }
    
    /**
     * @return true si el servidor esta siendo apagado.
     */
    public boolean apagado() {
    	return this.apagado;
    }

    /**
     * @return true si el servidor esta siendo apagado simulando un crash (apagado sin terminar las transacciones activas ni guardar bloques sucios en disco).
     */
    public boolean crash() {
    	return this.crash;
    }
    
    /**
     * Agrega un nuevo socket al conjunto de sockets activos.
     * @param socket el socket a agregar.
     */
    public void nuevoSocketActivo(Socket socket) {
    	this.socketActivos.add(socket);
    }
    
    /**
     * Elimina un socket del conjunto de sockets activos.
     * @param socket el socket a remover.
     */
    public void quitarSocket(Socket socket) {
    	this.socketActivos.remove(socket);
    }
    
}
