package client;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.table.TableModel;

/**
 * This code was generated using CloudGarden's Jigloo SWT/Swing GUI Builder,
 * which is free for non-commercial use. If Jigloo is being used commercially
 * (ie, by a corporation, company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo. Please visit
 * www.cloudgarden.com for details. Use of Jigloo implies acceptance of these
 * licensing terms. ************************************* A COMMERCIAL LICENSE
 * HAS NOT BEEN PURCHASED for this machine, so Jigloo or this code cannot be
 * used legally for any corporate or commercial purpose.
 * *************************************
 */
@SuppressWarnings("serial")
public class ClientFrame extends javax.swing.JFrame {

	private JTabbedPane clientsTabbedPane;

	private JButton removeClientButton;

	private JButton addClientButton;

	private JButton executeAllClientsButton;

	private JPanel jLogPanel;

	private JPanel jBothPanel;

	private JTable logTable;

	private JTable lockTable;

	private JScrollPane jLogScrollPane;

	private JScrollPane jLockScrollPanel;

	private JLabel jLogLabel;

	private JLabel jLocksLabel;

	private JPanel jLockPanel;

	// connection variables
	public Socket clientSocket; // connection to the server

	public ObjectInputStream objectInputStream; // write buffer

	public PrintStream printStream; // read buffer

	public int clientCounter = 0;

	private InetAddress serverAddress;

