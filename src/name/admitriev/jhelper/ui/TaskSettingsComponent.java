package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import name.admitriev.jhelper.task.Task;
import net.egork.chelper.ui.DirectorySelector;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class TaskSettingsComponent extends JPanel {
	private JTextField name = null;
	private JTextField className = null;
	private DirectorySelector path = null;
	private Project project;

	public TaskSettingsComponent(Project project) {
		super(new VerticalLayout());
		this.project = project;
		//noinspection OverridableMethodCallDuringObjectConstruction
		setTask(Task.emptyTask(project));
	}

	public Task getTask() {
		return new Task(name.getText(), className.getText(), path.getText());
	}

	public void setTask(Task task) {
		removeAll();
		name = new JTextField(task.getName());
		className = new JTextField(task.getClassName());
		path = new DirectorySelector(project, task.getPath());

		add(LabeledComponent.create(name, "Task name"));
		add(LabeledComponent.create(className, "Class name"));
		add(LabeledComponent.create(path, "Path"));
	}


}
