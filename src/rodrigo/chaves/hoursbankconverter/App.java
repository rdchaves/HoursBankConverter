package rodrigo.chaves.hoursbankconverter;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import rodrigo.chaves.hoursbankconverter.exporter.ExcelExporter;
import rodrigo.chaves.hoursbankconverter.reader.impl.LineSplitReader;

public class App implements Runnable {

	private static final String CSV_EXTENSION = "csv";
	private static final String EXCEL_EXTENSION = "xls";
	private JFileChooser openFileChooser;
	private JFileChooser saveFileChooser;

	public App() {
		openFileChooser = new JFileChooser(System.getProperty("user.home"));
		openFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Arquivos separados por virgula", CSV_EXTENSION));
		openFileChooser.setAcceptAllFileFilterUsed(false);
		openFileChooser.setMultiSelectionEnabled(false);

		saveFileChooser = new JFileChooser();
		saveFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Arquivos do Microsoft Excel", EXCEL_EXTENSION));
		saveFileChooser.setAcceptAllFileFilterUsed(false);
		saveFileChooser.setMultiSelectionEnabled(false);

	}

	private JFileChooser getSaveFileChooser() {
		return saveFileChooser;
	}

	private JFileChooser getOpenFileChooser() {
		return openFileChooser;
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
						JOptionPane.showMessageDialog(null, "O arquivo não pode ser substituído.",
								"Erro", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
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

	@Override
	public void run() {
		try {
			File inputFile = getInputFile();
			if (inputFile != null) {
				List<Date> checkpoints = new LineSplitReader().read(new FileReader(
						inputFile), ",");
				File temp = new ExcelExporter<Date>().export(checkpoints);
				File outputFile = getOutputFile();
				if (outputFile != null) {
					FileUtils.moveFile(temp, outputFile);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		SwingUtilities.invokeLater(new App());
	}

}
