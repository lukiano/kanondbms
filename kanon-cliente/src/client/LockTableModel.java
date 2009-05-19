/**
 * 
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class LockTableModel extends AbstractTableModel {
	
	private String[] columnNames = new String[] { "ID Tx", "Table", "Page", "Record", "Mode", "Action" }; 

	private LinkedList<String[]> rows = new LinkedList<String[]>();
	
    private ObjectInputStream objectInputStream;
    
    private Socket socket;

    private boolean stop = false;
    
    private boolean notConnected = true;
    
    private final int MAX_ROWS = 50;
    
    private InetAddress serverAddress;
    
	/**
	 * 
	 */
	public LockTableModel(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
		notConnected = true;
		Runnable runnable = new Runnable() {
			public void run() {
				connect();
				while (!LockTableModel.this.stop) {
					try {
						String[] data = (String[])LockTableModel.this.objectInputStream.readObject();
						LockTableModel.this.rows.add(data);
					} catch (IOException e) {
						closeCurrentConnection();
						connect();
					} catch (ClassNotFoundException e) {
						closeCurrentConnection();
						connect();
					}
					while (LockTableModel.this.rows.size() > MAX_ROWS) {
						LockTableModel.this.rows.removeFirst();
					}
					LockTableModel.this.fireTableDataChanged();
				}
			}
		};
		Thread thread = new Thread(runnable, "LockThread");
		thread.start();
	}

	private void closeCurrentConnection() {
		try {
			if (this.objectInputStream != null) {
				this.objectInputStream.close();
			}
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			notConnected=true;
		}
	}

	public void connect() {
		while (notConnected){
			try {
				this.socket = new Socket(this.serverAddress, 4446);
				this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
				notConnected=false;
			} catch (UnknownHostException e) {
				notConnected=true;
			} catch (IOException e) {
				notConnected=true;
			}
			
			if (notConnected) {
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
		return this.rows.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		String[] datos = this.rows.get(rowIndex);
		if (datos != null && columnIndex < datos.length) {
			return datos[columnIndex];
		}
		return null;
	}

	@Override
    public String getColumnName(int column) {
    	return this.columnNames[column];
	}

	public void closeConnection() {
		this.stop = true;
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
