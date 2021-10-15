package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.CodeGenerationUtils;
import name.admitriev.jhelper.ui.ArchiveDialog;
import name.admitriev.jhelper.ui.Notificator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ArchiveAction extends BaseAction {

	@Override
	protected void performAction(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			throw new NotificationException("No project found", "Are you in any project?");
		}
		Configurator configurator = project.getComponent(Configurator.class);
		Configurator.State configuration = configurator.getState();

		RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
		RunnerAndConfigurationSettings selectedConfiguration = runManager.getSelectedConfiguration();
		if (selectedConfiguration == null) {
			return;
		}

		RunConfiguration runConfiguration = selectedConfiguration.getConfiguration();
		if (!(runConfiguration instanceof TaskConfiguration)) {
			Notificator.showNotification(
				"Not a JHelper configuration",
				"You have to choose JHelper Task to copy",
				NotificationType.WARNING
			);
			return;
		}

		CodeGenerationUtils.generateSubmissionFileForTask(project, (TaskConfiguration) runConfiguration);

		VirtualFile file = project.getBaseDir().findFileByRelativePath(configuration.getOutputFile());
		if (file == null) throw new NotificationException("Couldn't find output file");

		ArchiveDialog archiveDialog = new ArchiveDialog(project, configuration, (TaskConfiguration) runConfiguration);
		archiveDialog.show();
		if (archiveDialog.isOK()) {
			Document document = FileDocumentManager.getInstance().getDocument(file);
			if (document == null) throw new NotificationException("Cannot create Document");
			try {
				String fileName = archiveDialog.getArchiveFile();
				PrintWriter writer = new PrintWriter(fileName, String.valueOf(StandardCharsets.UTF_8));
				writer.write(document.getText());
				writer.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}
