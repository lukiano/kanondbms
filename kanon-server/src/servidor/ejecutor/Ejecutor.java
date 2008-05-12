package servidor.ejecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;

import servidor.catalog.tipo.Conversor;
import servidor.conexion.FabricaServidorTcp;
import servidor.conexion.ServidorTcp;
import servidor.ejecutor.xql.XStatement;
import servidor.ejecutor.xql.XStatementFactory;
import servidor.excepciones.ParseException;
import servidor.excepciones.VictimaDeadlockRuntimeException;
import servidor.parser.impl.MainParser;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;
import Zql.ZStatement;

/**
 * Clase principal que atiende los pedidos de un cliente.
 * Realiza un ciclo sobre cada sentencia que llega por la conexion.
 * El ciclo se compone de: parsear la sentencia, ejecutar el comando correspondiente y devolver el resultado o error.
 * @see XStatement
 */
public class Ejecutor implements Runnable {
	
	/**
	 * Constante con el primer mensaje que se le envia al cliente para corroborar la conexion.
	 */
	private static final String OK = "OK";
    
    /**
     * Socket conectado al cliente.
     */
    private Socket socket;
    
    /**
     * Variable con el Servidor de conexiones para avisarle cuando hay una nueva conexion y cuando se termina la misma.
     * Tambien se lo consulta para saber si el motor esta siendo apagado (comun o crash).
     */
    private ServidorTcp servidorTcp;
    
    /**
     * Constructor de la clase.
     * @param socket el socket conectado al cliente.
     */
    public Ejecutor(Socket socket) {
        this.socket = socket;
        this.servidorTcp = FabricaServidorTcp.dameInstancia();
        this.servidorTcp.nuevoSocketActivo(this.socket); // se le avisa sobre un nuevo socket activo.
    }
    
    /**
     * Metodo principal del thread con el ciclo que atiende los pedidos.
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            // se abre la conexion
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            
           PrintWriter salidaPrintWriter = new PrintWriter(socket.getOutputStream());
            //mensaje de OK al cliente
           salidaPrintWriter.println(OK);
           salidaPrintWriter.flush();
           
           StringWriter stringWriter = new StringWriter();
           PrintWriter bufferIntermedio = new PrintWriter(stringWriter);

            String linea = null;
            try {
        		do {
                    // se lee una linea de la entrada (hasta que llega un enter)
            		linea = bufferedReader.readLine();
        			if (linea != null) {
                        boolean errorGrave = this.procesarLinea(bufferIntermedio, linea);
                        if (errorGrave) {
                        	// se flushea toda la entrada
            				bufferIntermedio.flush();
            				salidaPrintWriter.write(stringWriter.toString());
            				salidaPrintWriter.flush();
            				stringWriter.getBuffer().setLength(0);
            				while (bufferedReader.ready()) {
            					bufferedReader.read();
            				}
                        } else if (!bufferedReader.ready()) {
                        	// hay mas sentencias que llegan desde el cliente
               				bufferIntermedio.flush();
               				salidaPrintWriter.write(stringWriter.toString());
               				salidaPrintWriter.flush();
               				stringWriter.getBuffer().setLength(0);
                        }
        			}
        		} while (linea != null && !this.servidorTcp.apagado()); // el servidor se esta apagando, no leer mas lineas
           	} catch (SocketException ignorado) {
           		// se ignoran los errores del socket (ej, se cerro la conexion)
            } finally {
                // se cierra la conexion
            	bufferIntermedio.close();
            	salidaPrintWriter.close();
                bufferedReader.close();
                this.socket.close();
                this.servidorTcp.quitarSocket(this.socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
   }

	/**
	 * Llama al parser para que analice la linea y luego ejecuta el comando correspondiente.
	 * La ejecucion es encapsulada en una transaccion si no existia ningna.
	 * @param printWriter el escritor donde se escribe el resultado del comando.
	 * @param linea la sentencia a procesar y ejecutar.
	 * @return true si hubo un error grave y no hay que seguir procesando.
	 */
	private boolean procesarLinea(PrintWriter printWriter, String linea) {
        XStatement xql;
        try {
            xql = this.parsear(linea);
        } catch (ParseException ex) {
            ex.printStackTrace();
            printWriter.println("ERROR: (PARSE) " + ex.getMessage());
            printWriter.flush();
            return false;
        } catch (RuntimeException e) {
            printWriter.println("ERROR: " + e.getMessage());
            printWriter.flush();
            throw e;
        } catch (Error e) {
            printWriter.println("ERROR: " + e.getMessage());
            printWriter.flush();
        	throw e;
        }
        if (xql == null) {
        	return false;
        }
        // se parseo correctamente.
                
        Resultado resultado = new Resultado(); // aqui se alojara el resultado de la operacion
        try {
            resultado = xql.execute();
        } catch (RuntimeException e) {
            printWriter.println("ERROR: " + e.getMessage());
            printWriter.flush();
        	if (e instanceof VictimaDeadlockRuntimeException) {
        		return true;
        	} else {
                e.printStackTrace();
                return false;
        	}
        } catch (Error e) {
            printWriter.println("ERROR: " + e.getMessage());
            printWriter.flush();
            throw e;
        }
        
		if (resultado.getMensaje() != null) {
			// hubo un mensaje (puede o no ser un error)
		    printWriter.println(resultado.getMensaje()); // se responde con el resultado
		    printWriter.flush();
		} else {
			// el resultado es una tabla (fue un SELECT)
			Tabla tabla = resultado.getTabla();
			printWriter.println(this.procesarTabla(tabla));
			printWriter.flush();
		}
		return false;
	}

