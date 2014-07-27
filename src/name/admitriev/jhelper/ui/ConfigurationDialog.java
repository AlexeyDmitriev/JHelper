package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import name.admitriev.jhelper.components.Configurator;
import net.egork.chelper.ui.DirectorySelector;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConfigurationDialog extends DialogWrapper {
	private final JComponent component;
	private JTextField author;
	private DirectorySelector tasksDirectory;

	public ConfigurationDialog(@NotNull Project project, Configurator.State configuration) {
		super(project);
		setTitle("JHelper configuration for " + project.getName());

		author = new JTextField(configuration.getAuthor());
		tasksDirectory = new DirectorySelector(project, configuration.getTasksDirectory());

		JPanel panel = new JPanel(new VerticalLayout());
		panel.add(LabeledComponent.create(author, "Author"));
		panel.add(LabeledComponent.create(tasksDirectory, "Tasks directory"));

		component = panel;

		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return component;
	}

	public Configurator.State getConfiguration() {
		return new Configurator.State(author.getText(), tasksDirectory.getText());
	}
}