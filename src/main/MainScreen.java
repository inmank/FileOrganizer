package main;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;

public class MainScreen {
	public static final String STATUS_INITIAL 	= "No Change";
	public static final String STATUS_SUCESS 	= "Sucess";
	public static final String STATUS_FAIL 		= "Failed";

	String[] column_names = {"File Name", "Extension", "Status"};

	JTextField path 		= new JTextField(30);
	JButton	pathButton 		= new JButton("Browse");
	JButton	renameButton 	= new JButton("Rename");
	JTable 	fileTable 		= new JTable();

	@SuppressWarnings("serial")
	DefaultTableModel model = new DefaultTableModel(0, column_names.length) {
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex != 2;
		}
	};

	JFrame frame;
	ArrayList<String> oldList = new ArrayList<String>();

	public MainScreen() {

		model.setColumnIdentifiers(column_names);
		fileTable.setModel(model);
		fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		path.setEnabled(false);

		pathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int out = chooser.showOpenDialog(null);

				if (out == JFileChooser.APPROVE_OPTION) {
					File selFile = chooser.getSelectedFile();
					path.setText(selFile.getAbsolutePath());
					loadTable(selFile);
				}
			}
		});

		renameButton.addActionListener(new RenameAction());

		frame = new JFrame("File Name Changer");

		JPanel inPanel = new JPanel();
		inPanel.add(path);
		inPanel.add(pathButton);

		JPanel outPanel = new JPanel();
		JScrollPane tablePane = new JScrollPane(fileTable);
		outPanel.add(tablePane);
		outPanel.add(renameButton);

		JPanel workingPanel = new JPanel();
		workingPanel.setLayout(new BoxLayout(workingPanel, BoxLayout.PAGE_AXIS));

		workingPanel.add(inPanel);
		workingPanel.add(outPanel);

		frame.add(workingPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	protected void loadTable(String fileName) {
		loadTable(new File(fileName));
	}

	protected void loadTable(File selDirectory) {
		model.getDataVector().removeAllElements();
		oldList.clear();
		model.fireTableDataChanged();
		for (File file : selDirectory.listFiles()) {
			Vector<String> data = new Vector<String>();
			if (!file.isDirectory()) {
				oldList.add(file.getName());
				data.add(FilenameUtils.getBaseName(file.getName()));
				data.add(FilenameUtils.getExtension(file.getName()));

			} else {
				oldList.add(file.getAbsolutePath());
				data.add(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator)+1));
				data.add("");
			}

			data.add(STATUS_INITIAL);
			model.addRow(data);
		}
	}

	public static void main(String[] args) {
		new MainScreen();
	}

	private class RenameAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Vector<Object> data = model.getDataVector();
			for (int i = 0; i < data.size(); i++) {
				Vector<String> rowData = (Vector<String>) data.get(i);

				String modFileName 	= rowData.get(0);
				String extension	= rowData.get(1);
				String orgFileName 	= oldList.get(i);

				if (modFileName.equals(orgFileName))
					continue;

				File modFile = new File(path.getText() + File.separator + modFileName+"."+extension);
				File orgFile = new File(path.getText() + File.separator + orgFileName /*+ File.separator + extension*/);

				if (orgFile.renameTo(modFile)) {
					model.setValueAt(STATUS_SUCESS, i, 2);
					System.out.println("SUCESS: Modify file name from " + orgFileName + " to " + modFileName);
				} else {
					model.setValueAt(STATUS_FAIL, i, 2);
					model.setValueAt(FilenameUtils.getBaseName(orgFileName), i, 0);
					System.err.println("FAILED: Modify the file name from " + orgFileName + " to " + modFileName);
				}
			}
		}
	}
}
