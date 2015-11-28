package it.unige.dibris.moodtc;

import it.unige.dibris.moodtc.utils.JenaUtils;
import it.unige.dibris.moodtc.utils.LanguageDetection;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.jlt.util.Pair;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import adm.ClassifierObject;
import adm.ModuleOutput;
import adm.TCModule;
import adm.TCOutput;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
//import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class GUI extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6770820958929905210L;
	private boolean ontologyIsPresent, textIsPresent;
	private TCModule module = null;
	private TCModule provisory = null;
	private String ontologyFile = null;
	private String textFile = null;
	private TreePath pathToCollapse = null;
	private List<String> wordsToUnhighlight = null;

	/**
	 * Creates new form MainInterface
	 */
	public GUI() {
		getContentPane().setPreferredSize(new Dimension(763, 550));
		setLocation(new Point(200, 50));
		getContentPane().setForeground(new Color(51, 153, 255));
		getContentPane().setBackground(new Color(255, 255, 255));
		initComponents();
		getContentPane().setLayout(null);
		ClassModule.addItem(new TCModuleNode(module));
		getContentPane().add(panel);

		JLabel lblNewLabel = new JLabel("ONTOLOGY");
		lblNewLabel.setForeground(new Color(30, 144, 255));
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 28));
		lblNewLabel.setBounds(12, 24, 193, 36);
		panel.add(lblNewLabel);

		panel_2 = new JPanel();
		panel_2.setLocation(new Point(10, 500));
		panel_2.setBounds(16, 321, 185, 3);
		panel.add(panel_2);
		panel_2.setPreferredSize(new Dimension(1000, 10));
		panel_2.setMinimumSize(new Dimension(0, 0));
		panel_2.setBackground(Color.WHITE);
		panel_2.setLayout(null);
		getContentPane().add(jScrollPane2);
		getContentPane().add(textLabel);
		getContentPane().add(jScrollPane1);
		getContentPane().add(outputLabel);
		getContentPane().add(progressBar);
		getContentPane().add(beginButton);

		JLabel lblText = new JLabel("TEXT");
		lblText.setForeground(new Color(30, 144, 255));
		lblText.setFont(new Font("Dialog", Font.BOLD, 28));
		lblText.setBounds(228, 9, 191, 31);
		getContentPane().add(lblText);

		JPanel panel_1 = new JPanel();
		panel_1.setMinimumSize(new Dimension(0, 0));
		panel_1.setSize(new Dimension(100, 10));
		panel_1.setPreferredSize(new Dimension(1000, 10));
		panel_1.setBackground(Color.DARK_GRAY);
		panel_1.setBounds(230, 302, 515, 3);
		getContentPane().add(panel_1);

		outlabel = new JLabel("");
		outlabel.setFont(new Font("Dialog", Font.BOLD, 16));
		outlabel.setBounds(367, 510, 251, 20);
		getContentPane().add(outlabel);
	}

	public static String readFile(String filename) {
		String text = "";
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String entity;
			while ((entity = br.readLine()) != null) {
				text += entity + "\n";
			}
		} catch (IOException e) {
			System.err.println("\"" + filename + "\" not found.");
		}
		return text;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		textLabel = new javax.swing.JLabel();
		textLabel.setBounds(230, 40, 190, 20);
		textLabel.setForeground(Color.BLACK);
		jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1.setBounds(226, 61, 523, 229);
		txtpnTextToClassify = new javax.swing.JTextPane();
		txtpnTextToClassify.setEditable(false);
		jScrollPane2 = new javax.swing.JScrollPane();
		jScrollPane2.setBackground(Color.WHITE);
		jScrollPane2.setBounds(226, 318, 523, 170);
		final ImageIcon start = new ImageIcon(getClass().getResource(
				"/button.png"));
		final ImageIcon hover = new ImageIcon(getClass().getResource(
				"/buttonHover.png"));
		beginButton = new JButton(start);
		beginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				beginButton.setIcon(hover);
				beginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				beginButton.setIcon(start);
			}
		});
		// beginButton.setIcon(new
		// ImageIcon(GUI.class.getResource("button.png")));
		beginButton.setBounds(611, 495, 150, 35);
		beginButton.setBorder(BorderFactory.createEmptyBorder());
		beginButton.setContentAreaFilled(false);
		outputLabel = new javax.swing.JLabel();
		outputLabel.setFont(new Font("Dialog", Font.BOLD, 28));
		outputLabel.setBounds(225, 500, 130, 30);
		outputLabel.setForeground(new Color(30, 144, 255));
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenuBar1.setBackground(Color.WHITE);
		jMenu1 = new javax.swing.JMenu();
		jMenu1.setBackground(Color.WHITE);
		jMenu1.setForeground(new Color(51, 153, 255));
		jMenuItem1 = new javax.swing.JMenuItem();
		jMenuItem1.setForeground(new Color(51, 153, 255));
		jMenuItem2 = new javax.swing.JMenuItem();
		jMenuItem2.setForeground(new Color(51, 153, 255));
		jSeparator1 = new javax.swing.JPopupMenu.Separator();
		HelpWindow = new javax.swing.JMenuItem();
		HelpWindow.setForeground(new Color(51, 153, 255));
		classifier = new TC();
		ontModel = JenaUtils.ONTMODEL;
		tableModel = null;

		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.blue);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("MOod-TC");
		setBackground(new java.awt.Color(255, 255, 255));

		jScrollPane1.setViewportView(txtpnTextToClassify);
		Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 3);
		Border border1 = BorderFactory.createLineBorder(Color.WHITE, 3);

		beginButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				beginButtonActionPerformed(evt);
			}
		});

		outputLabel.setText("RESULT");

		jMenu1.setText("File selection");

		jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_O,
				java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem1.setText("Choose ontology ...");
		jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem1ActionPerformed(evt);
			}
		});
		jMenu1.add(jMenuItem1);

		jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_T,
				java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem2.setText("Choose text ...");
		jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem2ActionPerformed(evt);
			}
		});
		jMenu1.add(jMenuItem2);

		jMenuItem3 = new JMenuItem();
		jMenuItem3.setForeground(new Color(51, 153, 255));
		jMenuItem3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				InputEvent.CTRL_MASK));
		jMenuItem3.setText("Choose module ...");
		jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem3ActionPerformed(evt);
			}
		});
		jMenu1.add(jMenuItem3);
		jMenu1.add(jSeparator1);

		HelpWindow.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_H,
				java.awt.event.InputEvent.CTRL_MASK));
		HelpWindow.setText("Help");
		HelpWindow.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentHidden(java.awt.event.ComponentEvent evt) {
				HelpWindowComponentHidden(evt);
			}
		});
		HelpWindow.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				HelpWindowActionPerformed(evt);
			}
		});
		jMenu1.add(HelpWindow);

		jMenuBar1.add(jMenu1);

		setJMenuBar(jMenuBar1);

		root = new DefaultMutableTreeNode("Roots");

		/*
		 * class MyTreeSelectionListener implements TreeSelectionListener {
		 * 
		 * public void valueChanged(TreeSelectionEvent se) { JTree tree =
		 * (JTree) se.getSource(); DefaultMutableTreeNode selectedNode =
		 * (DefaultMutableTreeNode) tree .getLastSelectedPathComponent();
		 * addOntologyNode(selectedNode); } }
		 */

		progressBar = new JProgressBar();
		progressBar.setBounds(623, 533, 127, 13);
		progressBar.setVisible(false);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(2000, 2000));
		panel.setSize(new Dimension(2000, 2000));
		panel.setBackground(Color.DARK_GRAY);
		panel.setBounds(0, -19, 217, 593);
		panel.setLayout(null);

		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(12, 80, 193, 230);
		scrollPane.setMinimumSize(new Dimension(100, 200));
		panel.add(scrollPane);
		scrollPane.setBorder(border1);
		tree = new JTree(root);
		scrollPane.setViewportView(tree);

		// tree.addTreeSelectionListener(new MyTreeSelectionListener());
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree
				.getCellRenderer();

		lblModule = new JLabel("MODULE");
		lblModule.setFont(new Font("Dialog", Font.BOLD, 28));
		lblModule.setBounds(12, 346, 205, 29);
		panel.add(lblModule);
		lblModule.setForeground(new Color(30, 144, 255));
		ClassModule = new JComboBox<TCModuleNode>();
		ClassModule.setBackground(Color.WHITE);
		ClassModule.setBounds(12, 387, 193, 24);
		panel.add(ClassModule);

		ClassModule.setModel(new javax.swing.DefaultComboBoxModel<TCModuleNode>());
		ClassModule.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ClassModuleActionPerformed(evt);
			}
		});
		ontologyLabel = new javax.swing.JLabel();
		ontologyLabel.setBounds(14, 60, 190, 20);
		panel.add(ontologyLabel);
		ontologyLabel.setForeground(Color.WHITE);
		jScrollPane2.setBorder(border);

		outputTable = new JTable();
		outputTable.setFillsViewportHeight(true);
		jScrollPane2.setViewportView(outputTable);
		jScrollPane1.setBorder(border);
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		ClassModule.setBorder(border1);

		pack();
	}// </editor-fold>

	// Add node of ontology dynamically on click
	/*
	 * private void addOntologyNode(DefaultMutableTreeNode selectedNode) { if
	 * (ontologyFile != null) { InputStream in =
	 * FileManager.get().open(ontologyFile); if (in == null) throw new
	 * IllegalArgumentException( "File: " + ontologyFile + " not found"); //
	 * read the ontology file ontModel.read( in, "" ); if (selectedNode != null)
	 * { ExtendedIterator it =
	 * ontModel.getOntClass(((MyTreeNode)selectedNode.getUserObject
	 * ()).getC().getURI()).listSubClasses(); while (it.hasNext()) {
	 * DefaultMutableTreeNode a = new DefaultMutableTreeNode(new
	 * MyTreeNode(((OntClass)it.next()))); selectedNode.add(a); } } } }
	 */

	// Class Module Language selection
	private void ClassModuleActionPerformed(java.awt.event.ActionEvent evt) {
		TCModule m = ((TCModuleNode) ClassModule.getSelectedItem()).getModule();
		if (m != null) {
			provisory = m;
			System.err.println("Selezionato modulo: " + m.getClass().getName());
		} else
			provisory = null;
	}

	private void HelpWindowActionPerformed(java.awt.event.ActionEvent evt) {
		HelpWindowComponentHidden(null);
	}

	// help menu
	private void HelpWindowComponentHidden(java.awt.event.ComponentEvent evt) {
		JFrame help = new JFrame("Help");
		help.setSize(250, 350);
		help.setBackground(Color.GRAY);
		help.setVisible(true);
		help.setLocation(450, 150);
		JTextPane j = new JTextPane();
		j.setEditable(false);
		StyledDocument doc = j.getStyledDocument();
		try {
			SimpleAttributeSet title = new SimpleAttributeSet();
			SimpleAttributeSet generic = new SimpleAttributeSet();
			StyleConstants.setFontFamily(title, "Monospace");
			StyleConstants.setFontSize(title, 22);
			StyleConstants.setBold(title, true);
			StyleConstants.setItalic(title, true);
			StyleConstants.setFontFamily(generic, "Monospace");
			StyleConstants.setFontSize(generic, 12);
			doc.insertString(0, "Help window\n\n", title);
			doc.insertString(
					doc.getLength(),
					"How to use the program:\n\n"
							+ "1. Load the ontology file and the text file, on which you want to do the classification;\n"
							+ "2. Load a module to perform specific classification (facultative);\n"
							+ "3. Press the 'CLASSIFY' button to start the classification;\n"
							+ "4. See the results on the output panel.", null);
		} catch (BadLocationException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		}
		help.getContentPane().add(j);
	}

	// Text loader
	private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"TEXT FILES", "txt", "text", "pdf");
		fileChooser.setFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			this.textFile = selectedFile.getPath();
			try {
				String text = readFile(textFile);
				txtpnTextToClassify.setText(text);
				txtpnTextToClassify.setCaretPosition(0);
				classifier.setTextFileName(textFile);
				Language textLang = new LanguageDetection().detection(text);
				classifier.setTextLang(textLang);
				textLabel.setText("Language: " + textLang.getName());
				System.out.println(textFile);
				textIsPresent = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Ontology loader
	private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
		JFileChooser fileChooser = new JFileChooser();
		// fileChooser.set
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"OWL FILES", "owl");
		fileChooser.setFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String ont = "";
			File selectedFile = fileChooser.getSelectedFile();
			this.ontologyFile = selectedFile.getPath();
			System.out.println(ontologyFile);
			classifier.setOntFileName(ontologyFile);
			InputStream in = FileManager.get().open(ontologyFile);
			if (in == null)
				throw new IllegalArgumentException("File: " + ontologyFile
						+ " not found");
			// read the ontology file
			ontModel.read(in, "");
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
					.getRoot();
			if (ontologyIsPresent) {
				root.removeAllChildren();
				model.reload();
			}
			ExtendedIterator<?> it = ontModel.listHierarchyRootClasses();
			while (it.hasNext()) {
				OntClass c = (OntClass) it.next();
				new DefaultMutableTreeNode(
						new TCOntologyTreeNode(c));
				DefaultMutableTreeNode actualFather = root;
				LinkedList<Pair<OntClass, DefaultMutableTreeNode>> fifo = new LinkedList<Pair<OntClass, DefaultMutableTreeNode>>();
				fifo.add(new Pair<OntClass, DefaultMutableTreeNode>(c, null));
				while (!fifo.isEmpty()) {
					Pair<OntClass, DefaultMutableTreeNode> pair = fifo.remove();
					OntClass cl = pair.getFirst();
					ont += (cl.getLocalName() + " ");
					DefaultMutableTreeNode father = pair.getSecond();
					DefaultMutableTreeNode actualNode = new DefaultMutableTreeNode(
							new TCOntologyTreeNode(cl));
					ExtendedIterator<?> it1 = cl.listSubClasses();
					while (it1.hasNext())
						fifo.add(new Pair<OntClass, DefaultMutableTreeNode>((OntClass) it1.next(), actualNode));
					if (father == null) {
						actualFather.add(actualNode);
						actualFather = actualNode;
					} else if (father.equals(actualFather))
						actualFather.add(actualNode);
					else {
						actualFather = father;
						actualFather.add(actualNode);
					}
				}
			}
			model.reload(root);
			ontologyIsPresent = true;
			Language ontLang = new LanguageDetection().detection(ont);
			classifier.setOntLang(ontLang);
			ontologyLabel.setText("Language: " + ontLang.getName());
		}
	}

	// Module loader
	private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"JAR FILES", "jar");
		fileChooser.setFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			// String textFile = selectedFile.getPath();
			URL downloadURL;
			try {
				downloadURL = file.toURI().toURL();
				URL[] downloadURLs = new URL[] { downloadURL };
				URLClassLoader loader = URLClassLoader.newInstance(
						downloadURLs, getClass().getClassLoader());
				try {
					List<Class<?>> implementingClasses = ModuleLoad
							.findImplementingClassesInJarFile(file,
									TCModule.class, loader);
					boolean first = true;
					for (Class<?> clazz : implementingClasses) {
						// System.out.println(clazz.getName());
						// assume there is a public default constructor
						// available
						TCModule instance = (TCModule) clazz.newInstance();
						TCModuleNode node = new TCModuleNode(instance);
						ClassModule.addItem(node);
						// instance.postProcessing(out);
						if (first) {
							ClassModule.setSelectedItem(node);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	// search a specific node by name into the tree
	public DefaultMutableTreeNode searchNode(String nodeStr) {
		DefaultMutableTreeNode node = null;
		Enumeration<?> e = root.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			node = (DefaultMutableTreeNode) e.nextElement();
			if (nodeStr.equals(node.getUserObject().toString())) {
				return node;
			}
		}
		return null;
	}

	// Collapse sequence of nodes in the tree
	public void collapseAllPath(TreePath tp) {
		while (tp.getParentPath() != null) {
			tree.collapsePath(tp);
			tp = tp.getParentPath();
		}
	}

	// highlights original text words on output click
	public void highlightWords(List<String> words) {
		wordsToUnhighlight = words;
		StyledDocument sdoc = txtpnTextToClassify.getStyledDocument();
		StyleConstants.setForeground(style, Color.blue);
		for (String word : words) {
			String low = txtpnTextToClassify.getText().toLowerCase();
			int position = low.indexOf(word);
			while (position >= 0) {
				sdoc.setCharacterAttributes(position, word.length(), style,
						false);
				txtpnTextToClassify.setCaretPosition(position);
				position = low.indexOf(word, position + 1);
			}
		}
	}

	// unhighlight previous text words to a new result
	public void unhighlightWords() {
		StyledDocument sdoc = txtpnTextToClassify.getStyledDocument();
		StyleConstants.setForeground(style, Color.black);
		for (String word : wordsToUnhighlight) {
			sdoc.setCharacterAttributes(
					txtpnTextToClassify.getText().indexOf(word), word.length(),
					style, false);
		}
	}

	// Main execution
	private void beginButtonActionPerformed(java.awt.event.ActionEvent evt) {
		module = provisory;
		outputTable.clearSelection();
		txtpnTextToClassify.setCaretPosition(0);
		// actions to do pressing an output result
		class MyListSelectionListener implements ListSelectionListener {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (module == null && outputTable.getSelectedRow() >= 0) {
					if (pathToCollapse != null)
						collapseAllPath(pathToCollapse);
					if (wordsToUnhighlight != null) {
						unhighlightWords();
					}
					List<String> path = (List<String>) outputTable.getModel()
							.getValueAt(outputTable.getSelectedRow(), 4);
					// DefaultMutableTreeNode last = null;
					// for (String n : path){
					// last = searchNode(n);
					// addOntologyNode(last);
					// }
					TreePath tp = new TreePath(searchNode(
							path.get(path.size() - 2)).getPath());
					pathToCollapse = tp;
					tree.expandPath(tp);
					highlightWords((List<String>) (outputTable.getModel()
							.getValueAt(outputTable.getSelectedRow(), 0)));
				}
			}
		}

		if (wordsToUnhighlight != null) {
			unhighlightWords();
		}
		if (ontologyIsPresent
				&& (textIsPresent || !"".equals(txtpnTextToClassify.getText()))) {
			progressBar.setVisible(true);
			progressBar.setIndeterminate(true);
			ClassModule.setEnabled(false);
			new Thread(new Runnable() {
				@Override
				public void run() {
					// outputList = null;
					beginButton.setEnabled(false);
					outlabel.setText("");
					if (tableModel != null) {
						tableModel.setRowCount(0);
						// tableModel.setDataVector(null, null);
						tableModel.setColumnCount(0);
						outputTable.setModel(tableModel);
					}
					// outputText.setText("");
					System.out.println(pathToCollapse);
					if (pathToCollapse != null)
						collapseAllPath(pathToCollapse);
					if (module != null)
						classifier.setTextFileName(module
								.preProcessing(textFile));
					TCOutput out = classifier.classification();
					if (module != null) {
						ModuleOutput m = module.postProcessing(out);
						tableModel = new DefaultTableModel() {
							/**
							 * 
							 */
							private static final long serialVersionUID = 4760470044752407874L;

							@Override
							public boolean isCellEditable(int row, int column) {
								// all cells false
								return false;
							}
						};
						tableModel.setDataVector(m.getData(),
								m.getColumnNames());
						outputTable.setModel(tableModel);
						outputTable.setSelectionMode(0);
						outlabel.setText(m.getResult());
					} else {
						// listModel = new
						// MyOutputModel<ClassifierObject>(out.getInfo());
						if (out == null)
							outlabel.setText("NO MATCHES FOUND!");
						else {
							List<ClassifierObject> output = out.getInfo();
							Object columnNames[] = new Object[] { "Text words",
									"Lemma word", "Ontology word",
									"Occurences", "Ontology tree" };
							Object rowData[][] = new Object[output.size()][5];
							for (int i = 0; i < output.size(); i++) {
								ClassifierObject o = output.get(i);
								rowData[i] = new Object[] { o.getTextWords(),
										o.getLemmaWord(), o.getOntologyWord(),
										o.getNumberOfOcc(), o.getOntologyTree() };
							}
							tableModel = new DefaultTableModel() {
								/**
								 * 
								 */
								private static final long serialVersionUID = -9165937556303532220L;

								@Override
								public boolean isCellEditable(int row,
										int column) {
									// all cells false
									return false;
								}
							};
							tableModel.setDataVector(rowData, columnNames);
							outputTable.setModel(tableModel);
							outputTable.removeColumn(outputTable
									.getColumnModel().getColumn(4));
							outputTable.removeColumn(outputTable
									.getColumnModel().getColumn(0));
							outputTable.getSelectionModel()
									.addListSelectionListener(
											new MyListSelectionListener());
							// outputText.setText("PERCENTAGE OF BELONGING HERE");
						}
						beginButton.setEnabled(true);
						progressBar.setIndeterminate(false);
						progressBar.setVisible(false);
						ClassModule.setEnabled(true);
					}
				}
			}).start();
		} else {
			outlabel.setText("You have to load ontology and text first!");
		}
	}// GEN-LAST:event_beginButtonActionPerformed

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager
							.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new GUI().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify
	private javax.swing.JMenuItem HelpWindow;
	private javax.swing.JButton beginButton;
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenuItem jMenuItem1;
	private javax.swing.JMenuItem jMenuItem2;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JPopupMenu.Separator jSeparator1;
	private javax.swing.JLabel ontologyLabel;
	private javax.swing.JLabel outputLabel;
	private javax.swing.JLabel textLabel;
	private javax.swing.JTextPane txtpnTextToClassify;
	private JMenuItem jMenuItem3;
	private JLabel lblModule;
	private TC classifier;
	private OntModel ontModel;
	private JScrollPane scrollPane;
	private JTree tree;
	private JComboBox<TCModuleNode> ClassModule;
	private JTable outputTable;
	private DefaultTableModel tableModel;
	private DefaultMutableTreeNode root;
	private JProgressBar progressBar;
	private SimpleAttributeSet style;
	private JPanel panel;
	private JPanel panel_2;
	private JLabel outlabel;
}