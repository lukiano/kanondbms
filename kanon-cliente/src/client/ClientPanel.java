package client;

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
public class ClientPanel extends javax.swing.JPanel {

	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 */
	private ScEditorKit sc;

	private JButton deleteButton;

	private JScrollPane jQueryEditorScrollPane;

	private JEditorPane queryEditorPane;

	private JButton loadQueryFileButton;

	private JButton sendQueryButton;

	private JLabel jQueryEditorLabel;

	private JLabel jResultLabel;

	private JScrollPane ResultPanel;

	private JButton clearOutputButton;

	private JTextArea outputTextArea;

	// connection variables
	public Socket clientSocket; // para la conexion con el servidor

	public BufferedReader bufferedReader; // read buffer

	public PrintStream printStream; // write buffer

	public boolean isConnected = false;

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

	public ClientPanel(Socket clientSocketFrame) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException, UnknownHostException, IOException {
		super();
		initGUI();

		// at this point a new connection has been established
		clientSocket = clientSocketFrame;
		printStream = new PrintStream(clientSocket.getOutputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(clientSocket
				.getInputStream()));
		// flush the write buffer
		// printStream.flush();

		// bufferedReader = new BufferedReader(new
		// InputStreamReader(cliSocket.getInputStream()));
		this.processResult();

		// this.jTextResult.append(res);
		// this.jTextResult.append("\n");

		enableButtons();

	}

	private void initGUI() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		try {
			this.setPreferredSize(new java.awt.Dimension(481, 432));
			this.setLayout(null);
			this.setOpaque(false);
			{
				jResultLabel = new JLabel();
				this.add(jResultLabel);
				jResultLabel.setText("Result");
				jResultLabel.setBounds(14, 169, 164, 17);
			}
			{
				jQueryEditorLabel = new JLabel();
				this.add(jQueryEditorLabel);
				jQueryEditorLabel.setText("Query editor");
				jQueryEditorLabel.setBounds(11, 16, 127, 19);
			}
			{
				sendQueryButton = new JButton();
				this.add(getEnviarConsultaButton());
				sendQueryButton.setText("Send query");
				sendQueryButton.setBounds(260, 34, 110, 28);
				sendQueryButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {

						Thread thread = new Thread() {

							@Override
							public void run() {
								sendQueryActionPerformed();
							}
						};
						thread.start();
					}
				});
			}
			{
				loadQueryFileButton = new JButton();
				this.add(getLoadQueryFileButton());
				loadQueryFileButton.setText("Load file");
				loadQueryFileButton.setBounds(260, 69, 110, 28);
				loadQueryFileButton.setEnabled(false);
				loadQueryFileButton
						.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								loadFile();
							}
						});
			}
			{
				jQueryEditorScrollPane = new JScrollPane();
				this.add(jQueryEditorScrollPane);
				jQueryEditorScrollPane.setBounds(14, 42, 215, 120);
				jQueryEditorScrollPane.setPreferredSize(new java.awt.Dimension(215,
						120));
				{
					// EditorConsulta = new JTextArea();
					queryEditorPane = new JEditorPane();
					queryEditorPane.setText("");
					jQueryEditorScrollPane.setViewportView(queryEditorPane);
//					queryEditorPane.setBounds(518, 126, 311, 119);
					queryEditorPane.setAutoscrolls(false);
					queryEditorPane.setBorder(BorderFactory
							.createBevelBorder(BevelBorder.LOWERED));
					queryEditorPane.setPreferredSize(new java.awt.Dimension(215,
							120));
					queryEditorPane.setOpaque(false);

					try {
						// create syntax coloring
						sc = new ScEditorKit();
						// load syntax coloring file
						sc.readSyntaxColorDescriptor(System
								.getProperty("user.dir")
								+ File.separator
								+ "lib"
								+ File.separator
								+ "sql.prop");
						// set the kit
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					queryEditorPane.setEditorKit(sc);
				}
			}
			{
				deleteButton = new JButton();
				this.add(deleteButton);
				this.add(getResultPanel());
				this.add(getClearOutputButton());
				deleteButton.setText("Clear query");
				deleteButton.setBounds(260, 104, 110, 28);
				deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						queryEditorPane.setText("");
					}
				});
				deleteButton.setEnabled(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendQueryActionPerformed() {
		// this command sends the query to the server
		try {

			// send the written query to the server...
			printStream.println(queryEditorPane.getText());
			System.out.println(queryEditorPane.getText());

			// flush the write buffer
			printStream.flush();

			sendQueryButton.setEnabled(false);
			// waits for the result
			String result = processResult();
			// set the result message to the output screen
			this.outputTextArea.append(result);

			sendQueryButton.setEnabled(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadFile() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			queryEditorPane.setText(loadFile(file));
		}

	}

	public JButton getEnviarConsultaButton() {
		return sendQueryButton;
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

	public JButton getLoadQueryFileButton() {
		return loadQueryFileButton;
	}

	public String loadFile(File f) {

		FileReader entrada = null;
		StringBuilder str = new StringBuilder();

		try {
			entrada = new FileReader(f.getPath());
			int c;
			while ((c = entrada.read()) != -1) {
				str.append((char) c);
			}
		} catch (IOException ex) {
		}

		return str.toString();
	}

	private void enableButtons() {
		sendQueryButton.setEnabled(true);
		loadQueryFileButton.setEnabled(true);
		deleteButton.setEnabled(true);
		clearOutputButton.setEnabled(true);
	}

	/**
	 * This method process the result returned by the server, be it ok or an error.
	 * 
	 * @author Julian R Berlin
	 * 
	 */
	private String processResult() {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			do {

				try {
					String result = this.bufferedReader.readLine();
					if (result != null) {
						stringBuilder.append(result);
						stringBuilder.append('\n');
					}
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

	public void closeConnection() {
		try {
			this.bufferedReader.close();
			this.printStream.close();
			this.clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JScrollPane getResultPanel() {
		if (ResultPanel == null) {
			ResultPanel = new JScrollPane();
			ResultPanel.setBounds(16, 196, 336, 183);
			ResultPanel.getVerticalScrollBar().setAutoscrolls(true);
			ResultPanel.setViewportView(getJTextResult());
		}
		return ResultPanel;
	}

	public JTextArea getJTextResult() {
		if (outputTextArea == null) {
			outputTextArea = new JTextArea();
			outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
			outputTextArea.setEditable(false);
			outputTextArea.setAutoscrolls(true);
			// jTextResult.setPreferredSize(new java.awt.Dimension(535, 273));
		}
		return outputTextArea;
	}

	private JButton getClearOutputButton() {
		if (clearOutputButton == null) {
			clearOutputButton = new JButton();
			clearOutputButton.setText("Clear output");
			clearOutputButton.setBounds(260, 139, 110, 28);
			clearOutputButton.setEnabled(false);
			clearOutputButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					borrarResultados();
				}

			});

		}

		return clearOutputButton;

	}

	public void borrarResultados() {
		outputTextArea.setText("");

	}
}
