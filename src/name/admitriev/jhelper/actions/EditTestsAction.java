package name.admitriev.jhelper.actions;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.task.TaskUtils;
import name.admitriev.jhelper.ui.EditTestsDialog;

public class EditTestsAction extends BaseAction {

	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		RunnerAndConfigurationSettings selectedConfiguration =
				RunManagerImpl.getInstanceImpl(project).getSelectedConfiguration();
		if (selectedConfiguration == null) {
			return;
		}
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (configuration instanceof TaskConfiguration) {
			TaskConfiguration taskConfiguration = (TaskConfiguration) configuration;
			Task task = taskConfiguration.getTask();
			EditTestsDialog dialog = new EditTestsDialog(task.getTests(), project);
			dialog.show();
			if (!dialog.isOK()) {
				return;
			}
			Task newTask = task.withTests(dialog.getTests());
			taskConfiguration.setTask(newTask);

			VirtualFile taskFile = project.getBaseDir().findFileByRelativePath(newTask.getPath());
			if (taskFile == null) {
				throw new NotificationException("Couldn't find task file to save: " + newTask.getPath());
			}
			TaskUtils.saveTaskFile(newTask, project);
		}
	}
}