	/**
	 * Se convierte una tabla en un formato String para ser mostrado por el cliente.
	 * @param tabla la tabla a transformar.
	 * @return una cadena con varias lineas que representan la tabla en un formato textual.
	 * @see StringWriter
	 * @see PrintWriter
	 * @see Ejecutor#fijarEn(String, int)
	 */
	private String procesarTabla(Tabla tabla) {
		final int TAMANIO = 20; // el tamanio maximo que puede tener cada columna
		Columna[] columnas = tabla.columnas();
		int[] tamanio = new int[columnas.length];
		for (int i = 0; i < tamanio.length; i++) {
			tamanio[i] = Math.min(columnas[i].nombre().length() + 4, TAMANIO); // se calcula para cada columna el tamanio que va a tener.
		}
		
		Conversor conversor = Conversor.conversorATexto(); // Permite convertir los valores de la tabla (tomando en cuenta el campo de la columna) a texto.
		
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		// primero se imprimen los nombres de las columnas
		for (int i = 0; i < columnas.length; i++) {
			printWriter.print(this.fijarEn(columnas[i].nombre(), tamanio[i]));
			printWriter.print('|');
		}
		printWriter.println();
		// ahora se imprime un separador que diferencie los nombres de las columnas con el contenido
		for (int i = 0; i < columnas.length; i++) {
			for (int j = 0; j < tamanio[i]; j++) {
				printWriter.print('-');
			}
			printWriter.print('+');
		}
		printWriter.println();
		// ahora se imprime el contenido. Se va iterando por los elementos de la tabla.
		Iterador<Registro.ID> iterador = tabla.registros();
		try {
			while (iterador.hayProximo()) {
				Registro.ID proximo = iterador.proximo();
				try {
					Registro registro = tabla.registro(proximo);
					for (int i = 0; i < columnas.length; i++) {
						Object valorCrudo = registro.valor(i);
						String valor = (String) conversor.convertir(columnas[i].campo(), valorCrudo);
						printWriter.print(this.fijarEn(valor, tamanio[i]));
						printWriter.print('|');
					}
					printWriter.println();
				} finally {
					tabla.liberarRegistro(proximo);
				}
			}
		} finally {
			iterador.cerrar();
		}
		// se cierran los escritores y se devuelve el resultado
		printWriter.flush();
		printWriter.close();
		try {
			stringWriter.close();
		} catch (IOException ignorada) {
			// es un flujo en memoria, no va a ocurrir
		}
		return stringWriter.toString();
	}

    /**
     * Centra un texto entre espacio para obtener un nuevo texto de una determinada longitud.
     * Si la nueva longitud es menor a la del texto original, este es cortado para que quepa.
     * @param string el texto a centrar o recortar.
     * @param tamanio el largo del texto que se desea fijar.
     * @return una secuencia de caracteres del tamanio pasado por parametro.
     */
    private CharSequence fijarEn(String string, int tamanio) {
    	string = string.trim(); // se eliminan los espacios en los bordes que pueda tener el texto.
    	if (string.length() < tamanio) {
    		// el tamanio del texto es menor al nuevo => se centra
    		int espacio = (tamanio - string.length()) / 2; // la cantidad de espacios que iran a cada lado.
    		int resto = (tamanio - string.length()) % 2; // si hay una diferencia impar, los espacios del lado derecho tendran un caracter mas.
			StringBuilder stringBuilder = new StringBuilder();
			for (int j = 0; j < espacio; j++) {
				stringBuilder.append(' '); // los espacios del lado izquierdo
			}
			stringBuilder.append(string); // el texto
			for (int j = 0; j < espacio + resto; j++) {
				stringBuilder.append(' '); // los espacios del lado derecho
			}
			return stringBuilder;
    	} else if (string.length() > tamanio) {
    		// el tamanio del texto es mayor al nuevo => se trunca
    		return string.subSequence(0, tamanio);
    	} else {
    		// el tamanio viejo y el nuevo son iguales => se devuelve el mismo texto
    		return string;
    	}
	}

	/**
	 * Llama al parser para que analice la linea y devuelve un error al cliente en caso de
	 * encotrarse uno en la parte de analisis.
	 * @param sentencia la linea a procesar.
	 * @return NULL si la sentencia estaba vacia, o un XStatement correspondiente segun la setencia con los datos descompuestos a partir de la misma.
	 * @see ZStatement
	 * @see XStatement
	 * @throws ParseException si hubo un error al analizar la sentencia.
	 */
	private XStatement parsear(String sentencia) throws ParseException {
        
        /*
         * recibo la sentencia y hago algo con ella (por ahora solo la descompongo en pedazos)
         */
        ZStatement zql = MainParser.initParser(sentencia);
        if (zql == null) {
        	// solo es NULL si la sentencia estaba vacia.
        	return null;
        }
        XStatement xql = XStatementFactory.getStatement(zql); // se obtiene el XStatement correspondiente
        xql.zqlToXql(zql); // se llenan las variables del XStatement a partir de las obtenidas en el ZStatement.
        return xql;
    }

}