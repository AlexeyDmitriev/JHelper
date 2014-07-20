package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import name.admitriev.jhelper.components.Configurator;
import net.egork.chelper.ui.DirectorySelector;
import net.egork.chelper.ui.OkCancelPanel;
import net.egork.chelper.util.Utilities;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.Point;


public class ConfigurationDialog extends JDialog {
	private final JTextField author;
	private final DirectorySelector tasksDirectory;
	private boolean isOk = false;
	@Nullable
	private Configurator.State result = null;

	private ConfigurationDialog(Project project, Configurator.State configuration) {
		super(null, "Project Jhelper configuration", ModalityType.APPLICATION_MODAL);
		setAlwaysOnTop(true);
		author = new JTextField(configuration.getAuthor());
		tasksDirectory = new DirectorySelector(project, configuration.getTasksDirectory());

		OkCancelPanel main = new OkCancelPanel(new VerticalLayout()) {
			@Override
			public void onOk() {
				isOk = true;
				regenerateResult();
				ConfigurationDialog.this.setVisible(false);
			}

			@Override
			public void onCancel() {
				result = null;
				ConfigurationDialog.this.setVisible(false);
			}
		};


		JPanel okCancelPanel = new JPanel(new GridLayout(1, 2));
		okCancelPanel.add(main.getOkButton());
		okCancelPanel.add(main.getCancelButton());
		main.add(new JLabel("Task directory:"));
		main.add(tasksDirectory);
		main.add(new JLabel("Author:"));
		main.add(author);
		main.add(okCancelPanel);
		setContentPane(main);
		regenerateResult();
		pack();
		Point center = Utilities.getLocation(project, main.getSize());
		setLocation(center);
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			author.requestFocusInWindow();
			author.setSelectionStart(0);
			author.setSelectionEnd(author.getText().length());
		} else if (!isOk) {
			result = null;
		}
		super.setVisible(b);
	}

	private void regenerateResult() {
		result = new Configurator.State(author.getText(), tasksDirectory.getText());
	}

	public static Configurator.State edit(Project project, Configurator.State configuration) {
		ConfigurationDialog dialog = new ConfigurationDialog(project, configuration);
		dialog.setVisible(true);
		return dialog.result;
	}
}
