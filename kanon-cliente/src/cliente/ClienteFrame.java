package cliente;

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
public class ClienteFrame extends javax.swing.JFrame {

	private JTabbedPane TabClientes;

	private JButton BorrarClienteButton;

	private JButton AgregarClienteButton;

	private JButton ExecuteAllClientsButton;

	private JPanel jPanel1;

	private JPanel jLogLock;

	private JTable tablaLog;

	private JTable tablaLocks;

	private JScrollPane jLogScrollPane;

	private JScrollPane jLockScrollPanel;

	private JLabel jLabel2;

	private JLabel jLabel1;

	private JPanel jPanel2;

	// variables para la conexion
	public Socket cliSocket; // para la conexion con el servidor

	public ObjectInputStream objectInputStream; // buffer de escritura

	public PrintStream printStream; // buffer de lectura

	public int clientCounter = 0;

	private InetAddress direccionServidor;

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
		ClienteFrame inst = new ClienteFrame(direccionServidor);
		inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		inst.setVisible(true);
	}

	public ClienteFrame(InetAddress direccionServidor)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		super();
		this.direccionServidor = direccionServidor;
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
					System.out.println("cerrando ventana ");
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
				TabClientes = new JTabbedPane();
				this.getContentPane().add(getTabClientes());
				TabClientes.setBounds(14, 126, 581, 532);
			}
			{
				AgregarClienteButton = new JButton();
				this.getContentPane().add(getAgregarClienteButton());
				AgregarClienteButton.setText("Agregar cliente");
				AgregarClienteButton.setBounds(20, 7, 224, 28);
				AgregarClienteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						agregarCliente();

					}

				});
			}
			{
				BorrarClienteButton = new JButton();
				this.getContentPane().add(getBorrarClienteButton());
				BorrarClienteButton.setText("Borrar cliente");
				BorrarClienteButton.setBounds(21, 42, 224, 28);
				BorrarClienteButton.setEnabled(false);
				BorrarClienteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						if (clientCounter != 0) {
							// TODO add your code for
							// BorrarClienteButton.actionPerformed
							int selectedTabIndex = getTabClientes()
									.getSelectedIndex();
							Component component = getTabClientes()
									.getComponentAt(selectedTabIndex);
							((ClientePanel) component).cerrarConexion();
							getTabClientes().removeTabAt(selectedTabIndex);

							clientCounter--;

							// deshabilito el boton borrar
							if (clientCounter == 0)
								BorrarClienteButton.setEnabled(false);
						} else {
							// deshabilito el boton borrar
							BorrarClienteButton.setEnabled(false);

						}

					}
				});
			}
			{
				ExecuteAllClientsButton = new JButton();
				this.getContentPane().add(getExecuteAllClientsButton());
				ExecuteAllClientsButton.setText("Ejecutar concurrentemente");
				ExecuteAllClientsButton.setBounds(21, 77, 224, 28);
				ExecuteAllClientsButton.addActionListener(new ActionListener() {

					final class MyThread extends Thread {
						private int i;

						public MyThread(int i) {
							this.i = i;
						}

						@Override
						public void run() {
							ClientePanel tabCliente = (ClientePanel) (TabClientes
									.getComponent(this.i));
							tabCliente.enviarConsultaActionPerformed();
						}
					}

					public void actionPerformed(ActionEvent evt) {
						System.out
								.println("ExecuteAllClientsButton.actionPerformed, event="
										+ evt);

						ejecutarConcurrentemente();
					}

					private void ejecutarConcurrentemente() {
						int cantidadClientes = TabClientes.getComponentCount();
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
				jLogLock = new JPanel();
				BoxLayout jLogLockLayout = new BoxLayout(jLogLock,
						javax.swing.BoxLayout.Y_AXIS);
				jLogLock.setLayout(jLogLockLayout);
				getContentPane().add(jLogLock);
				jLogLock.setBounds(602, 7, 406, 651);
				jLogLock.setRequestFocusEnabled(false);
				{
					jPanel2 = new JPanel();
					FlowLayout jPanel2Layout = new FlowLayout();
					jLogLock.add(jPanel2);
					jPanel2.setLayout(jPanel2Layout);
					jPanel2.setPreferredSize(new java.awt.Dimension(406, 330));
					{
						jLabel1 = new JLabel();
						jPanel2.add(jLabel1);
						jLabel1.setText("   Locks");
						jLabel1
								.setPreferredSize(new java.awt.Dimension(416,
										14));
					}
					{
						jLockScrollPanel = new JScrollPane();
						jPanel2.add(jLockScrollPanel);
						jLockScrollPanel
								.setPreferredSize(new java.awt.Dimension(367,
										303));
						jLockScrollPanel.setOpaque(false);
						{
							TableModel jTable1Model = new LockTableModel(
									this.direccionServidor);
							tablaLocks = new JTable();
							jLockScrollPanel.setViewportView(tablaLocks);
							tablaLocks.setOpaque(true);
							tablaLocks.setModel(jTable1Model);
						}
					}
				}
				{
					jPanel1 = new JPanel();
					jLogLock.add(jPanel1);
					jPanel1.setLayout(null);
					jPanel1.setPreferredSize(new java.awt.Dimension(406, 315));
					{
						jLabel2 = new JLabel();
						jPanel1.add(jLabel2);
						jLabel2.setText("Log");
						jLabel2.setBounds(21, 7, 63, 21);
					}
					{
						jLogScrollPane = new JScrollPane();
						jPanel1.add(jLogScrollPane);
						jLogScrollPane.setBounds(14, 28, 385, 280);
						{
							TableModel logTableModel = new LogTableModel(
									this.direccionServidor);
							tablaLog = new JTable();
							jLogScrollPane.setViewportView(tablaLog);
							tablaLog.setModel(logTableModel);

						}
					}
				}
			}
			pack();
			this.setSize(1024, 700);
			this.setResizable(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void agregarCliente() {
		// comments by Julian
		// creamos una nueva conexion para pasarle al panel
		try {
			Socket cliSocket = new Socket(this.direccionServidor, 4444);
			// le paso la conexion al panel correspondiente al nuevo cliente
			try {
				getTabClientes().addTab("Cliente " + (clientCounter + 1),
						new ClientePanel(cliSocket));
				clientCounter++;
				BorrarClienteButton.setEnabled(true);
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
			// incremento el contador de clientes

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();

		}
		;
	}

	public JTabbedPane getTabClientes() {
		return TabClientes;
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
	public JButton getAgregarClienteButton() {
		return AgregarClienteButton;
	}

	public JButton getBorrarClienteButton() {
		return BorrarClienteButton;
	}

	public JButton getExecuteAllClientsButton() {
		return ExecuteAllClientsButton;
	}

}
