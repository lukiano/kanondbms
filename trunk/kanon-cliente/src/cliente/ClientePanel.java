package cliente;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;

import com.japisoft.sc.ScEditorKit;

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
public class ClientePanel extends javax.swing.JPanel {

	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 */
	private ScEditorKit sc;

	private JButton deleteButton;

	private JScrollPane jScrollPane2;

	private JEditorPane EditorConsulta;

	private JButton CargarArchivoConsultaButton;

	private JButton EnviarConsultaButton;

	private JLabel jLabel3;

	private JLabel jLabel1;

	private JScrollPane ResultPanel;

	private JButton btnBorrarResultados;

	private JTextArea jTextResult;

	// variables para la conexion
	public Socket cliSocket; // para la conexion con el servidor

	public BufferedReader bufferedReader; // buffer de lectura

	public PrintStream printStream; // buffer de escritura

	public boolean HayConexion = false;

//	@SuppressWarnings("deprecation")
//	public static void main(String[] args) throws ClassNotFoundException,
//			InstantiationException, IllegalAccessException,
//			UnsupportedLookAndFeelException {
//		JFrame frame = new JFrame();
//		try {
//			frame.getContentPane().add(new ClientePanel());
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("no se conecto");
//
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		frame.pack();
//		frame.show();
//	}

//	public ClientePanel() throws ClassNotFoundException,
//			InstantiationException, IllegalAccessException,
//			UnsupportedLookAndFeelException, UnknownHostException, IOException {
//
//		super();
//		initGUI();
//		ConectarServidor();
//	}

	public ClientePanel(Socket cliSocketFrame) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException, UnknownHostException, IOException {
		super();
		initGUI();

		// si entra por aca es que efectivamente se ha podido
		// establecer una conexion
		cliSocket = cliSocketFrame;
		printStream = new PrintStream(cliSocket.getOutputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(cliSocket
				.getInputStream()));
		// vacio una ves q envio.
		// printStream.flush();

		// bufferedReader = new BufferedReader(new
		// InputStreamReader(cliSocket.getInputStream()));
		this.ProcesarResultado();

		// this.jTextResult.append(res);
		// this.jTextResult.append("\n");

		habilitarBotones();

	}

