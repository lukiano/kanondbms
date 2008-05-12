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
public class LogTableModel extends AbstractTableModel {
	
	private LinkedList<String[]> filas = new LinkedList<String[]>();
	
    private ObjectInputStream objectInputStream = null;
    
    private Socket socket;

    private boolean parar = false;
    
    private boolean noConecto= true;
    
    private final int MAX_FILAS = 50;
    
    private InetAddress direccionServidor;
    
	/**
	 * 
	 */
	public LogTableModel(InetAddress direccionServidor) {
		this.direccionServidor = direccionServidor;
		noConecto= true;
		Runnable runnable = new Runnable() {
			public void run() {
				conectar();
				while (!LogTableModel.this.parar) {
					try {
						String[] datos = (String[])LogTableModel.this.objectInputStream.readObject();
						LogTableModel.this.filas.add(datos);
					} catch (IOException e) {
						cerrarConexionActual();
						conectar();
					} catch (ClassNotFoundException e) {
						cerrarConexionActual();
						conectar();
					}
					while (LogTableModel.this.filas.size() > MAX_FILAS) {
						LogTableModel.this.filas.removeFirst();
					}
					LogTableModel.this.fireTableDataChanged();
				}
			}

		};
		Thread thread = new Thread(runnable, "LogThread");
		thread.start();
	}

	private void cerrarConexionActual() {
		try {
			if (this.objectInputStream != null) {
				this.objectInputStream.close();
			}
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void conectar() {
		while (noConecto){
			try {
				this.socket = new Socket(this.direccionServidor, 4447);
				LogTableModel.this.objectInputStream = new ObjectInputStream(socket.getInputStream());
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
		return 5;
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
    	return "";
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
