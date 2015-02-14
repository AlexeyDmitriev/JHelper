package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import name.admitriev.jhelper.task.Task;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.TestType;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

/**
 * Panel for task configuration.
 */
public class TaskSettingsComponent extends JPanel {
	private JTextField name = null;
	private JTextField className = null;
	private FileSelector path = null;
	private StreamConfigurationPanel input = null;
	private StreamConfigurationPanel output = null;
	private ComboBox testType = null;
	private Task task = null;

	private Project project;

	private StreamConfigurationPanel.SizeChangedListener listener;

	public TaskSettingsComponent(Project project) {
		this(project, null);
	}

	public TaskSettingsComponent(Project project, StreamConfigurationPanel.SizeChangedListener listener) {
		super(new VerticalLayout());
		this.project = project;
		this.listener = listener;
		//noinspection OverridableMethodCallDuringObjectConstruction
		setTask(Task.emptyTask(project));
	}

	public Task getTask() {
		return new Task(
				name.getText(),
				className.getText(),
				path.getText(),
				input.getStreamConfiguration(),
				output.getStreamConfiguration(),
				(TestType) testType.getSelectedItem(),
				task.getTests()
		);
	}

	public void setTask(Task task) {
		removeAll();
		name = new JTextField(task.getName());
		className = new JTextField(task.getClassName());
		path = new FileSelector(
				project,
				task.getPath(),
				RelativeFileChooserDescriptor.fileChooser(project.getBaseDir())
		);
		input = new StreamConfigurationPanel(
				task.getInput(),
				StreamConfiguration.StreamType.values(),
				"input.txt",
				listener
		);
		output = new StreamConfigurationPanel(
				task.getOutput(),
				StreamConfiguration.OUTPUT_TYPES,
				"output.txt",
				listener
		);

		testType = new ComboBox(TestType.values());
		testType.setSelectedItem(task.getTestType());

		this.task = task;

		add(LabeledComponent.create(name, "Task name"));
		add(LabeledComponent.create(className, "Class name"));
		add(LabeledComponent.create(path, "Path"));
		add(LabeledComponent.create(input, "Input"));
		add(LabeledComponent.create(output, "Output"));
		add(LabeledComponent.create(testType, "Test type"));

		UIUtils.mirrorFields(name, className);
		UIUtils.mirrorFields(name, path.getTextField(), Task.defaultPathFormat(project));
	}


}