	private void initGUI() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		try {
			this.setPreferredSize(new java.awt.Dimension(581, 549));
			this.setLayout(null);
			this.setOpaque(false);
			{
				jLabel1 = new JLabel();
				this.add(jLabel1);
				jLabel1.setText("Resultado");
				jLabel1.setBounds(14, 169, 164, 17);
			}
			{
				jLabel3 = new JLabel();
				this.add(jLabel3);
				jLabel3.setText("Editor de consultas");
				jLabel3.setBounds(11, 16, 127, 19);
			}
			{
				EnviarConsultaButton = new JButton();
				this.add(getEnviarConsultaButton());
				EnviarConsultaButton.setText("Enviar consulta");
				EnviarConsultaButton.setBounds(360, 34, 168, 28);
				EnviarConsultaButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {

						Thread thread = new Thread() {

							@Override
							public void run() {
								enviarConsultaActionPerformed();
							}
						};
						thread.start();
					}
				});
			}
			{
				CargarArchivoConsultaButton = new JButton();
				this.add(getCargarArchivoConsultaButton());
				CargarArchivoConsultaButton.setText("Cargar archivo");
				CargarArchivoConsultaButton.setBounds(360, 69, 168, 28);
				CargarArchivoConsultaButton.setEnabled(false);
				CargarArchivoConsultaButton
						.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								elegir();
							}
						});
			}
			{
				jScrollPane2 = new JScrollPane();
				this.add(jScrollPane2);
				jScrollPane2.setBounds(14, 42, 315, 119);
				{
					// EditorConsulta = new JTextArea();
					EditorConsulta = new JEditorPane();
					EditorConsulta.setText("");
					jScrollPane2.setViewportView(EditorConsulta);
					EditorConsulta.setBounds(518, 126, 511, 119);
					EditorConsulta.setAutoscrolls(false);
					EditorConsulta.setBorder(BorderFactory
							.createBevelBorder(BevelBorder.LOWERED));
					EditorConsulta.setPreferredSize(new java.awt.Dimension(315,
							385));
					EditorConsulta.setOpaque(false);

					try {
						// coloco el syntax coloring
						sc = new ScEditorKit();
						// cargo el archivo
						sc.readSyntaxColorDescriptor(System
								.getProperty("user.dir")
								+ File.separator
								+ "lib"
								+ File.separator
								+ "sql.prop");
						// seteo el kit
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					EditorConsulta.setEditorKit(sc);
				}
			}
			{
				deleteButton = new JButton();
				this.add(deleteButton);
				this.add(getResultPanel());
				this.add(getBtnBorrarResultados());
				deleteButton.setText("Borrar consulta");
				deleteButton.setBounds(360, 104, 168, 28);
				deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						EditorConsulta.setText("");
					}
				});
				deleteButton.setEnabled(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void enviarConsultaActionPerformed() {
		// este boton envia la query al servidor
		try {

			// borrarResultados();
			// envio la consulta escrita al server...
			// XXX: hardcodeo el ";"
			printStream.println(EditorConsulta.getText()/* +" ;" */);

			// para ir viendo q ejecute
			System.out.println(EditorConsulta.getText());

			// vacio una ves q envie el buffer
			printStream.flush();

			EnviarConsultaButton.setEnabled(false);
			// se espera respuesta
			String res = ProcesarResultado();
			// seteo el mensaje que envio el servidor
			this.jTextResult.append(res);

			/*
			 * Runnable runnable = new Runnable() {
			 * 
			 * public void run() { int
			 * maximum=ResultPanel.getVerticalScrollBar().getMaximum();
			 * ResultPanel.getVerticalScrollBar().setAutoscrolls( true);
			 * ResultPanel.getVerticalScrollBar().setValue(maximum);
			 *  }
			 *  }; SwingUtilities.invokeLater(runnable);
			 */

			EnviarConsultaButton.setEnabled(true);

			// ResultPanel.getVerticalScrollBar().updateUI();
			// ResultPanel.updateUI();
			// vacio el editor para poder enviar otra consulta
			// if(res instanceof String ){
			// EditorConsulta.setText((String)res);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void elegir() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			EditorConsulta.setText(LeerArchivo(file).toString());

		} else {
		}

	}

	public JButton getEnviarConsultaButton() {
		return EnviarConsultaButton;
	}

	/**
	 * Auto-generated method for setting the popup menu for a component
	 */
//	private void setComponentPopupMenu(final java.awt.Component parent,
//			final javax.swing.JPopupMenu menu) {
//		parent.addMouseListener(new java.awt.event.MouseAdapter() {
//			public void mousePressed(java.awt.event.MouseEvent e) {
//				if (e.isPopupTrigger())
//					menu.show(parent, e.getX(), e.getY());
//			}
//
//			public void mouseReleased(java.awt.event.MouseEvent e) {
//				if (e.isPopupTrigger())
//					menu.show(parent, e.getX(), e.getY());
//			}
//		});
//	}

	public JButton getCargarArchivoConsultaButton() {
		return CargarArchivoConsultaButton;
	}

	public StringBuffer LeerArchivo(File f) {

		FileReader entrada = null;
		StringBuffer str = new StringBuffer();

		try {
			entrada = new FileReader(f.getPath());
			int c;
			while ((c = entrada.read()) != -1) {
				str.append((char) c);
			}
		} catch (IOException ex) {
		}

		return str;
	}

//	private void ConectarServidor() throws UnknownHostException, IOException {
//
//		boolean seconecto = false;
//		// se abre la conexion..me fijo si ya fue abierta
//		while (!seconecto) {
//			if (cliSocket == null) {
//				try {
//					String res = conectar();
//
//					// String res = bufferedReader.readLine();
//					// ObjectInputStream objectInputStream = new
//					// ObjectInputStream( cliSocket.getInputStream() );
//					// System.out.println(res);
//					// se espera respuesta
//
//					if (res.toUpperCase().equals("OK")) {
//						// PanelConsola.setText("Sesion iniciada con exito");
//						seconecto = true;
//						habilitarBotones();
//						// habilito el boton para enviar consultas al server...
//						// EnviarConsultaButton.setEnabled(false);
//
//					} else {
//						// PanelConsola.setText("No se ha podido iniciar
//						// sesion");
//						cliSocket.close();
//						cliSocket = null;
//					}
//				} catch (UnknownHostException e) {
//					// PanelConsola.setText("No se ha podido conectar con el
//					// servidor: " + e.getMessage());
//					cliSocket = null;
//
//				} catch (IOException e) {
//					// PanelConsola.setText("No se ha podido conectar con el
//					// servidor: " + e.getMessage());
//					cliSocket = null;
//				}
//			} else {
//				// PanelConsola.setText("Desconectando Servidor");
//
//				// si ya estaba abierta ..lo cierro ya que ya habia una conexion
//				// y se pidio desconectar del servidor
//				cliSocket.close();
//				printStream.close();
//
//				EnviarConsultaButton.setEnabled(false);
//				cliSocket = null;
//
//				// getConectarServidor().setText("Conectar");
//			}
//		}
//	}

//	private String conectar() throws UnknownHostException, IOException {
//		// PanelConsola.setText("Conectando Servidor");
//		cliSocket = new Socket("localhost", 4444);
//		printStream = new PrintStream(cliSocket.getOutputStream());
//
//		// si no hubo problemas al instancias el socket la conexion se realizo
//		// ocn exito
//		// PanelConsola.setText("Conexion exitosa");
//
//		// vacio una ves q envio.
//		printStream.flush();
//
//		bufferedReader = new BufferedReader(new InputStreamReader(cliSocket
//				.getInputStream()));
//		String res = this.ProcesarResultado();
//
//		return res;
//	}

	private void habilitarBotones() {
		EnviarConsultaButton.setEnabled(true);
		CargarArchivoConsultaButton.setEnabled(true);
		deleteButton.setEnabled(true);
		btnBorrarResultados.setEnabled(true);
	}

	/**
	 * Esta funcion procesa el resultado devuelto por el server ya sea de error
	 * o resultado
	 * 
	 * @author Julian R Berlin
	 * 
	 */
	private String ProcesarResultado() {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			do {

				try {
					stringBuilder.append(this.bufferedReader.readLine());
					stringBuilder.append('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (bufferedReader.ready());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}

	public void cerrarConexion() {
		try {
			this.bufferedReader.close();
			this.printStream.close();
			this.cliSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JScrollPane getResultPanel() {
		if (ResultPanel == null) {
			ResultPanel = new JScrollPane();
			ResultPanel.setBounds(16, 196, 536, 283);
			ResultPanel.getVerticalScrollBar().setAutoscrolls(true);
			ResultPanel.setViewportView(getJTextResult());
		}
		return ResultPanel;
	}

	public JTextArea getJTextResult() {
		if (jTextResult == null) {
			jTextResult = new JTextArea();
			jTextResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
			jTextResult.setEditable(false);
			jTextResult.setAutoscrolls(true);
			// jTextResult.setPreferredSize(new java.awt.Dimension(535, 273));
		}
		return jTextResult;
	}

	private JButton getBtnBorrarResultados() {
		if (btnBorrarResultados == null) {
			btnBorrarResultados = new JButton();
			btnBorrarResultados.setText("Borrar Resultados");
			btnBorrarResultados.setBounds(361, 139, 166, 30);
			btnBorrarResultados.setEnabled(false);
			btnBorrarResultados.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					borrarResultados();
				}

			});

		}

		return btnBorrarResultados;

	}

	public void borrarResultados() {
		jTextResult.setText("");

	}
}
