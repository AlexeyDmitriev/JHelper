package name.admitriev.jhelper.actions;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.FileUtils;
import name.admitriev.jhelper.task.Task;
import net.egork.chelper.ui.EditTestsDialog;
import net.egork.chelper.util.OutputWriter;

public class EditTestsAction extends BaseAction {

	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		RunnerAndConfigurationSettings selectedConfiguration =
				RunManagerImpl.getInstanceImpl(project).getSelectedConfiguration();
		if (selectedConfiguration == null)
			return;
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (configuration instanceof TaskConfiguration) {
			TaskConfiguration taskConfiguration = (TaskConfiguration) configuration;
			Task task = taskConfiguration.getTask();
			Task newTask = task.withTests(EditTestsDialog.editTests(task.getTests(), project));
			taskConfiguration.setTask(newTask);

			VirtualFile taskFile = project.getBaseDir().findFileByRelativePath(newTask.getPath());
			if (taskFile == null) {
				throw new NotificationException("Couldn't find task file to save: " + newTask.getPath());
			}
			OutputWriter writer = FileUtils.getOutputWriter(taskFile, this);
			newTask.saveTask(writer);
			writer.flush();
			writer.close();

		}
	}
}
