package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.ui.Notificator;

import java.io.IOException;

public class DeleteTaskAction extends BaseAction {
	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}

		RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
		RunnerAndConfigurationSettings selectedConfiguration = runManager.getSelectedConfiguration();
		if (selectedConfiguration == null) {
			return;
		}
		RunConfiguration configuration = selectedConfiguration.getConfiguration();
		if (configuration instanceof TaskConfiguration) {
			removeFiles(project, (TaskConfiguration) configuration);
			runManager.removeConfiguration(selectedConfiguration);
			selectSomeTaskConfiguration(runManager);
		}
		else {
			Notificator.showNotification(
					"Not a JHelper configuration",
					"To delete a configuration you should choose it first",
					NotificationType.WARNING
			);
		}
	}

	private void removeFiles(Project project, TaskConfiguration taskConfiguration) {
		String cppPath = taskConfiguration.getCppPath();

		ApplicationManager.getApplication().runWriteAction(
				new Runnable() {
					@Override
					public void run() {
						VirtualFile classFile = project.getBaseDir().findFileByRelativePath(cppPath);
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
					}
				}
		);
	}

	private static void selectSomeTaskConfiguration(RunManagerEx runManager) {
		for (RunnerAndConfigurationSettings settings : runManager.getAllSettings()) {
			if (settings.getConfiguration() instanceof TaskConfiguration) {
				runManager.setSelectedConfiguration(settings);
				return;
			}
		}
	}
}
