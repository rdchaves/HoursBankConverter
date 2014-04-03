package rodrigo.chaves.hoursbankconverter;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rodrigo.chaves.hoursbankconverter.ui.MainWindow;

public class AppUI {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new MainWindow();
			}
		});
	}

}