	/**
	 * Auto-generated main method to display this JFrame
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException, UnknownHostException {
		InetAddress direccionServidor;
		if (args.length > 0 && args[1] != null) {
			direccionServidor = InetAddress.getByName(args[1]);
		} else {
			direccionServidor = InetAddress.getLocalHost();
		}
		ClientFrame inst = new ClientFrame(direccionServidor);
		inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		inst.setVisible(true);
	}

	public ClientFrame(InetAddress serverAddress)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		super();
		this.serverAddress = serverAddress;
		initGUI();
		// only for test purposes

	}

	private void initGUI() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		try {
			this.getContentPane().setLayout(null);
			this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			this.setTitle("Kanon DBMS Client");
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent evt) {
					System.out.println("Closing window...");
					try {
						// sc = null;
						// this.finalize();
						// this.notify();

					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// TODO add your code for this.windowClosed
				}
			});
			{
				clientsTabbedPane = new JTabbedPane();
				this.getContentPane().add(getClientsTabbedPane());
				clientsTabbedPane.setBounds(14, 126, 381, 432);
			}
			{
				addClientButton = new JButton();
				this.getContentPane().add(getAddClientButton());
				addClientButton.setText("Add client");
				addClientButton.setBounds(20, 7, 224, 28);
				addClientButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						addClient();

					}

				});
			}
			{
				removeClientButton = new JButton();
				this.getContentPane().add(getRemoveClientButton());
				removeClientButton.setText("Remove client");
				removeClientButton.setBounds(21, 42, 224, 28);
				removeClientButton.setEnabled(false);
				removeClientButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						if (clientCounter != 0) {
							// TODO add your code for
							// BorrarClienteButton.actionPerformed
							int selectedTabIndex = getClientsTabbedPane()
									.getSelectedIndex();
							Component component = getClientsTabbedPane()
									.getComponentAt(selectedTabIndex);
							((ClientPanel) component).closeConnection();
							getClientsTabbedPane().removeTabAt(selectedTabIndex);

							clientCounter--;

							// disable remove button
							if (clientCounter == 0)
								removeClientButton.setEnabled(false);
						} else {
							// disable remove button
							removeClientButton.setEnabled(false);

						}

					}
				});
			}
			{
				executeAllClientsButton = new JButton();
				this.getContentPane().add(getExecuteAllClientsButton());
				executeAllClientsButton.setText("Execute concurrently");
				executeAllClientsButton.setBounds(21, 77, 224, 28);
				executeAllClientsButton.addActionListener(new ActionListener() {

					final class MyThread extends Thread {
						private int i;

						public MyThread(int i) {
							this.i = i;
						}

						@Override
						public void run() {
							ClientPanel tabCliente = (ClientPanel) (clientsTabbedPane
									.getComponent(this.i));
							tabCliente.sendQueryActionPerformed();
						}
					}

					public void actionPerformed(ActionEvent evt) {
						System.out
								.println("ExecuteAllClientsButton.actionPerformed, event="
										+ evt);

						ejecutarConcurrentemente();
					}

					private void ejecutarConcurrentemente() {
						int cantidadClientes = clientsTabbedPane.getComponentCount();
						Thread[] threads = new Thread[cantidadClientes];
						for (int i = 0; i < cantidadClientes; i++) {
							threads[i] = new MyThread(i);
						}
						for (int i = 0; i < cantidadClientes; i++) {
							threads[i].start();
						}
					}
				});
			}
			{
				jBothPanel = new JPanel();
				BoxLayout jLogLockLayout = new BoxLayout(jBothPanel,
						javax.swing.BoxLayout.Y_AXIS);
				jBothPanel.setLayout(jLogLockLayout);
				getContentPane().add(jBothPanel);
				jBothPanel.setBounds(402, 7, 376, 551);
				jBothPanel.setRequestFocusEnabled(false);
				{
					jLockPanel = new JPanel();
					jBothPanel.add(jLockPanel);
					jLockPanel.setLayout(new BoxLayout(jLockPanel,
							javax.swing.BoxLayout.Y_AXIS));
//					jLockPanel.setPreferredSize(new java.awt.Dimension(406, 230));
					{
						jLocksLabel = new JLabel();
						jLockPanel.add(jLocksLabel);
						jLocksLabel.setHorizontalAlignment(SwingConstants.LEFT);
						jLocksLabel.setText("Locks");
//						jLocksLabel.setPreferredSize(new java.awt.Dimension(416, 14));
					}
					{
						jLockScrollPanel = new JScrollPane();
						jLockPanel.add(jLockScrollPanel);
//						jLockScrollPanel.setPreferredSize(new java.awt.Dimension(367, 203));
						jLockScrollPanel.setOpaque(false);
						{
							TableModel jTable1Model = new LockTableModel(
									this.serverAddress);
							lockTable = new JTable();
							jLockScrollPanel.setViewportView(lockTable);
							lockTable.setOpaque(true);
							lockTable.setModel(jTable1Model);
							
							/* descomentar esto para soporte de tablas lindas en Mac OS 
							lockTable.getTableHeader().setDefaultRenderer(new ITunesHeaderRenderer());
							lockTable.setDefaultRenderer(Object.class, new ITunesCellRenderer());
							lockTable.setIntercellSpacing(new Dimension(1, 1));
							lockTable.setShowHorizontalLines(false);
							lockTable.setShowVerticalLines(true);
							lockTable.setGridColor(Color.lightGray);
							*/
						}
					}
				}
				{
					jLogPanel = new JPanel();
					jBothPanel.add(jLogPanel);
					jLogPanel.setLayout(new BoxLayout(jLogPanel,
							javax.swing.BoxLayout.Y_AXIS));
//					jLogPanel.setPreferredSize(new java.awt.Dimension(406, 315));
					{
						jLogLabel = new JLabel();
						jLogPanel.add(jLogLabel);
						jLogLabel.setHorizontalAlignment(SwingConstants.LEFT);
						jLogLabel.setText("Log");
//						jLogLabel.setBounds(21, 7, 63, 21);
					}
					{
						jLogScrollPane = new JScrollPane();
						jLogPanel.add(jLogScrollPane);
//						jLogScrollPane.setBounds(14, 28, 385, 290);
						{
							TableModel logTableModel = new LogTableModel(
									this.serverAddress);
							logTable = new JTable();
							jLogScrollPane.setViewportView(logTable);
							logTable.setModel(logTableModel);
							
							/* descomentar esto para soporte de tablas lindas en Mac OS 
							logTable.getTableHeader().setDefaultRenderer(new ITunesHeaderRenderer());
							logTable.setDefaultRenderer(Object.class, new ITunesCellRenderer());
							logTable.setIntercellSpacing(new Dimension(1, 1));
							logTable.setShowHorizontalLines(false);
							logTable.setShowVerticalLines(true);
							logTable.setGridColor(Color.lightGray);
							*/
						}
					}
				}
			}
			pack();
			this.setSize(790, 590);
			this.setResizable(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void addClient() {
		// comments by Julian
		// set a new connection and pass it to the panel
		try {
			Socket cliSocket = new Socket(this.serverAddress, 34444);
			try {
				getClientsTabbedPane().addTab("Client " + (clientCounter + 1),
						new ClientPanel(cliSocket));
				// increment client counter
				clientCounter++;
				removeClientButton.setEnabled(true);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();

		}
		;
	}

	public JTabbedPane getClientsTabbedPane() {
		return clientsTabbedPane;
	}

	/**
	 * Auto-generated method for setting the popup menu for a component
	 */
	// private void setComponentPopupMenu(final java.awt.Component parent,
	// final javax.swing.JPopupMenu menu) {
	// parent.addMouseListener(new java.awt.event.MouseAdapter() {
	// public void mousePressed(java.awt.event.MouseEvent e) {
	// if (e.isPopupTrigger())
	// menu.show(parent, e.getX(), e.getY());
	// }
	//
	// public void mouseReleased(java.awt.event.MouseEvent e) {
	// if (e.isPopupTrigger())
	// menu.show(parent, e.getX(), e.getY());
	// }
	// });
	// }
	public JButton getAddClientButton() {
		return addClientButton;
	}

	public JButton getRemoveClientButton() {
		return removeClientButton;
	}

	public JButton getExecuteAllClientsButton() {
		return executeAllClientsButton;
	}

}
