/**
 * 
 */
package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class LockTableModel extends AbstractTableModel {
	
	private String[] columnNames = new String[] { "ID Tx", "Tabla", "Pagina", "Registro", "Modo", "Accion" }; 

	private LinkedList<String[]> filas = new LinkedList<String[]>();
	
    private ObjectInputStream objectInputStream;
    
    private Socket socket;

    private boolean parar = false;
    
    private boolean noConecto= true;
    
    private final int MAX_FILAS = 50;
    
    private InetAddress direccionServidor;
    
	/**
	 * 
	 */
	public LockTableModel(InetAddress direccionServidor) {
		this.direccionServidor = direccionServidor;
		noConecto = true;
		Runnable runnable = new Runnable() {
			public void run() {
				conectar();
				while (!LockTableModel.this.parar) {
					try {
						String[] datos = (String[])LockTableModel.this.objectInputStream.readObject();
						LockTableModel.this.filas.add(datos);
					} catch (IOException e) {
						conectar();
					} catch (ClassNotFoundException e) {
						conectar();
					}
					while (LockTableModel.this.filas.size() > MAX_FILAS) {
						LockTableModel.this.filas.removeFirst();
					}
					LockTableModel.this.fireTableDataChanged();
				}
			}
		};
		Thread thread = new Thread(runnable, "LockThread");
		thread.start();
	}

	public void conectar() {
		while (noConecto){
			try {
				this.socket = new Socket(this.direccionServidor, 4446);
				this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
				noConecto=false;
			} catch (UnknownHostException e) {
				noConecto=true;
			} catch (IOException e) {
				noConecto=true;
			}
			
			if (noConecto) {
				try {
					synchronized (this) {
						wait(2500);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 6;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return this.filas.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		String[] datos = this.filas.get(rowIndex);
		if (datos != null && columnIndex < datos.length) {
			return datos[columnIndex];
		}
		return null;
	}

	@Override
    public String getColumnName(int column) {
    	return this.columnNames[column];
	}

	public void cerrarConexion() {
		this.parar = true;
		try {
			if (this.objectInputStream != null) {
				this.objectInputStream.close();
			}
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
