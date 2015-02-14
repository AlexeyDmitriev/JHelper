package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.ui.Notificator;

import java.io.IOException;

public class DeleteTaskAction extends BaseAction {
	@Override
	protected void performAction(AnActionEvent e) {
		final Project project = e.getProject();
		RunnerAndConfigurationSettings selectedConfiguration =
				RunManagerImpl.getInstanceImpl(project).getSelectedConfiguration();
		if (selectedConfiguration == null) {
			return;
		}
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (configuration instanceof TaskConfiguration) {
			TaskConfiguration taskConfiguration = (TaskConfiguration) configuration;

			final String path = taskConfiguration.getTask().getPath();
			final String className = taskConfiguration.getTask().getClassName();

			ApplicationManager.getApplication().runWriteAction(
					new Runnable() {
						@Override
						public void run() {
							VirtualFile classFile = project.getBaseDir().findFileByRelativePath(
									path + "/../" + className + ".cpp"
							);
							if (classFile != null) {
								try {
									classFile.delete(this);
								}
								catch (IOException ignored) {
									Notificator.showNotification(
											"Couldn't delete class file",
											NotificationType.WARNING
									);
								}
							}
							VirtualFile taskFile = project.getBaseDir().findFileByRelativePath(path);
							if (taskFile != null) {
								try {
									taskFile.delete(this);
								}
								catch (IOException ignored) {
									Notificator.showNotification(
											"Couldn't delete task file",
											NotificationType.WARNING
									);
								}
							}
						}
					}
			);

			RunManagerEx.getInstanceEx(project).removeConfiguration(selectedConfiguration);
		}
		else {
			Notificator.showNotification(
					"Not a JHelper configuration",
					"To delete configuration you should chose it as a configuration first",
					NotificationType.WARNING
			);
		}
	}
}
