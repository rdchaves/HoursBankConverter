package rodrigo.chaves.hoursbankconverter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import rodrigo.chaves.hoursbankconverter.reader.impl.LineSplitReader;

import br.com.passeionaweb.android.hoursbank.exporter.ExcelExporter;

import com.jeta.forms.components.panel.FormPanel;

public class MainWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = -6889944958307916804L;

	public static final String CLEAR_COMMAND = "clear";
	public static final String OPEN_FILE_COMMAND = "open";
	public static final String CONVERT_FILE_COMMAND = "convert";
	private static final String CSV_EXTENSION = "csv";
	private static final String EXCEL_EXTENSION = "xls";

	private FormPanel formPanel;
	private JFileChooser openFileChooser;
	private JFileChooser saveFileChooser;
	private JTextArea csvContent;
	private JButton btOpen;
	private JButton btClear;
	private JButton btConvert;
	private JLabel lbStatus;

	public MainWindow() {
		super("Conversor de Banco de Horas");
		configureUI();
		configureEvents();
	}

	private void configureUI() {
		add(getFormPanel());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setSize(600, 450);
		setLocationRelativeTo(null);
		setVisible(true);

		getCsvContent().setEditable(false);
	}

	private void configureEvents() {

		// buttons
		getBtClear().addActionListener(this);
		getBtConvert().addActionListener(this);
		getBtOpen().addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (CLEAR_COMMAND.equals(e.getActionCommand())) {
			clear();
		} else if (CONVERT_FILE_COMMAND.equals(e.getActionCommand())) {
			updateStatus("Convertendo o arquivo...");
			convertFile();
		} else if (OPEN_FILE_COMMAND.equals(e.getActionCommand())) {
			loadFile();
		}
	}

	private void clear() {
		getCsvContent().setText("");
		getBtClear().setEnabled(false);
		getBtConvert().setEnabled(false);
		getBtOpen().setEnabled(true);
		updateStatus("Aguardando conversão");
	}

	private void convertFile() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					enableButtons(false);
					List<Date> checkpoints = new LineSplitReader().read(new StringReader(
							getCsvContent().getText()), ",");
					File temp = new ExcelExporter<Date>().export(checkpoints);
					File outputFile = getOutputFile();
					if (outputFile != null) {
						FileUtils.moveFile(temp, outputFile);
					}
					getBtClear().setEnabled(true);
					getBtConvert().setEnabled(true);
					updateStatus("Arquivo convertido com sucesso!");
				} catch (Exception e) {
					updateStatus("Falha ao converter o arquivo.");
					enableButtons(true);
					e.printStackTrace();
				}
			}
		});
	}

	private void enableButtons(final Boolean enabled) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				getBtClear().setEnabled((enabled != null) && enabled);
				getBtConvert().setEnabled((enabled != null) && enabled);
				getBtOpen().setEnabled((enabled != null) && enabled);
			}
		});
	}

	private void loadFile() {
		Reader reader = null;
		try {
			File inputFile = getInputFile();
			if (inputFile != null) {
				updateStatus("Carregando arquivo...");
				reader = new FileReader(inputFile);
				LineIterator it = IOUtils.lineIterator(reader);
				while (it.hasNext()) {
					getCsvContent().append(it.nextLine());
					if (it.hasNext()) {
						getCsvContent().append("\n");
					}
				}
				getBtClear().setEnabled(true);
				getBtConvert().setEnabled(true);
				getBtOpen().setEnabled(false);
				updateStatus("Arquivo carregado com sucesso!");
			}
		} catch (Exception e) {
			updateStatus("Falha ao carregar o arquivo.");
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private File getInputFile() {
		File file = null;
		if (getOpenFileChooser().showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = getOpenFileChooser().getSelectedFile();
		}
		return file;
	}

	private File getOutputFile() {
		File file = null;
		getSaveFileChooser().setCurrentDirectory(
				getOpenFileChooser().getSelectedFile().getParentFile());
		if (getSaveFileChooser().showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = getSaveFileChooser().getSelectedFile();
			final String suffix = "." + EXCEL_EXTENSION;

			// defining extension
			if (!StringUtils.endsWithIgnoreCase(file.getPath(), suffix)) {
				file = new File(file.getPath() + suffix);
			}

			if (file.exists()) {
				boolean confirm = JOptionPane.showConfirmDialog(null,
						"O arquivo selecionado já existe. Deseja substituí-lo?",
						"Arquivo existente", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
				if (!confirm || !FileUtils.deleteQuietly(file)) {
					file = generateNewFile(file, suffix, 1);
					if (confirm) {
						JOptionPane.showMessageDialog(this,
								"Um novo arquivo foi gerado: " + file.getName(),
								"O arquivo não pode ser substituído", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		} else {
			updateStatus("Conversão cancelada.");
		}
		return file;
	}

	private File generateNewFile(final File file, final String suffix, final Integer index) {

		StringBuilder fileName = new StringBuilder(StringUtils.substringBeforeLast(file.getName(),
				suffix));
		fileName.append("(").append(index).append(")").append(suffix);
		File newFile = new File(file.getParent(), fileName.toString());
		if (newFile.exists()) {
			newFile = generateNewFile(file, suffix, index + 1);
		}
		return newFile;
	}

	private void updateStatus(final String... status) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < status.length; i++) {
					if (i > 0) {
						sb.append(" / ");
					}
					sb.append(status[i]);
				}
				getLbStatus().setText(sb.toString());
			}
		});
	}

	public FormPanel getFormPanel() {
		if (formPanel == null) {
			formPanel = new FormPanel("rodrigo/chaves/hoursbankconverter/ui/MainPanel.jfrm");
			formPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		}
		return formPanel;
	}

	public JFileChooser getOpenFileChooser() {
		if (openFileChooser == null) {
			openFileChooser = new JFileChooser(System.getProperty("user.home"));
			openFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
					"Arquivos separados por virgula", CSV_EXTENSION));
			openFileChooser.setAcceptAllFileFilterUsed(false);
			openFileChooser.setMultiSelectionEnabled(false);
		}
		return openFileChooser;
	}

	public JFileChooser getSaveFileChooser() {
		if (saveFileChooser == null) {
			saveFileChooser = new JFileChooser();
			saveFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
					"Arquivos do Microsoft Excel", EXCEL_EXTENSION));
			saveFileChooser.setAcceptAllFileFilterUsed(false);
			saveFileChooser.setMultiSelectionEnabled(false);
		}
		return saveFileChooser;
	}

	public JTextArea getCsvContent() {
		if (csvContent == null) {
			csvContent = (JTextArea) getFormPanel().getTextComponent("csvContent");
		}
		return csvContent;
	}

	public JButton getBtOpen() {
		if (btOpen == null) {
			btOpen = (JButton) getFormPanel().getButton("btOpen");
			btOpen.setActionCommand(OPEN_FILE_COMMAND);
		}
		return btOpen;
	}

	public JButton getBtClear() {
		if (btClear == null) {
			btClear = (JButton) getFormPanel().getButton("btClear");
			btClear.setActionCommand(CLEAR_COMMAND);
			btClear.setEnabled(false);
		}
		return btClear;
	}

	public JButton getBtConvert() {
		if (btConvert == null) {
			btConvert = (JButton) getFormPanel().getButton("btConvert");
			btConvert.setActionCommand(CONVERT_FILE_COMMAND);
			btConvert.setEnabled(false);
		}
		return btConvert;
	}

	public JLabel getLbStatus() {
		if (lbStatus == null) {
			lbStatus = getFormPanel().getLabel("lbStatus");
		}
		return lbStatus;
	}

}
